package com.op.kclock.model;

import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import android.widget.*;
import java.util.*;
import com.op.kclock.alarm.*;


public class AlarmClock implements Parcelable {

	private static final int MINUTE = 60;

	private static final int HOUR = 3600;

	private Vibrator vibrator;
	
	private static final long[] vibratePattern = {0, 200, 500};

	private String TAG ="op";
	
	
	public static enum TimerState {
		STOPPED, PAUSED, RUNNING, ALARMING
	};

	private TimerState state = TimerState.STOPPED;

	public TimerState getState() {
		return state;
	}

	public void setState(TimerState state) {
		this.state = state;
	}

	private int id;

	private String name;

	private Calendar stopTime;

	private Calendar currTime;

	private long seconds;

	private LinearLayout element;

	final static String LOG_TAG = "op";

	// обычный конструктор
	public AlarmClock(String _name, int _id) {
		name = _name;
		id = _id;
	}

	public AlarmClock() {
	}

	public void updateElement() {
		// TextView widget =(TextView)element.getChildAt(1);

		getWidget().post(new Runnable() {
			public void run() {
				// getWidget().setText(String.format("%02d:%02d:%tS", 60, 60,
				// Calendar.getInstance()));
				getWidget().setText(
						(CharSequence) (String.format("%02d:%02d:%02d",
								getHour(), getMin(), getSec())));
			}
		});
	}

	public void setElement(LinearLayout element) {
		this.element = element;
	}

	public LinearLayout getElement() {
		return element;
	}

	public void setSec(long sec) {
		this.seconds = sec;
	}

	public long getSec() {
		return seconds - (getHour() * HOUR) - (getMin() * MINUTE);
	}

	public void setMin(int min) {
		seconds += min * MINUTE;
	}

	public long getMin() {
		return (seconds - (getHour() * HOUR)) / MINUTE;
	}

	public void setHour(int hour) {
		seconds += hour * HOUR;
	}

	public long getHour() {
		return seconds / HOUR;
	}

	public void setCurrTime(Calendar currTime) {
		this.currTime = currTime;
	}

	public Calendar getCurrTime() {
		return currTime;
	}

	public void setStopTime(Calendar stopTime) {
		this.stopTime = stopTime;
	}

	public Calendar getStopTime() {
		return stopTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int describeContents() {
		return 0;
	}

	// упаковываем объект в Parcel
	public void writeToParcel(Parcel parcel, int flags) {
		Log.d(LOG_TAG, "writeToParcel");
		parcel.writeString(name);
		parcel.writeInt(id);
		parcel.writeLong(seconds);
	}

	public TextView getWidget() {
		final TextView widget = (TextView) element.getChildAt(1);
		return widget;
	}

	public static final Parcelable.Creator<AlarmClock> CREATOR = new Parcelable.Creator<AlarmClock>() {
		// распаковываем объект из Parcel
		public AlarmClock createFromParcel(Parcel in) {
			Log.d(LOG_TAG, "createFromParcel");
			return new AlarmClock(in);
		}

		public AlarmClock[] newArray(int size) {
			return new AlarmClock[size];
		}
	};

	private AlarmClock(Parcel parcel) {
		Log.d(LOG_TAG, "AlarmClock(Parcel parcel)");
		name = parcel.readString();
		id = parcel.readInt();
		seconds = parcel.readLong();
	}

	public boolean tick() {
		
		if (state == TimerState.RUNNING) seconds--;
		if (seconds > 0) {
			return true;
		} else {
			this.state = TimerState.ALARMING;
			return false;
		}
	}

	public void setTime(long i) {
		seconds = i;
	}

	public long getTime(){
		return seconds;
	}
	
	public void alarmNOW(Context context)
	{
		if (1==1) vibratePhone(context);
		//if (1==2) playMusic();
		WakeUpLock.acquire(context);
	}

	public void alarmSTOP()
	{
		setState(AlarmClock.TimerState.STOPPED);
		//music.release();          // Alarm has rung and we have closed the dialog. Music is released.
        if (1==1) vibrator.cancel();        // Also no need to vibrate anymore.
	   WakeUpLock.release();
	}
	
	

    /**
     * Vibrates the phone unless user is on call.
     */
    private void vibratePhone(Context context) {
        Log.v(TAG, "I want to Vibrate");

        // Check that the phone wont vibrate if the user is on the phone
        if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() ==
			TelephonyManager.CALL_STATE_IDLE){
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(vibratePattern, 0);
            Log.v(TAG, "Vibrator says:" + vibrator.toString());
        }
    }

}
