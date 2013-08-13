package edu.cmu.cma;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener{

	
	private final Context mContext;
	boolean isGPSenabled=false;
	boolean isNewworkenabled=false;
	boolean canGetLocation=false;
	Location mLocation;
	private double latitude;
	private double longitudue;
	private double altitude;
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE=10;
	private static final long MIN_TIME_BETWEEN_UPDATE=30*1000;
	
	protected LocationManager mLocationManager;
	public GPSTracker(Context mContext){
		this.mContext=mContext;
		getLocation();
	}
	
	
	private void getLocation() {
		// TODO Auto-generated method stub
		try {
			mLocationManager=(LocationManager)mContext.getSystemService(LOCATION_SERVICE);
			isGPSenabled=mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNewworkenabled=mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (!isGPSenabled && !isNewworkenabled) {
				//Do nothing because nothing is allowed!
			}else {
				this.canGetLocation=true;
				
				if (isNewworkenabled) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
					Log.d("NetWork", ":::Works");
					if (mLocationManager!=null) {
						mLocation=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (null!=mLocation) {
							latitude=mLocation.getLatitude();
							longitudue=mLocation.getLongitude();
							altitude=mLocation.getAltitude();
						}
					}
				}
				if (isNewworkenabled) {
					if (null==mLocation) {
						mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
						Log.d("GPS", "::::works");
						if (mLocationManager!=null) {
							mLocation=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (null!=mLocation) {
								latitude=mLocation.getLatitude();
								longitudue=mLocation.getLongitude();
								altitude=mLocation.getAltitude();
							}
						}
					}
				}
				
			}
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	public double getLatitude() {
		if (mLocation!=null) {
			return latitude;
		}
		else {
			return 0;
		}
	}


	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public double getLongitudue() {
		if (mLocation!=null) {
			return longitudue;
		}
		else {
			return 0;
		}
	}


	public void setLongitudue(double longitudue) {
		this.longitudue = longitudue;
	}


	public double getAltitude() {
		if (mLocation!=null) {
			return altitude;
		}
		else {
			return 0;
		}
	}


	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
    
	public boolean EnableGetLocation(){
		return this.canGetLocation;
	}
	
	public void showSettingAlert(){
		AlertDialog.Builder GPSdialog=new AlertDialog.Builder(mContext);
		GPSdialog.setTitle("GPS Settings");
		GPSdialog.setMessage("GPS is not enabled. Please check the settings");
		GPSdialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		GPSdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		GPSdialog.show();
	}
	public void stopUsingGPS(){
		if (mLocationManager!=null) {
			mLocationManager.removeUpdates(GPSTracker.this );
			
		}
	}
	
}
