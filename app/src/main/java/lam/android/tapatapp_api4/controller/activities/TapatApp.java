package lam.android.tapatapp_api4.controller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import lam.android.tapatapp_api4.R;
import lam.android.tapatapp_api4.model.Model;
import lam.android.tapatapp_api4.model.User;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.connection.interfaces.Result;

public class TapatApp extends AppCompatActivity implements Result {
    private Model model;
    private TextView tapatapp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("-----------------------", "-----------------------");
        model = Model.getInstance(getApplicationContext());
        model.setResult(this);

        setContentView(R.layout.activity_tapatapp);
        model.getUser();
        tapatapp = findViewById(R.id.tapatappTV);
    }

    @Override
    public void Response() {
        model.setUserSession((User) model.getObject());
        goToMainActivity();
    }

    @Override
    public void NegativeResponse() {
        NegativeResult negativeResult = model.getOnError();
        if (negativeResult.getCode() == -666 || negativeResult.getCode() == 0) {
            goToUserLogin();
        } else {
            tapatapp.setText("Inténtelo de nuevo más tarde."); // Ni idea de que significa el -666
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

    private void goToMainActivity() {
        Intent intent = new Intent(TapatApp.this, Activity_Main.class);
        startActivity(intent);
        finish();
    }


    private void goToUserLogin() {
        Intent intent = new Intent(TapatApp.this, Activity_UserLogin.class);
        startActivity(intent);
        finish();
    }

}
