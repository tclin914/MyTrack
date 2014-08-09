package com.demo.android.mytrack;

import static android.provider.BaseColumns._ID;
import static com.demo.android.mytrack.DbConstants.AVERAGE_VELOCITY;
import static com.demo.android.mytrack.DbConstants.CALORIES;
import static com.demo.android.mytrack.DbConstants.DATE;
import static com.demo.android.mytrack.DbConstants.DISTANCE;
import static com.demo.android.mytrack.DbConstants.HEIGHT;
import static com.demo.android.mytrack.DbConstants.INTCNT;
import static com.demo.android.mytrack.DbConstants.LOCATION;
import static com.demo.android.mytrack.DbConstants.MAX_ALTITUDE;
import static com.demo.android.mytrack.DbConstants.MAX_VELOCITY;
import static com.demo.android.mytrack.DbConstants.MIN_ALTITUDE;
import static com.demo.android.mytrack.DbConstants.TABLE_NAME;
import static com.demo.android.mytrack.DbConstants.TIME;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.SessionDefaultAudience;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebook.OnLoginListener;
import com.sromku.simple.fb.SimpleFacebook.OnPublishListener;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.utils.Logger;

public class Report extends Activity implements OnGestureListener{	
	GoogleMap map;	
	private TextView  tvDate
	                , tvCalories
	                , tvTime
	                , tvDistance	               
	                , tvAverageVelocity
	                , tvMaxVelocity
                    , tvHeight
                    , tvMinAltitude
                    , tvMaxAltitude ;                    
//    private View Altitudechart,Velocitychart;
    private GestureDetector detector;//觸摸監聽
    private ViewFlipper flipper;//ViewFlipper   
    private List<LatLng> latlnglist=null;   
    private String[] columns = {_ID, DATE, CALORIES, TIME, DISTANCE, AVERAGE_VELOCITY,
            MAX_VELOCITY, HEIGHT, MIN_ALTITUDE, MAX_ALTITUDE, INTCNT}; 
    private double intCnt ,distance, average_velocity;  
    private Button btPublish;
    private LinearLayout layout;  
    private String messege;    
    
    private SimpleFacebook mSimpleFacebook;    
    private static final String APP_ID = "374289906035869";
	private static final String APP_NAMESPACE = "mytrackspace";   
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		findView();
		setMap();		
		setResourse();	
		setSimpleFacebook();
		
		btPublish.setText("上傳我的運動紀錄到FaceBook");

		//滑動		
		detector = new GestureDetector(this);//起始化觸摸
		flipper = (ViewFlipper)findViewById(R.id.ViewFlipper04);//取得ViewFlipper
						                                                //將View增加到flipper陣列中   	
		
		Bundle bundle=this.getIntent().getExtras();
		String id=bundle.getString("id");
			
