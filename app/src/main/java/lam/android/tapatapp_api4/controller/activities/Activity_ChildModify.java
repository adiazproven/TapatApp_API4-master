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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;
import lam.android.tapatapp_api4.model.connection.NegativeResult;

/**
 * In this Activity, the user can modify an already existing Child profile.
 * It shows a form, in which the user can change:
 * - The name of the child
 * - The type of treatment (hours / percentage)
 * - The time (number of hours / percentaje of time awake, depending on the treatment selected)
 * Then, the user can either tap in the Accept button, or the Cancel button.
 * - Accept: Checks if the inputs are valid, applies the changes to the Child in the database, and redirects to the activity Main.
 * - Cancel: Goes to the previous activity.
 * Also, there is a menu bar at the top with the options:
 * - Go to user configuration: Goes to the activity UserConfig.
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 * - Go back: Goes back to the previous activity.
 */
public class Activity_ChildModify extends AppCompatActivity implements Result
{
    /**
     * Show error message inside the log. For testing.
     * @param o : string or object to show
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
    private void showtoast (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    // ----------------------------------------------------------------------------------- Variables

    // Layout Components
    EditText cajaDeTexto_nombreNino;
    TextView mensajeDeError_childName;
    RadioButton opcion_horas;
    RadioButton opcion_porcentaje;
    EditText cajaDeTexto_horasOporcentaje;
    TextView texto_horas;
    TextView texto_porcentaje;
    TextView mensajeDeError_time;
    ImageView imagen_historialDespierto;
    ImageView imagen_historialParche;
    Button boton_aceptarCambios;
    Button boton_cancelarCambios;

    // Listeners
    OnClickListener listener;

    // Attributes
    private Model model;
    private Child child;
    private int treatment_selected;

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
        if(model.getTemp_child() != null){
            model.setMethod("getChildByID");
            model.getChildByID(model.getTemp_child().getId());
        }else{
            showtoast("El nino no existe.");
            goToActivity(Activity_Main.class);
            finish();
        }
    }

    /**
     * Assigns a layout to this activity, initializes its interactive layout components and gives
     * them functionality by adding new listeners to them.
     */
    void init_view_and_listeners() {
        setContentView(R.layout.activity_child_modify);

        cajaDeTexto_nombreNino = findViewById(R.id.editText_childName);
        mensajeDeError_childName = findViewById(R.id.errorMessage_childName);
        opcion_horas = findViewById(R.id.radioButton_hours);
        opcion_porcentaje = findViewById(R.id.radioButton_percentage);
        cajaDeTexto_horasOporcentaje = findViewById(R.id.editText_hoursOrPercentage);
        texto_horas = findViewById(R.id.textView_hours);
        texto_porcentaje = findViewById(R.id.textView_percentage);
        mensajeDeError_time = findViewById(R.id.errorMessage_hoursOrPercentage);
        imagen_historialDespierto = findViewById(R.id.imageView_awakeHistory);
        imagen_historialParche = findViewById(R.id.imageView_eyepatchHistory);
        boton_aceptarCambios = findViewById(R.id.button_ok);
        boton_cancelarCambios = findViewById(R.id.button_cancel);


        // si no es admin, no le deja editar nada excepto el historial
        if (model.getTemp_child().getRol_id() == Child.ROL_CARETAKER)
        {
            cajaDeTexto_nombreNino.setEnabled(false);
            opcion_horas.setEnabled(false);
            opcion_porcentaje.setEnabled(false);
            cajaDeTexto_horasOporcentaje.setEnabled(false);
        }

        listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.imageView_awakeHistory:
                        goToActivity(Activity_ListOfTaps.class, "type", "awake");
                        break;
                    case R.id.imageView_eyepatchHistory:
                        goToActivity(Activity_ListOfTaps.class, "type", "eyepatch");
                        break;
                    case R.id.button_ok:
                        if (!update_view()) modifyProfile();
                        break;
                    case R.id.button_cancel:
                        goToActivity(Activity_Main.class);
                        break;
                    case R.id.radioButton_hours:
                        treatment_selected = Child.TREATMENT_HOURS;
                        break;
                    case R.id.radioButton_percentage:
                        treatment_selected = Child.TREATMENT_PERCENTAGE;
                        break;
                }
                update_view();
            }
        };

        opcion_horas.setOnClickListener(listener);
        opcion_porcentaje.setOnClickListener(listener);
        imagen_historialDespierto.setOnClickListener(listener);
        imagen_historialParche.setOnClickListener(listener);
        boton_aceptarCambios.setOnClickListener(listener);
        boton_cancelarCambios.setOnClickListener(listener);
    }

    /**
     * Access to the Child temporarily saved in the model (model.temp_child) to show its data in the
     * correct layout components.
     */
    public void load_view() {
        if (child != null) {
            cajaDeTexto_nombreNino.setText(child.getName());

            cajaDeTexto_horasOporcentaje.setText(String.valueOf(child.getHoras_o_porcentaje()));

            treatment_selected = child.getTreatment_id();
            if (treatment_selected == Child.TREATMENT_HOURS) // 1=horas
            {
                opcion_horas.setChecked(true);
                opcion_porcentaje.setChecked(false);
                texto_horas.setVisibility(View.VISIBLE);
                texto_porcentaje.setVisibility(View.GONE);
            } else // 2=porcentaje
            {
                opcion_horas.setChecked(false);
                opcion_porcentaje.setChecked(true);
                texto_horas.setVisibility(View.GONE);
                texto_porcentaje.setVisibility(View.VISIBLE);
            }
        }
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /**
     * Shows (inflates) the menu bar on the top
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_child_modify, menu);
        return true;
    }

    /**
     * Gives functionality to the menu bar buttons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuItem_userConfig:
                goToActivity(Activity_UserConfig.class);
                break;
            case R.id.menuItem_logout:
                model.setMethod("getLogout");
                model.getLogout();
                break;

            case R.id.menuItem_deleteChild:
                deleteProfile();
                break;
            case R.id.menuItem_shareChild:
                shareProfile();
                break;
            //default: return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // --------------------------------------------------------------------------------- Update View

    /**
     * Checks each one of the form fields.
     * If it is superior to its limit, it shows an error (eg: 25 hours per day)
     * Enables/Disables the editText components of hours/percentage depending on the selected option.
     * Shows/Hides information about hours/percentage depending on the option selected.
     * @return true if one or more errors are shown, false otherwise.
     */
    public boolean update_view() {
        // De momento no sabemos si hay algun error o no, asi que vaciamos las cajas de texto de mensajes de error.
        mensajeDeError_time.setText("");
        mensajeDeError_childName.setText("");

        if (treatment_selected == Child.TREATMENT_HOURS) {
            opcion_horas.setChecked(true);
            opcion_porcentaje.setChecked(false);
            texto_horas.setVisibility(View.VISIBLE);
            texto_porcentaje.setVisibility(View.GONE);
        }
        else {
            opcion_horas.setChecked(false);
            opcion_porcentaje.setChecked(true);
            texto_horas.setVisibility(View.GONE);
            texto_porcentaje.setVisibility(View.VISIBLE);
        }
        // Comprobar CHILD NAME
        if (cajaDeTexto_nombreNino.getText().toString().isEmpty())
            mensajeDeError_childName.setText("Este campo no puede estar vacio.");

        // Comprobar TIME y ademas mostrar opciones segun el tratamiento seleccionado (hora/porcentaje)
        if (cajaDeTexto_horasOporcentaje.getText().toString().isEmpty())
            mensajeDeError_time.setText("Este campo no puede estar vacio.");
        else
        {
            int time = Integer.parseInt(cajaDeTexto_horasOporcentaje.getText().toString());
            if (treatment_selected == Child.TREATMENT_HOURS)
            {
                if (time > 24) mensajeDeError_time.setText("Un dia solo tiene 24 horas!");
            }
            else if (time > 100)
            {
                    mensajeDeError_time.setText("El porcentaje no puede ser mayor que 100.");
            }
        }

        // Si las cajas de mensajes de error estan vacias, retorna false (significa que NO ha tenido que mostrar un mensaje de error)
        return !mensajeDeError_childName.getText().toString().isEmpty()
                || !mensajeDeError_time.getText().toString().isEmpty();
    }

    // ------------------------------------------------------------------------------------- Options

    /**
     * Based on the form, it changes the data of the Child that is temporarily saved in the model (temp_child)
     * and then it modifies its counterpart (same id) in the database.
     */
    void modifyProfile() {
        // Child child = null;
        if (!update_view()) { //generateChildFromForm();
            child.setName(cajaDeTexto_nombreNino.getText().toString());
            if (treatment_selected == Child.TREATMENT_HOURS) {
                child.setHoras_o_porcentaje(Integer.parseInt(cajaDeTexto_horasOporcentaje.getText().toString()));
            } else if (treatment_selected == Child.TREATMENT_PERCENTAGE) {
                child.setHoras_o_porcentaje(Integer.parseInt(cajaDeTexto_horasOporcentaje.getText().toString()));
            }
            child.setTreatment_id(treatment_selected);
            model.setMethod("modifyChild");
            model.modifyChild(child);
        }
    }

    /**
     * It deletes the counterpart (same id) of the Child that is temporarily saved in the model (temp_child) in the database.
     */
    void deleteProfile() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Eliminar perfil");
        alertDialog.setMessage("¿Seguro que quieres eliminar este perfil?");
        alertDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (child != null) {
                    int deleted = 1;//model.remove_child_from_current_session(controller.get_temp_child());
                    if (deleted == 1) {
                        model.setMethod("deleteChild");
                        model.deleteChild(child.getId());
                    }
                }
            }
        });
        alertDialog.setNegativeButton("No", null);
        alertDialog.create().show();
    }

    //Toast.makeText(getApplicationContext(),"Contraseña cambiada", Toast.LENGTH_SHORT); TODO

    /**
     * Shows a dialog in which the user inputs the username of another user.
     * Then, that other user will have caretaker access (role) towards the Child that is being shown.
     */
    void shareProfile() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Dar acceso a otro usuario");
        alertDialog.setMessage("¿A que usuario quieres dar acceso?");
        final EditText input = new EditText(Activity_ChildModify.this);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("Dar acceso", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dar acceso a este usuario :
                String username = input.getText().toString();
                model.setMethod("addRelationOfUserAndChild");
                model.addRelationOfUserAndChild(username, child.getId(), 2);
            }
        });
        alertDialog.setNegativeButton("Cancelar", null);
        alertDialog.create().show();
    }

    /**
     * GENERATE CHILD (if form is OK)
     * --------------
     * Hace el update_view() para comprobar si algun campo del formulario está mal y arreglarlo.
     * Luego, si algun campo estba mal, no hace nada. Si todos estaban bien, hace una copia del
     * child mostrado y lo retorna.
     *
     * @return Child con los datos del formulario, o null si algun dato no es valido.
     */
   /* public void generateChildFromForm ()
    {
        //Child child = new Child(this.child);

        child.setName(cajaDeTexto_nombreNino.getText().toString());
        if(treatment_selected == Child.TREATMENT_HOURS){
            //TODO Las horas se pasan a segundos
            child.setHoras_o_porcentaje(3600 * Integer.valueOf(cajaDeTexto_horasOporcentaje.getText().toString()));
        }else if (treatment_selected == Child.TREATMENT_PERCENTAGE){
            //TODO el % se deja igual
            child.setHoras_o_porcentaje(Integer.valueOf(cajaDeTexto_horasOporcentaje.getText().toString()));
        }
        //return child;
    }*/

    // -------------------------------------------------------------------- Server NegativeResult Listener

    /**
     * Reacts to a server response. Reacts differently depending on the response.
     */
    @Override
    public void Response() {
        switch (model.getMethod()) {
            case "getChildByID":
               child = (Child) model.getObject();
                init_view_and_listeners();
                load_view();
                break;
            case "modifyChild":
                model.setTemp_child(child);
                goToActivity(Activity_Main.class);
                break;
            case "deleteChild":
                Toast.makeText(getApplicationContext(), "Perfil eliminado", Toast.LENGTH_SHORT).show();
                model.setTemp_child(null);
                goToActivity(Activity_Main.class);
                break;
            case "addRelationOfUserAndChild":
                Toast.makeText(getApplicationContext(), "Se ha compartido este perfil", Toast.LENGTH_SHORT).show();
                break;
            case "getLogout":
                goToActivity_UserLogin();
                break;
            default:
                break;
        }
    }

    /**
     * Reacts to a server negative (error) response. Reacts differently depending on the response.
     */
    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        Toast.makeText(getApplicationContext(), negativeResult.getMessage(), Toast.LENGTH_SHORT).show();

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
     * Goes to activity User Login and also restarts the model temporarily saved data to avoid contradictions.
     */
    private void goToActivity_UserLogin() {
        Toast.makeText(getApplicationContext(), "Has cerrado sesión.", Toast.LENGTH_SHORT).show();
        model.restart();
        Intent intent = new Intent(Activity_ChildModify.this, Activity_UserLogin.class);
        startActivity(intent);
        finish();
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

    /**
     * Goes to the activity specified in the parenthesis.
     * @param activity Class object to specify to what activity to go
     * @param nameOfExtra Name of the extra to put in the intent
     * @param extra Value of the extra to put in the intent
     */
    private void goToActivity (Class activity, String nameOfExtra, String extra)
    {
        Intent intent = new Intent(this, activity);
        intent.putExtra(nameOfExtra, extra);
        startActivity(intent);
    }
}

