package com.demo.android.mytrack;

import static com.demo.android.mytrack.DbConstants.ALTITUDE;
import static com.demo.android.mytrack.DbConstants.AVERAGE_VELOCITY;
import static com.demo.android.mytrack.DbConstants.CALORIES;
import static com.demo.android.mytrack.DbConstants.DATE;
import static com.demo.android.mytrack.DbConstants.DISTANCE;
import static com.demo.android.mytrack.DbConstants.HEIGHT;
import static com.demo.android.mytrack.DbConstants.INTCNT;
import static com.demo.android.mytrack.DbConstants.LOCATION;
import static com.demo.android.mytrack.DbConstants.MAX_ALTITUDE;
import static com.demo.android.mytrack.DbConstants.MAX_SLOPE;
import static com.demo.android.mytrack.DbConstants.MAX_VELOCITY;
import static com.demo.android.mytrack.DbConstants.MIN_ALTITUDE;
import static com.demo.android.mytrack.DbConstants.MIN_SLOPE;
import static com.demo.android.mytrack.DbConstants.TABLE_NAME;
import static com.demo.android.mytrack.DbConstants.TIME;
import static com.demo.android.mytrack.DbConstants.VELOCITY;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.*;
import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;



public class Result extends Activity implements OnGestureListener,OnCompletionListener{	
	GoogleMap map; 
	private CameraPosition cameraPosition;
	private MarkerOptions markerOpt;
	private Location location;	
    private TextView  tvDate,tvCalories
                    , tvTime
                    , tvDistance
                    , tvVelocity
                    , tvAverageVelocity
                    , tvMaxVelocity
                    , tvHeight
                    , tvMinAltitude
                    , tvMaxAltitude ;                   
    private double calories=0;  	
	private MyList loc=null
			     , alt=null
			     , vel=null;
	private long intCnt;
	//滑動
	private ViewFlipper flipper;//ViewFlipper   
	private GestureDetector detector;//觸摸監聽
	private View view=null;
	private final int ONE = 0;	
	private final int TWO = 1;
	private View Altitudechart,Velocitychart;
	private List<Double> AltitudeList,VelocityList;
	private double[] altValue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		findView();	
		
		//地圖基本設定
		setMap();		
				
		//顯示時間星期
		String dts;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dt=new Date();
		dts=sdf.format(dt);
		Calendar calendar=Calendar.getInstance();
		int day=calendar.get(Calendar.DAY_OF_WEEK);
		String week=null;
		switch(day){
		case 1:
			week="星期日";
			break;
		case 2:
			week="星期一";
			break;
		case 3:
			week="星期二";
			break;
		case 4:
			week="星期三";
			break;
		case 5:
			week="星期四";
			break;
		case 6:
			week="星期五";
			break;
		case 7:
			week="星期六";
			break;
		default:
		}
		tvDate.setText(dts+"\n"+week);
		
		//滑動		
		detector = new GestureDetector(this);//起始化觸摸
		flipper = (ViewFlipper)findViewById(R.id.ViewFlipper03);//取得ViewFlipper
				                                                //將View增加到flipper陣列中  
		
		//取出Bundle的值		
		Bundle bundle=this.getIntent().getExtras();
		intCnt=bundle.getLong("time");
		double distance=bundle.getDouble("distance");		
		double max_velocity=bundle.getDouble("max_velocity");
		double height=bundle.getDouble("height");
		double max_altitude=bundle.getDouble("max_altitude");
		double min_altitude=bundle.getDouble("min_altitude");

	
		ArrayList arraylist=bundle.getParcelableArrayList("list");
		List<Location> LocationList=((List<Location>)arraylist.get(0));

//		VelocityList=(List)arraylist.get(1);
//		AltitudeList=(List<Double>)arraylist.get(2);
		
		//顯示運動時間		
		DecimalFormat nf1 = new DecimalFormat("00");		
	    Long hour=intCnt/3600;            
        Long minius = (intCnt % 3600)/60;
        Long seconds = intCnt % 60;              
        tvTime.setText(nf1.format(hour)+":"+nf1.format(minius)+":"+nf1.format(seconds));
        
