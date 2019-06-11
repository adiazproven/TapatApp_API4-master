package lam.android.tapatapp_api4.controller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import java.util.ArrayList;
import java.util.List;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.controller.activities.fragments.DateTimeDialog;
import lam.android.tapatapp_api4.controller.activities.listelementadapters.CustomListAdapter_Tap;
import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.MyTimeStamp;
import lam.android.tapatapp_api4.model.Tap;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;
import lam.android.tapatapp_api4.model.connection.NegativeResult;

/**
 * In this Activity, the user can select one of the buttons of each Tap to change its dates and times.
 * It shows a list with the options of the Taps of one Child profile.
 * It shows only the Taps of a certain type (awake or eyepatch) and only the ones which date of start is today or yesterday.
 * For each Tap there are 4 buttons shown: Date of start, Date of end, Time of start, Time of end.
 * (Date refers to year-month-day, and Time to hour-minute-second)
 * Then, the user can tap on one of the four buttons of one Tap, which opens a window to change it.
 * Also, there is a menu bar at the top with the options:
 * - Create new Tap: Opens a window with the same four options, but for a new Tap. And the buttons Accept and Cancel.
 * Once the dates and times are right, the user can tap on the button Accept to store the new Tap on the database.
 * - Go to user configuration: Goes to the activity UserConfig.
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 */
public class Activity_ListOfTaps extends AppCompatActivity implements Result {
    CustomListAdapter_Tap customAdapter;
    private List<Tap> tapsByStatus;

    // ----------------------------------------------------------------------------------- Variables
    private Model model;
    private DateTimeDialog dateTimeDialog;

    /**
     * Show error message inside the log. For testing.
     *
     * @param o : string or object to show
     */
    private void showlog(Object o) {
        String this_class_name = this.getClass().getSimpleName();
        this_class_name = this_class_name.replace("Activity_", "");
        Log.i("--" + this_class_name, o.toString());
    }

    /**
     * Show message on the screen (Toast) to quickly communicate something to the user.
     *
     * @param s : message to show
     */
    private void showtoast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------------------ Initialization

