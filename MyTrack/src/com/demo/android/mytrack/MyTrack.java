package com.demo.android.mytrack;

import static android.provider.BaseColumns._ID;
import static com.demo.android.mytrack.DbConstants.DATE;
import static com.demo.android.mytrack.DbConstants.DISTANCE;
import static com.demo.android.mytrack.DbConstants.TABLE_NAME;
import static com.demo.android.mytrack.DbConstants.TIME;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.SessionDefaultAudience;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.utils.Logger;

public class MyTrack extends Activity{
    GoogleMap map;
	private CameraPosition cameraPosition;
	private LocationManager locManager;
	private Location location=null;
	private String bestProvider=null;	
	private static final String TAG="GoogleMap";
	private double lat=0, lng=0;
	private Button btBike, btRun;	         
    private TextView tvLastexercise, tvDistance, tvTime;	
    private SimpleFacebook mSimpleFacebook;    
    private static final String APP_ID = "374289906035869";
   	private static final String APP_NAMESPACE = "mytrackspace";
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//SimpleFacebook初始設定
		Logger.DEBUG_WITH_STACKTRACE = true;				
		Permissions[] permissions = new Permissions[]{
				Permissions.BASIC_INFO,
				Permissions.EMAIL,
				Permissions.USER_PHOTOS,
				Permissions.USER_BIRTHDAY,
				Permissions.USER_LOCATION,
				Permissions.PUBLISH_ACTION,
		        Permissions.PUBLISH_STREAM
		};
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
				.setAppId(APP_ID)
				.setNamespace(APP_NAMESPACE)
				.setPermissions(permissions)
				.setDefaultAudience(SessionDefaultAudience.FRIENDS)
				.build();
		SimpleFacebook.setConfiguration(configuration);			
		
		boolean a=initProvider();	
		
		findView();
		
		//設定監聽器
		setListeners();  
		
		//上次運動紀錄
		lastexercise();		
		
		//map基本設定
		setMap();
				
		if(a){
			location = locManager.getLastKnownLocation(bestProvider);
			updateToNewLocation(location);	
			//移動視窗到GPS所抓取到的經緯度位置
			cameraPosition=new CameraPosition.Builder()
			.target(new LatLng(location.getLatitude(), location.getLongitude()))
			.zoom(15)
			.bearing(0)
			.tilt(0)
			.build();
			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));	
			//LocationManager的監聽，LocationListener
			locManager.requestLocationUpdates(bestProvider,1000,1,new LocationListener(){
				
			    public void onLocationChanged(Location location){		
				    // TODO Auto-generated method stub
				    updateToNewLocation(location);	
					Log.v(TAG, "緯度:"+location.getLatitude()+"經度:"+location.getLongitude());
//					Log.v(TAG,"MyTrack.onLocationChanged");
				}
				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub	
					updateToNewLocation(null);
//					Log.v(TAG,"MyTrack.onProviderDisabled");
				}
				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					//當GPS LocationProvider可用時，更新位置
					location=locManager.getLastKnownLocation(provider);
//					Log.v(TAG,"MyTrack.onProviderEnabled");
				}
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					// TODO Auto-generated method stub	
//					Log.v(TAG,"MyTrack.onStatusChanged");
				}
			});	
		}	
	}	
	
	//map設定
	private void setMap(){
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setCompassEnabled(true);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);			
	}
	
    private boolean initProvider(){		 
		 locManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		 //List all providers
//		 List<String> providers=locManager.getAllProviders();
//		 Criteria criteria=new Criteria();
//		 criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		 criteria.setAltitudeRequired(true);
//		 criteria.setBearingRequired(true);
//		 criteria.setCostAllowed(false);
//		 criteria.setSpeedRequired(true);
//		 criteria.setPowerRequirement(Criteria.POWER_LOW);
//		 bestProvider=locManager.getBestProvider(criteria, true);
//		 location=locManager.getLastKnownLocation(bestProvider);
		 
		 if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
	         bestProvider = LocationManager.GPS_PROVIDER;
			 return true;
		 }
	    AlertDialog alertDialog=getAlertDialog("請開啟GPS設定", "請選擇");
	    alertDialog.show();
		Toast.makeText(MyTrack.this, "請開啟GPS定位", Toast.LENGTH_SHORT).show();		 		 
		return false; 
	 }
    
    private void updateToNewLocation(Location location){
		map.clear();	
		if(location!=null){
			lat=location.getLatitude();
			lng=location.getLongitude();			
		}
		//移動視窗到GPS所抓取到的經緯度位置
		cameraPosition=new CameraPosition.Builder()
		.target(new LatLng(lat,lng))
		.zoom(15)
		.bearing(0)
		.tilt(0)
		.build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));		 