        //顯示距離
        DecimalFormat nf2=new DecimalFormat("0.00");
	    nf2.setMaximumFractionDigits(2);	
        tvDistance.setText(nf2.format(distance/1000));
        
        //顯示高度
        DecimalFormat nf3=new DecimalFormat("0");
		tvHeight.setText(nf3.format(height));
		
		//顯示速度
		DecimalFormat nf4=new DecimalFormat();
		nf4.setMaximumFractionDigits(2);
		tvAverageVelocity.setText(nf4.format(Double.valueOf((distance/1000)/(Double.valueOf(intCnt)/3600))));
		
		//顯示最大速度		
		tvMaxVelocity.setText(nf4.format(max_velocity));
		
		//顯示最高海拔
		DecimalFormat nf5=new DecimalFormat();
		nf5.setMaximumFractionDigits(3);
		tvMaxAltitude.setText(nf5.format(max_altitude));
		
		//顯示最低海拔
		tvMinAltitude.setText(nf5.format(min_altitude));
				
	    //將list中所有儲存的經緯印出
//		String temp=null,c;
//		for(int i=0;i<LocationList.size();i++){
//			c="緯度:"+LocationList.get(i).getLatitude()+" "+"經度:"+LocationList.get(i).getLongitude()+"\n";	
//			temp=temp+c;
//		}
//		Log.e("LocList所儲存的Location",temp);     
		
		//將location的經緯度拆開成兩筆資料儲存到LocList		
		List<String> LocList = new ArrayList<String>();
		Log.e("原本LocationList大小", String.valueOf(LocationList.size()));			
		for(int i=0;i<LocationList.size();i++){
			double temp1,temp2;
			temp1=LocationList.get(i).getLatitude();
			LocList.add(String.valueOf(temp1));
			temp2=LocationList.get(i).getLongitude();
			LocList.add(String.valueOf(temp2));		
		}        
			
		Log.e("拆開後LocList大小", String.valueOf(LocList.size()));	
		
		//Serializable
		loc=new MyList();
//		vel=new MyList();
//		alt=new MyList();		
		
		loc.Stringlist= LocList;
//		vel.doublelist= VelocityList;
//	    alt.doublelist= AltitudeList;
	    
//	    Collections.sort(AltitudeList);	   
	    
//		tvMinAltitude.setText(String.valueOf(nf.format(Double.valueOf(String.valueOf(AltitudeList.get(0))))));
//		tvMaxAltitude.setText(String.valueOf(nf.format(Double.valueOf(String.valueOf(AltitudeList.get(AltitudeList.size()-1))))));
	  
		
	    add();	   
	    
//	    String t=null,r;
//		for(int i=0;i<AltitudeList.size();i++){
//			r = "Altitude=" + AltitudeList.get(i)+"\n";	
//			t=t+r;
//		}		
//		Log.e("LocList所儲存的Location",t);  
		
