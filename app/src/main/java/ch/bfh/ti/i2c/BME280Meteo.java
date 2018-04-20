/*
 ***************************************************************************
 * \brief   BME280Meteo
 *	        This Class allows you to control and reading Data from the
 *	        BME280 Weather Sensor. This Class requiers the I2C Class.
 *
 *			The program reads the temperature value from the MCP9802 sensor
 *			and will show it on the display.
 *
 *
 * \file    BME280Meteo.java
 * \version 1.0
 * \date    10.04.2018
 * \author  Daniel Schären
 *
 * \remark  Last Modifications:
 * \remark  V1.0, SCHAD4, 20.04.2018
 ***************************************************************************
 */

package ch.bfh.ti.i2c;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;



public class BME280Meteo {

    /* BME280 Data Register Adresses */
    private static final char BME280_I2C_hum_lsb_addr       = 0xFE; /* Lower Humidity Register */
    private static final char BME280_I2C_hum_msb_addr       = 0xFD; /* Higher Humidity Register */
    private static final char BME280_I2C_temp_xlsb_addr     = 0xFC; /* Lowest Temperature Register */
    private static final char BME280_I2C_temp_lsb_addr      = 0xFB; /* Lower Temperature Register */
    private static final char BME280_I2C_temp_msb_addr      = 0xFA; /* Higher Temperature Register */
    private static final char BME280_I2C_press_xlsb_addr    = 0xF9; /* Lowest Pressure Register */
    private static final char BME280_I2C_press_lsb_addr     = 0xF8; /* Lower Pressure Register */
    private static final char BME280_I2C_press_msb_addr     = 0xF7; /* Highest Pressure Register */

    /* BME280 Configuration and Status Register Adresses */
    private static final char BME280_I2C_config_addr        = 0xF5; /* Config Register */
    private static final char BME280_I2C_ctrl_meas_addr     = 0xF4; /* Ctrl Meas Register */
    private static final char BME280_I2C_status_addr        = 0xF3; /* Status Register */
    private static final char BME280_I2C_ctrl_hum_addr      = 0xF2; /* Ctrl Hum Register */
    private static final char BME280_I2C_reset_addr         = 0xE0; /* Reset Register */
    private static final char BME280_I2C_id_addr            = 0xD0; /* ID Register */

    /* BME280 Configuration and Status Bits */
    private static final char BME280_I2C_reset_bits         = 0xB6; /* Make the hool for Power On Reset */
    private static final char BME280_I2C_ctrl_hum_bits      = 0x04; /* Set this before ctrl_meas! Set 8xOversampling Humidity for noise reduction*/
    private static final char BME280_I2C_ctrl_meas_bits     = 0x93; /* Set this after ctrl_hums! Set 8xOversampling Pressure and Temperature for noise reduction. Set normal mode for auto update*/
    private static final char BME280_I2C_config_bits        = 0x80; /* Set 0.5s auto update period. No Filter. SPI Mode 00 */

    /* I2C device file name */
    private static final String BME280_FILE_NAME = "/dev/i2c-3";

    /* I2C Address of the BME280 device */
    private static final char BME280_I2C_ADDR_1 = 0x76; // SDO Pin set to GND
    private static final char BME280_I2C_ADDR_2 = 0x77; // SDO Pin set to VCC

    /* BME280 Callibraiton Register Adresses */
    private static final char BME280_Callibration_0_addr = 0x88;
    private static final char BME280_Callibration_25_addr = 0xA1;
    private static final char BME280_Callibration_26_addr = 0xE1;

    /* BME280 Raw Data Variable for Temperature Humidity and Pressure */
    private long BME280_Temp_raw     = 0;
    private long BME280_Humidity_raw = 0;
    private long BME280_Pressure_raw = 0;

    /* BME280 Real Data Variable for Temperature Humidity and Pressure */
    private double BME280_Humidity_proz;
    private double BME280_Temp_celsius;
    private double BME280_Temp_fahrnheit;
    private double BME280_Pressure_hPa;
    private float t_fine; // Compensation Temperatur

    /* BME280_Variable for Callibraiton */
    private char[] BME280_Callibration_data = new char[41];
    private char reg_num;

    /* Compensation Data Variable */
    private int dig_T1;
    private int dig_T2;
    private int dig_T3;
    private int dig_P1;
    private int dig_P2;
    private int dig_P3;
    private int dig_P4;
    private int dig_P5;
    private int dig_P6;
    private int dig_P7;
    private int dig_P8;
    private int dig_P9;
    private int dig_H1;
    private int dig_H2;
    private int dig_H3;
    private int dig_H4;
    private int dig_H5;
    private int dig_H6;

    /* I2C object variable */
    public I2C i2c;