//		Log.e("location.getAccuracy()",""+location.getAccuracy());	
	}
    
	@Override
	protected void onResume(){
		super.onResume();
		Log.v(TAG,"MyTrack.onResume");	
		mSimpleFacebook = SimpleFacebook.getInstance(this);
	}	
	
	private void findView(){
		btBike=(Button)findViewById(R.id.bike_button);
		btRun=(Button)findViewById(R.id.run_button);	
		tvLastexercise=(TextView)findViewById(R.id.LastExercise);
		tvTime=(TextView)findViewById(R.id.idTime);
		tvDistance=(TextView)findViewById(R.id.idDistance);		
	}
	
	private void setListeners(){
		btBike.setOnClickListener(btBikeOnClick);
		btRun.setOnClickListener(btRunOnClick);
	    locManager.addGpsStatusListener(gpsListener); 
	}	
	
	private OnClickListener btBikeOnClick=new Button.OnClickListener()
	{
		public void onClick(View v){			
			Intent intent=new Intent();
			intent.setClass(MyTrack.this,StartExercise.class);			
			startActivity(intent);
			}		
	};
	
	private OnClickListener btRunOnClick=new Button.OnClickListener()
	{
		public void onClick(View v){			
			Intent intent=new Intent();
			intent.setClass(MyTrack.this,StartExercise.class);			
			startActivity(intent);
			}		
	};
	
	//上次運動紀錄顯示
	private void lastexercise(){
		DBHelper dbhelper= new DBHelper(MyTrack.this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();		
		Cursor c=db.query(TABLE_NAME, new String[]{_ID, DATE, TIME, DISTANCE}, null, null, null, null, null);
		if(c.getCount()==0){
			
		}
		else{
			c.moveToPosition(c.getCount()-1);
			tvLastexercise.setText(c.getString(1));	
			tvTime.setText(c.getString(2));
			tvDistance.setText(c.getString(3));			
		}			
	}
	
	private AlertDialog getAlertDialog(String title,String message){
		//產生一個Builder物件
	    Builder builder = new AlertDialog.Builder(MyTrack.this);
		//設定Dialog的標題
		builder.setTitle(title);
		//設定Dialog的內容
		builder.setMessage(message);
		//設定Positive按鈕資料
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener(){
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    }
		});
	    //設定Negative按鈕資料
		builder.setNegativeButton("開啟GPS", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Intent intent=new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		        startActivity(intent);		               
		    }
		});
		//利用Builder物件建立AlertDialog
	    return builder.create();
    }
	
	//gps監聽處理
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
		public void onGpsStatusChanged(int event) {
		    switch (event) {
		        case GpsStatus.GPS_EVENT_STARTED:
		            Log.d(TAG, "GPS_EVENT_STARTED");
		            Toast.makeText(MyTrack.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
		            break;
		        case GpsStatus.GPS_EVENT_STOPPED:
		            Log.d(TAG, "GPS_EVENT_STOPPED");
		            Toast.makeText(MyTrack.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
		            break;
		        case GpsStatus.GPS_EVENT_FIRST_FIX:
		            Log.d(TAG, "GPS_EVENT_FIRST_FIX");
		            Toast.makeText(MyTrack.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
		            break;
		        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
		            Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");	            
		            break;
		    }
		}
   };
	
	//Menu設定
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		menu.add(0, 0, 0, "首頁");
		menu.add(0, 1, 1, "紀錄");
		menu.add(0, 2, 2, "設定");
		menu.add(0, 3, 3, "登出");		
		return super.onCreateOptionsMenu(menu);
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case 0:			
		case 1:
		{
			Intent intent=new Intent();
			intent.setClass(MyTrack.this,Record.class);		
		    startActivity(intent);
		}
		case 2:
		case 3:
		default:
		}		
		return super.onOptionsItemSelected(item); 
	} 	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	
}
