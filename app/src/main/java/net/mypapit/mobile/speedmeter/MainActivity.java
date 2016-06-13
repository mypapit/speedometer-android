package net.mypapit.mobile.speedmeter;

import java.text.DecimalFormat;


import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	

	private TextView tvSpeed,tvUnit,tvLat,tvLon,tvAccuracy,tvHeading,tvMaxSpeed;
	static final String[] unit= {"km/h","mph","meter/sec","knots"};
	private int unitType;
	private NotificationCompat.Builder mbuilder;
	private NotificationManager mnotice;
	private double maxSpeed=-100;
	MainActivity activity;
	
	SharedPreferences prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
		tvUnit = (TextView) findViewById(R.id.tvUnitc);
		tvLat = (TextView) findViewById(R.id.tvLat);
		tvLon = (TextView) findViewById(R.id.tvLon);
		tvAccuracy = (TextView) findViewById(R.id.tvAccuracy);
		tvHeading = (TextView) findViewById(R.id.tvHeading);
		Typeface font = Typeface.createFromAsset(getBaseContext().getAssets(), "font/lcdn.ttf");
		tvSpeed.setTypeface(font);
		tvLat.setTypeface(font);
		tvLon.setTypeface(font);
		tvHeading.setTypeface(font);
		tvAccuracy.setTypeface(font);
		tvMaxSpeed.setTypeface(font);
		
		activity = this;
		//for handling notification
		mbuilder = new NotificationCompat.Builder(this);
		mnotice = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
		unitType=Integer.parseInt(prefs.getString("unit", "1"));
		tvUnit.setText(unit[unitType-1]);
		
		if (!this.isLocationEnabled()) {
			
			
			//show dialog if Location Services is not enabled
			

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.gps_not_found_title);  // GPS not found
			builder.setMessage(R.string.gps_not_found_message); // Want to enable?
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {

					Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					activity.startActivity(intent);
				}
			});

			//if no - bring user to selecting Static Location Activity
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(activity, "Please enable Location-based service / GPS", Toast.LENGTH_LONG).show();


				}


			});
			builder.create().show();

			
			
		}



		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK, "My wakelook");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		new SpeedTask(this).execute("string");


		
	}

	
	protected void onResume() {
		super.onResume();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		unitType=Integer.parseInt(prefs.getString("unit", "1"));
		tvUnit.setText(unit[unitType-1]);
		
		removeNotification();
		
		
	}
	
	protected void onStop(){
		super.onStop();
		
	displayNotification();
		
		
	}
	
	public void displayNotification() {
		
		mbuilder.setSmallIcon(R.drawable.ic_stat_notification);
		mbuilder.setContentTitle("SpeedoMeter is running...");
		mbuilder.setContentText("Click to view");
		
		Intent resultIntent = new Intent(this,MainActivity.class);
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		TaskStackBuilder stackBuilder =TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
		mbuilder.setContentIntent(resultPendingIntent);
		
		
		
		
		mnotice.notify(1337, mbuilder.build());

		
	}
	
	public void removeNotification() {
		mnotice.cancel(1337);
	}
	
	public void showDialog() throws NameNotFoundException {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.about_dialog);
		dialog.setTitle("About Speedometer "
				+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		dialog.setCancelable(true);

		// text
		TextView text = (TextView) dialog.findViewById(R.id.tvAbout);
		text.setText(R.string.txtLicense);

		// icon image
		ImageView img = (ImageView) dialog.findViewById(R.id.ivAbout);
		img.setImageResource(R.drawable.ic_launcher);

		dialog.show();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent();
			intent.setClass(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_quit:
			this.finish();
			System.exit(0);
			return true;
		case R.id.action_about:
			try {
				this.showDialog();
			} catch (NameNotFoundException e) {

				e.printStackTrace();
			}
			return true;
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class SpeedTask extends AsyncTask<String,Void,String>{
		MainActivity activity;
		float speed=0.0f;
		double lat;
		LocationManager locationManager ;
		
		public SpeedTask(MainActivity activity){
			this.activity=activity;
		}
		@Override
		protected String doInBackground(String... params) {
						locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
			
			
			
			return null;
			
		}
		protected void onPostExecute(String result)
		{
			tvUnit.setText(unit[unitType-1]);
			LocationListener listener = new LocationListener() {
				float filtSpeed;
				float localspeed;
				@Override
				public void onLocationChanged(Location location) {
					speed=location.getSpeed();
					float multiplier=3.6f;
					
					switch(unitType){
						case 1:
							multiplier=3.6f;
						break;
						case 2:
							multiplier = 2.25f;
						break;
						case 3:
							multiplier = 1.0f; 
						break;
						
						case 4:
							multiplier = 1.943856f;
						break;
							
					}
					
					if (maxSpeed<speed){
						maxSpeed = speed;
					}
					
				
					
					localspeed=(float) (speed*multiplier);
					
					filtSpeed = filter(filtSpeed, localspeed, 3); 
					
					DecimalFormat df = new DecimalFormat("#");
					
					
							
					lat=location.getLatitude();
					//speed=(float) location.getLatitude();
					Log.d("net.mypapit.speedview","Speed " + localspeed +"latitude: " + lat+" longitude: " + location.getLongitude());
					tvSpeed.setText(df.format(filtSpeed));
					
					tvMaxSpeed.setText(df.format(maxSpeed*multiplier));
					
					if (location.hasAltitude()){
						tvAccuracy.setText(df.format(location.getAccuracy()) +" m");
					} else {
						tvAccuracy.setText("NIL");
					}
					
					df = new DecimalFormat("#.0");
					
					if (location.hasBearing()){
						
						double bearing= location.getBearing();
						String strBearing="NIL";
						if (bearing <20.0){
							strBearing="North";
						} else if (bearing < 65.0){
							strBearing="North-East";
						} else if (bearing <110.0){
							strBearing="East";
						} else if (bearing <155.0) {
							strBearing="South-East";
						} else if (bearing < 200.0) {
							strBearing="South";
						} else if (bearing <250.0) {
							strBearing="South-West";
						} else if (bearing <290.0) {
							strBearing="West";
						} else if (bearing <345){
							strBearing="North-West";
						} else if (bearing <361) {
							strBearing="North";
						}
						
						tvHeading.setText(strBearing);
					} else {
						tvHeading.setText("NIL");
					}
					
					
					
					
					
					df = new DecimalFormat("#.0000");
					tvLat.setText(df.format(location.getLatitude()));
					tvLon.setText(df.format(location.getLongitude()));
					
					
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onProviderEnabled(String provider) {
					tvSpeed.setText("STDBY");
					tvMaxSpeed.setText("NIL");
					
					tvLat.setText("LATITUDE");
					tvLon.setText("LONGITUDE");
					tvHeading.setText("HEADING");
					tvAccuracy.setText("ACCURACY");
					
				}

				@Override
				public void onProviderDisabled(String provider) {
					tvSpeed.setText("NOFIX");
					tvMaxSpeed.setText("NIL");
					tvLat.setText("LATITUDE");
					tvLon.setText("LONGITUDE");
					tvHeading.setText("HEADING");
					tvAccuracy.setText("ACCURACY");
					
					
				}
				
			};
			
			
		
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			
			
				
			
			
		}
		   /** 
	     * Simple recursive filter 
	     * 
	     * @param prev Previous value of filter 
	     * @param curr New input value into filter 
	     * @return New filtered value 
	     * 
	     */ 
	    private float filter(final float prev, final float curr, final int ratio) {
	      // If first time through, initialise digital filter with current values 
	      if (Float.isNaN(prev))
	        return curr;
	      // If current value is invalid, return previous filtered value 
	      if (Float.isNaN(curr))
	        return prev;
	      // Calculate new filtered value 
	      return (float) (curr / ratio + prev * (1.0 - 1.0 / ratio));
	    } 
	 
		
	}
	
	public  boolean isLocationEnabled() {
		LocationManager lm = (LocationManager)
				this.getSystemService(Context.LOCATION_SERVICE);
		String provider = lm.getBestProvider(new Criteria(), true);
		return (!TextUtils.isEmpty(provider) &&
				!LocationManager.GPS_PROVIDER.equals(provider));


	} 	
	
}
