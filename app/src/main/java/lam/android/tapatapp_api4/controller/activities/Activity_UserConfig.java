package lam.android.tapatapp_api4.controller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
 * In this Activity, the user can modify its account options.
 * It shows:
 * - The name of the user
 * - The email of the user
 * - Some *** symbolizing the user's password.
 * - A button that says "Delete Account".
 * The options are:
 * - If the user taps on the email: It shows a window to change the email.
 *   The window has a form in which to input the password, and the new email. Also Accept and Cancel buttons.
 *   Accept: Checks if the inputs are valid and applies the changes to the User in the database.
 *   Cancel: Goes to the previous activity.
 * - If the user taps on the ***: It shows a window to change the password.
 *   The window has a form in which to input the current password, and the new password. Also Accept and Cancel buttons.
 *   Accept: Checks if the inputs are valid and applies the changes to the User in the database.
 *   Cancel: Goes to the previous activity.
 * Also, there is a menu bar at the top with the options:
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 * - Go back: Goes back to the previous activity.
 */
public class Activity_UserConfig extends AppCompatActivity implements Result
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

    // Attributes
    private Model model;

    // Layout Components
    TextView texto_username;
    Button boton_cambiarEmail;
    Button boton_cambiarPassword;
    Button boton_eliminarCuenta;

    // Listeners
    OnClickListener listener;
    private User user;
    private String newEmail;
    private String newPassword;

    // ------------------------------------------------------------------------------ Initialization

    /**
     * First, if it needs to show data, it loads it from the server database. Then, it initializes
     * the view (layout and components) and the listeners (and sets them).
     * @param savedInstanceState : bundle passed through the previous activity
     */
    @Override protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i("-----------------------", "-----------------------");
        showlog("START ACTIVITY");
        model = Model.getInstance(this);
        model.setResult(this);

        init_view_and_listeners();
        show_user();
    }

    /** asigna una layout a esta activity, inicializa los componentes que vayan a ser interactivos,
     * y le da funcionalidad a los botones anyadiendoles listeners */
    private void init_view_and_listeners()
    {
        setContentView(R.layout.activity_user_config);

        texto_username = findViewById(R.id.textView_username);
        boton_cambiarEmail = findViewById(R.id.button_changeEmail);
        boton_cambiarPassword = findViewById(R.id.button_changePassword);
        boton_eliminarCuenta = findViewById(R.id.button_deleteAccount);

        listener = new OnClickListener ()
        {
            @Override public void onClick (View v)
            {
                switch (v.getId())
                {
                    case R.id.button_changeEmail: cambiarEmail(); break;
                    case R.id.button_changePassword: cambiarPassword(); break;
                    case R.id.button_deleteAccount: eliminarCuenta(); break;
                }
            }
        };

        boton_cambiarEmail.setOnClickListener(listener);
        boton_cambiarPassword.setOnClickListener(listener);
        boton_eliminarCuenta.setOnClickListener(listener);
    }

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
    private void show_user ()
    {
        user = model.getUserSession();
        texto_username.setText(user.getUsername());
        boton_cambiarEmail.setText(user.getEmail());
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /** infla la barra de menu de arriba */
    @Override public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    /** le da funcionalidad a los botones de la barra de menu */
    @Override public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
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

    // ------------------------------------------------------------------------------ Option Methods

    private void cambiarEmail ()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Cambiar email");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_email, null);
        alertDialogBuilder.setView(dialogView);

        final EditText editText_currentPassword = dialogView.findViewById(R.id.editText_currentPassword);
        final TextView errorMessageCurrentPassword = dialogView.findViewById(R.id.errorMessage_currentPassword);
        final EditText editText_newEmail = dialogView.findViewById(R.id.editText_newEmail);
        final TextView errorMessageNewEmail = dialogView.findViewById(R.id.errorMessage_newEmail);

        alertDialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener ()
        {
            @Override public void onClick(DialogInterface dialog, int which)
            {
                // esto debe estar aqui pero vacio. si quieres saber porque, mira esto:
                // https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
            }
        });
        alertDialogBuilder.setNegativeButton("Cancelar", null);

        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //boolean wantToCloseDialog = false;
                ccf.data=0;

                // COMPROBAR QUE LOS CAMPOS NO ESTEN VACIOS Y QUE EL EMAIL SEA VALIDO

                if (editText_currentPassword.getText().length() == 0)
                {
                    errorMessageCurrentPassword.setText("Este campo no puede estar vacío.");
                    return;
                }

                if (editText_newEmail.getText().length() == 0)
                {
                    errorMessageNewEmail.setText("Este campo no puede estar vacío.");
                    return;
                }
                else if (!validate(editText_newEmail.getText().toString()))
                {
                    errorMessageNewEmail.setText("Este no es un email valido.");
                    return;
                }
                else if (ccf.data==2)
                {
                    errorMessageNewEmail.setText("Este email ya existe.");
                    ccf.data=0;
                    return;
                }

                // LLAMAR AL MODELO PARA PREGUNTARLE SI EL PASSWORD ES CORRECTO Y, SI LO ES, CAMBIAR EL EMAIL

                String currentPassword = editText_currentPassword.getText().toString();
                if(model.getUserSession().getPassword().equals(currentPassword))
                {
                    newEmail = editText_newEmail.getText().toString();
                    model.setMethod("modifyEmailUser");
                    model.modifyUser(new User(model.getUserSession().getUsername(), currentPassword, newEmail));
                    //wantToCloseDialog = true;
                }
                else errorMessageCurrentPassword.setText("Contraseña incorrecta.");

                if(ccf.data==1) dialog.dismiss();
            }
        });
    }

    //TODO PORQUE STATIC Y LUEGO DATA ????????????????????????????????????????????
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

    final Data ccf = new Data();

    private void cambiarPassword ()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Cambiar contraseña");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        alertDialogBuilder.setView(dialogView);

        final EditText editText_currentPassword = dialogView.findViewById(R.id.editText_currentPassword);
        final TextView errorMessageCurrentPassword = dialogView.findViewById(R.id.errorMessage_currentPassword);
        final EditText editText_newPassword = dialogView.findViewById(R.id.editText_newPassword);
        final TextView errorMessageNewPassword = dialogView.findViewById(R.id.errorMessage_newPassword);

        alertDialogBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener ()
        {
            @Override public void onClick(DialogInterface dialog, int which)
            {
                // esto debe estar aqui pero vacio. si quieres saber porque, mira esto:
                // https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
            }
        });
        alertDialogBuilder.setNegativeButton("Cancelar", null);

        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean wantToCloseDialog = false;

                // COMPROBAR QUE LOS CAMPOS NO ESTEN VACIOS Y QUE EL EMAIL SEA VALIDO

                if (editText_currentPassword.getText().length() == 0) {
                    errorMessageCurrentPassword.setText("Este campo no puede estar vacío.");
                    return;
                }

                if (editText_newPassword.getText().length() == 0) {
                    errorMessageNewPassword.setText("Este campo no puede estar vacío.");
                    return;
                }

                // LLAMAR AL MODELO PARA PREGUNTARLE SI EL PASSWORD ES CORRECTO Y, SI LO ES, CAMBIAR EL EMAIL

                // TODO --- Araceli --- Comprobar que el email sea valido (que tenga @ y todo eso)
                String currentPassword = editText_currentPassword.getText().toString();
                if(model.getUserSession().getPassword().equals(currentPassword)){
                    newPassword = editText_newPassword.getText().toString();
                    model.setMethod("modifyPasswordUser");
                    model.modifyUser(new User(model.getUserSession().getUsername(), newPassword, model.getUserSession().getEmail()));
                    wantToCloseDialog = true;
                }
                else errorMessageCurrentPassword.setText("Contraseña incorrecta.");

                if(wantToCloseDialog==true) dialog.dismiss();
            }
        });
    }

    private void eliminarCuenta ()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Eliminar cuenta");
        alert.setMessage("Al eliminar tu cuenta, perderas todos tus datos. ¿Estas seguro de querer eliminarla?");
        alert.setPositiveButton("Si", new DialogInterface.OnClickListener ()
        {
            @Override public void onClick (DialogInterface dialog, int which)
            {
                showlog("ACEPTADR");
                model.setMethod("deleteUser");
                model.deleteUser(model.getUserSession().getUsername());
            }
        });
        alert.setNegativeButton("No", null);
        alert.create().show();
    }

    @Override
    public void Response() {
        switch (model.getMethod()){
            case "modifyEmailUser":
                user.setEmail(newEmail);
                ccf.data=1;
                show_user();
                break;
            case "modifyPasswordUser":
                user.setPassword(newPassword);
                Toast.makeText(getApplicationContext(),"Contraseña cambiada", Toast.LENGTH_SHORT);
                break;
            case "deleteUser":
                showlog("deleteUser REPSONSe");
                model.restart();
                finish();
                goToActivity_UserLogin();
                break;
            case "getLogout":
                showlog("getLogout");
                model.restart();
                finish();
                goToActivity_UserLogin();
                break;
        }
    }

    private void goToActivity_UserLogin() {
        goToActivity(Activity_UserLogin.class);
    }

    //TODO ARACELI dónde pongo el mensaje para avisar de que el correo ya está en uso?
    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        if(negativeResult.getCode() == -1){
            ccf.data=2;
            //TV_MENSAJE.setText(negativeResult.getMessage());
        }else{
            //TV_MENSAJE.setText(negativeResult.getMessage());
        }

        // SI NO HAY CONEXION (error -777) MUESTRA UNA VENTANITA. Al darle a OK, te lleva a la Activity User Login.
        if (negativeResult.getCode() == -777)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Error de conexion");
            alertDialog.setMessage("No se ha podido conectar con el servidor");
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
