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

	public TimePickDialog(Context context)
	{
		super(context);
		this.setTitle("Set alarm time");
	}


//	@Override	
//	public void show()
//	{
//		super.show();
//
//		final WheelView hours = (WheelView) findViewById(R.id.hour);
//		hours.scroll(5, 1000);
//	}	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.picktime);

		final WheelView hours = (WheelView) findViewById(R.id.hour);
		hours.setViewAdapter(new NumericWheelAdapter(this.getContext(), 0, 23));

		final WheelView mins = (WheelView) findViewById(R.id.mins);
		mins.setViewAdapter(new NumericWheelAdapter(this.getContext(), 0, 59,
													"%02d"));
		mins.setCyclic(true);

		final WheelView secs = (WheelView) findViewById(R.id.secs);
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
			if (alarm == null)
			{
				alarm = new AlarmClock();
			}

			final TextView buttonCancel = (TextView) findViewById(R.id.hourslbl);
			final WheelView hours = (WheelView) findViewById(R.id.hour);
			final WheelView mins = (WheelView) findViewById(R.id.mins);
			final WheelView secs = (WheelView) findViewById(R.id.secs);

//			final Calendar cal = Calendar.getInstance();
//			cal.set(Calendar.HOUR_OF_DAY, hours.getCurrentItem());
//			cal.set(Calendar.MINUTE, mins.getCurrentItem());
//			cal.set(Calendar.SECOND, secs.getCurrentItem());

			long seconds = hours.getCurrentItem() * 3600 + 
				mins.getCurrentItem() * 60 +
				secs.getCurrentItem(); 
			alarm.setTime(seconds);	
			mDialogResult.finish(alarm);
			dismiss();
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
			// it was the 2nd/ button
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
