package com.demo.android.mytrack;

import android.app.Application;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.utils.Logger;

public class SampleApplication extends Application
{
	private static final String APP_ID = "374289906035869";
	private static final String APP_NAMESPACE = "mytrackspace";
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
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
}
