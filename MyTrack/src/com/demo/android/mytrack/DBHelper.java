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
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME="mytrack.db";
	private final static int DATABASE_VERSION=1;
			

	 public DBHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		  final String INIT_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                  _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +				  
                  DATE + " CHAR," +
                  CALORIES + " CHAR,"+                
                  TIME+ " CHAR, " +
                  DISTANCE+" CHAR,"+
//                  VELOCITY+" BLOB,"+
                  AVERAGE_VELOCITY+" CHAR,"+
                  MAX_VELOCITY+" CHAR,"+
                  HEIGHT+" CHAR,"+
//                  ALTITUDE+" BLOB,"+
                  MIN_ALTITUDE+" CHAR,"+
                  MAX_ALTITUDE+" CHAR,"+
//                  MIN_SLOPE+" CHAR,"+
//                  MAX_SLOPE+" CHAR,"+
                  INTCNT+" CHAR,"+                  
                  LOCATION + " BLOB"+");"; 
                  db.execSQL(INIT_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
		
	}
	

}
