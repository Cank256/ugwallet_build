package com.appworld.ugwallet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.google.firebase.iid.FirebaseInstanceId;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    EditText phone_field,password_field;
    Button signInBtn;
    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("UG Wallet Login");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView session_timeout = (TextView)findViewById(R.id.session_timeout);
        session_timeout.setVisibility(View.GONE);
        boolean timeout = getIntent().getBooleanExtra("session_timeout", false);
        if (timeout == true){
            session_timeout.setVisibility(View.VISIBLE);
        }
        phone_field= (EditText) findViewById(R.id.phone_number_field);
        password_field= (EditText) findViewById(R.id.password1);
        signInBtn = (Button) findViewById(R.id.login_btn);

        phone_field.setText(PrefManager.getLastLoginNumber(this).replaceAll("^(0|256)",""));

        signInBtn.setOnClickListener(this);
        findViewById(R.id.forgot_password_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,PasswordResetActivity.class));
            }
        });
        findViewById(R.id.register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
                finish();
            }
        });

        ccp = findViewById(R.id.ccp);
        //ccp.setCountryPreference("UG");
        //ccp.setCustomMasterCountries("UG");
        ccp.setAutoDetectedCountry(false);
        ccp.setDefaultCountryUsingNameCode("UG");
        ccp.setCountryPreference("UG");
        ccp.setCustomMasterCountries("UG");
        ccp.resetToDefaultCountry();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_btn:
                phone_field.setError(null);
                password_field.setError(null);

                boolean cancelSend = false;
                View focusView = null;



                if ( TextUtils.isEmpty(phone_field.getText().toString().trim()) )
                {
                    phone_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = phone_field;
                }
                /*
                if ( phone_field.getText().toString().trim().length() < 10 )
                {
                    phone_field.setError(getString(R.string.invalid_phone));
                    cancelSend = true;
                    focusView = phone_field;
                }
                */
                if ( !phone_field.getText().toString().matches(Utils.UG_PHONE_REGEX)) {
                    phone_field.setError(getString(R.string.invalid_phone));
                    cancelSend = true;
                    focusView = phone_field;
                }
                String password = password_field.getText().toString().trim();
                if ( TextUtils.isEmpty(password) ) {
                    password_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = password_field;
                }

                if ( cancelSend )
                {
                    //if there are errors in the form, focus on the field with the error
                    focusView.requestFocus();
                }else {
                    //register client.
                    final ProgressDialog progressDialog = Utils.createProgressDialog(LoginActivity.this, "Signing In... Please Wait", false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    try {
                        Bridge.get(Utils.CLIENT_LOGIN_URL)
                                .body(
                                        new JSONObject()
                                                .put("username",phone_field.getText().toString().trim()+"@"+Utils.PLATFORM_CODE)
                                                .put("password",password_field.getText().toString().trim())
                                ).asString(new ResponseConvertCallback<String>() {
                            @Override
                            public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {

                                if(Utils.DEBUG){
                                    Log.d(TAG, "onResponse: "+s);
                                }
                                if(s==null){
                                    progressDialog.dismiss();
                                    Utils.showToastMessage(LoginActivity.this,"Please check your connection and try again");
                                return;
                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    if(jsonObject.has("token")){
//                                        progressDialog.show();


                                        //save last login number
                                        PrefManager.saveLastLoginNumber(LoginActivity.this,jsonObject.getString("phone"));
                                        PrefManager.saveLastLoginUserDetails(LoginActivity.this,jsonObject.toString());
                                        //save fcm device id to server
                                        Bridge.post(Utils.WALLET_DEVICE_REG).
                                                header("Authorization","Token "+jsonObject.getString("token"))
                                                .body(
                                                        new JSONObject()
                                                                .put("type","android")
                                                        .put("device_token", FirebaseInstanceId.getInstance().getToken())
                                                ).asString(new ResponseConvertCallback<String>() {
                                            @Override
                                            public void onResponse(@Nullable Response response, @Nullable String object, @Nullable BridgeException e) {
                                                progressDialog.dismiss();
                                                startActivity(
                                                        new Intent(LoginActivity.this,AgentHomeActivity.class)

                                                );
                                                finish();
                                            }
                                        });

                                    }else if(jsonObject.has("detail")){
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(LoginActivity.this)
                                                .setTitle("Login Error")
                                                .setMessage(jsonObject.getString("detail"))
                                                .setCancelable(false)
                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).show();
                                    }
                                    else if(jsonObject.has("non_field_errors")){
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(LoginActivity.this)
                                                .setTitle("Login Error")
                                                .setMessage(jsonObject.getJSONArray("non_field_errors").getString(0))
                                                .setCancelable(false)
                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).show();
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}