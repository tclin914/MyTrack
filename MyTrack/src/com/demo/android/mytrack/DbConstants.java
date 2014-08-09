package com.demo.android.mytrack;

import android.provider.BaseColumns;

public interface DbConstants extends BaseColumns {
	public static final String TABLE_NAME="mytrack";
	public static final String DATE="date";	
	public static final String TIME="time";
	public static final String DISTANCE="distance";
	public static final String VELOCITY="velocity";
	public static final String AVERAGE_VELOCITY="average_velocity";
	public static final String MAX_VELOCITY="max_velocity";
	public static final String HEIGHT="height";
	public static final String MIN_ALTITUDE="min_altitude";
	public static final String MAX_ALTITUDE="max_altitude";
	public static final String MIN_SLOPE="min_slope";
	public static final String MAX_SLOPE="max_slope";
	public static final String INTCNT="intCnt";
	public static final String CALORIES="calories";
	
	
	public static final String LOCATION="location";
	public static final String ALTITUDE="altitude";
	

}
