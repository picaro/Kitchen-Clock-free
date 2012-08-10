package com.op.kclock.model;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import android.widget.*;
import com.op.kclock.*;
import com.op.kclock.alarm.*;
import java.util.*;
import android.view.animation.*;
import android.animation.*;


public class AlarmClock implements Parcelable
{

	private static final int MINUTE = 60;

	private static final int HOUR = 3600;

	private Vibrator vibrator;

	private static final long[] vibratePattern = {0, 200, 500};

	private String TAG ="op";

	private CharSequence timerName;


	public static enum TimerState
	{
		STOPPED, PAUSED, RUNNING, ALARMING
		};

	private TimerState state = TimerState.STOPPED;

	public TimerState getState()
	{
		return state;
	}

	public void setState(TimerState state)
	{
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
	public AlarmClock(String _name, int _id)
	{
		name = _name;
		id = _id;
	}

	public AlarmClock()
	{
	}

	public void updateElement()
	{
		// TextView widget =(TextView)element.getChildAt(1);

		if (getWidget() != null && this.getState() != TimerState.STOPPED) getWidget().post(new Runnable() {
					public void run()
					{
						getWidget().setText(
							(CharSequence) (String.format("%02d:%02d:%02d",
														  getHour(), getMin(), getSec())));
					}
				});
	}

	public void setElement(LinearLayout element)
	{
		this.element = element;
	}

	public LinearLayout getElement()
	{
		return element;
	}

	public void setSec(long sec)
	{
		this.seconds = sec;
	}

	public long getSec()
	{
		return seconds - (getHour() * HOUR) - (getMin() * MINUTE);
	}

	public void setMin(int min)
	{
		seconds += min * MINUTE;
	}

	public long getMin()
	{
		return (seconds - (getHour() * HOUR)) / MINUTE;
	}

	public void setHour(int hour)
	{
		seconds += hour * HOUR;
	}

	public long getHour()
	{
		return seconds / HOUR;
	}

	public void setCurrTime(Calendar currTime)
	{
		this.currTime = currTime;
	}

	public Calendar getCurrTime()
	{
		return currTime;
	}

	public void setStopTime(Calendar stopTime)
	{
		this.stopTime = stopTime;
	}

	public Calendar getStopTime()
	{
		return stopTime;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public int describeContents()
	{
		return 0;
	}

	// упаковываем объект в Parcel
	public void writeToParcel(Parcel parcel, int flags)
	{
		Log.d(LOG_TAG, "writeToParcel");
		parcel.writeString(name);
		parcel.writeInt(id);
		parcel.writeLong(seconds);
	}

	public TextView getWidget()
	{
		if (element == null) return null;
		final TextView widget = (TextView) element.getChildAt(1);
		return widget;
	}

	public static final Parcelable.Creator<AlarmClock> CREATOR = new Parcelable.Creator<AlarmClock>() {
		// распаковываем объект из Parcel
		public AlarmClock createFromParcel(Parcel in)
		{
			Log.d(LOG_TAG, "createFromParcel");
			return new AlarmClock(in);
		}

		public AlarmClock[] newArray(int size)
		{
			return new AlarmClock[size];
		}
	};

	private AlarmClock(Parcel parcel)
	{
		Log.d(LOG_TAG, "AlarmClock(Parcel parcel)");
		name = parcel.readString();
		id = parcel.readInt();
		seconds = parcel.readLong();
	}

	public boolean tick()
	{

		if (state == TimerState.RUNNING) seconds--;
		if (seconds > 0)
		{
			return true;
		}
		else
		{
			this.state = TimerState.ALARMING;
			return false;
		}
	}

	public void setTime(long i)
	{
		seconds = i;
	}

	public long getTime()
	{
		return seconds;
	}

	public void alarmNOW(final Context context)
	{
		if (1 == 1) vibratePhone(context);
		//if (1==2) playMusic();
		final Animation anim = 
			AnimationUtils.loadAnimation(context, R.anim.fade_bwb);	
			anim.setRepeatMode(Animation.RESTART);
			anim.setRepeatCount(1000);
			anim.setDuration(500);
//		element.startAnimation(anim);
			
		final LinearLayout element = getElement();	
		if (getWidget() != null ) getWidget().post(new Runnable() {
					public void run()
					{
						anim.setAnimationListener(new AnimationListener() {
					                @Override
					                public void onAnimationEnd(Animation arg0) {
					                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_bwb);
					                    anim.setAnimationListener(context);
					                    element.startAnimation(anim);
					                }
					
					                @Override
					                public void onAnimationRepeat(Animation arg0) {                }
					
					                @Override
					                public void onAnimationStart(Animation arg0) {}
					            });
						element.startAnimation(anim);							
						
						
					}
				});		
			
		Log.d("op","a" +element);
		WakeUpLock.acquire(context);
	}

	public void alarmSTOP()
	{
		if (this.state == TimerState.ALARMING)
		{
			//music.release();          // Alarm has rung and we have closed the dialog. Music is released.
			if (1 == 1) vibrator.cancel();        // Also no need to vibrate anymore.
			WakeUpLock.release();
		}
		setState(AlarmClock.TimerState.STOPPED);
		
	}
	


    /**
     * Vibrates the phone unless user is on call.
     */
    private void vibratePhone(Context context)
	{
        Log.v(TAG, "I want to Vibrate");

        // Check that the phone wont vibrate if the user is on the phone
        if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() ==
			TelephonyManager.CALL_STATE_IDLE)
		{
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(vibratePattern, 0);
            Log.v(TAG, "Vibrator says:" + vibrator.toString());
        }
    }



	/**
	 * Parse the user provided custom vibrate pattern into a long[] Borrowed
	 * from SMSPopup
	 */
	public static long[] parseVibratePattern(String stringPattern)
	{
		ArrayList<Long> arrayListPattern = new ArrayList<Long>();
		Long l;
		String[] splitPattern = stringPattern.split(",");
		int VIBRATE_PATTERN_MAX_SECONDS = 60000;
		int VIBRATE_PATTERN_MAX_PATTERN = 100;

		for (int i = 0; i < splitPattern.length; i++)
		{
			try
			{
				l = Long.parseLong(splitPattern[i].trim());
			}
			catch (NumberFormatException e)
			{
				return null;
			}
			if (l > VIBRATE_PATTERN_MAX_SECONDS)
			{
				return null;
			}
			arrayListPattern.add(l);
		}

		int size = arrayListPattern.size();
		if (size > 0 && size < VIBRATE_PATTERN_MAX_PATTERN)
		{
			long[] pattern = new long[size];
			for (int i = 0; i < pattern.length; i++)
			{
				pattern[i] = arrayListPattern.get(i);
			}
			return pattern;
		}

		return null;
	}	

}
