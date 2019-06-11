package lam.android.tapatapp_api4.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.User;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;
import lam.android.tapatapp_api4.model.connection.NegativeResult;

/**
 * In this Activity, the user can create a new User.
 * It shows a form, in which the user has to input:
 * - The email of the User.
 * - The name of the User.
 * - The password of the User.
 * - The password of the User, again.
 * It also shows one button:
 * - Register: Checks if the inputs are valid, stores the new User in the database,
 *   logs the user into the system, and redirects to the activity Main.
 */
public class Activity_UserRegister extends AppCompatActivity implements Result
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
    private EditText cajaDeTexto_email;

    private TextView mensajeDeError_email;
    private EditText cajaDeTexto_nombreDeUsuario;
    private TextView mensajeDeError_username;
    private EditText cajaDeTexto_contrasena;
    private TextView mensajeDeError_password;
    private EditText cajaDeTexto_contrasena_repetida;
    private TextView mensajeDeError_passwordRepeat;

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
        model.setResult(this);

        init_view_and_listeners();
    }

    /** asigna una layout a esta activity, inicializa los componentes que vayan a ser interactivos,
     * y le da funcionalidad a los botones anyadiendoles listeners */
    private void init_view_and_listeners() {
        setContentView(R.layout.activity_user_register);

        cajaDeTexto_email = findViewById(R.id.editText_email);
        mensajeDeError_email = findViewById(R.id.errorMessage_email);
        cajaDeTexto_nombreDeUsuario = findViewById(R.id.editText_username);
        mensajeDeError_username = findViewById(R.id.errorMessage_username);
        cajaDeTexto_contrasena = findViewById(R.id.editText_password);
        mensajeDeError_password = findViewById(R.id.errorMessage_password);
        cajaDeTexto_contrasena_repetida = findViewById(R.id.editText_passwordRepeat);
        mensajeDeError_passwordRepeat = findViewById(R.id.errorMessage_passwordRepeat);
        Button boton_registrarse = findViewById(R.id.button_register);

        boton_registrarse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.button_register) if (!update_view()) registrarse();
            }
        });
    }

    // ------------------------------------------------------------------------------------- Methods
    private boolean update_view() {
        mensajeDeError_email.setText("");
        mensajeDeError_username.setText("");
        mensajeDeError_password.setText("");
        mensajeDeError_passwordRepeat.setText("");


        if (cajaDeTexto_email.getText().length() == 0){
            mensajeDeError_email.setText("Este campo no puede estar vacío.");
        }
        else if (!validate(cajaDeTexto_email.getText().toString()))
        {
            mensajeDeError_email.setText("Este no es un email valido.");
        }

        if (cajaDeTexto_nombreDeUsuario.getText().length() == 0)
            mensajeDeError_username.setText("Este campo no puede estar vacío.");
        if (cajaDeTexto_contrasena.getText().length() == 0)
            mensajeDeError_password.setText("Este campo no puede estar vacío.");
        if (cajaDeTexto_contrasena_repetida.getText().length() == 0)
            mensajeDeError_passwordRepeat.setText("Este campo no puede estar vacío.");
        else{
            String password = cajaDeTexto_contrasena.getText().toString();
            String passwordRepeat = cajaDeTexto_contrasena_repetida.getText().toString();
            if (!password.equals(passwordRepeat))
                mensajeDeError_passwordRepeat.setText("Las contraseñas no coinciden.");
        }

        // Si las cajas de mensajes de error estan vacias, retorna false (significa que NO ha tenido que mostrar un mensaje de error)
        return !mensajeDeError_email.getText().toString().isEmpty()
                || !mensajeDeError_username.getText().toString().isEmpty()
                || !mensajeDeError_password.getText().toString().isEmpty()
                || !mensajeDeError_passwordRepeat.getText().toString().isEmpty();
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

    private void registrarse() {
        String username = cajaDeTexto_nombreDeUsuario.getText().toString();
        String password = cajaDeTexto_contrasena.getText().toString();
        String email = cajaDeTexto_email.getText().toString();

        model.addUser(new User(username, password, email));
    }

    // -------------------------------------------------------------------------- On Server NegativeResult

    @Override
    public void Response() {
        finish();
    }

    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        if (negativeResult.getCode() == -1) {
            mensajeDeError_username.setText(negativeResult.getMessage());
        } else if (negativeResult.getCode() == -2) {
            mensajeDeError_email.setText(negativeResult.getMessage());
        } else {
            mensajeDeError_username.setText(negativeResult.getMessage());
            mensajeDeError_email.setText(negativeResult.getMessage());
        }

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
}
