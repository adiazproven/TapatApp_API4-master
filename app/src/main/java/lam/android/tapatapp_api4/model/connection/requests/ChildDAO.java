package lam.android.tapatapp_api4.model.connection.requests;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.connection.MyRequestQueue;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.interfaces.OperationResult;

public class ChildDAO {
    /**
     * ATRIUBTOS
     */
    private final String url = "https://apps.proven.cat/tapatapp/servlets/child";

    //TODO: Cambiar y comprobar cosas de esta clase.
    private String request_url;
    private Context context;

    /**
     * CONSTRUCTOR: Inicializa algunos atributos de la clase.
     *
     * @param context
     */
    public ChildDAO(Context context) {
        try {
            this.context = context;
        } catch (Exception e) {
            showlog("ERROR CHILD CONNECTION E " + e.getMessage());
        }
    }

    private void showlog(Object o) {
        Log.i("--" + this.getClass().getSimpleName(), o.toString());
    }

    /**
     * Cambia el valor de la request_url por su valor inicial.
     */
    public void setRequest_url_Empty() {
        this.request_url = this.url;
    }

    //TODO

    /**
     * Dependiendo de la cantidad y cuales parametros hay, leera unos u otros.
     *
     * @param child de donde cogerá los valores que serán los futuros parametros.
     * @return un Mapa con la key que es el nombre del campo y el value que es el valor.
     */
    private LinkedHashMap<String, String> setParameters(Child child) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        if (child.getId() != 0) {
            parametersList.put("id", String.valueOf(child.getId()));
        }
        parametersList.put("child_name", child.getName());
        parametersList.put("awake_average", String.valueOf(child.getaverageAwakeADay()));
        parametersList.put("treatment_id", String.valueOf(child.getTreatment_id()));
        parametersList.put("time", String.valueOf(child.getHoras_o_porcentaje()));
        return parametersList;
    }


    public void setRequest_url(String action, LinkedHashMap<String, String> parametersList) {
        String parameters = formatParameters(parametersList);
        if (!parameters.isEmpty()) {
            this.request_url = this.url + "?action=" + action + parameters;
        } else {
            showlog("ERROR EMPTY PARAMETERS");
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
     * Obtiene un objeto Child a partir de un JSONObject.
     *
     * @param object el JSONObject obtenido como respuesta del servidor
     * @return el objeto Child ya formado a partir de la respuesta.
     */
    public Child getChildData(JSONObject object) {
        Child child;
        try {
            int id = object.getInt("id");
            String child_name = object.getString("name");
            int rol_id = object.getInt("rol_id");
            int awake_average = object.getInt("awake_average");
            int treatment_id = object.getInt("treatment_id");
            int time = object.getInt("time");
            int treatment_time_today = object.getInt("treatment_time_today");
            child = new Child(id, child_name, rol_id, awake_average, treatment_id, time, treatment_time_today);
        } catch (JSONException e) {
            child = null;
        }
        return child;
    }
// ----------------------------------------------------- GET -----------------------------------------------------

    /**
     * Enviamos una petición al servidor, para que valide un usuario de la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param child_id del usuario a validar
     */
    public void getTreatmentTime(String child_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", child_id);
        setRequest_url("treatment_time", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showlog(response.toString());
                    int resultCode = response.getInt("resultCode");
                    if (resultCode >= 0 && response.getString("data").contains("hoy")) {
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

    public void getChildByID(String child_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", child_id);
        setRequest_url("child_by_id", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONObject mJsonObject = response.getJSONObject("data");
                        Child child = getChildData(mJsonObject);
                        if (child != null) {
                            operationResult.setObject(child);
                        } else {
                            operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                        }
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

    /*public void getChildByID(String child_id, String user_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", child_id);
        parametersList.put("user_id", user_id);
        setRequest_url("child_by_id", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONObject mJsonObject = response.getJSONObject("data");
                        Child child = getChildData(mJsonObject);
                        if (child != null) {
                            operationResult.setObject(child);
                        } else {
                            operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                        }
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
    }*/

    public void getChildrenByRol(String user_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("user_id", user_id);
        setRequest_url("children_by_rol", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONArray mJsonArray = response.getJSONArray("data");
                        Log.i("ARRAy11",response.toString());
                        List<Child> children = new ArrayList<>();
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject mJsonObject = mJsonArray.getJSONObject(i);
                            Child child = getChildData(mJsonObject);
                            if (child != null) {
                                children.add(child);
                            } else {
                                operationResult.setOnError(new NegativeResult(-777));
                            }
                        }
                        operationResult.setObject(children);
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

    public void getOneChildByUser(String user_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("user_id", user_id);
        setRequest_url("one_child_by_user", parametersList);
        showlog("URL X" + request_url); //TESTER

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONObject mJsonObject = response.getJSONObject("data");
                        Child child = getChildData(mJsonObject);
                        if (child != null) {
                            operationResult.setObject(child);
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

    // ----------------------------------------------------- POST -----------------------------------------------------

    public void addChild(Child insertChild, String username, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(insertChild);
        parametersList.put("username", username);
        setRequest_url("add_child", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("ADDCHILD", response.toString());
                    int resultCode = response.getInt("resultCode");
                    if (resultCode > 0) {
                        JSONObject mjsonObject = response.getJSONObject("data");
                        Child child = getChildData(mjsonObject);
                        if(child != null){
                            operationResult.setObject(child);
                        }else{
                            operationResult.setOnError(new NegativeResult(-777));
                        }

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

    public void modifyChild(Child modifiedChild, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(modifiedChild);
        setRequest_url("modify_child", parametersList);

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

    public void modifyAwakeAverageOfChild(String modifiedAwakeAverage, String time, String idOfChild, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("awake_average", modifiedAwakeAverage);
        parametersList.put("time", time);
        parametersList.put("id", idOfChild);
        setRequest_url("modify_awake_average_of_child", parametersList);

        showlog("URL X" + request_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String resultS = response.getString("resultCode");
                    int result = Integer.valueOf(resultS.replaceAll(" ", ""));
                    showlog("modifyAwakeAverage" + resultS);
                    operationResult.setObject(result);
                } catch (JSONException e) {
                    showlog("ERROR E - " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showlog("ERROR E - " + error.getMessage());
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

    public void deleteChild(String deleteChild, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", deleteChild);
        setRequest_url("delete_child", parametersList);

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

    public void addRelationOfUserAndChild(String username, String childId, String rol, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("username", username);
        parametersList.put("child_id", childId);
        parametersList.put("rol_id", rol);
        setRequest_url("add_relation_of_user_and_child", parametersList);

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
/*
* public void getMissingTreatmentTime(String child_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("child_id", child_id);
        setRequest_url("missing_treatment_time_by_child", parametersList);

        showlog("URL X" + request_url); //TESTER

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String resultS = response.getString("resultCode");
                    int result = Integer.valueOf(resultS.replaceAll(" ", ""));
                    showlog("getMissingTreatmentTime" + resultS);
                    operationResult.setObject(result);
                } catch (JSONException e) {
                    showlog("ERROR E - " + e.getMessage());//TESTER
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showlog("ERROR E - " + error.getMessage());//TESTER
            }
        });
        MyRequestQueue.getInstance(context).addToRequestQueue(request);
        setRequest_url_Empty();
    }

* */