    /**
     * First, if it needs to show data, it loads it from the server database. Then, it initializes
     * the view (layout and components) and the listeners (and sets them).
     *
     * @param savedInstanceState : bundle passed through the previous activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("-----------------------", "-----------------------");
        showlog("START ACTIVITY");

        model = Model.getInstance(getApplicationContext());
        model.setResult(this);

        tapsByStatus = new ArrayList<>();
        init_or_refresh_attributes();
    }

    String today = MyTimeStamp.now().format2();
    String yesterday = MyTimeStamp.now().minusDays(1).format2();
    int type;
    private void init_or_refresh_attributes() {
        // init arrayList, depending on the type (awake/eyepatch) passed through bundle
        String type_name = String.valueOf(getIntent().getExtras().get("type"));

        if (type_name.equals("awake")) {
            type = 1;
            model.setMethod("getTapsByChildAndStatus");
            model.getTapsByChildAndStatus(model.getTemp_child().getId(), 1, today, yesterday);
        } else //if (type.equals("eyepatch"))
        {
            type = 2;
            model.setMethod("getTapsByChildAndStatus");
            model.getTapsByChildAndStatus(model.getTemp_child().getId(), 2, today, yesterday);
        }
    }

    /**
     * asigna una layout a esta activity, inicializa los componentes que vayan a ser interactivos,
     * y le da funcionalidad a los botones anyadiendoles listeners
     */
    private void init_view_and_listeners() {
        setContentView(R.layout.activity_list);

        ListView lista_taps = findViewById(R.id.list);

        FragmentManager fm = getSupportFragmentManager();
        dateTimeDialog = new DateTimeDialog(this, fm, model);
        customAdapter = new CustomListAdapter_Tap(this, tapsByStatus, dateTimeDialog, model);

        lista_taps.setAdapter(customAdapter);

        Button boton_reload = findViewById(R.id.button_reload);

        OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_reload:
                        model.setMethod("getTapsByChildAndStatus");
                        model.getTapsByChildAndStatus(model.getTemp_child().getId(), type, today, yesterday);
                        break;
                }
            }
        };

        boton_reload.setOnClickListener(listener);
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /**
     * infla la barra de menu de arriba
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    /**
     * le da funcionalidad a los botones de la barra de menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_new:
                createTap();
                break; // TODO probar crear Tap (NO ESTA PROBADO)

            case R.id.menuItem_userConfig:
                goToActivity(Activity_UserConfig.class);
                break;

            case R.id.menuItem_logout:
                model.setMethod("getLogout");
                model.getLogout();
                break;

            case R.id.menuItem_delete:
                customAdapter.toggleDeleteOption();
                init_or_refresh_attributes();
                break;

            case R.id.menuItem_goBack:
                goToActivity(Activity_Main.class);
                break;
        }
        return true;
    }

    // -------------------------------------------------------------------------------- Create Tap

    private void createTap() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Crear nuevo tap");

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View dialogVieww = layoutInflater.inflate(R.layout.list_element_tap, null);
        /* Hacer referencias a los componentes del layout que es un unico elemento (un unico
        tap) para que muestren el tap que toca (que es list.get(position) ): */
        alertDialogBuilder.setView(dialogVieww);

        TextView tapTypeInit = dialogVieww.findViewById(R.id.tapTypeInit);
        TextView tapTypeEnd = dialogVieww.findViewById(R.id.tapTypeEnd);
        Button buttonChangeDateInit = dialogVieww.findViewById(R.id.buttonChangeDateInit);
        Button buttonChangeDateEnd = dialogVieww.findViewById(R.id.buttonChangeDateEnd);
        Button buttonChangeHourInit = dialogVieww.findViewById(R.id.buttonChangeHourInit);
        Button buttonChangeHourEnd = dialogVieww.findViewById(R.id.buttonChangeHourEnd);

        // Hacer los setters

        int tapType = 0;
        String type = String.valueOf(getIntent().getExtras().get("type"));
        if (type.equals("awake")) tapType = Tap.AWAKE;
        else if (type.equals("eyepatch")) tapType = Tap.EYEPATCH;
        final Tap tap = new Tap(-1, tapType, MyTimeStamp.now(), MyTimeStamp.now());

        if (tap.getStatus_id() == 1) {
            tapTypeInit.setText("Despertó:");
            tapTypeEnd.setText("Se durmió:");
        } else {
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

        str = tap.getInit_date().getHour() + ":"
                + tap.getInit_date().getMinute();
        buttonChangeHourInit.setText(str);

        if (tap.getEnd_date() != null)
            str = tap.getEnd_date().getHour() + ":"
                    + tap.getEnd_date().getMinute();
        else str = "---";
        buttonChangeHourEnd.setText(str);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showlog("Pulsado un boton del tap con id " + tap.getId());
                switch (view.getId()) {
                    case R.id.buttonChangeDateInit: set_date_init(tap); break;
                    case R.id.buttonChangeDateEnd:  set_date_end (tap); break;
                    case R.id.buttonChangeHourInit: set_hour_init(tap); break;
                    case R.id.buttonChangeHourEnd:  set_hour_end (tap); break;
                }
            }
        };

        dialogVieww.setOnClickListener(listener);
        buttonChangeDateEnd.setOnClickListener(listener);
        buttonChangeDateInit.setOnClickListener(listener);
        buttonChangeHourEnd.setOnClickListener(listener);
        buttonChangeHourInit.setOnClickListener(listener);

        alertDialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // esto debe estar aqui pero vacio. si quieres saber porque, mira esto:
                // https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
            }
        });
        alertDialogBuilder.setNegativeButton("Cancelar", null);

        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wantToCloseDialog = true;//false;

                // TODO comprobar que estan bien las horas  y fechas (como compruebas eso? en que te basas? que esta bien y que esta mal?)

                model.setMethod("createTap");
                model.addTap(tap);

                if (wantToCloseDialog) dialog.dismiss();
            }
        });
    }

    private void set_date_init(Tap tap) {
        showlog("set_date_init");
        dateTimeDialog.setCalendar(new MyTimeStamp(tap.getInit_date().getCalendar()).getCalendar());
        dateTimeDialog.showDatePicker();
        tap.setInit_date(new MyTimeStamp(dateTimeDialog.getCalendar()));

    }

    private void set_date_end(Tap tap) {
        showlog("set_date_end");
        dateTimeDialog.setCalendar(new MyTimeStamp(tap.getEnd_date().getCalendar()).getCalendar());
        dateTimeDialog.showDatePicker();
        tap.setInit_date(new MyTimeStamp(dateTimeDialog.getCalendar()));
    }

    private void set_hour_init(Tap tap) {
        showlog("set_hour_init");
        dateTimeDialog.setCalendar(new MyTimeStamp(tap.getInit_date().getCalendar()).getCalendar());
        dateTimeDialog.showTimePicker();
        tap.setInit_date(new MyTimeStamp(dateTimeDialog.getCalendar()));
    }

    private void set_hour_end(Tap tap) {
        showlog("set_hour_end");
        dateTimeDialog.setCalendar(new MyTimeStamp(tap.getEnd_date().getCalendar()).getCalendar());
        dateTimeDialog.showTimePicker();
        tap.setInit_date(new MyTimeStamp(dateTimeDialog.getCalendar()));
    }

    // -------------------------------------------------------------------------- On Server NegativeResult
    @Override
    public void Response() {
        switch (model.getMethod()) {
            case "getTapsByChildAndStatus": {
                tapsByStatus = model.getTaps();
                for(Tap tap : tapsByStatus){
                    showlog(tap.toString());
                }
                if (!tapsByStatus.isEmpty()) {
                    init_view_and_listeners();
                    showtoast("Actualizado");
                } else {
                    setContentView(R.layout.activity_list);
                    showtoast("¡No hay ningún tap todavia!");
                    finish();
                }
                //customAdapter.notifyDataSetChanged();
                break;
            }

            case "deleteTap":
                showtoast("Tap eliminado " + model.getObject().toString());
                init_or_refresh_attributes();
                //customAdapter.notifyDataSetChanged();
                break;

            case "modifyTap":
                showtoast("Tap modificado " + model.getObject().toString());
                init_or_refresh_attributes();
                //customAdapter.notifyDataSetChanged();
                break;

            case "createTap":
                showtoast("Tap creado " + model.getObject().toString());
                init_or_refresh_attributes();
                //customAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();

        if (negativeResult.getCode() == 0) showtoast(negativeResult.getMessage());
        else showtoast(negativeResult.getMessage());

        // SI NO HAY CONEXION (error -777) MUESTRA UNA VENTANITA. Al darle a OK, te lleva a la Activity User Login.
        if (negativeResult.getCode() == -777)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.conectionErrorTitle);
            alertDialog.setMessage(R.string.conectionErrorMesasge);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener ()
            {
                @Override public void onClick (DialogInterface dialog, int which)
                {
                    goToUserLogin();
                }
            });
            alertDialog.create().show();
        }
    }

    private void goToUserLogin() {
        Intent intent = new Intent(this, Activity_UserLogin.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //startService(new Intent(this, NotificationService.class));
        model.mam.finishActivity(this);
    }

    private void goToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        if (model.mam.activityIsAlreadyOpened(activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }
}
