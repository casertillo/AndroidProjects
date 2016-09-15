//Copyright (c) Microsoft Corporation All rights reserved.  
// 
//MIT License: 
// 
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//documentation files (the  "Software"), to deal in the Software without restriction, including without limitation
//the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
//to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
// 
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of
//the Software. 
// 
//THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
package com.microsoft.band.sdk.heartrate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.HeartRateQuality;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BandHeartRateAppActivity extends Activity {


	private BandClient client = null;	//Verifies if the band is connected
	private Button btnStartMen, 		//button when a male is tested
					btnConsent, 		//button to heart rate...Hidden during test but if the app is used the first time
										//it has to be added so the band has the permission from android to detect the heart rate
					btnStartWomen;		//button when a female is tested.
	private TextView txtStatusHeart;	//Text Message for heart rate sensor
	private TextView txtStatusGSR;		//Text Message for GSR sensor
	private TextView txtStatusBarom;	//Text Message for Barometer
	private TextView txtSteps;			//Text that indicates the steps at the beginning
	private String heartRate;			//hearRate variable
	private String quality;				//quality signal from the band
	private String resistance;			//GSR variable
	private String airPresure;			// air pressure variable
	private String temperature;			// temperature variable
	private String hrr;					// heart rate variability
	private Boolean started=false;		//if initialized variable
	private String preference;			//stores the gender from the user when te button is clicked
	private int counter = 0;			//image counter
	int flag=0;							//simple variable to stop the automatic slider

	//Array that stores the images' names.
	String images[] = new String[29];

	//Images for men
	//The images are stored in app-res-drawable can be either jpg or png.
	//-----------------------------------------------------------------------------
	//IMPORTANT:No images are provided due to licence agreement PLEASE ADD YOUR OWN.
	//mh = MALE HIGH(POSITIVE VALENCE AND AROUSAL)
	//mN = MALE NEGATIVE(LOW VALENCE AND AROUSAL)
	//THE OTHER IMAGES L=LOW, V=VALENCE, H=HIGH, A=AROUSAL
	String imagesforMen[]= {"black","mh","black","mn","black","mh1","black","mn1","black","mn2","black","mh2","black","hvla1","black"
			,"hvha","black","lvha","black","hvla","black","lvha1","black","lvla","black","hvha1", "black","lvla1","bmlack"};

	//Images for women
	//wh = WOMEN HIGH(POSITIVE VALENCE AND AROUSAL)
	//wN = WOMEN NEGATIVE(LOW VALENCE AND AROUSAL)
	//THE OTHER IMAGES L=LOW, V=VALENCE, H=HIGH, A=AROUSAL
	String imagesforWomen[]= {"black","wh","black","wn","black","wh1","black","wn1","black","wn2","black","wh2","black","hvla1","black"
			,"hvha","black","lvha","black","hvla","black","lvha1","black","lvla","black","hvha1", "black","lvla1","black"};
	//----------------------------------------------------------------------------

	ImageView iv;
	//Store the name of the variable that contains the emotion stimuli
	String CurrentImage;

	Handler handler = new Handler();

	//AUTOMATIC SLIDER
	Runnable changeImage = new Runnable(){

		@Override
		public void run(){
			//Conditional when the last image is being displayed
			if(flag>images.length-1) {
				handler.removeCallbacks(changeImage);
				btnStartMen.setVisibility(View.VISIBLE);
				btnStartMen.setEnabled(true);
				btnStartMen.setText("Stop");
			}else{

				btnConsent.setVisibility(View.GONE);
				btnStartMen.setVisibility(View.GONE);
				txtStatusHeart.setVisibility(View.GONE);
				txtSteps.setText(counter+"/14");
				txtStatusBarom.setVisibility(View.GONE);
				iv.setImageResource(getResources().getIdentifier(images[flag], "drawable", "com.microsoft.band.sdk.heartrate"));
				CurrentImage = images[flag];
				if(CurrentImage == "black")
					counter++;
				flag++;
				//IMPORTANT: Duration of every image
				//TIME IN MILISECONDS
				handler.postDelayed(changeImage, 10000);
			}
		}

	};

	//Heartrate listener
	private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
            	//appendToUI(String.format("Heart Rate = %d beats per minute\n"
            	//		+ "Quality = %s\n", event.getHeartRate(), event.getQuality()));
				appendToUI(String.format("Quality = %s\n",event.getQuality()));
				heartRate =  Integer.toString(event.getHeartRate());
				quality = event.getQuality().toString();
				appendLog(quality, heartRate, hrr, resistance, airPresure, temperature, CurrentImage);
            }
        }
    };

	private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener() {
		@Override
		public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
			if (event != null) {
				//appendToUI(String.format("RR Interval = %.3f s\n", event.getInterval()));
				hrr = String.format("%.5f",event.getInterval());
				appendLog(quality, heartRate,hrr, resistance, airPresure, temperature, CurrentImage);
			}
		}
	};

	private BandGsrEventListener mGsrEventListener = new BandGsrEventListener() {
		@Override
		public void onBandGsrChanged(final BandGsrEvent event) {
			if (event != null) {
				//appendToUIGsr(String.format("Resistance = %d kOhms\n", event.getResistance()));
				resistance = Integer.toString(event.getResistance());
					appendLog(quality, heartRate, hrr, resistance, airPresure, temperature, CurrentImage);
			}
		}
	};

	private BandBarometerEventListener mBarometerEventListener = new BandBarometerEventListener() {
		@Override
		public void onBandBarometerChanged(final BandBarometerEvent event) {
			if (event != null) {
				//appendToUIBar(String.format("Air Pressure = %.3f hPa\n"
				//		+ "Temperature = %.2f degrees Celsius", event.getAirPressure(), event.getTemperature()));
				airPresure = String.format("%.3f", event.getAirPressure());
				temperature = String.format("%.3f", event.getTemperature());
					appendLog(quality, heartRate, hrr, resistance, airPresure, temperature, CurrentImage);
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatusHeart = (TextView) findViewById(R.id.txtStatusHeart);
		txtStatusGSR = (TextView) findViewById(R.id.txtStatusGSR);
		txtStatusBarom = (TextView) findViewById(R.id.txtStatusBarom);
		txtSteps = (TextView) findViewById(R.id.txtStep);
		iv=(ImageView) findViewById(R.id.images);

        btnStartMen = (Button) findViewById(R.id.btnStartMen);
		btnStartMen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!started)
				{
					images = imagesforWomen.clone();
					preference = "female";
					txtStatusHeart.setText("");
					txtStatusGSR.setText("");
					txtStatusBarom.setText("");
					btnStartMen.setEnabled(false);
					btnConsent.setEnabled(false);
					btnStartWomen.setVisibility(View.GONE);
					new HeartRateSubscriptionTask().execute();
					new GsrSubscriptionTask().execute();
					new BarometerSubscriptionTask().execute();
					new RRIntervalSubscriptionTask().execute();
					handler.postDelayed(changeImage, 10000);
					started = true;
				}
				else{
					btnStartMen.setEnabled(false);
					Log.d("BAND", "stopped");
					if (client != null) {
						try {
							//UNREGISTER WHEN THE TEST FINISH
							client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
							client.getSensorManager().unregisterGsrEventListener(mGsrEventListener);
							client.getSensorManager().unregisterBarometerEventListener(mBarometerEventListener);
							client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
						} catch (BandIOException e) {
							appendToUI(e.getMessage());
						}
					}
				}

			}
		});

		btnStartWomen = (Button) findViewById(R.id.btnStartWomen);
		btnStartWomen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!started)
				{
					images = imagesforMen.clone();
					preference = "male";
					txtStatusHeart.setText("");
					txtStatusGSR.setText("");
					txtStatusBarom.setText("");
					btnStartWomen.setEnabled(false);
					btnConsent.setEnabled(false);
					btnStartMen.setVisibility(View.GONE);
					btnStartWomen.setVisibility(View.GONE);
					//launch listener for the Microsoft Band
					new HeartRateSubscriptionTask().execute();
					new GsrSubscriptionTask().execute();
					new BarometerSubscriptionTask().execute();
					new RRIntervalSubscriptionTask().execute();
					//slider launched
					handler.postDelayed(changeImage, 10000);
					started = true;
				}
			}
		});


		final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        
        btnConsent = (Button) findViewById(R.id.btnConsent);
        btnConsent.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("unchecked")
            @Override
			public void onClick(View v) {
				new HeartRateConsentTask().execute(reference);
			}
		});
    }


	@Override
	protected void onResume() {
		super.onResume();
		txtStatusHeart.setText("");
		txtStatusGSR.setText("");
		txtStatusBarom.setText("");
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		if (client != null) {
			try {
				client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
				client.getSensorManager().unregisterGsrEventListener(mGsrEventListener);
				client.getSensorManager().unregisterBarometerEventListener(mBarometerEventListener);
				client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
			} catch (BandIOException e) {
				appendToUI(e.getMessage());
			}
		}
	}
	
    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }
	private class RRIntervalSubscriptionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
					if (hardwareVersion >= 20) {
						if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
							client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
						} else {
							appendToUI("You have not given this application consent to access heart rate data yet."
									+ " Please press the Heart Rate Consent button.\n");
						}
					} else {
						appendToUI("The RR Interval sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
					}
				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
					case UNSUPPORTED_SDK_VERSION_ERROR:
						exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
						break;
					case SERVICE_ERROR:
						exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
						break;
					default:
						exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
						break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}
	private class BarometerSubscriptionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
					if (hardwareVersion >= 20) {
						//appendToUIBar("Band is connected.\n");
						client.getSensorManager().registerBarometerEventListener(mBarometerEventListener);
					} else {
						appendToUIBar("The Barometer sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
					}
				} else {
					appendToUIBar("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
					case UNSUPPORTED_SDK_VERSION_ERROR:
						exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
						break;
					case SERVICE_ERROR:
						exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
						break;
					default:
						exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
						break;
				}
				appendToUIBar(exceptionMessage);

			} catch (Exception e) {
				appendToUIBar(e.getMessage());
			}
			return null;
		}
	}
	private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
						client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
					} else {
						appendToUI("You have not given this application consent to access heart rate data yet."
								+ " Please press the Heart Rate Consent button.\n");
					}
				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
				case UNSUPPORTED_SDK_VERSION_ERROR:
					exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
					break;
				case SERVICE_ERROR:
					exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
					break;
				default:
					exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
					break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}
	
	private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
		@Override
		protected Void doInBackground(WeakReference<Activity>... params) {
			try {
				if (getConnectedBandClient()) {
					
					if (params[0].get() != null) {
						client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
							@Override
							public void userAccepted(boolean consentGiven) {
							}
					    });
					}
				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
				case UNSUPPORTED_SDK_VERSION_ERROR:
					exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
					break;
				case SERVICE_ERROR:
					exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
					break;
				default:
					exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
					break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}

	private class GsrSubscriptionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
					if (hardwareVersion >= 20) {
						//appendToUIGsr("Band is connected.\n");
						client.getSensorManager().registerGsrEventListener(mGsrEventListener);
					} else {
						appendToUIGsr("The Gsr sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
					}
				} else {
					appendToUIGsr("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage="";
				switch (e.getErrorType()) {
					case UNSUPPORTED_SDK_VERSION_ERROR:
						exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
						break;
					case SERVICE_ERROR:
						exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
						break;
					default:
						exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
						break;
				}
				appendToUIGsr(exceptionMessage);

			} catch (Exception e) {
				appendToUIGsr(e.getMessage());
			}
			return null;
		}
	}

	private void appendToUIBar(final String string) {
		this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	txtStatusHeart.setText(string);
            }
        });
	}
	private void appendToUIGsr(final String string) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				txtStatusGSR.setText(string);
			}
		});
	}
	private void appendToUI(final String string) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				txtStatusBarom.setText(string);
			}
		});
	}
	private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				appendToUI("Band isn't paired with your phone.\n");
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {
			return true;
		}
		
		appendToUI("Band is connecting...\n");
		return ConnectionState.CONNECTED == client.connect().await();
	}


	//METHOD THAT STORES THE DATA IN THE PHONE
	public void appendLog(String quality, String HeartRate, String hrr, String GSR, String AirP, String Temp, String image)
	{
		File backupPath = Environment.getExternalStorageDirectory();

		//PATH IN THE PHONE
		backupPath = new File(backupPath.getPath() + "/Android/data/Sensors");
		if(!backupPath.exists()){
			try{
				backupPath.mkdirs();
			} catch(Exception e){
				e.printStackTrace();
			}

		}

		File logFile = new File(backupPath + "/data.txt");
		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			//Write values as a lines with comas to converter in csv later.
			buf.append(preference+", "+quality+", "+HeartRate + ", "+hrr+", "+GSR+", "+AirP+", "+Temp+", "+ System.currentTimeMillis()+", "+image);
			buf.newLine();
			buf.close();

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

