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
import android.widget.Toast;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;
import lam.android.tapatapp_api4.model.connection.NegativeResult;

/**
 * In this Activity, the user is welcomed by the application with enthusiasm.
 * It shows a single button to create a new Child, which redirects to the activity ChildCreate.
 * Also, there is a menu bar at the top with the options:
 * - Create new Child profile: Goes to the activity ChildCreate.
 * - Go to user configuration: Goes to the activity UserConfig.
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 */
public class Activity_MainEmpty extends AppCompatActivity implements Result
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

    /**
     * Assigns a layout to this activity, initializes its interactive layout components and gives
     * them functionality by adding new listeners to them.
     */
    private void init_view_and_listeners() {
        setContentView(R.layout.activity_welcome);

        Button buttonCrearPerfil = findViewById(R.id.button_createChild);
        Button boton_reload = findViewById(R.id.button_reload);

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_reload:
                        // Y aqui que ?
                        break;
                    case R.id.button_createChild:
                        goToActivity_ChildCreate();
                }
            }
        };

        buttonCrearPerfil.setOnClickListener(listener);
        boton_reload.setOnClickListener(listener);
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /**
     * Shows (inflates) the menu bar on the top
     */
    @Override public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    /**
     * Gives functionality to the menu bar buttons
     */
    @Override public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuItem_new:
                goToActivity_ChildCreate();
                break;

            case R.id.menuItem_userConfig:
                goToActivity(Activity_UserConfig.class);
                break;

            case R.id.menuItem_logout:
                model.setMethod("getLogout");
                model.getLogout();
                break;

        }
        return true;
    }

    // ------------------------------------------------------------------------ Start other Activity

    private void goToActivity_ChildCreate() {
        goToActivity(Activity_ChildCreate.class);
    }

    private void goToActivity_UserLogin() {
        goToActivity(Activity_UserLogin.class);
    }

    /**
     * Reacts to a server response. Reacts differently depending on the response.
     */
    @Override
    public void Response() {
        model.restart();
        Toast.makeText(getApplicationContext(),"Has cerrado sesión.", Toast.LENGTH_LONG);
        goToActivity_UserLogin();
    }

    /**
     * Reacts to a server negative (error) response. Reacts differently depending on the response.
     */
    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        String message_error = negativeResult.getMessage();

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
        //if (model.mam.activityIsAlreadyOpened(activity)) intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
