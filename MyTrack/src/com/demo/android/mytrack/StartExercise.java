package com.demo.android.mytrack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;





import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class StartExercise extends Activity implements OnGestureListener,OnCompletionListener {    
	GoogleMap map;
	private CameraPosition cameraPosition;
	private MarkerOptions markerOpt;
	private LocationManager locManager;
	private Location location;	
	private String bestProvider=null;	
	private static final String TAG="GoogleMap";
	private double lat=0, lng=0;
	private Button start, cancel;
	private boolean startorcontinue=true;
	private Button btPrev, btNext, btPlay, btStop;		
	private TextView tvTime1, tvTime2, tvTime3
	               , tvDistance1, tvDistance2, tvDistance3
	               , tvAverageVelocity1, tvAverageVelocity2, tvAverageVelocity3
	               , tvHeight, tvAltitude
	               , tvMaxAltitude
	               , tvSongName	
	               , tvMaxVelocity;
	private List<Location> LocationList;
	private List VelocityList;
	private List AltitudeList;
	private double distance=0.00
			     , average_velocity=0.00
			     , altitude=0, height=0    
		         , max_velocity=0
		         , max_altitude=0 ,min_altitude=0;
	private Spinner spinner;	
	private int intCnt =  0;	
	private Handler handler = new Handler();	
	private ViewFlipper flipper; 
    private GestureDetector detector;	  
	private MediaPlayer mediaPlayer;
	private LinkedList<Song> songList;
	private int index = 0;
	private boolean isPause;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startexercise);
		
		//取得Provider
		boolean a=initProvider();
		
		findView();
		
		//設置監聽器
		setListeners();
		
		//取得音樂
		getMusics();
		
		//list資料
		LocationList=new ArrayList<Location>();
//		VelocityList=new ArrayList();
//		AltitudeList=new ArrayList();			
		
		//設定圖片
		setResource();
		
		//map基本設定
		initMap();				
		
		setToast();	
		
		//下拉選單
		setSpinner();		
		
		//滑動		
		detector = new GestureDetector(this);//起始化觸摸
		flipper = (ViewFlipper)findViewById(R.id.ViewFlipper01);//取得ViewFlipper
		                                                        //將View增加到flipper陣列中   		
		
		
		
		
