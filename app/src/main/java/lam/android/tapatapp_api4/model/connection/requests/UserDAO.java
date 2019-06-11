package lam.android.tapatapp_api4.model.connection.requests;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import lam.android.tapatapp_api4.model.connection.MyRequestQueue;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.User;
import lam.android.tapatapp_api4.model.connection.interfaces.OperationResult;

public class UserDAO {
    /**
     * ATRIUBTOS
     */
    private final String url = "https://apps.proven.cat/tapatapp/servlets/user";
    private String request_url;
    private Context context;

    /**
     * CONSTRUCTOR: Inicializa algunos atributos de la clase.
     *
     * @param context
     */
    public UserDAO(Context context) {
        try {
            this.context = context;
        } catch (Exception e) {
            Log.i("ERROR USER CONNECTION", "E " + e.getMessage());
        }
    }

    private void showlog(Object o) {
        Log.i("--" + this.getClass().getSimpleName(), o.toString());
    }

    /**
     * Dependiendo de la cantidad y cuales parametros hay, leera unos u otros.
     *
     * @param user de donde cogerá los valores que serán los futuros parametros.
     * @return un Mapa con la key que es el nombre del campo y el value que es el valor.
     */
    private LinkedHashMap<String, String> setParameters(User user, int option) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();

        switch (option) {
            case 0:
                parametersList.put("username", user.getUsername());
                parametersList.put("password", user.getPassword());
                break;
            case 1:
                parametersList.put("username", user.getUsername());
                parametersList.put("password", user.getPassword());
                parametersList.put("email", user.getEmail());
                break;
            case 2:
                parametersList.put("password", user.getPassword());
                parametersList.put("email", user.getEmail());
                parametersList.put("username", user.getUsername());
                break;
            default:
                break;
        }
        return parametersList;
    }

    /**
     * Cambia el valor de la request_url por su valor inicial.
     */
    public void setRequest_url_Empty() {
        this.request_url = this.url;
    }

    public void setRequest_url(String action, LinkedHashMap<String, String> parametersList) {
        String parameters = formatParameters(parametersList);
        if (!parameters.isEmpty()) {
            this.request_url = this.url + "?action=" + action + parameters;
        } else {
            Log.i("ERROR", "EMPTY PARAMETERS");
        }
    }

    /**
     * Formatea los parametros.
     *
     * @param parametersList la key es el nombre del campo y el value el valor de este.
     * @return una String con las keys y valores concatenados de forma correcta para la url.
     */
    private String formatParameters(LinkedHashMap<String, String> parametersList) {
        String result = "";
        for (String s : parametersList.keySet()) {
            result = result + "&" + s + "=" + parametersList.get(s);
        }
        return result;
    }

    /**
     * Obtiene un objeto User a partir de un JSONObject.
     *
     * @param object el JSONObject obtenido como respuesta del servidor
     * @return el objeto User ya formado a partir de la respuesta.
     */
    public User getUserData(JSONObject object) {
        User user;
        try {
            int id = object.getInt("id");
            String username = object.getString("username");
            String password = object.getString("password");
            String email = object.getString("email");
            user = new User(id, username, password, email);
        } catch (JSONException e) {
            user = null;
        }
        return user;
    }
    // ----------------------------------------------------- GET -----------------------------------------------------

    public void getNumberOfChildren(String user_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("user_id", user_id);
        setRequest_url("number_of_children", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode > 0) {
                        operationResult.setObject(resultCode);
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    NegativeResult error = new NegativeResult(-777);
                    operationResult.setOnError(error);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NegativeResult onErrorResponse = new NegativeResult(-777);
                operationResult.setOnError(onErrorResponse);
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    /**
     * Enviamos una petición al servidor, para que valide un usuario de la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param validUser del usuario a validar
     */
    public void validUSer(User validUser, final OperationResult operationResult) {
       String login = "https://apps.proven.cat/tapatapp/login?username=" + validUser.getUsername() + "&password=" + validUser.getPassword();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, login, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        int user_id = response.getInt("data");
                        operationResult.setObject(user_id);
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    NegativeResult error = new NegativeResult(-777);
                    operationResult.setOnError(error);
                }
            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NegativeResult onErrorResponse = new NegativeResult(-777);
                operationResult.setOnError(onErrorResponse);
            }
        }/*){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "login");
                params.put("username", validUser.getUsername());
                params.put("password", validUser.getPassword());
                return params;
            }*/
        );
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    public void getUserByID(String id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", id);
        setRequest_url("user_by_id", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showlog(response.toString());
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONObject mJsonObject = response.getJSONObject("data");
                        User user = getUserData(mJsonObject);
                        if (user != null) {
                            operationResult.setObject(user);
                        } else {
                            operationResult.setOnError(new NegativeResult(-777));
                        }
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    operationResult.setOnError(new NegativeResult(-777));
                }
            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                operationResult.setOnError(new NegativeResult(-777));
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    public void getUserSession(final OperationResult operationResult) {
        request_url = this.url + "?action=user";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONObject mJsonObject = response.getJSONObject("data");
                        User user = getUserData(mJsonObject);
                        if (user != null) {
                            operationResult.setObject(user);
                        } else {
                            operationResult.setOnError(new NegativeResult(-777));
                        }
                    } else {
                        Log.i("SETO N ERROR", response.toString());
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    operationResult.setOnError(new NegativeResult(-666));
                }
            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("-----ERROR", "getuser"+ error);
                operationResult.setOnError(new NegativeResult(-777));
            }
        });
        Log.i("DAO", "cola");
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    public void getLogout(final OperationResult operationResult) {
        request_url = "https://apps.proven.cat/tapatapp/servlets/logout";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        operationResult.setObject(resultCode);
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    operationResult.setOnError(new NegativeResult(-777));
                }
            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                operationResult.setOnError(new NegativeResult(-777));
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }


    public void getUsersByChild(String child_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("child_id", child_id);
        setRequest_url("users_by_child", parametersList);
        showlog("URL X" + request_url); //TESTER

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                /*try {


                    showlog("RESPONDE USERS " + response.toString());
                    String values = response.getString("data");
                    showlog("VALUES " + values);
                    List<Integer> ids = convertToIntegerArrayList(values);
                    operationResult.setIds(ids);

                } catch (JSONException e) {
                    NegativeResult onErrorResponse = new NegativeResult(-777);
                    operationResult.setOnError(onErrorResponse);
                }*/
            }
        }

                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NegativeResult onErrorResponse = new NegativeResult(-777);
                operationResult.setOnError(onErrorResponse);
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    // ----------------------------------------------------- POST -----------------------------------------------------

    /**
     * Enviamos una petición al servidor, para agregar un usuario a la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param addUser usuario que vamos a insertar.
     */
    public void addUser(User addUser, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(addUser, 1);
        setRequest_url("add_user", parametersList);
        showlog("URL X" + request_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        operationResult.setObject(resultCode);
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    operationResult.setOnError(new NegativeResult(-777));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                operationResult.setOnError(new NegativeResult(-777));
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    /**
     * Enviamos una petición al servidor, para modificar un usuario de la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param modifyUser el usuario modificado (la id no se modifica)
     */
    public void modifyUser(User modifyUser, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(modifyUser, 2);
        setRequest_url("modify_user", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showlog(response.toString());
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        operationResult.setObject(resultCode);
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    showlog(response.toString());
                    operationResult.setOnError(new NegativeResult(-777));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                operationResult.setOnError(new NegativeResult(-777));
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    /**
     * Enviamos una petición al servidor, para eliminar un usuario de la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param username único de cada usuario, que permite identificar al usuario que se desea eliminar
     */
    public void deleteUser(String username, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("username", username);
        setRequest_url("delete_user", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        operationResult.setObject(resultCode);
                    } else {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    }
                } catch (JSONException e) {
                    operationResult.setOnError(new NegativeResult(-777));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                operationResult.setOnError(new NegativeResult(-777));
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

}