		setDate(id);	
		showLocation(id);
		
//		showAltitude();
//		showVelocity();
//		flipper.addView(Altitudechart);	
//		flipper.addView(Velocitychart);		
		
//		View view=(View)findViewById(R.drawable.facebooktestpicture);
//		view.setDrawingCacheEnabled(true);
//		Bitmap image=Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
//		view.draw(new Canvas(image));			
	}
	
	private void setDate(String id){
		DBHelper dbhelper= new DBHelper(Report.this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();

        Cursor c=db.query(TABLE_NAME, columns, null, null, null, null, null);
		c.moveToPosition(Integer.valueOf(id)-1);       
        
		//計算平均速度
		intCnt=Double.valueOf(c.getString(10));
		Log.e("intCnt", ""+intCnt);
		distance=Double.valueOf(c.getString(4));
		average_velocity=distance/(intCnt/3600);
		DecimalFormat nf=new DecimalFormat();
		nf.setMaximumFractionDigits(2);
	
		
		tvDate.setText(c.getString(1));
		tvTime.setText(c.getString(3));
		tvDistance.setText(c.getString(4));

		tvAverageVelocity.setText(nf.format(average_velocity));
		tvMaxVelocity.setText(c.getString(6));
		tvHeight.setText(c.getString(7));
		tvMinAltitude.setText(c.getString(8));
		tvMaxAltitude.setText(c.getString(9));
			
		   
		btPublish.setOnClickListener(btPublish_OnClick);	
		
	}
	
	private OnPublishListener onPublishListener=new OnPublishListener(){

		@Override
		public void onThinking() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onException(Throwable throwable) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFail(String reason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onComplete(String id) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		
	}
	
	private void setSimpleFacebook(){
		// set log to true
		Logger.DEBUG_WITH_STACKTRACE = true;
		// initialize facebook configuration
		Permissions[] permissions = new Permissions[]
			{
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
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
	
		
	private OnClickListener btPublish_OnClick=new Button.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
//			publishFeedDialog();
			
			mSimpleFacebook.login(onLoginListener);
			Toast.makeText(Report.this, "成功登入我的Facebook", Toast.LENGTH_SHORT).show();
			
			openInputDialog();
			
			
			
	    }
	};
	
	private void uploadmyrecord(){
		//上傳運動紀錄
		layout.setDrawingCacheEnabled(true);
		layout.buildDrawingCache();
		Bitmap bmp=layout.getDrawingCache();
		
		
		
			Photo photo=new Photo(bmp);
			photo.addDescription(messege);
			photo.addPlace("110619208966868");				
			mSimpleFacebook.publish(photo, onPublishListener);
			Toast.makeText(Report.this, "成功上傳我的運動紀錄", Toast.LENGTH_SHORT).show();
		
		
	}
	
	private void uploadmytrack(){
		//上傳運動軌跡		
	
        SnapshotReadyCallback callback=new  SnapshotReadyCallback(){
//        	Bitmap bitmap;
			@Override
			public void onSnapshotReady(Bitmap arg0) {
				// TODO Auto-generated method stub
				Photo photoMap=new Photo(arg0);
				photoMap.addDescription(messege);
				photoMap.addPlace("110619208966868");			
				mSimpleFacebook.publish(photoMap, onPublishListener);
				Toast.makeText(Report.this, "成功上傳我的運動軌跡", Toast.LENGTH_SHORT).show();
			}
				
			};
			map.snapshot(callback);			
	}
	
	private void openInputDialog(){
		LayoutInflater inflater=LayoutInflater.from(Report.this);
		final View v=inflater.inflate(R.layout.alertdialog_edittext, null);
		new AlertDialog.Builder(Report.this)
		.setTitle("你的運動心得或描述")
		.setView(v)
		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
			
			EditText edittext=(EditText)v.findViewById(R.id.idedittext);
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			    messege=edittext.getText().toString();				
				
				
				switch(flipper.indexOfChild(flipper.getCurrentView())){
			    case 0: uploadmyrecord();
			    break;			            
			    case 1: uploadmytrack();
			    break;
			    }             
			}
		})
		.show();
		
	}
	
