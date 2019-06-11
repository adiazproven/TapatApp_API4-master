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
        child = model.getTemp_child();

        init_view_and_listeners();
        load_view();
    }

    /** asigna una layout a esta activity, inicializa los componentes que vayan a ser interactivos,
     * y le da funcionalidad a los botones anyadiendoles listeners */
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
                        goToActivity_ListOfTaps("awake");
                        break;
                    case R.id.imageView_eyepatchHistory:
                        goToActivity_ListOfTaps("eyepatch");
                        break;
                    case R.id.button_ok:
                        if (!update_view()) modifyProfile();
                        break;
                    case R.id.button_cancel:
                        goToActivity_Main();
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

    /** accede al Child guardado temporalmente en el modelo (model.temp_child, que ha sido asignado
     * en Activity_Main, que es la anterior Activity a esta) para mostrar sus datos (nombre, tipo de
     * tratamiento y numero de horas/porcentaje) */
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

    /** infla la barra de menu de arriba */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_child_modify, menu);
        return true;
    }

    /** le da funcionalidad a los botones de la barra de menu */
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
     * UPDATE VIEW
     * -----------
     * Si algun dato se pasa del limite, lo cambia (ejemplo: 25 horas -> 24 horas).
     * <p>
     * Según el radioButton seleccionado (horas / porcentaje):
     * Poner en blanco (ediatble) o en gris (no editable) las cajas de texto horas / porcentaje
     * Además, mostrar informacion sobre el radioButton seleccionado (horas / porcentaje)
     *
     * @return "true" si ha cambiado algo o muestra algun error. "false" si no.
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

    // ------------------------------------------------------------------------ Start other Activity

    void goToActivity_ListOfTaps(String type) // "type" puede ser "eyepatch" o "awake" (tambien le pasa el ID_OF_CHILD)
    {
        update_view();

        //model.setTemp_child(generateChildFromForm());

        goToActivity(Activity_ListOfTaps.class, "type", type);
    }

    /** se va a la Activity_Main y cierra esta */
    void goToActivity_Main() {
        goToActivity(Activity_Main.class);
    }

    // -------------------------------------------------------------------- Server NegativeResult Listener

    @Override
    public void Response() {
        switch (model.getMethod()) {
            case "modifyChild":
                model.setTemp_child(child);
                goToActivity_Main();
                break;
            case "deleteChild":
                Toast.makeText(getApplicationContext(), "Perfil eliminado", Toast.LENGTH_SHORT).show();
                model.setTemp_child(null);
                goToActivity_Main();
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

    private void goToActivity (Class activity)
    {
        Intent intent = new Intent(this, activity);
        if (model.mam.activityIsAlreadyOpened(activity))
        { intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); }
        startActivity(intent);
    }

    private void goToActivity_UserLogin() {
        Toast.makeText(getApplicationContext(), "Has cerrado sesión.", Toast.LENGTH_SHORT).show();
        model.restart();
        Intent intent = new Intent(Activity_ChildModify.this, Activity_UserLogin.class);
        startActivity(intent);
        finish();
    }

    private void goToActivity (Class activity, String nameOfExtra, String extra)
    {
        Intent intent = new Intent(this, activity);
        intent.putExtra(nameOfExtra, extra);
        if (model.mam.activityIsAlreadyOpened(activity))
        { intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); }
        startActivity(intent);
    }
}

