package lam.android.tapatapp_api4.controller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.User;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;

/**
 * In this Activity, the user can log into the system.
 * It shows a form, in which the user has to input:
 * - The username
 * - The password
 * It also shows to buttons:
 * - Login: Checks if the inputs are valid, logs the user into the system, and redirects to the activity Main.
 * - Register: Goes to the activity UserRegister.
 */
public class Activity_UserLogin extends AppCompatActivity implements Result
{
    /**
     * Show error message inside the log. For testing.
     * @param o : string or object (toString) to show
     */
    private void showlog (Object o) {
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
    private EditText cajaDeTexto_nombreDeUsuario;

    private TextView mensajeDeError_username;
    private EditText cajaDeTexto_contrasena;
    private TextView mensajeDeError_password;

    //  Listeners
    private View.OnClickListener listener;

    // Attributes
    private Model model;

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

        init_view_and_listeners();
    }

    /** asigna una layout a esta activity, inicializa los componentes que vayan a ser interactivos,
     * y le da funcionalidad a los botones anyadiendoles listeners */
    private void init_view_and_listeners() {
        setContentView(R.layout.activity_user_login);

        cajaDeTexto_nombreDeUsuario = findViewById(R.id.editText_username);
        mensajeDeError_username = findViewById(R.id.errorMessage_username);
        cajaDeTexto_contrasena = findViewById(R.id.editText_password);
        mensajeDeError_password = findViewById(R.id.errorMessage_password);
        Button boton_iniciarSesion = findViewById(R.id.button_login);
        Button boton_goToUserRegistrationActivity = findViewById(R.id.button_register);

        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_login:
                        if (!update_view()) logIn();
                        break;
                    case R.id.button_register:
                        goToUserRegistrationActivity();
                        break;
                }
            }
        };

        boton_iniciarSesion.setOnClickListener(listener);
        boton_goToUserRegistrationActivity.setOnClickListener(listener);
    }

    /**
     * Al presionar el botón iniciar sesión:
     * Comprueba que los campos no estén vacíos, si lo están muestra un mensaje de error, si no,
     * hace la petición para validar el usuario.
     */
    private void logIn() {
        model.setResult(this);
        String username = cajaDeTexto_nombreDeUsuario.getText().toString();
        String password = cajaDeTexto_contrasena.getText().toString();
        showlog("Iniciar sesion con username " + username + " y password " + password);
        model.setMethod("validUser");
        model.validUser(new User(username, password));
    }

    // ------------------------------------------------------------------------------------- Methods

    private boolean update_view() {
        mensajeDeError_username.setText("");
        mensajeDeError_password.setText("");

        if (cajaDeTexto_nombreDeUsuario.getText().toString().isEmpty())
            mensajeDeError_username.setText("Este campo no puede estar vacío.");

        if (cajaDeTexto_contrasena.getText().toString().isEmpty())
            mensajeDeError_password.setText("Este campo no puede estar vacío.");

        // Dependiendo de si los mensajes de error tienen texto o no, devuelve false o true:
        if (mensajeDeError_username.getText().toString().isEmpty()
                && mensajeDeError_password.getText().toString().isEmpty()) {
            //showlog("Ningun error en el formulario.");
            return false;
        } else {
            showlog("Errores en el formulario");
            return true;
        }
    }

    // ------------------------------------------------------------------------ Start other Activity
    private void goToUserRegistrationActivity() {
        goToActivity(Activity_UserRegister.class);
    }

    private void goToMainActivity() {
        goToActivity(Activity_Main.class);
    }

    // ---------------------------------------------------------------------- Server NegativeResult Methods

    @Override
    public void Response() {
        switch (model.getMethod()) {
            case "validUser":
                int valid = (Integer) model.getObject();
                model.setMethod("getUserByID");
                model.getUserByID(valid);
                break;
            case "getUserByID":
                model.setUserSession((User) model.getObject());
                goToMainActivity();
                finish();
                break;
        }
    }

    @Override
    public void NegativeResponse() {
        model.setMethod(null);
        NegativeResult negativeResult = model.getOnError();
        if (negativeResult.getCode() == 0) {
            mensajeDeError_username.setText(negativeResult.getMessage());
        }  else if (negativeResult.getCode() == -2) {
            mensajeDeError_password.setText(negativeResult.getMessage());
        } else {
            mensajeDeError_username.setText(negativeResult.getMessage());
            mensajeDeError_password.setText(negativeResult.getMessage());
        }

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

    private void goToActivity (Class activity)
    {
        Intent intent = new Intent(this, activity);
        if (model.mam.activityIsAlreadyOpened(activity))
        { intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); }
        startActivity(intent);
    }
}
