package lam.android.tapatapp_api4.model.connection;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class MyRequestQueue {
    private static MyRequestQueue instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private MyRequestQueue(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
        MyCookieStore cookieStore = new MyCookieStore(context);
        CookieManager cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    public static synchronized MyRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new MyRequestQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