//	 protected Bitmap ConvertToBitmap(LinearLayout layout) {
//
//	        Bitmap map;
//
//		    layout.setDrawingCacheEnabled(true);
//
//	        layout.buildDrawingCache();
//
//	        return map=layout.getDrawingCache();
//
//
//	    }
	
	public static Bitmap loadBitmapFromView(View v) {     
	       Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width,
	                                       v.getLayoutParams().height, 
	                                       Bitmap.Config.ARGB_8888);                     
	       Canvas c = new Canvas(b);     
	       v.measure(MeasureSpec.makeMeasureSpec(v.getLayoutParams().width, 
	                             MeasureSpec.EXACTLY),         
		         MeasureSpec.makeMeasureSpec(v.getLayoutParams().height, 
	                             MeasureSpec.EXACTLY)); 
	       v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight()); 
	       v.draw(c);
			
	       return b; 
	} 
	
	private void setMap(){
		//地圖基本設定
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setCompassEnabled(true);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);			
	}	
	
    private void showLocation(String id){		
		DBHelper dbhelper= new DBHelper(Report.this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String[] columns={_ID,LOCATION};
		Cursor c=db.query( TABLE_NAME, columns, null, null, null, null, null);		
		
		MyList loc=null;
		LatLng latlng = null;
		latlnglist=new ArrayList<LatLng>();		
		
		
		if(c!=null){		
			c.moveToPosition(Integer.valueOf(id)-1);
			Log.e("Last_id",c.getString(0)); 
				try{
					SerializerClass serializer = new SerializerClass();				
					
					loc = (MyList) serializer.deserializeObject(c.getBlob(1));
					
					Log.e("loc.Stringlist.size()",""+loc.Stringlist.size());					 
					
				}catch(Exception ex){
					ex.printStackTrace();
					Log.e("SIZE1",ex.toString());
				}			
		}				
		
		 for(int i=0;i<loc.Stringlist.size();i=i+2){                     	
			  latlng=new LatLng(Double.valueOf(loc.Stringlist.get(i)), Double.valueOf(loc.Stringlist.get(i+1)));    
			  Log.e("LatLng",latlng.latitude+","+latlng.longitude);
			  latlnglist.add(latlng); 
			 		  
         }
//		latlng=new LatLng(Double.valueOf(test.Stringlist.get(0)), Double.valueOf(test.Stringlist.get(1)));	
		
		drawPolyline(latlnglist);		
		c.close();
		db.close();				
	}

    //軌跡圖繪製
	private void drawPolyline(List<LatLng> latlnglist){
	    PolylineOptions polylineOpt = new PolylineOptions();
	    
		for(int i=0;i<latlnglist.size();i++){
		polylineOpt.add(latlnglist.get(i));
			    } 
		
		polylineOpt.color(Color.BLUE);
		Polyline polyline = map.addPolyline(polylineOpt);
		polyline.setWidth(10);
		
		
		//移動視窗到GPS所抓取到的經緯度位置
		CameraPosition cameraPosition=new CameraPosition.Builder()
		.target(latlnglist.get(0))
		.zoom(15)
		.bearing(0)
		.tilt(0)
		.build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
		MarkerOptions markerOpt = new MarkerOptions();
		markerOpt.position(latlnglist.get(0));
		markerOpt.title("My Position");
		markerOpt.draggable(false);
		map.addMarker(markerOpt); 
	}
	
	private void findView(){
		tvDate=(TextView)findViewById(R.id.idDate5);
		tvTime=(TextView)findViewById(R.id.idTime5);
		tvDistance=(TextView)findViewById(R.id.idDistance5);		
		tvAverageVelocity=(TextView)findViewById(R.id.idAverageVelocity5);
		tvMaxVelocity=(TextView)findViewById(R.id.idMaxVelocity5);
		tvHeight=(TextView)findViewById(R.id.idHeight5);
		tvMinAltitude=(TextView)findViewById(R.id.idMinAltitude5);
        tvMaxAltitude=(TextView)findViewById(R.id.idMaxAltitude5);      
        btPublish=(Button)findViewById(R.id.idPublish);
        layout=(LinearLayout)findViewById(R.id.report1);      
	}
	
	private void setResourse(){
		Drawable PublishImage = getResources().getDrawable( R.drawable.button_upload_icon);	
		btPublish.setCompoundDrawablesWithIntrinsicBounds(PublishImage, null , null, null);		
	}

	
	public boolean onKeyDown(int keyCode , KeyEvent event) {
		  if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
		    // do something on back.
			
			Intent intent=new Intent();
		    intent.setClass(Report.this,Record.class);		
			startActivity(intent);
			Report.this.finish();
		    
		    return true;
		  }

		  return super.onKeyDown(keyCode, event);
		}
	
	private OnLoginListener onLoginListener=new OnLoginListener(){
	    @Override
		public void onThinking() {
			// TODO Auto-generated method stub
				
		}
		@Override
		public void onException(Throwable throwable) {
			// TODO Auto-generated method stub
				
		}
		@Override
		public void onFail(String reason) {
			// TODO Auto-generated method stub
				
		}
		@Override
		public void onLogin() {
			// TODO Auto-generated method stub
				
		}
		@Override
		public void onNotAcceptingPermissions() {
			// TODO Auto-generated method stub
			Log.e("SimpleFacebook", "Logged in");
		}			
	};
		
