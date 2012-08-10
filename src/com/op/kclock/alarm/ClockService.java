package com.op.kclock.alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ClockService extends Service
{
  
  final String LOG_TAG = "myLogs";
  
    public void onCreate() {
    super.onCreate();
    Log.d(LOG_TAG, "onCreate");
  }
  
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(LOG_TAG, "onStartCommand");
    someTask();
    return super.onStartCommand(intent, flags, startId);
  }

  public void onDestroy() {
    super.onDestroy();
    Log.d(LOG_TAG, "onDestroy");
  }

  public IBinder onBind(Intent intent) {
    Log.d(LOG_TAG, "onBind");
    return null;
  }
  
  void someTask() {
    
    do{  	
			try
			{
        Log.d(LOG_TAG, "ho-ho");
	 			Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}

	} while(true);	
    
    
  }
  
}