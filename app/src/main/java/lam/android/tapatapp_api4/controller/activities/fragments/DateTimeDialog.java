package lam.android.tapatapp_api4.controller.activities.fragments;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.MyTimeStamp;
import lam.android.tapatapp_api4.model.Tap;

public class DateTimeDialog
{
    private void showlog (Object o) { Log.i("--" + this.getClass().getSimpleName(), o.toString()); }

    private Activity activity;
    private DatePickerFragment datePickerFragment;
    private TimePickerFragment timePickerFragment;
    private FragmentManager fragmentManager;
    private Calendar calendar;

    private Model model;

    public Calendar getCalendar () { return calendar; }

    public void setCalendar (Calendar calendar) { this.calendar = calendar; }

    public DateTimeDialog(Activity activity, FragmentManager fragmentManager, Model model)
    {
        this.activity = activity;
        this.fragmentManager = fragmentManager;

        this.calendar = Calendar.getInstance();
        this.datePickerFragment = new DatePickerFragment();
        this.timePickerFragment = new TimePickerFragment();

        this.datePickerFragment.setOnDateSelectedListener(getOnDateSelectedListener());
        this.timePickerFragment.setOnTimeSelectedListener(getOnTimeSelectedListener());

        this.model = model;
    }

    public void showDatePicker ()
    {
        showlog("showDatePicker 1");
        if (!datePickerFragment.isAdded())
        {
            showlog("showDatePicker 2");
            datePickerFragment.show(fragmentManager, "datePickerFragment");
            showlog("showDatePicker 3");
        }
    }

    public void showTimePicker ()
    {
        showlog("showTimePicker 1");
        if (!timePickerFragment.isAdded())
        {
            showlog("showTimePicker 2");
            timePickerFragment.show(fragmentManager, "timePickerFragment");
            showlog("showTimePicker 3");
        }
    }

    public Tap selected_tap;
    public int init_or_end; // 1=init, 2=end

    private TimePickerFragment.OnTimeSelectedListener getOnTimeSelectedListener ()
    {
        showlog("getOnTimeSelectedListener 1");
        if (!timePickerFragment.isAdded())
        {
            showlog("getOnTimeSelectedListener 2");
            return new TimePickerFragment.OnTimeSelectedListener() {

                @Override
                public void onTimeSelected(int hourOfDay, int minute) {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    // Here you have your finished result.
                    Toast.makeText(activity, calendar.getTime().toString(), Toast.LENGTH_LONG).show();

                    if (init_or_end==1) selected_tap.setInit_date(new MyTimeStamp(calendar));
                    else                selected_tap.setEnd_date(new MyTimeStamp(calendar));
                    model.setMethod("modifyTap");
                    model.modifyTap(selected_tap);
                }
            };
        }
        else return null;
    }

    private DatePickerFragment.OnDateSelectedListener getOnDateSelectedListener ()
    {
        showlog("getOnDateSelectedListener 1");
        if (!datePickerFragment.isAdded())
        {
            showlog("getOnDateSelectedListener 2");
            return new DatePickerFragment.OnDateSelectedListener() {

                @Override
                public void onDateSelected(int year, int monthOfYear, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Here you have your finished result.
                    Toast.makeText(activity, calendar.getTime().toString(), Toast.LENGTH_LONG).show();

                    if (init_or_end==1) selected_tap.setInit_date(new MyTimeStamp(calendar));
                    else                selected_tap.setEnd_date(new MyTimeStamp(calendar));
                    model.setMethod("modifyTap");
                    model.modifyTap(selected_tap);



                    //timePickerFragment.show(fragmentManager, "timePickerFragment");
                }
            };
        }
        else return null;
    }
}