		drawPolyline(LocationList);
		
//		showAltitude();
//		showVelocity();
//		flipper.addView(Altitudechart);
//		flipper.addView(Velocitychart);
		
	}
	
	//地圖設定
	private void setMap(){
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setCompassEnabled(true);
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);			
	}
	
	//軌跡圖繪製
	private void drawPolyline(List<Location> locationlist){
	    PolylineOptions polylineOpt = new PolylineOptions();
		for(int a=0;a<locationlist.size();a++){
		polylineOpt.add(new LatLng(locationlist.get(a).getLatitude(), locationlist.get(a).getLongitude()));
			    } 
		polylineOpt.color(Color.BLUE);
		Polyline polyline = map.addPolyline(polylineOpt);
		polyline.setWidth(10);
		
		location =locationlist.get(0);
		double lat=0,lng=0;
		
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
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		closeDatabase();
	}
	
	private void openDatabase(){
//		dbhelper=new DBHelper(this);
	}
	
	private void closeDatabase(){
//		dbhelper.close();
	}	
	
	private void findView(){
		tvDate=(TextView)findViewById(R.id.idDate4);
		tvTime=(TextView)findViewById(R.id.idTime4);
		tvDistance=(TextView)findViewById(R.id.idDistance4);
		tvVelocity=(TextView)findViewById(R.id.idVelocity4);
		tvAverageVelocity=(TextView)findViewById(R.id.idAverageVelocity4);
		tvMaxVelocity=(TextView)findViewById(R.id.idMaxVelocity4);
		tvHeight=(TextView)findViewById(R.id.idHeight4);
		tvMinAltitude=(TextView)findViewById(R.id.idMinAltitude4);
        tvMaxAltitude=(TextView)findViewById(R.id.idMaxAltitude4);
          
	}	
	private void add(){
		DBHelper dbhelper=new DBHelper(this);
		SQLiteDatabase db=dbhelper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(DATE,tvDate.getText().toString());
		values.put(CALORIES,"0");
		values.put(TIME, tvTime.getText().toString());
		values.put(DISTANCE, tvDistance.getText().toString());
		values.put(AVERAGE_VELOCITY, tvAverageVelocity.getText().toString());
		values.put(MAX_VELOCITY, tvMaxVelocity.getText().toString());
		values.put(HEIGHT, tvHeight.getText().toString());
		values.put(MIN_ALTITUDE, tvMinAltitude.getText().toString());
	    values.put(MAX_ALTITUDE, tvMaxAltitude.getText().toString());	
	    
		values.put(INTCNT,String.valueOf(intCnt));		
		
		SerializerClass test1 = new SerializerClass();
		byte[] blaBytes1 = test1.serializeObject(loc); 
		values.put(LOCATION, blaBytes1);   		

//		SerializerClass test2 = new SerializerClass();
//		byte[] blaBytes2 = test2.serializeObject(vel); 
//		values.put(VELOCITY, blaBytes2);   
//		
//		SerializerClass test3 = new SerializerClass();
//		byte[] blaBytes3 = test3.serializeObject(alt); 
//		values.put(ALTITUDE, blaBytes3); 		
		
		//deserialize測試
//		MyList test = (MyList)test1.deserializeObject(blaBytes1);		
//		Log.e("test",test.Stringlist.get(0)+","+test.Stringlist.get(1));
		
		db.insert(TABLE_NAME,null,values);
		db.close();
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
			Result.this.finish();
		case 1:
		{

			Result.this.finish();
			Intent intent=new Intent();
			intent.setClass(Result.this,Record.class);		
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
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	
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
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > 120) {//如果是從右向左滑動   
            //註冊flipper的進出效果 
            this.flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in));
            this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.left_out));
            this.flipper.showNext(); 
            Log.e("flipper數目", ""+flipper.getChildCount());
            Log.e("flipper ID", ""+  flipper.indexOfChild(flipper.getCurrentView()));            
           ;
            return true;  
        } else if (e1.getX() - e2.getX() < -120) {//如果是從左向右滑動 
            this.flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in)); 
            this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.right_out));
            this.flipper.showPrevious();
            Log.e("flipper ID", ""+  flipper.indexOfChild(flipper.getCurrentView()));
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
	
