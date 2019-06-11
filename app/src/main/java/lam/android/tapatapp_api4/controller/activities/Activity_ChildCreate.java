package lam.android.tapatapp_api4.controller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;

/**
 * In this Activity, the user can create a new Child profile.
 * It shows a form, in which the user has to input:
 * - The name of the child
 * - The type of treatment (hours / percentage)
 * - The time (number of hours / percentaje of time awake, depending on the treatment selected)
 * - The average quantity of hours during which the child is awake each day
 * Then, the user can either tap in the Accept button, or the Cancel button.
 * - Accept: Checks if the inputs are valid, stores the new Child in the database, and redirects to the activity Main.
 * - Cancel: Goes to the previous activity.
 * Also, there is a menu bar at the top with the options:
 * - Go to user configuration: Goes to the activity UserConfig.
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 * - Go back: Goes back to the previous activity.
 */
public class Activity_ChildCreate extends AppCompatActivity implements Result {
    // Layout Components
    private EditText cajaDeTexto_nombreNino;
    private TextView mensajeDeError_username;

    // ----------------------------------------------------------------------------------- Variables
    private RadioButton opcion_horas;
    private RadioButton opcion_porcentaje;
    private EditText cajaDeTexto_horasOporcentaje;
    private TextView texto_horas;
    private TextView texto_porcentaje;
    private TextView texto_infoHoras;
    private TextView texto_infoPorcentaje;
    private TextView mensajeDeError_time;
    private EditText cajaDeTexto_horasDespiertoAprox;
    private TextView mensajeDeError_average;
    // Attributes
    private Model model;
    private int treatment_selected = Child.TREATMENT_HOURS;

    /**
     * Show error message inside the log. For testing.
     * @param o : string or object (toString) to show
     */
    private void showlog(Object o) {
        String this_class_name = this.getClass().getSimpleName();
        this_class_name = this_class_name.replace("Activity_", "");
        Log.i("--" + this_class_name, o.toString());
    }

    /**
     * Show message on the screen (Toast) to quickly communicate something to the user.
     * @param s : message to show
     */
    private void showtoast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------------------ Initialization

