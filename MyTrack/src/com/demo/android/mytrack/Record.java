package com.demo.android.mytrack;

import static android.provider.BaseColumns._ID;
import static com.demo.android.mytrack.DbConstants.AVERAGE_VELOCITY;
import static com.demo.android.mytrack.DbConstants.CALORIES;
import static com.demo.android.mytrack.DbConstants.DATE;
import static com.demo.android.mytrack.DbConstants.DISTANCE;
import static com.demo.android.mytrack.DbConstants.HEIGHT;
import static com.demo.android.mytrack.DbConstants.INTCNT;
import static com.demo.android.mytrack.DbConstants.MAX_ALTITUDE;
import static com.demo.android.mytrack.DbConstants.MAX_VELOCITY;
import static com.demo.android.mytrack.DbConstants.MIN_ALTITUDE;
import static com.demo.android.mytrack.DbConstants.TABLE_NAME;
import static com.demo.android.mytrack.DbConstants.TIME;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class Record extends Activity{	
	private ListView listData = null;	
	StringBuilder resultData = new StringBuilder("RESULT: \n"); 
    private TextView  longtime_TV
                    , longtime_date_TV
                    , longdistance_TV
                    , longdistance_date_TV
                    , longaltitude_TV
                    , longaltitude_date_TV;
    private Button  All=null
    		      , MyBest=null;
    private ViewFlipper flipper;
    private boolean allormybest=false;
    private LinearLayout clicklongtime_TV, clicklongdistance_TV, clicklongaltitude_TV;
    private String[] columns = {_ID, DATE, CALORIES, TIME, DISTANCE, AVERAGE_VELOCITY,
            MAX_VELOCITY, HEIGHT, MIN_ALTITUDE, MAX_ALTITUDE, INTCNT};	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		findView();		
		setListeners();	
		showInList();	
		setClick();
	}
	
	private void setClick(){
		clicklongtime_TV.setClickable(true);
		clicklongtime_TV.setFocusable(true);
		clicklongdistance_TV.setClickable(true);
		clicklongdistance_TV.setFocusable(true);
		clicklongaltitude_TV.setClickable(true);
		clicklongaltitude_TV.setFocusable(true);				
	}
	
	private OnClickListener All_OnClick=new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub			
			if(allormybest){
				flipper.showNext();
			}
			else{
				
			}
			allormybest=false;
			showInList();
		}		
	};
	
	private OnClickListener MyBest_OnClick=new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showMyBest();
			if(allormybest){			
			
			}
			else{
				flipper.showNext();
			}
			allormybest=true;
		}		
	};
	
	private OnClickListener longdistance=new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
		    DBHelper dbhelper= new DBHelper(Record.this);
			SQLiteDatabase db = dbhelper.getReadableDatabase();
			Cursor cursor = db.rawQuery("select _id from mytrack where date=?"
					                     , new String[]{String.valueOf(longdistance_date_TV.getText())});
			Cursor c=db.query(TABLE_NAME, columns, null, null, null, null, null);
			cursor.moveToFirst();
			c.moveToPosition(Integer.valueOf(cursor.getString(0))-1);
		        
			Intent intent=new Intent();
			intent.setClass(Record.this,Report.class);				
			Bundle bundle=new Bundle();
			bundle.putString("id", c.getString(0));
            intent.putExtras(bundle);			
			startActivity(intent);							
		}    
	};
	
	private OnClickListener longtime=new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
		    DBHelper dbhelper= new DBHelper(Record.this);
			SQLiteDatabase db = dbhelper.getReadableDatabase();
			Cursor cursor = db.rawQuery("select _id from mytrack where date=?"
					                     , new String[]{String.valueOf(longtime_date_TV.getText())});
			Cursor c=db.query(TABLE_NAME, columns, null, null, null, null, null);
			cursor.moveToFirst();
			c.moveToPosition(Integer.valueOf(cursor.getString(0))-1);
		        
			Intent intent=new Intent();
			intent.setClass(Record.this,Report.class);				
			Bundle bundle=new Bundle();
			bundle.putString("id", c.getString(0));
            intent.putExtras(bundle);			
			startActivity(intent);	
			Record.this.finish();			
		}    
	};
	
	private OnClickListener longaltitude=new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			DBHelper dbhelper= new DBHelper(Record.this);
			SQLiteDatabase db = dbhelper.getReadableDatabase();
			Cursor cursor = db.rawQuery("select _id from mytrack where date=?"
					                     , new String[]{String.valueOf(longaltitude_date_TV.getText())});
			Cursor c=db.query(TABLE_NAME, columns, null, null, null, null, null);
			cursor.moveToFirst();
			c.moveToPosition(Integer.valueOf(cursor.getString(0))-1);
			
			Intent intent=new Intent();
			intent.setClass(Record.this,Report.class);				
			Bundle bundle=new Bundle();
			bundle.putString("id", c.getString(0));
            intent.putExtras(bundle);			
			startActivity(intent);	
			Record.this.finish();			
		}    
	};
	
	private void showInList(){		

		DBHelper dbhelper= new DBHelper(Record.this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
        final Cursor c;
        c=db.query(TABLE_NAME, columns, null, null, null, null, null);

        String[] from = { DATE, TIME, DISTANCE};
        final int[] to = {R.id.txtDate, R.id.txtTime, R.id.txtDistance };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.data_item, c, from, to , 1);
        listData.setAdapter(adapter);
        listData.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				c.moveToPosition(arg2);
					
				Intent intent=new Intent();
				intent.setClass(Record.this,Report.class);				
				Bundle bundle=new Bundle();
				bundle.putString("id", c.getString(0));

                intent.putExtras(bundle);			
				startActivity(intent);	
				Record.this.finish();
			}        	
        });       
    }	
	
	private void showMyBest(){		
		DBHelper dbhelper= new DBHelper(Record.this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
        final Cursor c;
        c=db.query(TABLE_NAME, columns, null, null, null, null, null);
        
        ArrayList<Integer> IntCntArrayList= new ArrayList<Integer>();
       
        double[] DistanceArray=new double[c.getCount()];
        double[] MaxAltitudeArray=new double[c.getCount()];
        
        int i=0;
        while(c.moveToNext()){
        	IntCntArrayList.add(Integer.valueOf(c.getString(10)));
        	DistanceArray[i]=Double.parseDouble(c.getString(4));
        	MaxAltitudeArray[i]=Double.parseDouble(c.getString(9));
        	i++;
        }
        QuickSort s=new QuickSort();
        s.sort(DistanceArray);
        
        QuickSort t=new QuickSort();
        t.sort(MaxAltitudeArray);
        Toast.makeText(this, ""+MaxAltitudeArray[c.getCount()-1], Toast.LENGTH_SHORT).show();          
        
        Collections.sort(IntCntArrayList); 

        Cursor cursor = db.rawQuery("select _id from mytrack where intCnt=?",
        		                    new String[]{String.valueOf(IntCntArrayList.get(IntCntArrayList.size()-1))});	                      
	    cursor.moveToFirst();        
        c.moveToPosition(Integer.valueOf(cursor.getString(0))-1);
        longtime_TV.setText(c.getString(3));
        longtime_date_TV.setText(c.getString(1));
        
        cursor = db.rawQuery("select _id from mytrack where distance=?",
                new String[]{String.valueOf(DistanceArray[c.getCount()-1])});	                      
        cursor.moveToFirst();        
        c.moveToPosition(Integer.valueOf(cursor.getString(0))-1);
        longdistance_TV.setText(c.getString(4));
        longdistance_date_TV.setText(c.getString(1));
        
        cursor = db.rawQuery("select _id from mytrack where max_altitude=?",
                new String[]{String.valueOf(MaxAltitudeArray[c.getCount()-1])});	                      
        cursor.moveToFirst();        
        c.moveToPosition(Integer.valueOf(cursor.getString(0))-1);
        longaltitude_TV.setText(c.getString(9));
        longaltitude_date_TV.setText(c.getString(1));     
        
        c.close();
        cursor.close();
		db.close();	      		
	}		

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
			Record.this.finish();
		case 1:
		
		case 2:
		case 3:
		default:
		}		
		return super.onOptionsItemSelected(item); 
	}
	
	private void findView(){
		All=(Button)findViewById(R.id.AllBtn);
		MyBest=(Button)findViewById(R.id.MyBestBtn);
		longtime_TV=(TextView)findViewById(R.id.longtime);
		longtime_date_TV=(TextView)findViewById(R.id.longtime_date);
		clicklongtime_TV=(LinearLayout)findViewById(R.id.clicklongtime);
		clicklongdistance_TV=(LinearLayout)findViewById(R.id.clicklongdistance);
		clicklongaltitude_TV=(LinearLayout)findViewById(R.id.clicklongaltitude);
		listData = (ListView) findViewById(R.id.listData);
		flipper=(ViewFlipper)findViewById(R.id.ViewFlipper02);
		longdistance_TV=(TextView)findViewById(R.id.longdistance);
        longdistance_date_TV=(TextView)findViewById(R.id.longdistance_date);
        longaltitude_TV=(TextView)findViewById(R.id.longaltitude);
        longaltitude_date_TV=(TextView)findViewById(R.id.longaltitude_date);       
	}
	
	private void setListeners(){
		All.setOnClickListener(All_OnClick);
		MyBest.setOnClickListener(MyBest_OnClick);
		clicklongtime_TV.setOnClickListener(longtime);
		clicklongdistance_TV.setOnClickListener(longdistance);
		clicklongaltitude_TV.setOnClickListener(longaltitude);		
	}
}