//	private void showAltitude(){
//        String[] titles = new String[] { "海拔" }; // 定義折線的名稱
//	    List<double[]> x = new ArrayList<double[]>(); // 點的x坐標
//	    List<double[]> y = new ArrayList<double[]>(); // 點的y坐標
//	    // 數值X,Y坐標值輸入
//	        
//	    double[] AltitudetoArray  = ArrayUtils.toPrimitive(AltitudeList.toArray(new Double[AltitudeList.size()]));
//	        
//	    Log.e("AltitudetoArray",""+ AltitudetoArray.length);
//	       
//	    double[] Time=new double[AltitudetoArray.length] ;
//	        
//	    for(int i=0;i<AltitudetoArray.length;i++){
//	        Time[i]=i*2;
//	        }	                
//	        
//	    x.add(Time);	       
//	    y.add(AltitudetoArray); 
//
//	        
//	    XYMultipleSeriesDataset dataset = buildDatset(titles, x, y); // 儲存座標值
//
//	    int[] colors = new int[] { Color.BLUE};// 折線的顏色
//	    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE }; // 折線點的形狀
//	    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);
//
//	    setChartSettings(renderer, "海拔折線圖", "時間(s)", "海拔(m)", 0, 60, 0, 100, Color.BLACK);// 定義折線圖
//	    Altitudechart = ChartFactory.getLineChartView(this, dataset, renderer);
//	    int height=getWindowManager().getDefaultDisplay().getHeight();
//	    LayoutParams parms = new LayoutParams(LayoutParams.MATCH_PARENT, height-300);
//	    Altitudechart.setLayoutParams(parms);   
//
//    }
//	
//	private void showVelocity(){
//        String[] titles = new String[] { "速度" }; // 定義折線的名稱
//	    List<double[]> x = new ArrayList<double[]>(); // 點的x坐標
//	    List<double[]> y = new ArrayList<double[]>(); // 點的y坐標
//	    // 數值X,Y坐標值輸入
//	        
//	    double[] VelocitytoArray  = ArrayUtils.toPrimitive(VelocityList.toArray(new Double[VelocityList.size()]));
//	        
//	    Log.e("AltitudetoArray",""+ VelocitytoArray.length);
//	       
//	    double[] Time=new double[VelocitytoArray.length] ;
//	        
//	    for(int i=0;i<VelocitytoArray.length;i++){
//	        Time[i]=i*2;
//	        }	                
//	        
//	    x.add(Time);	       
//	    y.add(VelocitytoArray); 
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
//	
//	// 定義折線圖名稱
//    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
//            String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor) {
//        renderer.setChartTitle(title); // 折線圖名稱
//        renderer.setChartTitleTextSize(24); // 折線圖名稱字形大小
//        renderer.setXTitle(xTitle); // X軸名稱
//        renderer.setYTitle(yTitle); // Y軸名稱
//        renderer.setXAxisMin(xMin); // X軸顯示最小值
//        renderer.setXAxisMax(xMax); // X軸顯示最大值
//        renderer.setXLabelsColor(Color.BLACK); // X軸線顏色
//        renderer.setYAxisMin(yMin); // Y軸顯示最小值
//        renderer.setYAxisMax(yMax); // Y軸顯示最大值
//        renderer.setAxesColor(axesColor); // 設定坐標軸顏色
//        renderer.setYLabelsColor(0, Color.BLACK); // Y軸線顏色
//        renderer.setLabelsColor(Color.BLACK); // 設定標籤顏色
//        renderer.setMarginsColor(Color.WHITE); // 設定背景顏色
//        renderer.setShowGrid(true); // 設定格線
//        renderer.setInScroll(false);
//        renderer.setClickEnabled(false);
//        renderer.setExternalZoomEnabled(false);
//        renderer.setAxisTitleTextSize(26);
//        renderer.setChartTitleTextSize(32);
//        
//                
//    }
//
//    // 定義折線圖的格式
//    private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles, boolean fill) {
//        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
//        int length = colors.length;
//        for (int i = 0; i < length; i++) {
//            XYSeriesRenderer r = new XYSeriesRenderer();
//            r.setColor(colors[i]);
//            r.setPointStyle(styles[i]);
//            r.setFillPoints(fill);
//            r.setLineWidth(6);
//            r.setPointStrokeWidth(30);            
//            renderer.addSeriesRenderer(r); //將座標變成線加入圖中顯示
//        }
//        return renderer;
//    }
//    
// 
//
//    // 資料處理
//    private XYMultipleSeriesDataset buildDatset(String[] titles, List<double[]> xValues,
//            List<double[]> yValues) {
//        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
//
//        int length = titles.length; // 折線數量
//        for (int i = 0; i < length; i++) {
//            // XYseries對象,用於提供繪製的點集合的資料
//            XYSeries series = new XYSeries(titles[i]); // 依據每條線的名稱新增
//            double[] xV = xValues.get(i); // 獲取第i條線的資料
//            double[] yV = yValues.get(i);
//            int seriesLength = xV.length; // 有幾個點
//
//            for (int k = 0; k < seriesLength; k++) // 每條線裡有幾個點
//            {
//                series.add(xV[k], yV[k]);
//            }
//            dataset.addSeries(series);
//        }
//        return dataset;
//    }

}
