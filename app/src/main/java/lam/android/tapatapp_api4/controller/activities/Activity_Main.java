package lam.android.tapatapp_api4.controller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.MyTimeStamp;
import lam.android.tapatapp_api4.model.Tap;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;

/**
 * In this Activity, the user can interact with the basic options of one Child.
 * It shows:
 * - The name of the Child.
 * - A button which is a picture of a cartoon face with/without an eyepatch, to symbolize if the Child is wearing the eyepatch or not.
 * - A button which is a picture of a cartoon eye opened/closed and with zzz, to symbolize if the Child is awake or asleep.
 * - The time (hour:minute:second) in which the Child should stop wearing the eyepatch.
 * The buttons function as follows:
 * - When the user taps on the face: If it's wearing the eyepatch, it removes it. If not, it places it.
 * If it places the eyepatch, it creates a new tap of type "eyepatch" which date of start is right now, and stores in in the database.
 * If it removes the eyepatch, it modifies the tap of type "eyepatch" related to this Child that has the most recent date of start: It sets its date of end to right now.
 * - When the user taps on the eye: If it's opened, it closes it. If not, it opens it.
 * If it opens it, it creates a new tap of type "awake" which date of start is right now, and stores in in the database.
 * If it closes it, it modifies the tap of type "awake" related to this Child that has the most recent date of start: It sets its date of end to right now.
 * - Everytime one of the buttons is pressed, the time at the bottom is recalculated.
 * Also, there is a menu bar at the top with the options:
 * - Modify this Child profile: Goes to the activity ChildModify.
 * - Go to the list of Child profiles related to this user session: Goes to the activity ListOfChildren.
 * - Create new Child profile: Goes to the activity ChildCreate.
 * - Go to user configuration: Goes to the activity UserConfig.
 * - Logout: Closes the user's session and goes to the activity UserLogin.
 */
