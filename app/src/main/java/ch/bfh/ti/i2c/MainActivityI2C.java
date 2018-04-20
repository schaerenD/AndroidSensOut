/*
 ***************************************************************************
 * \brief   Embedded Android I2C Exercise 4.2
 *	        This sample program shows how to use the native I2C interface.
 *			The program reads the temperature value from the MCP9802 sensor
 *			and will show it on the display.
 *
 *	        Only a minimal error handling is implemented.
 * \file    MainActivityI2C.java
 * \version 1.0
 * \date    06.03.2014
 * \author  Martin Aebersold
 *
 * \remark  Last Modifications:
 * \remark  V1.0, AOM1, 06.03.2014
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

import java.util.Timer;
import java.util.TimerTask;

public class MainActivityI2C extends AppCompatActivity {

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

    /* I2C Address of the LIS302DL device */
    private static final char BME280_I2C_ADDR_1 = 0x76;
    private static final char BME280_I2C_ADDR_2 = 0x77;

    /* I2C Address of the LIS302DL device */
    private static final char BME280_Callibration_0_addr = 0x88;
    private static final char BME280_Callibration_25_addr = 0xA1;
    private static final char BME280_Callibration_26_addr = 0xE1;
    //private static final char BME280_Callibration_1 = 0x89;
    //private static final char BME280_Callibration_1 = 0x8A;
    //private static final char BME280_Callibration_1 = 0x8B;
    //private static final char BME280_Callibration_1 = 0x8C;
    //private static final char BME280_Callibration_1 = 0x8D;



    private static final char LIS302DL_OUT_Y = 0x2B;      /* Y-Achse Register */
    private static final char LIS302DL_OUT_Z = 0x2D;      /* Z-Achse Register */
    private static final char LIS302DL_CTRL_REG1 = 0x20;    /* Sensor Configuration Register */
    private static final char LIS302DL_CTRL_REG2 = 0x21;    /* Sensor Configuration Register */
    private static final char LIS302DL_CTRL_REG3 = 0x22;    /* Sensor Configuration Register */

    /* Sensor Configuration Register Bits */
    private static final char LIS302DL_Conf_R1 = 0x47;
    private static final char LIS302DL_Conf_R2 = 0x00;
    private static final char LIS302DL_Conf_R3 = 0x00;

    /* Sensor Configuration Register Bits */
    int[] BME280_Empfang = new int[16];

    /* BME280_Variable fpr Temperature Humidity Pressure */
    long BME280_Temp_raw     = 0;
    long BME280_Humidity_raw = 0;
    long BME280_Pressure_raw = 0;

    float BME280_Temp_compens         = 0;
    float BME280_Humidity_compens     = 0;
    float BME280_Pressure_compens     = 0;

    double BME280_Humidity_proz;
    double BME280_Temp_celsius;
    double BME280_Temp_fahrnheit;

    float var1;
    float var2;

    int dig_T1;
    int dig_T2;
    int dig_T3;
    int dig_P1;
    int dig_P2;
    int dig_P3;
    int dig_P4;
    int dig_P5;
    int dig_P6;
    int dig_P7;
    int dig_P8;
    int dig_P9;
    int dig_H1;
    int dig_H2;
    int dig_H3;
    int dig_H4;
    int dig_H5;
    int dig_H6;


    /* BME280_Variable for Callibraiton */
    char[] BME280_Callibration_data = new char[41];
    char reg_num;


    /* I2C device file name */
    private static final String LIS302DL_FILE_NAME = "/dev/i2c-3";

    /* MCP9800 Register pointers */
    private static final char MCP9800_TEMP = 0x00;      /* Ambient Temperature Register */
    private static final char MCP9800_CONFIG = 0x01;    /* Sensor Configuration Register */

    /* Sensor Configuration Register Bits */
    private static final char MCP9800_12_BIT = 0x60;

    /* I2C Address of the MCP9802 device */
    private static final char MCP9800_I2C_ADDR = 0x48;

    /* I2C device file name */
    private static final String MCP9800_FILE_NAME = "/dev/i2c-3";

    /* I2C object variable */
    I2C i2c;

    /* I2C Communication buffer and file handle */
    int[] i2cCommBuffer = new int[50];
    int fileHandle;

    /* Temperature and conversion variable */
    double TempC;
    int Temperature;
    float Achse_X;

    /* Variable for TextView widgets */
    TextView textViewTemperature;

    /* Temperature Degrees Celsius text symbol */
    private static final String DEGREE_SYMBOL = "\u2103";

    /* Method is run at app startup */


    Timer timerli;
    int counterli;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_i2_c);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        //BME280Meteo bme280 = new BME280Meteo();
        //bme280.init();
        //bme280.ReadCompensationData();
       // bme280.UpdateMeteoData();
       // bme280.getTemperatur("Celsius");
      //  bme280.getHumidity();
      //  bme280.getPressure();
        
        textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);

	    /* Instantiate the new i2c device */
        i2c = new I2C();

	    /* Open the i2c device, get the file handle */
        //fileHandle = i2c.open(MCP9800_FILE_NAME);

	    /* Set the i2c slave address for all subsequent I2C device transfers */
        //i2c.SetSlaveAddress(fileHandle, MCP9800_I2C_ADDR);

	    /* Setup i2c buffer for the configuration register an write it to the MCP9800 device */
        //i2cCommBuffer[0] = MCP9800_CONFIG;



        //i2cCommBuffer[1] = MCP9800_12_BIT;
        //i2c.write(fileHandle, i2cCommBuffer, 2);

	    /* Setup the MCP9800 register to read the temperature */
        //i2cCommBuffer[0] = MCP9800_TEMP;
        //i2c.write(fileHandle, i2cCommBuffer, 1);

	    /* Read the current temperature from the MCP9800 device */
        //i2c.read(fileHandle, i2cCommBuffer, 2);

	    /* Assemble the temperature values */
        //Temperature = ((i2cCommBuffer[0] << 8) | i2cCommBuffer[1]);
        //Temperature = Temperature >> 4;

	    /* Convert current temperature to float */
        //TempC = 1.0 * Temperature * 0.0625;

        /* Display actual temperature in degrees celsius */
        textViewTemperature.setTextColor(Color.WHITE);
        textViewTemperature.setText("Temperature: " + String.format("%3.2f", TempC) + DEGREE_SYMBOL);

	    /* Close the i2c file */
        //i2c.close(fileHandle);

        //myTimer.schedule(myTask, 1000, 20);

        /* Open the i2c device, get the file handle */
        fileHandle = i2c.open(BME280_FILE_NAME);

        /* Set the i2c slave address for all subsequent I2C device transfers */
        i2c.SetSlaveAddress(fileHandle, BME280_I2C_ADDR_2);

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

        /* Read the Callibration Datas */
        i2cCommBuffer[0] = BME280_Callibration_0_addr;
        i2c.write(fileHandle, i2cCommBuffer,1);
        i2c.read(fileHandle, i2cCommBuffer, 25);
        BME280_Callibration_data[0] = (char) (0x00FF & i2cCommBuffer[0]);
        BME280_Callibration_data[1] = (char) (0x00FF & i2cCommBuffer[1]);
        BME280_Callibration_data[2] = (char) (0x00FF & i2cCommBuffer[2]);
        BME280_Callibration_data[3] = (char) (0x00FF & i2cCommBuffer[3]);
        BME280_Callibration_data[4] = (char) (0x00FF & i2cCommBuffer[4]);
        BME280_Callibration_data[5] = (char) (0x00FF & i2cCommBuffer[5]);
        BME280_Callibration_data[6] = (char) (0x00FF & i2cCommBuffer[6]);
        BME280_Callibration_data[7] = (char) (0x00FF & i2cCommBuffer[7]);
        BME280_Callibration_data[8] = (char) (0x00FF & i2cCommBuffer[8]);
        BME280_Callibration_data[9] = (char) (0x00FF & i2cCommBuffer[9]);
        BME280_Callibration_data[10] = (char) (0x00FF & i2cCommBuffer[10]);
        BME280_Callibration_data[11] = (char) (0x00FF & i2cCommBuffer[11]);
        BME280_Callibration_data[12] = (char) (0x00FF & i2cCommBuffer[12]);
        BME280_Callibration_data[13] = (char) (0x00FF & i2cCommBuffer[13]);
        BME280_Callibration_data[14] = (char) (0x00FF & i2cCommBuffer[14]);
        BME280_Callibration_data[15] = (char) (0x00FF & i2cCommBuffer[15]);
        BME280_Callibration_data[16] = (char) (0x00FF & i2cCommBuffer[16]);
        BME280_Callibration_data[17] = (char) (0x00FF & i2cCommBuffer[17]);
        BME280_Callibration_data[18] = (char) (0x00FF & i2cCommBuffer[18]);
        BME280_Callibration_data[19] = (char) (0x00FF & i2cCommBuffer[19]);
        BME280_Callibration_data[20] = (char) (0x00FF & i2cCommBuffer[20]);
        BME280_Callibration_data[21] = (char) (0x00FF & i2cCommBuffer[21]);
        BME280_Callibration_data[22] = (char) (0x00FF & i2cCommBuffer[22]);
        BME280_Callibration_data[23] = (char) (0x00FF & i2cCommBuffer[23]);
        BME280_Callibration_data[24] = (char) (0x00FF & i2cCommBuffer[24]);
        BME280_Callibration_data[25] = (char) (0x00FF & i2cCommBuffer[25]);
        i2cCommBuffer[0] = BME280_Callibration_26_addr;
        i2c.write(fileHandle, i2cCommBuffer,1);
        i2c.read(fileHandle, i2cCommBuffer, 25);
        //BME280_Callibration_data[1] = (char) (0x00FF & i2cCommBuffer[0]);
        for(reg_num = 0; reg_num < 15; reg_num++)
        {
            BME280_Callibration_data[reg_num+26] = (char) (0x00FF & i2cCommBuffer[reg_num]);
        }

        dig_T1 = (BME280_Callibration_data[0] + BME280_Callibration_data[1] * 256);
        dig_T2 = (BME280_Callibration_data[2] + BME280_Callibration_data[3] * 256);
        dig_T3 = (BME280_Callibration_data[4] + BME280_Callibration_data[5] * 256);
        //Overflow control
        if(dig_T2 > 32767)
        {
            dig_T2 -= 65536;
        }
        if(dig_T3 > 32767)
        {
            dig_T3 -= 65536;
        }

        dig_P1 = (BME280_Callibration_data[6] + BME280_Callibration_data[7] * 256);
        dig_P2 = (BME280_Callibration_data[8] + BME280_Callibration_data[9] * 256);
        dig_P3 = (BME280_Callibration_data[10] + BME280_Callibration_data[11] * 256);
        dig_P4 = (BME280_Callibration_data[12] + BME280_Callibration_data[13] * 256);
        dig_P5 = (BME280_Callibration_data[14] + BME280_Callibration_data[15] * 256);
        dig_P6 = (BME280_Callibration_data[16] + BME280_Callibration_data[17] * 256);
        dig_P7 = (BME280_Callibration_data[18] + BME280_Callibration_data[19] * 256);
        dig_P8 = (BME280_Callibration_data[20] + BME280_Callibration_data[21] * 256);
        dig_P9 = (BME280_Callibration_data[22] + BME280_Callibration_data[23] * 256);
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
        /* Setup i2c buffer for the configuration register an write it to the MCP9800 device */
        //i2cCommBuffer[0] = LIS302DL_CTRL_REG1;
        //i2cCommBuffer[1] = LIS302DL_Conf_R1;
        //i2cCommBuffer[1] = LIS302DL_Conf_R2;
        //i2cCommBuffer[1] = LIS302DL_Conf_R3;
        //i2c.write(fileHandle, i2cCommBuffer, 2);

        timerli = new Timer();
        timerli.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                counterli++;
                runOnUiThread(new Runnable() //run on ui thread
                {
                    public void run()
                    {
                         /* Setup the MCP9800 register to read the temperature */
                        i2cCommBuffer[0] = BME280_I2C_press_msb_addr;
                        i2c.write(fileHandle, i2cCommBuffer, 1);
                        /* Read the current temperature from the MCP9800 device */
                        i2cCommBuffer[0] = 0;
                        i2cCommBuffer[1] = 0;
                        i2cCommBuffer[2] = 0;
                        i2cCommBuffer[3] = 0;
                        i2cCommBuffer[4] = 0;
                        i2cCommBuffer[5] = 0;
                        i2cCommBuffer[6] = 0;
                        i2cCommBuffer[7] = 0;
                        i2c.read(fileHandle, i2cCommBuffer, 8);
                        /* Assemble the temperature values */
                        BME280_Empfang[0] = i2cCommBuffer[0];//<<8 | i2cCommBuffer[1] ;
                        BME280_Empfang[1] = i2cCommBuffer[1];
                        BME280_Empfang[2] = i2cCommBuffer[2];
                        BME280_Empfang[3] = i2cCommBuffer[3];
                        BME280_Empfang[4] = i2cCommBuffer[4];
                        BME280_Empfang[5] = i2cCommBuffer[5];
                        BME280_Empfang[6] = i2cCommBuffer[6];
                        BME280_Empfang[7] = i2cCommBuffer[7];

                        //BME280_Humidity_raw = BME280_Empfang[1]<<8;
                        //BME280_Humidity_raw = (0x00FF & BME280_Empfang[0]) | BME280_Humidity_raw ;
                        long adc_h = (BME280_Empfang[6] * 256 + BME280_Empfang[7]);
                        BME280_Humidity_raw = adc_h;

                        //BME280_Temp_raw = (0x00FF & BME280_Empfang[4])<<12 | (0x00FF & BME280_Empfang[3])<<4 | (0x00FF & BME280_Empfang[2]) >>4;
                        long adc_t = ((long)(BME280_Empfang[3] * 65536 + ((long)(BME280_Empfang[4] * 256) + (long)(BME280_Empfang[5] & 0xF0)))) / 16;
                        BME280_Temp_raw = adc_t;

                        //BME280_Pressure_raw = BME280_Empfang[7]<<12 |BME280_Empfang[6]<<4 | BME280_Empfang[5]>>4;
                        long adc_p = ((long)(BME280_Empfang[0] * 65536 + ((long)(BME280_Empfang[1] * 256) + (long)(BME280_Empfang[2] & 0xF0)))) / 16;
                        BME280_Pressure_raw =  adc_p;




                        //i2c.close(fileHandle);
                        //fileHandle = i2c.open(BME280_FILE_NAME);
                        //i2c.SetSlaveAddress(fileHandle, BME280_I2C_ADDR_2);


                        //Achse_X = Temperature >> 4;
	                    /* Convert current temperature to float */
                        //Achse_X = (float) (0.018*Achse_X);
                        textViewTemperature.setText("Druck: " + String.format("%d", BME280_Pressure_raw) + " Temp: " + String.format("%d", BME280_Temp_raw)+ " Feucht.: " + String.format("%d", BME280_Humidity_raw));
                        //textViewTemperature.setText("Adress: " + String.format("%d", (0x00FF & BME280_Empfang[0])));

                        // Temperature offset calculations
                        float var1 = (((float)BME280_Temp_raw) / 16384 - ((float)dig_T1) / 1024) * ((float)dig_T2);
                        float var2 = ((((float)BME280_Temp_raw) / 131072 - ((float)dig_T1) / 8192) * (((float)BME280_Temp_raw)/131072 - ((float)dig_T1)/8192)) * ((float)dig_T3);
                        float t_fine = (long)(var1 + var2);
                        BME280_Temp_celsius = (var1 + var2) / 5120;
                        BME280_Temp_fahrnheit = BME280_Temp_celsius * 1.8 + 32;
                        textViewTemperature.setText("celsTemp_Real: " + String.format("%f", BME280_Temp_celsius) + "fahrTemp_Real: " + String.format("%f", BME280_Temp_fahrnheit));

                        // Pressure offset calculations
                        var1 = ((float)t_fine / 2) - 64000;
                        var2 = var1 * var1 * ((float)dig_P6) / 32768;
                        var2 = var2 + var1 * ((float)dig_P5) * 2;
                        var2 = (var2 / 4) + (((float)dig_P4) * 65536);
                        var1 = (((float) dig_P3) * var1 * var1 / 524288 + ((float) dig_P2) * var1) / 524288;
                        var1 = (1 + var1 / 32768) * ((float)dig_P1);
                        float p = 1048576 - (float)adc_p;
                        p = (p - (var2 / 4096)) * 6250 / var1;
                        var1 = (float) (((double) dig_P9) * p * p / 214748364);
                        var1 = (float) var1 / 10;
                        var2 = p * ((float) dig_P8) / 32768;

                        double BME280_Pressure_hPa = (p + (var1 + var2 + ((float)dig_P7)) / 16.0) / 100;
                        textViewTemperature.setText("Luftdruck_hPa: " + String.format("%f", BME280_Pressure_hPa));

                        // Humidity offset calculations
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
                        textViewTemperature.setText("Luftfeuchte:: " + String.format("%f", BME280_Humidity_proz));
                    }
                });
            }
        }, 500, 2000 );


    }

    /*
     * 	(non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    protected void onStop()
    {
        i2c.close(fileHandle);
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
        super.onStop();
    }
}