    /* I2C Communication buffer and file handle */
    private int[] i2cCommBuffer = new int[50];
    private int fileHandle;
    private int[] BME280_Empfang = new int[16];

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_i2_c);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
    }*/

    void init ()
    {
        startI2C();
        /* Setup the Humidity Sensor Control Register */
        i2cCommBuffer[0] = BME280_I2C_ctrl_hum_addr;
        i2cCommBuffer[1] = BME280_I2C_ctrl_hum_bits;
        i2c.write(fileHandle, i2cCommBuffer, 2);

        /* Setup the other Sensor Control Register */
        i2cCommBuffer[0] = BME280_I2C_ctrl_meas_addr;
        i2cCommBuffer[1] = BME280_I2C_ctrl_meas_bits;
        i2c.write(fileHandle, i2cCommBuffer, 2);

        /* Setup the general Config Register */
        i2cCommBuffer[0] = BME280_I2C_config_addr;
        i2cCommBuffer[1] = BME280_I2C_config_bits;
        i2c.write(fileHandle, i2cCommBuffer,2);
        stopI2C();
    }

    void ReadCompensationData ()
    {
        startI2C();
        /* Read the Callibration Datas */
        i2cCommBuffer[0] = BME280_Callibration_0_addr;
        i2c.write(fileHandle, i2cCommBuffer,1);
        i2c.read(fileHandle, i2cCommBuffer, 25);
        for(reg_num = 0; reg_num < 25; reg_num++)
        {
            BME280_Callibration_data[reg_num] = (char) (0x00FF & i2cCommBuffer[reg_num]);
        }
        dig_T1 = (i2cCommBuffer[0] + i2cCommBuffer[1] * 256);
        dig_T2 = (i2cCommBuffer[2] + i2cCommBuffer[3] * 256);
        dig_T3 = (i2cCommBuffer[4] + i2cCommBuffer[5] * 256);
        //Overflow control
        if(dig_T2 > 32767)
        {
            dig_T2 -= 65536;
        }
        if(dig_T3 > 32767)
        {
            dig_T3 -= 65536;
        }

        dig_P1 = (i2cCommBuffer[6] + i2cCommBuffer[7] * 256);
        dig_P2 = (i2cCommBuffer[8] + i2cCommBuffer[9] * 256);
        dig_P3 = (i2cCommBuffer[10] + i2cCommBuffer[11] * 256);
        dig_P4 = (i2cCommBuffer[12] + i2cCommBuffer[13] * 256);
        dig_P5 = (i2cCommBuffer[14] + i2cCommBuffer[15] * 256);
        dig_P6 = (i2cCommBuffer[16] + i2cCommBuffer[17] * 256);
        dig_P7 = (i2cCommBuffer[18] + i2cCommBuffer[19] * 256);
        dig_P8 = (i2cCommBuffer[20] + i2cCommBuffer[21] * 256);
        dig_P9 = (i2cCommBuffer[22] + i2cCommBuffer[23] * 256);
        //Overflow control
        if(dig_P2 > 32767)
        {
            dig_P2 -= 65536;
        }
        if(dig_P3 > 32767)
        {
            dig_P3 -= 65536;
        }
        if(dig_P4 > 32767)
        {
            dig_P4 -= 65536;
        }
        if(dig_P5 > 32767)
        {
            dig_P5 -= 65536;
        }
        if(dig_P6 > 32767)
        {
            dig_P6 -= 65536;
        }
        if(dig_P7 > 32767)
        {
            dig_P7 -= 65536;
        }
        if(dig_P8 > 32767)
        {
            dig_P8 -= 65536;
        }
        if(dig_P9 > 32767)
        {
            dig_P9 -= 65536;
        }
        i2cCommBuffer[0] = BME280_Callibration_25_addr;
        i2c.write(fileHandle, i2cCommBuffer, 1);
        i2c.read(fileHandle, i2cCommBuffer, 1);

        dig_H1 = (char) i2cCommBuffer[0];

        i2cCommBuffer[0] = BME280_Callibration_26_addr;
        i2c.write(fileHandle, i2cCommBuffer, 1);
        i2c.read(fileHandle, i2cCommBuffer, 7);


        dig_H2 = (i2cCommBuffer[0] + i2cCommBuffer[1] * 256);
        dig_H3 = i2cCommBuffer[2] & 0xFF ;
        dig_H4 = (i2cCommBuffer[3] * 16 + (i2cCommBuffer[4] & 0xF));
        dig_H5 = (i2cCommBuffer[4] / 16) + (i2cCommBuffer[5] * 16);
        dig_H6 = i2cCommBuffer[6];
        //Overflow Check

        if(dig_H2 > 32767)
        {
            dig_H2 -= 65536;
        }
        if(dig_H4 > 32767)
        {
            dig_H4 -= 65536;
        }
        if(dig_H5 > 32767)
        {
            dig_H5 -= 65536;
        }
        if(dig_H6 > 127)
        {
            dig_H6 -= 256;
        }
        stopI2C();

    }

    public void UpdateMeteoData()
    {
        startI2C();
        /* Setup the MCP9800 register to read the temperature */
        i2cCommBuffer[0] = BME280_I2C_press_msb_addr;
        i2c.write(fileHandle, i2cCommBuffer, 1);

        /* Read the current Temperature, Pressure and Humidity from the BME280 device */
        i2cCommBuffer[0] = 0;
        i2cCommBuffer[1] = 0;
        i2cCommBuffer[2] = 0;
        i2cCommBuffer[3] = 0;
        i2cCommBuffer[4] = 0;
        i2cCommBuffer[5] = 0;
        i2cCommBuffer[6] = 0;
        i2cCommBuffer[7] = 0;
        i2c.read(fileHandle, i2cCommBuffer, 8);
        BME280_Empfang[0] = i2cCommBuffer[0];
        BME280_Empfang[1] = i2cCommBuffer[1];
        BME280_Empfang[2] = i2cCommBuffer[2];
        BME280_Empfang[3] = i2cCommBuffer[3];
        BME280_Empfang[4] = i2cCommBuffer[4];
        BME280_Empfang[5] = i2cCommBuffer[5];
        BME280_Empfang[6] = i2cCommBuffer[6];
        BME280_Empfang[7] = i2cCommBuffer[7];
        stopI2C();

        // Put the Raw Data into the Register
        BME280_Humidity_raw = (BME280_Empfang[6] * 256 + BME280_Empfang[7]);
        BME280_Temp_raw = ((long)(BME280_Empfang[3] * 65536 + ((long)(BME280_Empfang[4] * 256) + (long)(BME280_Empfang[5] & 0xF0)))) / 16;
        BME280_Pressure_raw = ((long)(BME280_Empfang[0] * 65536 + ((long)(BME280_Empfang[1] * 256) + (long)(BME280_Empfang[2] & 0xF0)))) / 16;

    }

    public double getHumidity ()
    {
        /* Working Register for Calculate the Compensation */
        float var1;
        float var2;

        /* Calculate the real Humidity with Compensation Data */
        double var_H = ((t_fine) - 76800.0);
        var_H = (BME280_Humidity_raw - (dig_H4 * 64.0 + dig_H5 / 16384.0 * var_H)) * (dig_H2 / 65536.0 * (1.0 + dig_H6 / 67108864.0 * var_H * (1.0 + dig_H3 / 67108864.0 * var_H)));
        BME280_Humidity_proz =  (var_H * (1.0 -  dig_H1 * var_H / 524288.0));
        if(BME280_Humidity_proz > 100.0)
        {
            BME280_Humidity_proz = 100;
        }else
        if(BME280_Humidity_proz < 0.0)
        {
            BME280_Humidity_proz = 0;
        }

        return BME280_Humidity_proz;
    }

    public double getTemperatur (String Einheit)
    {
        /* Working Register for Calculate the Compensation */
        double var1;
        double var2;

        /* Calculate the real Temperatur with Compensation Data */
        var1 = (((float)BME280_Temp_raw) / 16384.0 - ((float)dig_T1) / 1024.0) * ((float)dig_T2);
        var2 = ((((float)BME280_Temp_raw) / 131072.0 - ((float)dig_T1) / 8192.0) * (((float)BME280_Temp_raw)/131072 - ((float)dig_T1)/8192)) * ((float)dig_T3);
        t_fine = (long)(var1 + var2);
        BME280_Temp_celsius = (var1 + var2) / 5120.0;
        BME280_Temp_fahrnheit = BME280_Temp_celsius * 1.8 + 32;

        /* Return the correct Value */
        if(Einheit == "Fahrenheit")
        {
            return BME280_Temp_celsius;
        }
        else
        {
            return BME280_Temp_fahrnheit;
        }
    }

    public double getPressure ()
    {
        /* Working Register for Calculate the Compensation */
        double var1;
        double var2;

        /* Calculate the real Pressure with Compensation Data */
        var1 = ((float)t_fine / 2) - 64000;
        var2 = var1 * var1 * ((float)dig_P6) / 32768.0;
        var2 = var2 + var1 * ((float)dig_P5) * 2.0;
        var2 = (var2 / 4) + (((float)dig_P4) * 65536.0);
        var1 = (((float) dig_P3) * var1 * var1 / 524288.0 + ((float) dig_P2) * var1) / 524288.0;
        var1 = (1 + var1 / 32768.0) * ((float)dig_P1);
        double p = 1048576.0 - (float)BME280_Pressure_raw;
        p = (p - (var2 / 4096.0)) * 6250.0 / var1;
        var1 = (float) (((double) dig_P9) * p * p / 214748364);
        var1 = (float) var1 / 10.0;
        var2 = p * ((float) dig_P8) / 32768.0;
        BME280_Pressure_hPa = (p + (var1 + var2 + ((float)dig_P7)) / 16.0) / 100.0;

        return BME280_Pressure_hPa;
    }

    private void startI2C()
    {
        /* Open the i2c device, get the file handle */
        fileHandle = i2c.open(BME280_FILE_NAME);

        /* Set the i2c slave address for all subsequent I2C device transfers */
        i2c.SetSlaveAddress(fileHandle, BME280_I2C_ADDR_2);
    }

    private void stopI2C()
    {
        /* Close the i2c device*/
        i2c.close(fileHandle);
    }




}