//	private void showAltitude(){
//		
//		DBHelper dbhelper= new DBHelper(Report.this);
//		SQLiteDatabase db = dbhelper.getReadableDatabase();
//		String[] columns={_ID,ALTITUDE};
//		Cursor c=db.query( TABLE_NAME, columns, null, null, null, null, null);	
//		
//		MyList alt=null;
//		
//		if(c!=null){		
//			c.moveToPosition(Integer.valueOf(id)-1);
//			Log.e("Last_id",c.getString(0));
//		    try{
//					SerializerClass serializer = new SerializerClass();				
//					
//					alt = (MyList) serializer.deserializeObject(c.getBlob(1));
//					
//					Log.e("alt.doublelist.size()",""+alt.doublelist.size());				                              
//					 
//					
//					}catch(Exception ex){
//						ex.printStackTrace();
//						Log.e("SIZE1",ex.toString());
//					}
//			for(int i=0;i<alt.doublelist.size();i++){      	
//					  
//					  Log.e("alt",""+alt.doublelist.get(i));					 		  
//		         }			
//		}
//		
////		Collections.sort(alt.doublelist);
////		
////		DecimalFormat nf=new DecimalFormat();
////		nf.setMaximumFractionDigits(3);
////		
////		
////		min_altitude=String.valueOf(nf.format(Double.valueOf(String.valueOf(alt.doublelist.get(0)))));
////	  
////		max_altitude=String.valueOf(nf.format(Double.valueOf(String.valueOf(alt.doublelist.get(alt.doublelist.size()-1)))));
////		
////		Log.e("MinMax", min_altitude+max_altitude);
//		
//		
//		
//		String[] titles = new String[] { "海拔" }; // 定義折線的名稱
//	    List<double[]> x = new ArrayList<double[]>(); // 點的x坐標
//	    List<double[]> y = new ArrayList<double[]>(); // 點的y坐標
//	        // 數值X,Y坐標值輸入
//	    double[] AltitudetoArray=new double[alt.doublelist.size()];
//	    for(int i=0;i<alt.doublelist.size();i++){
//	    	AltitudetoArray[i]=Double.valueOf(String.valueOf(alt.doublelist.get(i)));
//	    }
//	 
////	    double[] AltitudetoArray  = ArrayUtils.toPrimitive( alt.doublelist.toArray(new Double[alt.doublelist.size()]));
////	        
//	        Log.e("AltitudetoArray",""+ AltitudetoArray.length);
//	       
//	        double[] Time=new double[AltitudetoArray.length] ;
//	        
//			for(int i=0;i<AltitudetoArray.length;i++){
//	        	Time[i]=i*2;
//	        }
//	                
//	        
//	        x.add(Time);	       
//	        y.add(AltitudetoArray); 
////	        x.add(new double[]{1,3,5,7,9});
////	        y.add(new double[]{2,4,6,8,10});
//	        
//	        XYMultipleSeriesDataset dataset = buildDatset(titles, x, y); // 儲存座標值
//
//	        int[] colors = new int[] { Color.BLUE};// 折線的顏色
//	        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE }; // 折線點的形狀
//	        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);
//
//	        setChartSettings(renderer, "海拔折線圖", "時間(s)", "海拔(m)", 0, 60, 0, 100, Color.BLACK);// 定義折線圖
//	        Altitudechart = ChartFactory.getLineChartView(this, dataset, renderer);
//	        int height=getWindowManager().getDefaultDisplay().getHeight();
//	        LayoutParams parms = new LayoutParams(LayoutParams.MATCH_PARENT, height-300);
//	        Altitudechart.setLayoutParams(parms);   
//
//   }
	
//	private void showVelocity(){
//        String[] titles = new String[] { "速度" }; // 定義折線的名稱
//	    List<double[]> x = new ArrayList<double[]>(); // 點的x坐標
//	    List<double[]> y = new ArrayList<double[]>(); // 點的y坐標
//	    // 數值X,Y坐標值輸入
//	        
////	    double[] VelocitytoArray  = ArrayUtils.toPrimitive(VelocityList.toArray(new Double[VelocityList.size()]));
//	        
////	    Log.e("AltitudetoArray",""+ VelocitytoArray.length);
////	       
////	    double[] Time=new double[VelocitytoArray.length] ;
////	        
////	    for(int i=0;i<VelocitytoArray.length;i++){
////	        Time[i]=i*2;
////	        }	                
////	        
////	    x.add(Time);	       
////	    y.add(VelocitytoArray); 
//        x.add(new double[]{1,3,5,7,9});
//        y.add(new double[]{2,4,6,8,10});
//
//	        
//	    XYMultipleSeriesDataset dataset = buildDatset(titles, x, y); // 儲存座標值
//
//	    int[] colors = new int[] { Color.GREEN};// 折線的顏色
//	    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE }; // 折線點的形狀
//	    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);
//
//	    setChartSettings(renderer, "速度折線圖", "時間(s)", "速度(km/h)", 0, 60, 0, 100, Color.BLACK);// 定義折線圖
//	    Velocitychart = ChartFactory.getLineChartView(this, dataset, renderer);
//	    int height=getWindowManager().getDefaultDisplay().getHeight();
//	    LayoutParams parms = new LayoutParams(LayoutParams.MATCH_PARENT, height-300);
//	    Velocitychart.setLayoutParams(parms);   
//
//    }    

