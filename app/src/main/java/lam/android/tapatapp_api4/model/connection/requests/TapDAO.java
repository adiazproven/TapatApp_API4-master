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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.MyTimeStamp;
import lam.android.tapatapp_api4.model.Tap;
import lam.android.tapatapp_api4.model.connection.MyRequestQueue;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.interfaces.OperationResult;

public class TapDAO {
    /**
     * ATRIUBTOS
     */
    private final String url = "https://apps.proven.cat/tapatapp/servlets/tap";
    private String request_url;
    private Context context;
    /**
     * CONSTRUCTOR: Inicializa algunos atributos de la clase.
     *
     * @param context
     */
    public TapDAO(Context context) {
        try {
            this.context = context;
        } catch (Exception e) {
            showlog("ERROR TAP CONNECTION: " + "E " + e.getMessage());
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

    /**
     * Dependiendo de la cantidad y cuales parametros hay, leera unos u otros.
     *
     * @param tap de donde cogerá los valores que serán los futuros parametros.
     * @return un Mapa con la key que es el nombre del campo y el value que es el valor.
     */
    private LinkedHashMap<String, String> setParameters(Tap tap) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        if (tap.getId() > 0) {
            parametersList.put("id", String.valueOf(tap.getId()));
        }
        if (tap.getChild_id() != 0) {
            parametersList.put("child_id", String.valueOf(tap.getChild_id()));
        }
        if (tap.getStatus_id() != 0) {
            parametersList.put("status_id", String.valueOf(tap.getStatus_id()));
        }
        if (tap.getInit_date() != null) {
            parametersList.put("initDate", tap.getInit_date().toString());
        }
        if (tap.getEnd_date() != null) {
            parametersList.put("endDate", tap.getEnd_date().toString());
        } else {
            parametersList.put("endDate", null);
        }
        return parametersList;
    }

    /**
     * Cambia el valor de request_url.
     *
     * @param action         añade como parametro "accion" y su valor en la url
     * @param parametersList tras formatearlos los añade com parametros de la url
     */
    public void setRequest_url(String action, HashMap<String, String> parametersList) {
        String parameters = formatParameters(parametersList);
        if (!parameters.isEmpty()) {
            this.request_url = this.url + "?action=" + action + parameters;
        } else {
            showlog("ERROR: " + "EMPTY PARAMETERS");
        }
    }

    /**
     * Formatea los parametros.
     *
     * @param parametersList la key es el nombre del campo y el value el valor de este.
     * @return una String con las keys y valores concatenados de forma correcta para la url.
     */
    private String formatParameters(HashMap<String, String> parametersList) {
        String result = "";
        for (String s : parametersList.keySet()) {
            result = result + "&" + s + "=" + parametersList.get(s);
        }
        return result;
    }

    private Tap getTap(JSONObject o) {
        Tap tap = null;
        try {
            int id = o.getInt("id");
            int child_id = o.getInt("child_id");
            int status_id = o.getInt("status_id");
            JSONObject object = o.getJSONObject("initDate");
            MyTimeStamp init_date = convertJSONtoMyTimeStamp(object);
            tap = new Tap(id, child_id, status_id, init_date, null);
            JSONObject object1 = o.getJSONObject("endDate");
            MyTimeStamp end_date = convertJSONtoMyTimeStamp(object1);
            tap.setEnd_date(end_date);
        } catch (JSONException e) {
            if (tap != null) {
                tap.setEnd_date(null);
            }
        }
        showlog("getTap " + tap.toString());
        return tap;

    }

    private MyTimeStamp convertJSONtoMyTimeStamp(JSONObject object) {
        MyTimeStamp mts = null;

        try {
            JSONObject jsonDate = object.getJSONObject("date");
            int year = jsonDate.getInt("year");
            int month = jsonDate.getInt("month");
            month--;
            int day = jsonDate.getInt("day");
            JSONObject jsonTime = object.getJSONObject("time");
            int hour = jsonTime.getInt("hour");
            int minute = jsonTime.getInt("minute");
            int second = jsonTime.getInt("second");

            mts = new MyTimeStamp(year, month, day, hour, minute, second);

        } catch (JSONException e) {
            mts = null;
        }
        return mts;
    }


    // ----------------------------------------------------- GET -----------------------------------------------------

    public void getTapsByChildAndStatus(String child_id, String status_id, String today, String yesterday, final OperationResult operationResult) {
        HashMap<String, String> parametersList = new HashMap<>();
        parametersList.put("child_id", child_id);
        parametersList.put("status_id", status_id);
        parametersList.put("today", today);
        parametersList.put("yesterday", yesterday);
        setRequest_url("taps_by_child_and_status", parametersList);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONArray mJsonArray = response.getJSONArray("data");
                        List<Tap> taps = new ArrayList<>();
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject mJsonObject = mJsonArray.getJSONObject(i);
                            Tap tap = getTap(mJsonObject);
                            if (tap != null) {
                                taps.add(tap);
                            } else {
                                operationResult.setOnError(new NegativeResult(-777));
                            }
                        }
                        operationResult.setTaps(taps);
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

    // ----------------------------------------------------- POST -----------------------------------------------------

    /**
     * Enviamos una petición al servidor, para agregar un usuario a la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param addTap usuario que vamos a insertar.
     */
    public void addTap(Tap addTap, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(addTap);
        setRequest_url("add_tap", parametersList);
        showlog("URL: " + "X" + request_url);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int resultCode = response.getInt("resultCode");
                    Log.i("TAP DAO", response.toString());
                    if (resultCode > 0) {
                        JSONObject mJsonObject = response.getJSONObject("data");
                        Tap tap = getTap(mJsonObject);
                        if (tap != null) {
                            showlog("enmo"+tap.toString());
                            operationResult.setObject(tap);
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
     * Enviamos una petición al servidor, para agregar un usuario a la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param modifiedTap usuario que vamos a insertar.
     */
    public void modifyTap(Tap modifiedTap, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(modifiedTap);
        setRequest_url("modify_tap", parametersList);

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
     * Enviamos una petición al servidor, para agregar un usuario a la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param endTap usuario que vamos a insertar.
     */
    public void modifyEndDateOfTap(Tap endTap, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = setParameters(endTap);
        setRequest_url("modify_enddate_of_tap", parametersList);
        showlog("URL: " + "X" + request_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showlog("modify1");
                    int resultCode = response.getInt("resultCode");
                    if (resultCode >= 0 && response.getString("data").contains("tiempo")) { //Get TTT (podría ser 0 si ha finalizado)
                        operationResult.setObject(resultCode);
                    } else if (resultCode == 1) {
                        showlog("resultcodeEnmo" + resultCode);
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
     * Enviamos una petición al servidor, para agregar un usuario a la base de datos, esperamos respuesta.
     * Al obtenerla, activamos el listener de la clase desde donde se ha llamado este método, cambiamos el valor
     * de responseresult.
     *
     * @param id_tap usuario que vamos a insertar.
     */
    public void deleteTap(String id_tap, final OperationResult operationResult) {
        //LinkedHashMap<String, String> parametersList = setParameters(new Tap(id_tap));
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", id_tap);
        setRequest_url("delete_tap", parametersList);
        showlog("URL: " + "X" + request_url);

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

    public void getLastTapsByChildAndStatus(String child_id, final OperationResult operationResult) {
        HashMap<String, String> parametersList = new HashMap<>();
        parametersList.put("child_id", child_id);
        setRequest_url("last_taps_by_child_and_status", parametersList);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showlog("RESPONSE "+ response.toString());
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 1) {
                        JSONArray mJsonArray = response.getJSONArray("data");
                        List<Tap> taps = new ArrayList<>();
                        for (int i = 0; i < mJsonArray.length(); i++) {
                            JSONObject mJsonObject = mJsonArray.getJSONObject(i);
                            Tap tap = getTap(mJsonObject);
                            if (tap != null) {
                                taps.add(tap);
                            } else {
                                operationResult.setOnError(new NegativeResult(-777));
                            }
                        }
                        operationResult.setTaps(taps);
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

    public void modifyEndDateOfEyePatchTap(String tap_id, String awake_tap_id, String child_id, final OperationResult operationResult) {
        LinkedHashMap<String, String> parametersList = new LinkedHashMap<>();
        parametersList.put("id", tap_id);
        parametersList.put("awake_tap_id", awake_tap_id);
        parametersList.put("child_id", child_id);
        setRequest_url("modify_enddate_of_eyepatch_tap", parametersList);
        showlog("URL: " + "X" + request_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, request_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    showlog("response.to " + response.toString());
                    int resultCode = response.getInt("resultCode");
                    if (resultCode == 0 && response.getString("data").contains("no ha finalizado")) {
                        operationResult.setOnError(new NegativeResult(resultCode, response.getString("data")));
                    } else if (resultCode >= 0) { //Get TTT (podría ser 0 si ha finalizado)
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
