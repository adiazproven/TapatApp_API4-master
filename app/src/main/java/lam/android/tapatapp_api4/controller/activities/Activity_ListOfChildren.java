package lam.android.tapatapp_api4.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.controller.activities.listelementadapters.CustomListAdapter_Child;
import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;
import lam.android.tapatapp_api4.model.connection.NegativeResult;

/**
 * In this Activity, the user can select one Child to see its options.
 * It shows a list with the names of the Child profiles that are related to this user session.
 * Then, the user can tap on one of the Child names, which redirect to the activity Main to show the options of that Child.
 * Also, there is a menu bar at the top with the options:
 * - Create new Child profile: Goes to the activity ChildCreate.
 * - Go to user configuration: Goes to the activity UserConfig.
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 */
public class Activity_ListOfChildren extends AppCompatActivity implements Result
{
    //TODO*****************************************************************
    //TODO ARACELI -> Poner botón para hacer RELOAD en la lista de ninios
    /*
    * Cuando se clique en el botón:
    *    model.setMethod("getChildrenByRol");
        model.getChildrenByRol(model.getUserSession().getId());
    * */
    //TODO*****************************************************************

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

    // Attributes
    private List<Child> list_children;
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

        list_children = new ArrayList<>();
        model.setMethod("getChildrenByRol");
        model.getChildrenByRol(model.getUserSession().getId());
    }

    /** asigna una layout a esta activity, inicializa los componentes que vayan a ser interactivos,
     * y le da funcionalidad a los botones anyadiendoles listeners */
    private void init_view_and_listeners() {
        setContentView(R.layout.activity_list);

        ListView lista = findViewById(R.id.list);
        CustomListAdapter_Child customAdapter = new CustomListAdapter_Child(this, list_children);
        lista.setAdapter(customAdapter);

        lista.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //int childId = list_children.get((int)id).getId();
                model.setTemp_child(list_children.get((int) id));
                goToActivity_Main();
            }
        });
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /** infla la barra de menu de arriba */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    /** le da funcionalidad a los botones de la barra de menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_new:
                goToCreateProfileActivity();
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

    /** se va a la Activity_Main y cierra esta */
    private void goToActivity_Main()//(int id_of_child)
    {
        goToActivity(Activity_Main.class);
    }


    // -------------------------------------------------------------------------- On Server NegativeResult

    private void goToCreateProfileActivity() {
        goToActivity(Activity_ChildCreate.class);
    }

    //ROL 1 = ADMINISTRADOR / CREADOR
    //ROL 2 = CUIDADOR
    @Override
    public void Response() {
        switch (model.getMethod()){
            case  "getChildrenByRol":
                list_children = (List<Child>)model.getObject();
                init_view_and_listeners();
                showtoast("Actualizado");
                break;
            case  "getLogout":
                model.restart();
                goToActivity_UserLogin();
                break;
        }
    }

    private void goToActivity_UserLogin() {
        goToActivity(Activity_UserLogin.class);
    }

    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        //TODO DONDE SE MUESTRA EL MENSAJE SI NO TIENE NINIOS EL USUARIO
        Log.i("Respondo mal", negativeResult.getMessage() + " " + negativeResult.getCode());
        String message_error = negativeResult.getMessage();
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