//	// 定義折線圖名稱
//   protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
//           String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor) {
//       renderer.setChartTitle(title); // 折線圖名稱
//       renderer.setChartTitleTextSize(24); // 折線圖名稱字形大小
//       renderer.setXTitle(xTitle); // X軸名稱
//       renderer.setYTitle(yTitle); // Y軸名稱
//       renderer.setXAxisMin(xMin); // X軸顯示最小值
//       renderer.setXAxisMax(xMax); // X軸顯示最大值
//       renderer.setXLabelsColor(Color.BLACK); // X軸線顏色
//       renderer.setYAxisMin(yMin); // Y軸顯示最小值
//       renderer.setYAxisMax(yMax); // Y軸顯示最大值
//       renderer.setAxesColor(axesColor); // 設定坐標軸顏色
//       renderer.setYLabelsColor(0, Color.BLACK); // Y軸線顏色
//       renderer.setLabelsColor(Color.BLACK); // 設定標籤顏色
//       renderer.setMarginsColor(Color.WHITE); // 設定背景顏色
//       renderer.setShowGrid(true); // 設定格線       
//       renderer.setInScroll(false);
//       renderer.setClickEnabled(false);
//       renderer.setExternalZoomEnabled(false);
//       renderer.setAxisTitleTextSize(26);
//       renderer.setChartTitleTextSize(32);
//       
//               
//   }
//
//   // 定義折線圖的格式
//   private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles, boolean fill) {
//       XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
//       int length = colors.length;
//       for (int i = 0; i < length; i++) {
//           XYSeriesRenderer r = new XYSeriesRenderer();
//           r.setColor(colors[i]);
//           r.setPointStyle(styles[i]);
//           r.setFillPoints(fill);
//           r.setLineWidth(3);
//           r.setPointStrokeWidth(30);             
//           renderer.addSeriesRenderer(r); //將座標變成線加入圖中顯示
//       }
//       return renderer;
//   }
//   
//
//
//   // 資料處理
//   private XYMultipleSeriesDataset buildDatset(String[] titles, List<double[]> xValues,
//           List<double[]> yValues) {
//       XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
//
//       int length = titles.length; // 折線數量
//       for (int i = 0; i < length; i++) {
//           // XYseries對象,用於提供繪製的點集合的資料
//           XYSeries series = new XYSeries(titles[i]); // 依據每條線的名稱新增
//           double[] xV = xValues.get(i); // 獲取第i條線的資料
//           double[] yV = yValues.get(i);
//           int seriesLength = xV.length; // 有幾個點
//
//           for (int k = 0; k < seriesLength; k++) // 每條線裡有幾個點
//           {
//               series.add(xV[k], yV[k]);
//           }
//           dataset.addSeries(series);
//       }
//       return dataset;
//   }
		
	//Meun設定
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
			Report.this.finish();
			
		case 1:
		    {
			Report.this.finish();
			Intent intent=new Intent();
			intent.setClass(Report.this,Record.class);		
			startActivity(intent);	
		    }
		case 2:
		case 3:
		default:
		}		
		return super.onOptionsItemSelected(item); 
	}
	
    //滑動效果
	@Override  
    public boolean onTouchEvent(MotionEvent event) {   
        return this.detector.onTouchEvent(event);   
    }  	

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > 120) {//如果是從右向左滑動   
            //註冊flipper的進出效果 
            this.flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in));
            this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
            this.flipper.showNext();  
            
            switch(flipper.indexOfChild(flipper.getCurrentView())){
    		case 0:btPublish.setText("上傳我的運動紀錄到FaceBook");
    		    break;
    		case 1:btPublish.setText("上傳我的運動軌跡到FaceBook");
    		    break;		
    		}
            return true;  
        } else if (e1.getX() - e2.getX() < -120) {//如果是從左向右滑動 
            this.flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in)); 
            this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
            this.flipper.showPrevious();
            Log.e("flipper ID", ""+  flipper.indexOfChild(flipper.getCurrentView()));
            
            switch(flipper.indexOfChild(flipper.getCurrentView())){
    		case 0:btPublish.setText("上傳我的運動紀錄到FaceBook");
    		    break;
    		case 1:btPublish.setText("上傳我的運動軌跡到FaceBook");
    		    break;		
    		}
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
}
