package lam.android.tapatapp_api4.controller.activities.listelementadapters;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.controller.activities.fragments.DateTimeDialog;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.MyTimeStamp;
import lam.android.tapatapp_api4.model.Tap;

/**
 * Esta clase es utilizada por Activity_ListOfTaps para cargar cada elemento de la lista.
 */
public class CustomListAdapter_Tap extends BaseAdapter //implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener
{

    private void showlog (Object o) { Log.i("--" + this.getClass().getSimpleName(), o.toString()); }

    // ---------------------------------------------------------------------------- Global Variables

    private List<Tap> list;
    private Context context;

    DateTimeDialog dtd;
    Model model;
    private MyTimeStamp myTimeStamp = MyTimeStamp.now();
    public boolean showDeleteOption = false;

    // -------------------------------------------------------------------------------- Constructors

    public CustomListAdapter_Tap(Context context, List<Tap> arrayList, DateTimeDialog dtd, Model model)
    {
        this.list = arrayList;
        this.context = context;
        this.dtd = dtd;
        this.model = model;
    }

    // ------------------------------------------------------------------------------------- Methods

    @Override public View getView (int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_element_tap, null);
        }
        /* Hacer referencias a los componentes del layout que es un unico elemento (un unico
        tap) para que muestren el tap que toca (que es list.get(position) ): */

        TextView tapTypeInit = convertView.findViewById(R.id.tapTypeInit);
        TextView tapTypeEnd = convertView.findViewById(R.id.tapTypeEnd);
        Button buttonChangeDateInit = convertView.findViewById(R.id.buttonChangeDateInit);
        Button buttonChangeDateEnd = convertView.findViewById(R.id.buttonChangeDateEnd);
        Button buttonChangeHourInit = convertView.findViewById(R.id.buttonChangeHourInit);
        Button buttonChangeHourEnd = convertView.findViewById(R.id.buttonChangeHourEnd);
        Button buttonDeleteTap = convertView.findViewById(R.id.button_deleteTap);

        // Hacer los setters

        final Tap tap = list.get(position);
        showlog("Tap selected: " + list.get(position));

        if (tap.getStatus_id() == 1)
        {
            tapTypeInit.setText("Despertó:");
            tapTypeEnd.setText("Se durmió:");
        }
        else
        {
            tapTypeInit.setText("Se puso el parche:");
            tapTypeInit.setTextSize(14);
            tapTypeEnd.setText("Se lo quitó:");
        }

        String str = tap.getInit_date().getDay() + "-"
                + tap.getInit_date().getMonth() + "-"
                + tap.getInit_date().getYear();
        buttonChangeDateInit.setText(str);

        if (tap.getEnd_date() != null)
        str = tap.getEnd_date().getDay() + "-"
                + tap.getEnd_date().getMonth() + "-"
                + tap.getEnd_date().getYear();
        else str = "---";
        buttonChangeDateEnd.setText(str);

        //str = tap.getInit_date().getHour() + ":"
        //        + tap.getInit_date().getMinute();
        str = tap.getInitHourAndMinute();
        buttonChangeHourInit.setText(str);

        if (tap.getEnd_date() != null)
        //str = tap.getEnd_date().getHour() + ":"
        //        + tap.getEnd_date().getMinute();
            str = tap.getEndHourAndMinute();
        else str = "---";
        buttonChangeHourEnd.setText(str);

        if (showDeleteOption) buttonDeleteTap.setVisibility(View.VISIBLE);
        else                  buttonDeleteTap.setVisibility(View.GONE);

        View.OnClickListener listener = new View.OnClickListener ()
        {
            @Override public void onClick (View view)
            {
                showlog("Pulsado un boton del tap con id " + tap.getId());
                switch(view.getId())
                {
                    case R.id.buttonChangeDateInit: change_date_init(tap); break;
                    case R.id.buttonChangeDateEnd:  change_date_end (tap); break;
                    case R.id.buttonChangeHourInit: change_hour_init(tap); break;
                    case R.id.buttonChangeHourEnd:  change_hour_end (tap); break;
                    case R.id.boton_layout_remove:  delete_tap      (tap); break;
                }
                //notifyDataSetChanged();
            }
        };

        convertView.setOnClickListener(listener);
        buttonChangeDateEnd.setOnClickListener(listener);
        buttonChangeDateInit.setOnClickListener(listener);
        buttonChangeHourEnd.setOnClickListener(listener);
        buttonChangeHourInit.setOnClickListener(listener);
        buttonDeleteTap.setOnClickListener(listener);

        return convertView;
    }

    // ------------------------------------------------------------------------------------- Options

    public void toggleDeleteOption ()
    {
        if (showDeleteOption) showDeleteOption = false;
        else showDeleteOption = true;
    }

    private void change_date_init (Tap tap)
    {
        showlog("change_date_init");
        //dtd.setCalendar(new MyTimeStamp(tap.getInit_date().getCalendar()).getCalendar());
        dtd.init_or_end = 1;
        dtd.selected_tap = tap;
        dtd.showDatePicker();
    }

    private void change_date_end (Tap tap)
    {
        showlog("change_date_end");
        //dtd.setCalendar(new MyTimeStamp(tap.getEnd_date().getCalendar()).getCalendar());
        dtd.init_or_end = 2;
        dtd.selected_tap = tap;
        dtd.showDatePicker();
    }

    private void change_hour_init (Tap tap)
    {
        showlog("change_hour_init");
        //dtd.setCalendar(new MyTimeStamp(tap.getInit_date().getCalendar()).getCalendar());
        dtd.init_or_end = 1;
        dtd.selected_tap = tap;
        dtd.showTimePicker();
    }

    private void change_hour_end (Tap tap)
    {
        showlog("change_hour_end");
        //dtd.setCalendar(new MyTimeStamp(tap.getEnd_date().getCalendar()).getCalendar());
        dtd.init_or_end = 2;
        dtd.selected_tap = tap;
        dtd.showTimePicker();
    }

    private void delete_tap (Tap ht)
    {
        final Tap tap = ht;

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Eliminar tap");
        alert.setMessage("¿Seguro que quieres eliminar este tap?");
        alert.setPositiveButton("Si", new DialogInterface.OnClickListener ()
        {
            @Override public void onClick (DialogInterface dialog, int which)
            {
                model.setMethod("delete_tap");
                model.deleteTap(tap.getId());
            }
        });
        alert.setNegativeButton("No", null);
        alert.create().show();
    }

    // Todos estos metodos de abajo venian asi por defecto. No se que hacen.

    @Override public boolean areAllItemsEnabled () { return false; }
    @Override public boolean isEnabled (int position) { return true; }
    @Override public void registerDataSetObserver (DataSetObserver observer) {}
    @Override public void unregisterDataSetObserver (DataSetObserver observer) {}
    @Override public int getCount () { return list.size(); }
    @Override public Object getItem (int position) { return position; }
    @Override public long getItemId (int position) { return position; }
    @Override public boolean hasStableIds () { return false; }
    @Override public int getItemViewType (int position) { return position; }
    @Override public int getViewTypeCount () { return list.size(); }
    @Override public boolean isEmpty () { return false; }
}