//		AltitudeList.add(location.getAltitude());	
//		System.out.println("經度:"+location.getLatitude()+"緯度"+location.getLongitude());
		
		if(a){
			//Location初始化處理		
			location = locManager.getLastKnownLocation(bestProvider);
			updateToNewLocation(location);	
			LocationList.add(location);	
			min_altitude=location.getAltitude();
			//移動視窗到GPS所抓取到的經緯度位置
			cameraPosition=new CameraPosition.Builder()
			.target(new LatLng(location.getLatitude(), location.getLongitude()))
			.zoom(15)
			.bearing(0)
			.tilt(0)
			.build();
			map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			//LocationManager的監聽  
			//onLocationChanged的監聽事件處理		
			locManager.requestLocationUpdates(bestProvider, 2000, 0,new LocationListener(){		
			    public void onLocationChanged(Location location){		
			        // TODO Auto-generated method stub			
				    updateToNewLocation(location);				
				
			    }
			    @Override
			    public void onProviderDisabled(String provider) {
			    	// TODO Auto-generated method stub	
			    	updateToNewLocation(null);
			    	Log.v(TAG,"onProviderDisabled");
			    }
			    @Override
			    public void onProviderEnabled(String provider) {
				    // TODO Auto-generated method stub
				    //當GPS LocationProvider可用時，更新位置
				    location=locManager.getLastKnownLocation(provider);
				    Log.v(TAG,"onProviderEnabled");
			    }
			    @Override
			    public void onStatusChanged(String provider, int status, Bundle extras) {
				    // TODO Auto-generated method stub	
				    Log.v(TAG,"onStatusChanged");
			    }
			});	
		}		
	}
	
	
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.v(TAG,"onResume");
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	private void initMap(){
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setCompassEnabled(true);	
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);		
	}
	
	//取得GPS的Provider
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
//		 criteria.setAltitudeRequired(true);	
		 
		 if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			  bestProvider = LocationManager.GPS_PROVIDER;
			  return true;
			 }		
		 AlertDialog alertDialog=getAlertDialog("請開啟GPS設定", "請選擇");
		 alertDialog.show();
		 Toast.makeText(StartExercise.this, "請開啟GPS定位", Toast.LENGTH_SHORT).show();		 
		 return false;		 
	}
	
	//map位置更新
	private void updateToNewLocation(Location location){
		if(startorcontinue){
			
		}else{
			map.clear(); 
			Log.v(TAG,"updateToNewLocation");
			
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
			
			markerOpt = new MarkerOptions();
			markerOpt.position(new LatLng(lat, lng));
			markerOpt.title("My Position");
			markerOpt.draggable(false);
			map.addMarker(markerOpt);
			
			if(LocationList.size()==0){					
			}
			else{
			    distance=distance+location.distanceTo(LocationList.get(LocationList.size()-1));			    
			    if(location.getAltitude()-LocationList.get(LocationList.size()-1).getAltitude()>0){
			    	  height=height+location.getAltitude()-LocationList.get(LocationList.size()-1).getAltitude();
			    }			      
			}
			
			//最高海拔
			if(location.getAltitude()>max_altitude){
				max_altitude=location.getAltitude();
				DecimalFormat nf=new DecimalFormat("0");
				nf.setMaximumFractionDigits(2);					
				tvMaxAltitude.setText(nf.format(max_altitude));
			}
			
			//最低海拔
			if(location.getAltitude()<min_altitude){
				min_altitude=location.getAltitude();
			}
		
			
			//資料更新					
			setData(distance/1000,height,location.getAltitude());	
			
//			AltitudeList.add(altitude);				
			//最高海拔
//			List SortAltitudeList=new ArrayList();
//			SortAltitudeList=AltitudeList;
//			Collections.sort(SortAltitudeList);
//			DecimalFormat nf=new DecimalFormat("0");
//			nf.setMaximumFractionDigits(2);			
//			tvMaxAltitude.setText(String.valueOf(nf.format(SortAltitudeList.get(SortAltitudeList.size()-1))));			

			Log.v(TAG, "緯度:"+location.getLatitude()+"經度:"+location.getLongitude()+"min_altitude="+min_altitude);
			Log.v(TAG,"onLocationChanged");				
			
			//更新描繪軌跡
			LocationList.add(location);	
			drawPolyline(LocationList);			
			
		}
	
	}		
	
	//軌跡圖繪製
	private void drawPolyline(List<Location> location){
		PolylineOptions polylineOpt = new PolylineOptions();
		for(int a=0;a<location.size();a++){
		polylineOpt.add(new LatLng(location.get(a).getLatitude(), location.get(a).getLongitude()));
		    } 
		polylineOpt.color(Color.BLUE);
		Polyline polyline = map.addPolyline(polylineOpt);
		polyline.setWidth(10);
		}
	
	//時間處理
	private Runnable updateTimer = new Runnable() {
	    public void run() {
	        DecimalFormat nf = new DecimalFormat("00");
	        intCnt = intCnt + 1;	        
	        double hour=intCnt/3600;            
	        double minius = (intCnt % 3600)/60;
	        double seconds = intCnt % 60;              
	        tvTime1.setText(nf.format(hour)+":"+nf.format(minius)+":"+nf.format(seconds));
	        tvTime2.setText(nf.format(hour)+":"+nf.format(minius)+":"+nf.format(seconds));
	        tvTime3.setText(nf.format(hour)+":"+nf.format(minius)+":"+nf.format(seconds));
	       
	           
	        DecimalFormat nf1=new DecimalFormat();
	    	nf1.setMaximumFractionDigits(2);	
	        		    	
	    	average_velocity=Double.valueOf((distance/1000)/(Double.valueOf(intCnt)/3600));
	    	
//	        Log.e("average_velocityOFdistance", String.valueOf(distance));
	    	   
	        tvAverageVelocity1.setText(nf1.format(average_velocity));
	        tvAverageVelocity2.setText(nf1.format(average_velocity));
	        tvAverageVelocity3.setText(nf1.format(average_velocity));
	        System.out.println("average_velocity="+average_velocity+"km/h");  
	        
	        if(average_velocity>max_velocity){
	        	max_velocity=average_velocity;
	        	tvMaxVelocity.setText(nf1.format(max_velocity));	        	
	        }	        
	        handler.postDelayed(updateTimer,1000);	        	    
	    }
	};
	
	//TextView資料更新
    private void setData(double distance,double height,double altitdue){
        DecimalFormat nf=new DecimalFormat("0.00");
		nf.setMaximumFractionDigits(2);
		tvDistance1.setText(nf.format(distance));
		tvDistance2.setText(nf.format(distance));
		tvDistance3.setText(nf.format(distance));
			
		DecimalFormat nf2=new DecimalFormat("0");
		tvHeight.setText(nf2.format(height));			
		tvAltitude.setText(nf2.format(altitdue));		
	}	
    
	//button事件處理
	private OnClickListener start_OnClick=new Button.OnClickListener()
	{
		public void onClick(View v){
			if(startorcontinue){
				Drawable image = getResources().getDrawable( R.drawable.pause_normal_red_icon);
				start.setCompoundDrawablesWithIntrinsicBounds(image, null , null, null);
				start.setText(R.string.pause);
			    cancel.setText(R.string.stop);
			    handler.removeCallbacks(updateTimer);
                handler.postDelayed(updateTimer,10);
			    //cancelorstop=false;
			    startorcontinue=false;			   
			}
			else 
			{
				Drawable image = getResources().getDrawable( R.drawable.play_icon);
				start.setCompoundDrawablesWithIntrinsicBounds(image, null , null, null);
			    start.setText(R.string.Continue);
			    handler.removeCallbacks(updateTimer);
			    startorcontinue=true;
			}
		}		
	};
	
	private OnClickListener cancel_OnClick=new Button.OnClickListener()
	{
		public void onClick(View v){
			if(intCnt==0){
				StartExercise.this.finish();
			}
			else {
				
				Intent intent=new Intent();
				intent.setClass(StartExercise.this,Result.class);				
				Bundle bundle=new Bundle();
				bundle.putLong("time", intCnt);					
				bundle.putDouble("distance",distance);						
				bundle.putDouble("max_velocity", max_velocity);
				bundle.putDouble("height", height);					
				bundle.putDouble("min_altitude", min_altitude);
				bundle.putDouble("max_altitude", max_altitude);
				
				ArrayList list=new ArrayList();
				list.add(LocationList);						
				bundle.putParcelableArrayList("list",list);
				
                intent.putExtras(bundle);			
				startActivity(intent);	
				StartExercise.this.finish();
			}			
		}			
	};
	
	//音樂播放監聽器
	private OnClickListener btnPrev_OnClick=new Button.OnClickListener()
	{
		public void onClick(View v){
			doPrev();					
		}			
	};
	private OnClickListener btnNext_OnClick=new Button.OnClickListener()
	{
		public void onClick(View v){
			doNext();			
		}			
	};
	private OnClickListener btnPlay_OnClick=new Button.OnClickListener()
	{
		public void onClick(View v){
			doPlay();				
		}			
	};
	private OnClickListener btnStop_OnClick=new Button.OnClickListener()
	{
		public void onClick(View v){
			doStop();				
		}			
	};	
	
	//滑動效果
	@Override  
    public boolean onTouchEvent(MotionEvent event) {   
        return this.detector.onTouchEvent(event);   
    }  	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
	    if (e1.getX() - e2.getX() > 120) {//如果是從右向左滑動   
	                    //註冊flipper的進出效果 
	        this.flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in));
	        this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
	        this.flipper.showNext();   
	        return true;  
	    } else if (e1.getX() - e2.getX() < -120) {//如果是從左向右滑動 
	        this.flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in)); 
	        this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
	        this.flipper.showPrevious();
	        return true; 
	    }   
	    return false; 
	}  
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub		
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}	
	
	//音樂播放功能
	private void doStop() {
		if (mediaPlayer != null) {
			isPause = false;
			mediaPlayer.stop();
			btPlay.setText("Play");
			Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_start_icon);	
			btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
		}		
	}
	private void doPlay() {
		if (songList == null || songList.size() == 0) {
			return;
		}		
		if (btPlay.getText().toString().equals("Play")) {
			playing();
			btPlay.setText("Pause");
			Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_pause_icon);	
			btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
		}else{
			isPause = true;
			mediaPlayer.pause();
			Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_start_icon);	
			btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
			btPlay.setText("Play");
		}		
	}
	private void doplaySpinner(){
		if(songList==null||songList.size()==0){
			return;
		}
		if (btPlay.getText().toString().equals("Play")) {
			playing();
			Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_pause_icon);	
			btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
			btPlay.setText("Pause");
		}else{
			playing();
		}		
		
	}
	private void doNext() {
		if (songList == null || songList.size() == 0) {
			return;
		}		
		if (index < songList.size() - 1) {
			index++;
			isPause = false;
			playing();
			Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_pause_icon);	
			btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
			btPlay.setText("Pause");
		}
	}
	private void doPrev() {
		if (songList == null || songList.size() == 0) {
			return;
		}		
		if (index > 0) {
			index--;
			isPause = false;
			playing();
			Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_pause_icon);	
			btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
			btPlay.setText("Pause");
		}
	}
	private void playing(){
		if (mediaPlayer != null && !isPause) {
			mediaPlayer.release();
			mediaPlayer = null;
		}		
		if (mediaPlayer == null) {
			long id = songList.get(index).getId();
			Uri songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

			mediaPlayer = MediaPlayer.create(this, songUri);
			mediaPlayer.setOnCompletionListener(this);
		}		
		mediaPlayer.start();		
		tvSongName.setText("曲目: " + songList.get(index).getTitle() + 
							"\n專輯: " + songList.get(index).getAlbum() + 
							"\n(" + (index + 1) + "/" + songList.size() + ")");
	}	
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		doNext();
	}
	
	//取得音樂
	private void getMusics(){
		songList = new LinkedList<Song>();		
		ContentResolver contentResolver = getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (cursor == null) {
			Log.d("=======>", "查詢錯誤");
		} else if (!cursor.moveToFirst()) {
			Log.d("=======>", "沒有媒體檔");
		} else {
		    int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
		    int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
		    int albumColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.AudioColumns.ALBUM);
		    do {
		        long thisId = cursor.getLong(idColumn);
		        String thisTitle = cursor.getString(titleColumn);
		        String thisAlbum = cursor.getString(albumColumn);
		        Log.d("=======>", "id: " + thisId + ", title: " + thisTitle);
		        Song song = new Song();
		        song.setId(thisId);
		        song.setTitle(thisTitle);
		        song.setAlbum(thisAlbum);		       
		        songList.add(song);
		    } while (cursor.moveToNext());
		}		
		tvSongName.setText("共有 " + songList.size() + " 首歌曲");
	}
	
	//取得歌名
	private String[] getSongname(){
		String[] songname=new String[songList.size()];
		for(int k=0;k<songList.size();k++){
			songname[k]=songList.get(k).getTitle();			
		}
		return songname;
	}
	
	//設定spinner
	private void setSpinner(){
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,getSongname());		
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(StartExercise.this, "您選擇"+position, Toast.LENGTH_SHORT).show();
				index=position;
				doplaySpinner();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		}); 		
	}
	
	
	
	private void setToast(){
		//左右滑動提醒
//		Toast toastright = Toast.makeText(this,"向右滑動 查看詳細資料", Toast.LENGTH_LONG);
//	    toastright.setGravity(Gravity.LEFT, 0, 0);
//		LinearLayout toastViewright = (LinearLayout) toastright.getView();
//		ImageView imageright = new ImageView(this);
//		imageright.setImageResource(R.drawable.actions_go_next_icon);
//		ImageView imageleft = new ImageView(this);
//		imageright.setImageResource(R.drawable.actions_go_previous_icon);
//		toastViewright.addView(imageright, 0);
//		toastViewright.addView(imageleft, 1);
//		
//		toastright.show(); 	
		
	}
	
	//Menu處理
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
			StartExercise.this.finish();
		case 1:
		    {
			StartExercise.this.finish();
			Intent intent=new Intent();
			intent.setClass(StartExercise.this,Record.class);		
			startActivity(intent);	
		    }
		case 2:
		case 3:
		default:
		}		
		return super.onOptionsItemSelected(item); 
	}	
	
	private AlertDialog getAlertDialog(String title,String message){
		//產生一個Builder物件
	    Builder builder = new AlertDialog.Builder(StartExercise.this);
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
	
	//Gps監聽處理
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
	    @Override
	    public void onGpsStatusChanged(int event) {
	        switch (event) {
	            case GpsStatus.GPS_EVENT_STARTED:
	            Log.d(TAG, "GPS_EVENT_STARTED");
	            Toast.makeText(StartExercise.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
//	            Toast.makeText(StartExercise.this, "取得GPS信號", Toast.LENGTH_SHORT).show();
	            break;
	            case GpsStatus.GPS_EVENT_STOPPED:
	            Log.d(TAG, "GPS_EVENT_STOPPED");
	            Toast.makeText(StartExercise.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
//	            Toast.makeText(StartExercise.this, "GPS信號終止", Toast.LENGTH_SHORT).show();
	            break;
	            case GpsStatus.GPS_EVENT_FIRST_FIX:
	            Log.d(TAG, "GPS_EVENT_FIRST_FIX");
	            Toast.makeText(StartExercise.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
	            break;
	            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	            Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");          
	            break;
	       }
	    }
	};
	
	//設定圖片
	private void setResource(){
		Drawable StartImage = getResources().getDrawable( R.drawable.play_icon);	
		start.setCompoundDrawablesWithIntrinsicBounds(StartImage, null , null, null);
		
		Drawable btPlayImage = getResources().getDrawable(R.drawable.actions_media_playback_start_icon);	
		btPlay.setCompoundDrawablesWithIntrinsicBounds(null, btPlayImage, null, null);
		
		Drawable btStopImage = getResources().getDrawable(R.drawable.actions_media_playback_stop_icon);	
		btStop.setCompoundDrawablesWithIntrinsicBounds(null, btStopImage, null, null);
		
		Drawable btNextImage = getResources().getDrawable(R.drawable.actions_media_seek_forward_icon);	
		btNext.setCompoundDrawablesWithIntrinsicBounds(null, btNextImage, null, null);
		
		Drawable btPrevImage = getResources().getDrawable(R.drawable.actions_media_seek_backward_icon);	
		btPrev.setCompoundDrawablesWithIntrinsicBounds(null, btPrevImage, null, null);
		
		DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        btPlay.setWidth(dm.widthPixels/4);
        btStop.setWidth(dm.widthPixels/4);
        btNext.setWidth(dm.widthPixels/4);
        btPrev.setWidth(dm.widthPixels/4);
        
        Drawable CancelImage = getResources().getDrawable(R.drawable.stop_disabled_icon);	
		cancel.setCompoundDrawablesWithIntrinsicBounds(CancelImage, null, null, null);		
	}
	
	//findView
	private void findView(){
		start=(Button)findViewById(R.id.idStart);
		cancel=(Button)findViewById(R.id.idCancel);
		spinner=(Spinner)findViewById(R.id.spinnner);
		tvTime1=(TextView)findViewById(R.id.idTime1);
		tvTime2=(TextView)findViewById(R.id.idTime2);
		tvTime3=(TextView)findViewById(R.id.idTime3);
		btPrev = (Button) findViewById(R.id.idPrev);
		btNext = (Button) findViewById(R.id.idNext);
		btPlay = (Button) findViewById(R.id.idPlay);
		btStop = (Button) findViewById(R.id.idStop);
		tvSongName = (TextView) findViewById(R.id.idSongName);
		tvDistance1=(TextView)findViewById(R.id.idDistance1);
		tvDistance2=(TextView)findViewById(R.id.idDistance2);
		tvDistance3=(TextView)findViewById(R.id.idDistance3);	
		tvAverageVelocity1=(TextView)findViewById(R.id.idAverageVelocity1);
		tvAverageVelocity2=(TextView)findViewById(R.id.idAverageVelocity2);
		tvAverageVelocity3=(TextView)findViewById(R.id.idAverageVelocity3);
		tvHeight=(TextView)findViewById(R.id.idHeight3);
		tvAltitude=(TextView)findViewById(R.id.idAltitude3);
		tvMaxAltitude=(TextView)findViewById(R.id.idMaxAltitude3);
		tvMaxVelocity=(TextView)findViewById(R.id.idMaxVelocity3);
	}
	
	//監聽事件
	private void setListeners(){
		start.setOnClickListener(start_OnClick);
		cancel.setOnClickListener(cancel_OnClick);
		btPrev.setOnClickListener(btnPrev_OnClick);
		btNext.setOnClickListener(btnNext_OnClick);
		btPlay.setOnClickListener(btnPlay_OnClick);
		btStop.setOnClickListener(btnStop_OnClick);
		locManager.addGpsStatusListener(gpsListener); 
	}
}