public class Activity_Main extends AppCompatActivity implements Result {
    // Layout Components
    private TextView textView_childName;
    // ----------------------------------------------------------------------------------- Variables
    private ImageView imageView_awakeAsleep;
    private ImageView imageView_eyepatch;
    private TextView textView_hoursLeft;
    private TextView textView_hoursTitle;
    // Listeners
    private OnClickListener listener;
    // Attributes
    private Tap awakeTap;
    private Tap eyepatchTap;
    private Model model;
    private Button boton_reload;
    // ------------------------------------------------------------------------------ Initialization
    private Boolean addAwakeTap;
    private Boolean addEyepatchTap;

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
     * @param a : message to show
     */
    private void showtoast(String a, String b) {
        Toast.makeText(getApplicationContext(), a, Toast.LENGTH_SHORT).show();
        if (!b.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), b, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    // -------------------------------------------------------------- Action Bar Menu Initialization

    /**
     * Actualizar la vista:
     * Cogiendo el CHILD BY ID.
     * Cogiendo los LAST TAPS :
     * AWAKE TAP != null -> está despierto, si AWAKE TAP == null -> está dormido.
     * EYEPATCH TAP != null -> lleva parche, si EYEPATCH TAP == null -> NO lleva parche
     * Pedir el MISSING_TREATMENT_TIME y actualizar el tiempo restante.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("-----------------------", "-----------------------");
        showlog("START ACTIVITY");
        addAwakeTap = false;
        addEyepatchTap = false;
        awakeTap = null;
        eyepatchTap = null;
        model = Model.getInstance(getApplicationContext());
        model.setResult(this);
        loadChildrenOrRedirect();
    }

    /**
     * Si existe un niño temporal se cargará la información actualizada de este, pidiendo los datos de nuevo al servidor.
     * Si no hay un niño temporal obtendrá el número de niños que tiene este usuario para redirigirlo a una lista, a una
     * actividad sin niños o a una con un solo niño.
     */
    private void loadChildrenOrRedirect() {
        if (model.getTemp_child() != null) {
            model.setMethod("getChildByID");
            model.getChildByID(model.getTemp_child().getId());
        } else {
            model.setMethod("getNumberOfChildren");
            model.getNumberOfChildren(model.getUserSession().getId());
        }
    }

    // ------------------------------------------------------------------------------------- Methods

    private void init_view() {
        setContentView(R.layout.activity_child_basic);
        textView_childName = findViewById(R.id.textView_childName);
        imageView_awakeAsleep = findViewById(R.id.imageView_awake);
        imageView_eyepatch = findViewById(R.id.imageView_eyepatch);
        textView_hoursLeft = findViewById(R.id.textView_hoursRemaining);
        boton_reload = findViewById(R.id.button_reload_main);
    }

    private void init_listeners() {
        /*
         * Si el usuario pulsa en el botón "AÑADIR": Va al Create Profile Activity.
         * Si el usuario pulsa en el nombre del niño: Va al Children Modify Activity.
         * Si el usuario pulsa en la imagen despierto/dormido: Comunicarselo al Child Profile.
         * Si el usuario pulsa en la imagen parche/no_parche: Comunicarselo al Child Profile.
         * Si el usuario pulsa en el botón "LISTA": Va al Children List Activity.
         */
        listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {

                    case R.id.textView_childName:
                        goToActivity(Activity_ChildModify.class);
                        break;

                    case R.id.imageView_awake:
                        addAwakeTap = true;
                        reload();
                        break;

                    case R.id.imageView_eyepatch:
                        addEyepatchTap = true;
                        reload();
                        break;
                    case R.id.button_reload_main:
                        addAwakeTap = false;
                        addEyepatchTap = false;
                        reload();
                        break;
                }
            }
        };
    }

    private void reload() {
        model.setMethod("getChildByID");
        model.getChildByID(model.getTemp_child().getId());
    }

    private void set_listeners() {
        textView_childName.setOnClickListener(listener);
        imageView_awakeAsleep.setOnClickListener(listener);
        imageView_eyepatch.setOnClickListener(listener);
        boton_reload.setOnClickListener(listener);
    }

    /**
     * TODO - este metodo accederá a la base de datos más adelante
     * <p>
     * Si al iniciar esta Activity se le ha enviado una ID_OF_CHILD en el bundle, entonces mostrará ese Child Profile.
     * Si no:
     * Comprueba los Child Profile que tiene el user actual (sesion actual), y luego:
     * - Si tiene más de un Child Profile: Redirecciona al Children List Activity.
     * - Si tiene un solo Child Profile: muestra ese Child Profile.
     * - Si no tiene ningun Child Profile: Redirecciona al Create Profile Activity.
     *
     * @return true si consigue la ID de un niño para mostrarlo, false si no.
     */
    private void checkChild(int numberOfChildren) {
        if (numberOfChildren == 0) {
            goToActivity_MainEmpty();
        } else if (numberOfChildren == 1) {
            model.setMethod("getOneChildByUser");
            model.getOneChildByUser(model.getUserSession().getId());
        } else if (numberOfChildren > 1) {
            goToActivity_ListOfChildren();
        }
    }

    // ------------------------------------------------------------------------ Start other Activity

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_userConfig:
                Intent intent = new Intent(this, Activity_UserConfig.class);
                startActivity(intent);
                finish();
                break;

            case R.id.menuItem_logout:
                model.setMethod("getLogout");
                model.getLogout();
                break;

            case R.id.menuItem_childModify:
                goToActivity(Activity_ChildModify.class);
                break;

            case R.id.menuItem_newChild:
                intent = new Intent(Activity_Main.this, Activity_ChildCreate.class);
                startActivity(intent);
                finish();
                break;

            case R.id.menuItem_childrenList:
                goToActivity_ListOfChildren();
                break;

        }
        return true;
    }

    private void goToActivity_ListOfChildren() {
        model.setTemp_child(null);
        Intent intent = new Intent(Activity_Main.this, Activity_ListOfChildren.class);
        startActivity(intent);
        finish();
    }

    /**
     * Update View - Este metodo comprueba las variables que tengan que ver con lo que se muestra en
     * pantalla para cambiar las propiedades graficas necesarias de los componentes del layout.
     */
    private void updateView() {
        if (model.getTemp_child() != null) {
            textView_childName.setText(model.getTemp_child().getName());
            if (!model.getTemp_child().isAwake()) {
                imageView_awakeAsleep.setImageResource(R.drawable.asleep);
                imageView_eyepatch.setImageResource(R.drawable.withouteyepatch);
            } else {
                imageView_awakeAsleep.setImageResource(R.drawable.awake);
                if (model.getTemp_child().isWearingEyepatch()) {
                    imageView_eyepatch.setImageResource(R.drawable.witheyepatch);
                } else {
                    imageView_eyepatch.setImageResource(R.drawable.withouteyepatch);
                }
            }
            setHoursLeft();
        }
    }

    // ---------------------------------------------------------------------- Server NegativeResult Method

    private String convertSecondsToTime(int seconds) {
        String time = MyTimeStamp.now().plusSeconds(seconds).format3();
        return time;
    }

    private void goToActivity_UserLogin() {
        Intent intent = new Intent(Activity_Main.this, Activity_UserLogin.class);
        startActivity(intent);
        finish();
    }

    private void goToActivity_MainEmpty() {
        Intent intent = new Intent(Activity_Main.this, Activity_MainEmpty.class);
        startActivity(intent);
        finish();
    }

    private void setTempChild(Child child) {
        model.setTemp_child(child);
        model.setMethod("taps");
        model.getLastTapsByChildAndStatus(model.getTemp_child().getId());
    }

    @Override
    public void Response() {
        switch (model.getMethod()) {
            case "getOneChildByUser":
                setTempChild((Child) model.getObject());
                break;
            case "getNumberOfChildren":
                checkChild((Integer) model.getObject());
                break;
            case "getChildByID":
                Child child = (Child) model.getObject();
                child.setRol_id(model.getTemp_child().getRol_id());
                setTempChild(child);
                break;
            case "taps":
                setTaps();
                break;
            case "awakeInit":
                showlog("awakeInit");
                imageView_awakeAsleep.setImageResource(R.drawable.awake);
                setHoursLeft();
                model.getTemp_child().setAwake(true);
                Tap old_awaketap = awakeTap;
                awakeTap = (Tap) model.getObject();
                showlog("addAwakeTap" + addAwakeTap.toString());
                if (addAwakeTap && old_awaketap == null) {
                    showlog("third if");
                    addAwakeTap = false;
                    checkEyePatch();
                }
                break;
            case "awakeEnd":
                imageView_awakeAsleep.setImageResource(R.drawable.asleep);
                setHoursLeft();
                showlog("awakeEnd");
                if (model.getTemp_child().isWearingEyepatch()) {
                    showlog("awakeEnd1");
                    model.setMethod("eyePatchEnd");
                    //Modifica el ENDDATE del EYEPATCHTAP por el ENDATE DEL AWAKETAP
                    model.modifyEndDateOfEyePatchTap(eyepatchTap.getId(), awakeTap.getId(), model.getTemp_child().getId());
                }
                awakeTap = null;
                break;
            case "eyePatchInit":
                showlog("eyePatchInit");
                imageView_eyepatch.setImageResource(R.drawable.witheyepatch);
                textView_hoursTitle.setVisibility(View.VISIBLE);
                textView_hoursLeft.setVisibility(View.VISIBLE);
                model.getTemp_child().setWearingEyepatch(true);
                eyepatchTap = (Tap) model.getObject();
                model.setMethod("getTreatmentTime");
                model.getTreatmentTime(model.getTemp_child().getId());
                break;
            case "getTreatmentTime":
                model.getTemp_child().setTreatment_time_today((Integer) model.getObject());
                setHoursLeft();
                break;
            case "eyePatchEnd":
                showlog("eyePatchEnd");
                imageView_eyepatch.setImageResource(R.drawable.withouteyepatch);
                textView_hoursTitle.setVisibility(View.GONE);
                textView_hoursLeft.setVisibility(View.GONE);
                model.getTemp_child().setWearingEyepatch(false);
                eyepatchTap = null;
                model.getTemp_child().setTreatment_time_today((Integer) model.getObject());
                setHoursLeft();
                break;
            /*case "modifyAwakeAverageOfChild":
                model.modifyAwakeAverageOfChild(modifiedAwakeAverage, time, idOfChild);
                int treatment_time_today = model.getTemp_child().getTreatment_time_today();
                int treatment_time_today_calculate = (3600 * model.getTemp_child().getaverageAwakeADay() * model.getTemp_child().getHoras_o_porcentaje())/100;
                if(treatment_time_today_calculate >= treatment_time_today){

                }
                break;*/
            case "getLogout":
                model.restart();
                goToActivity_UserLogin();
                break;
        }
    }

    private void setTaps() {
        Tap old_awakeTap = null;
        Tap old_eyepatchTap = null;

        if (model.getTaps().size() == 1) {
            // AWAKE TAP ACTIVO
            if (model.getTaps().get(0).getStatus_id() == 1) {
                model.getTemp_child().setAwake(true);
                model.getTemp_child().setWearingEyepatch(false);
                eyepatchTap = null;
                if (awakeTap == null) {
                    old_awakeTap = null;
                } else {
                    old_awakeTap = awakeTap;
                }
                awakeTap = model.getTaps().get(0);
            }// EYEPATCH -> AT.endDate (INACTIVO) = EYEPATCH.endDate = Tenía parche cuando se durmió
            else if (model.getTaps().get(0).getStatus_id() == 2 && model.getTaps().get(0).getEnd_date() != null) {
                showlog("first if");
                if (addAwakeTap && awakeTap == null) {
                    showlog("second if");
                    model.getTemp_child().setAwake(false);
                    model.getTemp_child().setWearingEyepatch(false);
                    eyepatchTap = null;
                    changeStatusAwake();
                }
            }
        } else {
            for (Tap tap : model.getTaps()) {
                if (tap.getStatus_id() == 1) {
                    model.getTemp_child().setAwake(true);
                    if (awakeTap == null) {
                        old_awakeTap = null;
                    } else {
                        old_awakeTap = awakeTap;
                    }
                    awakeTap = tap;
                } else {
                    model.getTemp_child().setWearingEyepatch(true);
                    if (eyepatchTap == null) {
                        old_eyepatchTap = null;
                    } else {
                        old_eyepatchTap = eyepatchTap;
                    }
                    eyepatchTap = tap;
                }
            }
        }

        init_view();
        init_listeners();
        set_listeners();
        updateView();
        setHoursLeft();

        showlog("BOOLENA" + addAwakeTap);
        showlog("BOOLENA" + addEyepatchTap);
        if (addAwakeTap && awakeTap != null) {
            showlog("settaps1");
            addAwakeTap = false;
            if (old_awakeTap != null && old_awakeTap.getId() == awakeTap.getId()) {
                changeStatusAwake();
            } /* if (old_awakeTap == null && awakeTap == null) {
                changeStatusAwake();
            }*/
        } else if (addEyepatchTap) {
            addEyepatchTap = false;
            if (old_eyepatchTap != null && eyepatchTap != null && old_eyepatchTap.getId() == eyepatchTap.getId()) {
                showlog("BOOLENA22222");
                changeStatusEyepatch();
            } else if (old_eyepatchTap == null && eyepatchTap == null) {
                showlog("BOOLENA333333");
                changeStatusEyepatch();
            }
        }
    }

    /**
     * Cuadro de dialogo preguntando si lleva o no parche.
     */
    private void checkEyePatch() {
        showlog("checkEyePatch");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("¿Sigue llevando el parche?");
        alert.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                model.setMethod("eyePatchInit");
                model.addTap(new Tap(model.getTemp_child().getId(), 2, MyTimeStamp.now(), null));//child_id, status, init. endDate
            }
        });
        alert.setNegativeButton("No", null);
        alert.create().show();

    }

    private void setHoursLeft() {
        showlog(MyTimeStamp.now().plusSeconds(model.getTemp_child().getTreatment_time_today()).format3());
        if (!model.getTemp_child().isWearingEyepatch()) {
            textView_hoursTitle.setVisibility(View.GONE);
            textView_hoursLeft.setVisibility(View.GONE);
        } else if (model.getTemp_child().isWearingEyepatch() || model.getTemp_child().isAwake() || !model.getTemp_child().isAwake()) {
            textView_hoursTitle.setVisibility(View.VISIBLE);
            textView_hoursLeft.setVisibility(View.VISIBLE);
            textView_hoursLeft.setText(MyTimeStamp.now().plusSeconds(model.getTemp_child().getTreatment_time_today()).format3());
        }
        if (awakeTap != null) {
            showtoast("Actualizado", awakeTap.getInit_date().toString());
        } else if (eyepatchTap != null) {
            showtoast("Actualizado", eyepatchTap.getInit_date().toString());
        } else {
            showtoast("Actualizado", "");
        }
    }

    /**
     * Crear un AWAKE TAP:
     * Si el último AT -> ED == null, es porque ya está despierto (otro usuario ha añadido este tap).
     * * Muestra un mensaje avisando de que USERNAME creço este TAP a esta HORA, RELOAD la página tras dar a aceptar.
     * Si el último AT -> ED != null, es porque el último AT está cerrado, entonces puedes crear el tap.
     */
    private void changeStatusAwake() {
        showlog("changeStatusAwake");
        if (model.getTemp_child().isAwake()) { //Si esta despierto > Se duerme
            showlog("despierto");
            model.setMethod("awakeEnd"); //tap_id, child_id, status, endDate
            model.modifyEndDateOfTap(new Tap(awakeTap.getId(), model.getTemp_child().getId(), 1, MyTimeStamp.now()));
        } else if (!model.getTemp_child().isAwake()) {//Si está dormido > Se despierta
            showlog("dormido");
            model.setMethod("awakeInit");
            model.addTap(new Tap(model.getTemp_child().getId(), 1, MyTimeStamp.now(), null));//child_id, status, init. endDate
        }
    }

    private void changeStatusEyepatch() {
        if (model.getTemp_child().isAwake()) {
            if (model.getTemp_child().isWearingEyepatch()) { //Si esta con parche > Se lo quita
                model.setMethod("eyePatchEnd"); //tap_id, child_id, status, endDate
                model.modifyEndDateOfTap(new Tap(eyepatchTap.getId(), model.getTemp_child().getId(), 2, MyTimeStamp.now()));
            } else if (!model.getTemp_child().isWearingEyepatch()) {//Si está sin parche > Se lo pone
                model.setMethod("eyePatchInit");
                model.addTap(new Tap(model.getTemp_child().getId(), 2, MyTimeStamp.now(), null));//child_id, status, init. endDate
            }
        }
    }

    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        Tap old_awakeTap = awakeTap;

        if (negativeResult.getCode() == 0 && negativeResult.getMessage().contains("taps activos")) {
            model.getTemp_child().setAwake(false);
            awakeTap = null;
            model.getTemp_child().setWearingEyepatch(false);
            eyepatchTap = null;
            init_view();
            init_listeners();
            set_listeners();
            updateView();
            setHoursLeft();
            if (addAwakeTap && awakeTap == null  && old_awakeTap == null) {
                addAwakeTap = false;
                changeStatusAwake();
            }
        } else if (negativeResult.getCode() == 0 && negativeResult.getMessage().contains("tap")) {
            Log.i("ERROR MAIN", negativeResult.getMessage() + negativeResult.getCode());
        } else if (negativeResult.getCode() == 0) {
            showlog(negativeResult.getMessage());
            goToActivity_MainEmpty();
        } else {
            Log.i("ERROR MAIN ELSE", negativeResult.getMessage() + negativeResult.getCode());
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

    private void goToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        if (model.mam.activityIsAlreadyOpened(activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
        finish();
    }
}