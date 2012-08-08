package com.op.kclock.dialogs;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.View.*;
import com.op.kclock.*;
import kankan.wheel.widget.*;
import kankan.wheel.widget.adapters.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.text.format.*;
import com.op.kclock.model.*;

public class TimePickDialog extends Dialog
{

	AlarmClock alarm = null;
	OnMyDialogResult mDialogResult; // the callback

	private WheelView hours;
	private WheelView mins;
	private WheelView secs;
	
	
	/**
	 * Constructor with setup context. 
	 * TODO - add alarm
	 */
	public TimePickDialog(Context context)
	{
		super(context);
	}	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.picktime);

	    hours = (WheelView) findViewById(R.id.hour);
		hours.setViewAdapter(new NumericWheelAdapter(this.getContext(), 0, 23));

	    mins = (WheelView) findViewById(R.id.mins);
		mins.setViewAdapter(new NumericWheelAdapter(this.getContext(), 0, 59,
													"%02d"));
		mins.setCyclic(true);

	    secs = (WheelView) findViewById(R.id.secs);
		secs.setViewAdapter(new NumericWheelAdapter(this.getContext(), 0, 59,
													"%02d"));
		secs.setCyclic(true);

		if (alarm != null && alarm.getTime() > 0)
		{
			hours.setCurrentItem((int) alarm.getHour());
			mins.setCurrentItem((int) alarm.getMin());
			secs.setCurrentItem((int) alarm.getSec());
		}

		hours.setCurrentItem(16, true);


		Button buttonCancel = (Button) findViewById(R.id.cancelsettimer);
		buttonCancel.setOnClickListener(cancelHandler);
		Button settimerbtn = (Button) findViewById(R.id.settimerbtn);
		settimerbtn.setOnClickListener(saveHandler);
	}

	View.OnClickListener saveHandler = new View.OnClickListener() {
		public void onClick(View v)
		{
			if (!validateTime()){
				Toast.makeText(getContext(),
                      getContext().getText(R.string.toast_time_invalid),
                      Toast.LENGTH_SHORT).show(); 
				return;
			}
			
			if (alarm == null)
			{
				alarm = new AlarmClock();
			}

		//	final TextView buttonCancel = (TextView) findViewById(R.id.hourslbl);

			long seconds = hours.getCurrentItem() * 3600 + 
				mins.getCurrentItem() * 60 +
				secs.getCurrentItem(); 
			alarm.setTime(seconds);	
			mDialogResult.finish(alarm);
			dismiss();
		}

		private boolean validateTime()
		{
			return !(hours.getCurrentItem() == 0 &&
				mins.getCurrentItem() == 0 &&
				secs.getCurrentItem() == 0)	;
		}
	};

	public static String formatTime(final Calendar c)
	{
		String M24 = "HH:mm:ss";
		final String format = M24;
		return (String) DateFormat.format(format, c);
	}

	View.OnClickListener cancelHandler = new View.OnClickListener() {
		public void onClick(View v)
		{
			dismiss();
		}
	};

	public void setDialogResult(Object dialogResult)
	{
		mDialogResult = (OnMyDialogResult) dialogResult;
	}

	public interface OnMyDialogResult
	{
		void finish(AlarmClock result);
	}

	public void setAlarm(AlarmClock _alarm)
	{

		alarm = _alarm;		
	}
}
