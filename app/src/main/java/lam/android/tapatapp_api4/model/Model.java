package lam.android.tapatapp_api4.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.requests.ChildDAO;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;
import lam.android.tapatapp_api4.model.connection.interfaces.OperationResult;
import lam.android.tapatapp_api4.model.connection.requests.TapDAO;
import lam.android.tapatapp_api4.model.connection.requests.UserDAO;

public class Model implements OperationResult {

    //public MyActivityManagerNO mam;

    private static Model instance;
    private Result result;
    private String method;
    /**
     * Atributos.
     */
    private Child temp_child;
    private User userSession;
    private Object object;
    private List<Child> children;
    private List<Tap> taps;
    private NegativeResult negativeResult;
    /**
     * DAO
     */
    private ChildDAO childDAO;//no lleva parche, solo esta despierto
    private UserDAO userDAO;
    private TapDAO tapDAO;
    /**
     * CONSTRUCTOR: Inicializa los DAOS.
     *
     * @param context
     */
    private Model(Context context) {
        userDAO = new UserDAO(context);
        childDAO = new ChildDAO(context);
        tapDAO = new TapDAO(context);
    }

    /**
     * Patron SINGLETON, una única instancia de la clase Modelo.
     * La primera vez que se inicia el model (USER LOGIN ACTIVITY) se cogerá ese contexto para todas
     * las otras instancias del modelo.
     *
     * @param context
     * @return
     */
    public static Model getInstance(Context context) {
        if (instance == null) {
            instance = new Model(context);
        }
        //if (instance.mam == null) instance.mam = new MyActivityManagerNO();
        return instance;
    }

    private void showlog(Object o) {
        Log.i("--" + this.getClass().getSimpleName(), o.toString());
    }

    /**
     * Modifica que activity es la que ha solicitado una petición.
     *
     * @param result
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * GETTERS Y SETTERS de las variables que queremos mantener
     */
    public User getUserSession() {
        return userSession;
    }

    public void setUserSession(User userSession) {
        this.userSession = userSession;
    }

    public Child getTemp_child() {
        return temp_child;
    }

    public void setTemp_child(Child temp_child) {
        this.temp_child = temp_child;
    }


    @Override
    public List<Child> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Child> children) {
        this.children = children;
        result.Response();
    }

    /**
     * ----------------------------- USER -----------------------------
     * List<Integer> : 1.
     * User : 3.
     * int : 3.
     * List<User> : 1.
     */

    public void getNumberOfChildren(int user_id) {
        userDAO.getNumberOfChildren(String.valueOf(user_id), this);
    }

    /**
     * Valida un usuario al iniciar sesión.
     *
     * @param user
     */
    public void validUser(User user) {
        userDAO.validUSer(user, this); // HABLAR CON LA DE PSP Porque igual esto podria ser/llamar un thread y devolver algo aqui en lugar de ser esto un void y luego tener otro metodo a parte que devuelva el Child encontrado.
    }

    /**
     * Obtiene ids de los niños que tienen relación con el usuario y según el rol.
     *
     * @param user_id
     */
    public void getChildrenByRol(int user_id) {
        childDAO.getChildrenByRol(String.valueOf(user_id), this);
    }

    /**
     * Obtiene los usuarios que tienen relación con un niño.
     *
     * @param child_id
     */
    public void getUsersByChild(int child_id) {
        userDAO.getUsersByChild(String.valueOf(child_id), this);
    }

    /**
     * Buscar un usuario por id.
     *
     * @param user_id
     */
    public void getUserByID(int user_id) {
        userDAO.getUserByID(String.valueOf(user_id), this); // HABLAR CON LA DE PSP Porque igual esto podria ser/llamar un thread y devolver algo aqui en lugar de ser esto un void y luego tener otro metodo a parte que devuelva el Child encontrado.
    }

    public void getLogout() {
        userDAO.getLogout(this);
    }

    public void getUser() {
        userDAO.getUserSession(this);
    }

    public void addUser(User insertUser) {
        userDAO.addUser(insertUser, this);
    }

    public void modifyUser(User modifyUser) {
        userDAO.modifyUser(modifyUser, this);
    }

    public void deleteUser(String username) {
        userDAO.deleteUser(username, this);
    }

    /**
     * ----------------------------- CHILD -----------------------------
     * User : 1.
     * int : 5.
     */
   /* public void getChildByID(int id, int user_id) {
        childDAO.getChildByID(String.valueOf(id), String.valueOf(user_id),this);
    }*/

    public void getChildByID(int child_id) {
        childDAO.getChildByID(String.valueOf(child_id),this);
    }

    public void getOneChildByUser(int user_id) {
        childDAO.getOneChildByUser(String.valueOf(user_id), this);
    }

    public void addChild(Child insertChild, String  username) {
        childDAO.addChild(insertChild, username, this);
    }

    public void addRelationOfUserAndChild(String username, int childId, int rol) {
        childDAO.addRelationOfUserAndChild(username, String.valueOf(childId), String.valueOf(rol), this);
    }

    public void modifyChild(Child modifiedChild) {
        childDAO.modifyChild(modifiedChild, this);
    }

    public void deleteChild(int deleteChild) {
        childDAO.deleteChild(String.valueOf(deleteChild), this);
    }
    public void modifyAwakeAverageOfChild(int modifiedAwakeAverage, int time, int idOfChild) {
        childDAO.modifyAwakeAverageOfChild(String.valueOf(modifiedAwakeAverage), String.valueOf(time), String.valueOf(idOfChild), this);
    }

    public void getTreatmentTime(int child_id) {
        childDAO.getTreatmentTime(String.valueOf(child_id), this);
    }

    /**
     * ----------------------------- TAP -----------------------------
     * List<Tap> : 1.
     * Tap : 1.
     * int : 4.
     */
    public void getLastTapsByChildAndStatus(int child_id) {
        tapDAO.getLastTapsByChildAndStatus(String.valueOf(child_id), this);
    }

    public void getTapsByChildAndStatus(int child_id, int status_id, String today, String yesterday) {
        tapDAO.getTapsByChildAndStatus(String.valueOf(child_id), String.valueOf(status_id), today, yesterday, this);
    }

    public void addTap(Tap insertTap) {
        tapDAO.addTap(insertTap, this);
    }

    public void modifyTap(Tap modifiedTap) {
        tapDAO.modifyTap(modifiedTap, this);
    }

    public void modifyEndDateOfTap(Tap endTap) {
        tapDAO.modifyEndDateOfTap(endTap, this);
    }

    public void modifyEndDateOfEyePatchTap(int tap_id, int awake_tap_id, int child_id) {
        tapDAO.modifyEndDateOfEyePatchTap(String.valueOf(tap_id), String.valueOf(awake_tap_id), String.valueOf(child_id), this);
    }

    public void deleteTap(int deleteTap) {
        tapDAO.deleteTap(String.valueOf(deleteTap), this);
    }

    @Override
    public Object getObject() {
        return this.object;
    }

    @Override
    public List<Tap> getTaps() {
        return taps;
    }

    @Override
    public void setTaps(List<Tap> taps) {
        this.taps = taps;
        result.Response();
    }

    @Override
    public void setObject(Object object) {
        this.object = object;
        result.Response();
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public NegativeResult getOnError() {
        return negativeResult;
    }

    @Override
    public void setOnError(NegativeResult negativeResult) {
        Log.i("MODEL ERROR", "setOnError");
        this.negativeResult = negativeResult;
        result.NegativeResponse();
    }

    public void restart() {
        this.result = null;
        this.temp_child = null;
        this.userSession = null;
    }

}