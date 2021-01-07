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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.appworld.ugwallet.utils.MaskWatcher;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;
import com.google.android.gms.common.api.internal.LifecycleActivity;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {
    private static int VERIFY_PHONE_REQUEST_CODE = 22;
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    EditText phone_field, referrer_phone_field, password_field, confirm_password_field, otp_field;
    AutoCompleteTextView email_field;
    private CountryCodePicker ccp, referrer_ccp;
    Button registerBtn, verifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setTitle("Create UG Wallet Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        phone_field= (EditText) findViewById(R.id.phone_number_field);
        referrer_phone_field = (EditText)findViewById(R.id.referrer_phone_number);
        //phone_field.addTextChangedListener(new MaskWatcher("256#########"));
        password_field= (EditText) findViewById(R.id.password1);
        confirm_password_field= (EditText) findViewById(R.id.confirm_password);
        otp_field= (EditText) findViewById(R.id.otp);
        email_field = (AutoCompleteTextView) findViewById(R.id.email_field);
        ccp = findViewById(R.id.ccp);
        referrer_ccp = findViewById(R.id.referrer_ccp);
        //ccp.setCountryPreference("UG");
        //ccp.setCustomMasterCountries("UG");
        ccp.setAutoDetectedCountry(false);
        ccp.setDefaultCountryUsingNameCode("UG");
        ccp.setCountryPreference("UG");
        ccp.setCustomMasterCountries("UG");
        ccp.resetToDefaultCountry();

        referrer_ccp.setAutoDetectedCountry(false);
        referrer_ccp.setDefaultCountryUsingNameCode("UG");
        referrer_ccp.setCountryPreference("UG");
        referrer_ccp.setCustomMasterCountries("UG");
        referrer_ccp.resetToDefaultCountry();

        registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);
        verifyBtn = (Button) findViewById(R.id.verify_btn);
        verifyBtn.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        phone_field.setText(PrefManager.getLastRegistrationNumber(this).replaceAll("^(0|256)",""));
        email_field.setText(PrefManager.getLastRegistrationEmail(this));
        String pin = PrefManager.getLastRegistrationPin(this);
        password_field.setText(pin);
        //confirm_password_field.setText(pin);
    }

    void showRegistrationLayout(){
        LinearLayout llRegister = (LinearLayout)findViewById(R.id.registration_layout);
        llRegister.setVisibility(View.VISIBLE);
        LinearLayout llVerify = (LinearLayout) findViewById(R.id.verification_layout);
        llVerify.setVisibility(View.GONE);
    }

    void showVerificationLayout(){
        LinearLayout llRegister = (LinearLayout)findViewById(R.id.registration_layout);
        llRegister.setVisibility(View.GONE);
        LinearLayout llVerify = (LinearLayout) findViewById(R.id.verification_layout);
        llVerify.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        boolean cancelSend = false;
        View focusView = null;
        switch (view.getId()){
            case R.id.register_btn:
                phone_field.setError(null);
                email_field.setError(null);
                password_field.setError(null);
                confirm_password_field.setError(null);



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

                String referrer = referrer_phone_field.getText().toString().trim();
                if ( !TextUtils.isEmpty(referrer) && !referrer.matches(Utils.UG_PHONE_REGEX)) {
                        referrer_phone_field.setError(getString(R.string.invalid_phone));
                        cancelSend = true;
                        focusView = referrer_phone_field;
                }

                String email = email_field.getText().toString().trim();
                if ( !TextUtils.isEmpty(email) )
                {
                    if ( !email.matches(Utils.EMAIL_REGEX) )
                    {
                       email_field.setError(getString(R.string.invalid_email));
                        cancelSend = true;
                        focusView = email_field;
                    }
                }/*else {
                    email_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = email_field;
                }*/

                if ( TextUtils.isEmpty(password_field.getText().toString().trim()) )
                {
                    password_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = password_field;
                }
                if ( !(password_field.getText().toString().trim().length()>4) )
                {
                    password_field.setError("Password is too short");
                    cancelSend = true;
                    focusView = password_field;
                }

                if ( TextUtils.isEmpty(confirm_password_field.getText().toString().trim()) )
                {
                    confirm_password_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = confirm_password_field;
                }
                String password = password_field.getText().toString().trim();
                if ( !confirm_password_field.getText().toString().equals(password) )
                {
                    password_field.setError("Password and confirmation do not match");
                    cancelSend = true;
                    focusView = password_field;
                }

                if ( cancelSend )
                {
                    //if there are errors in the form, focus on the field with the error
                    focusView.requestFocus();
                }else {
                    //register client.
                    final ProgressDialog progressDialog = Utils.createProgressDialog(RegistrationActivity.this, "Creating Account. Please wait ...", false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    try {
                        Bridge.post(Utils.REGISTRATION_URL)
                                .body(
                                        new JSONObject()
                                        .put("platform",Utils.PLATFORM_CODE)
                                        .put("phone",phone_field.getText().toString().trim())
                                        .put("email",email_field.getText().toString().trim())
                                        .put("password",password_field.getText().toString().trim())
                                        .put("referrer", referrer_phone_field.getText().toString().trim())
                                ).asString(new ResponseConvertCallback<String>() {
                            @Override
                            public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                progressDialog.dismiss();
                                if(Utils.DEBUG){
                                    Log.d(TAG, "onResponse: "+s);
                                }
                                if(s==null){
                                    Utils.showToastMessage(RegistrationActivity.this,"Please check your connection and try again");
                                return;

                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    if(jsonObject.has("pk")){
                                        PrefManager.saveLastRegistrationNumber(RegistrationActivity.this, phone_field.getText().toString().trim());
                                        PrefManager.saveLastRegistrationEmail(RegistrationActivity.this, email_field.getText().toString().trim());
                                        PrefManager.saveLastRegistrationPin(RegistrationActivity.this, password_field.getText().toString().trim());

                                        showVerificationLayout();
                                        /*
                                        Intent intent = new Intent(RegistrationActivity.this, OTPActivity.class);
                                        intent.putExtra("phone", phone_field.getText().toString().trim());
                                        intent.setType("verify_phone");
                                        startActivityForResult(intent, VERIFY_PHONE_REQUEST_CODE);
                                        */

                                        /*
                                        new AlertDialog.Builder(RegistrationActivity.this)
                                                .setTitle("Success")
                                                .setMessage("UG Wallet Account Created Successfully.\nPlease Login to get started")
                                                .setCancelable(false)
                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                        onBackPressed();
                                                        startActivity(
                                                                new Intent(RegistrationActivity.this,LoginActivity.class)
                                                        );
                                                        finish();
                                                    }
                                                }).show();
                                                */
                                    }else if(jsonObject.has("error")){
                                        new AlertDialog.Builder(RegistrationActivity.this)
                                                .setTitle("Registration Error")
                                                .setMessage(jsonObject.getString("error"))
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

            case R.id.verify_btn:
                otp_field.setError(null);


                if ( TextUtils.isEmpty(otp_field.getText().toString().trim()) )
                {
                    otp_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = otp_field;
                }

                if ( otp_field.getText().toString().trim().length() < 5 )
                {
                    otp_field.setError(getString(R.string.invalid_code));
                    cancelSend = true;
                    focusView = otp_field;
                }

                if ( cancelSend )
                {
                    //if there are errors in the form, focus on the field with the error
                    focusView.requestFocus();
                }else {
                    //register client.
                    final ProgressDialog progressDialog = Utils.createProgressDialog(RegistrationActivity.this, "Validating Code. Please wait ...", false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    String url = Utils.VALIDATE_OTP_URL;
                    try {
                        JSONObject object =  new JSONObject()
                                .put("platform",Utils.PLATFORM_CODE)
                                .put("phone", phone_field.getText().toString().trim())
                                .put("code",otp_field.getText().toString().trim());
                        Bridge.post(url)
                                .body(
                                        object
                                ).asString(new ResponseConvertCallback<String>() {
                            @Override
                            public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                progressDialog.dismiss();
                                if(Utils.DEBUG){
                                    Log.d(TAG, "onResponse: "+s);
                                }
                                if(s==null){
                                    Utils.showToastMessage(RegistrationActivity.this,"Please check your connection and try again");
                                    return;

                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    //if(jsonObject.has("status") && (jsonObject.getString("status").equals("SUCCESS"))){
                                    if(jsonObject.has("status")){
                                        PrefManager.scrubRegistrationDetails(RegistrationActivity.this);
                                        new AlertDialog.Builder(RegistrationActivity.this)
                                                .setTitle("Success")
                                                .setMessage("UG Wallet Account Created Successfully.\nPlease Login to get started")
                                                .setCancelable(false)
                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                        onBackPressed();
                                                        startActivity(
                                                                new Intent(RegistrationActivity.this,LoginActivity.class)
                                                        );
                                                        finish();
                                                    }
                                                }).show();

                                    }else if(jsonObject.has("error")){
                                        new AlertDialog.Builder(RegistrationActivity.this)
                                                .setTitle("Registration Error")
                                                .setMessage(jsonObject.getString("error"))
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == VERIFY_PHONE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
                PrefManager.scrubRegistrationDetails(RegistrationActivity.this);
                new AlertDialog.Builder(RegistrationActivity.this)
                        .setTitle("Success")
                        .setMessage("UG Wallet Account Created Successfully.\nPlease Login to get started")
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                onBackPressed();
                                startActivity(
                                        new Intent(RegistrationActivity.this,LoginActivity.class)
                                );
                                finish();
                            }
                        }).show();
            }
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