    /**
     * First, if it needs to show data, it loads it from the server database. Then, it initializes
     * the view (layout and components) and the listeners (and sets them).
     * @param savedInstanceState : bundle passed through the previous activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("-----------------------", "-----------------------");
        showlog("START ACTIVITY");

        model = Model.getInstance(getApplicationContext());
        model.setResult(this);

        init_view_and_listeners();
    }

    /**
     * Assigns a layout to this activity, initializes its interactive layout components and gives
     * them functionality by adding new listeners to them.
     */
    private void init_view_and_listeners() {
        setContentView(R.layout.activity_child_create);

        cajaDeTexto_nombreNino = findViewById(R.id.editText_childName);
        mensajeDeError_username = findViewById(R.id.errorMessage_childName);
        opcion_horas = findViewById(R.id.radioButton_hours);
        opcion_porcentaje = findViewById(R.id.radioButton_percentage);
        cajaDeTexto_horasOporcentaje = findViewById(R.id.editText_hoursOrPercentage);
        texto_horas = findViewById(R.id.textView_hours);
        texto_porcentaje = findViewById(R.id.textView_percentage);
        texto_infoHoras = findViewById(R.id.textView_infoHours);
        texto_infoPorcentaje = findViewById(R.id.textView_infoPercentage);
        mensajeDeError_time = findViewById(R.id.errorMessage_hoursOrPercentage);
        cajaDeTexto_horasDespiertoAprox = findViewById(R.id.editText_averageHoursAwake);
        mensajeDeError_average = findViewById(R.id.errorMessage_averageHoursAwake);
        Button boton_crearPerfil = findViewById(R.id.button_createChild);

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {//TODO hacer que el otro deje de estar seleccionado
                    case R.id.button_createChild:
                        if (update_view()) createChild();
                        break;
                    case R.id.radioButton_hours:
                        opcion_horas.setChecked(true);
                        opcion_porcentaje.setChecked(false);
                        treatment_selected = Child.TREATMENT_HOURS;
                        break;
                    case R.id.radioButton_percentage:
                        opcion_horas.setChecked(false);
                        opcion_porcentaje.setChecked(true);
                        treatment_selected = Child.TREATMENT_PERCENTAGE;
                        break;
                }
                update_view();
            }
        };

        opcion_horas.setOnClickListener(listener);
        opcion_porcentaje.setOnClickListener(listener);
        boton_crearPerfil.setOnClickListener(listener);
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /**
     * Shows (inflates) the menu bar on the top
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_config, menu);
        return true;
    }

    /**
     * Gives functionality to the menu bar buttons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_logout:
                model.setMethod("getLogout");
                model.getLogout();
                break;

            case R.id.menuItem_goBack:
                finish();
                break;
        }
        return true;
    }

    // --------------------------------------------------------------------------------- Update View

    /**
     * Shows or hides information about the option selected (hours/percentage)
     * Also shows error messages for the form fields.
     * @return true if one or more form fields showed an error, false otherwise
     */
    private boolean update_view() {
        boolean valido = true;

        // MOSTRAR O ESCONDER --- OPCIONES --- , DEPENDIENDO DEL TRATAMIENTO SELECCIONADO (TREATMENT_HOURS/TREATMENT_PERCENTAGE)

        if (treatment_selected == Child.TREATMENT_HOURS) {
            texto_infoHoras.setVisibility(View.VISIBLE);
            texto_horas.setVisibility(View.VISIBLE);

            texto_infoPorcentaje.setVisibility(View.GONE);
            texto_porcentaje.setVisibility(View.GONE);
        } else //if (treatment_selected == Child.TREATMENT_PERCENTAGE)
        {
            texto_infoHoras.setVisibility(View.GONE);
            texto_horas.setVisibility(View.GONE);

            texto_infoPorcentaje.setVisibility(View.VISIBLE);
            texto_porcentaje.setVisibility(View.VISIBLE);
        }

        // MOSTRAR O ESCONDER --- MENSAJE DE ERROR DE USERNAME --- , SI EL CAMPO DEL NOMBRE ESTA VACIO

        if (cajaDeTexto_nombreNino.getText().toString().isEmpty()) {
            mensajeDeError_username.setText("El campo del nombre no puede estar vacío");
            valido = false;
        } else mensajeDeError_username.setText("");

        // MOSTRAR O ESCONDER --- MENSAJE DE ERROR DE TIME --- , DEPENDIENDO DE SI ES MAS DE 24 TREATMENT_HOURS / MAS DE 100%

        if (opcion_horas.isChecked()) {
            if (!cajaDeTexto_horasOporcentaje.getText().toString().isEmpty()) {
                int horas = Integer.parseInt(cajaDeTexto_horasOporcentaje.getText().toString());
                if (horas > 24) {
                    mensajeDeError_time.setText("Un dia solo tiene 24 horas!");
                    valido = false;
                } else {
                    mensajeDeError_time.setText("");
                }
            } else {
                mensajeDeError_time.setText("Este campo no puede estar vacio.");
                valido = false;
            }

        } else {
            if (!cajaDeTexto_horasOporcentaje.getText().toString().isEmpty()) {
                int porcentaje = Integer.parseInt(cajaDeTexto_horasOporcentaje.getText().toString());
                if (porcentaje > 100) {
                    mensajeDeError_time.setText("El porcentaje no puede ser mayor que 100.");
                    valido = false;
                } else mensajeDeError_time.setText("");
            } else {
                mensajeDeError_time.setText("Este campo no puede estar vacio.");
                valido = false;
            }
        }

        // MOSTRAR O ESCONDER --- MENSAJE DE ERROR DE AVERAGE --- , DEPENDIENDO DE SI ES MAS DE 24 TREATMENT_HOURS:


        if (!cajaDeTexto_horasDespiertoAprox.getText().toString().isEmpty()) {
            int horasDespiertoAlDia = Integer.parseInt(cajaDeTexto_horasDespiertoAprox.getText().toString());
            if (horasDespiertoAlDia > 24) {
                mensajeDeError_average.setText("Un dia solo tiene 24 horas!");
                valido = false;
            } else mensajeDeError_average.setText("");
        } else {
            mensajeDeError_average.setText("Este campo no puede estar vacío.");
            valido = false;
        }

        return valido;
    }

    // ------------------------------------------------------------------------------------- Options

    /**
     * Creates a new Child object based on the user input on the form and adds it
     * to the database.
     */
    private void createChild() {
        String name;
        int treatment_id;
        int hoursORpercentage = 0;
        int averageTimeAwake;
        try {
            name = cajaDeTexto_nombreNino.getText().toString();
            if (name.length() != 0) {
                if (opcion_horas.isChecked()) treatment_id = 1;
                else treatment_id = 2;

                if (cajaDeTexto_horasOporcentaje.getText().length() > 0) {
                    hoursORpercentage = Integer.parseInt(cajaDeTexto_horasOporcentaje.getText().toString());
                }
                averageTimeAwake = Integer.parseInt(cajaDeTexto_horasDespiertoAprox.getText().toString());
                showlog("===================="+averageTimeAwake);
                Child child = new Child(name,averageTimeAwake, treatment_id, hoursORpercentage);
                showlog(child.toString());
                model.addChild(child, model.getUserSession().getUsername());
            }
        } catch (Exception e) {
            showlog(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------- On Server Response

    /**
     * Reacts to a server response. Reacts differently depending on the response.
     */
    @Override
    public void Response() {
        model.setTemp_child((Child) model.getObject());
        goToActivity(Activity_Main.class);
    }

    /**
     * Reacts to a server negative (error) response. Reacts differently depending on the response.
     */
    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        String message_error = negativeResult.getMessage();
        showlog(message_error + negativeResult.getCode());

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
                    goToActivity(Activity_UserLogin.class);
                }
            });
            alertDialog.create().show();
        }
    }

    /**
     * Goes to the activity specified in the parenthesis.
     * @param activity Class object to specify to what activity to go
     */
    private void goToActivity (Class activity)
    {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }
}
