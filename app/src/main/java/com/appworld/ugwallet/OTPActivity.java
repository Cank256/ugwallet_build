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

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.appworld.ugwallet.utils.Utils;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class OTPActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String AUTHORIZE_TOPUP_TYPE = "authorize_topup";
    private static final String TAG = OTPActivity.class.getSimpleName();
    EditText pin_field;
    Button verifyBtn;
    String phone, platform, otpType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        getSupportActionBar().setTitle("Verify Phone Number");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pin_field= (EditText) findViewById(R.id.pin);
        verifyBtn = (Button) findViewById(R.id.submit_btn);
        Intent intent = getIntent();
        otpType = intent.getType();
        phone = intent.getStringExtra("phone");
        verifyBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submit_btn:
                pin_field.setError(null);

                boolean cancelSend = false;
                View focusView = null;

                if ( TextUtils.isEmpty(pin_field.getText().toString().trim()) )
                {
                    pin_field.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = pin_field;
                }

                if ( pin_field.getText().toString().trim().length() < 5 )
                {
                    pin_field.setError(getString(R.string.invalid_code));
                    cancelSend = true;
                    focusView = pin_field;
                }

                if ( cancelSend )
                {
                    //if there are errors in the form, focus on the field with the error
                    focusView.requestFocus();
                }else {
                    //register client.
                    final ProgressDialog progressDialog = Utils.createProgressDialog(OTPActivity.this, "Validating Code. Please wait ...", false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    String url = Utils.VALIDATE_OTP_URL;
                    try {
                        JSONObject object =  new JSONObject()
                                .put("platform",Utils.PLATFORM_CODE)
                                .put("phone", phone)
                                .put("code",pin_field.getText().toString().trim());
                        if (otpType.equals(AUTHORIZE_TOPUP_TYPE)){
                            object.put("otp_type", AUTHORIZE_TOPUP_TYPE);
                        }
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
                                    Utils.showToastMessage(OTPActivity.this,"Please check your connection and try again");
                                return;

                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    //if(jsonObject.has("status") && (jsonObject.getString("status").equals("SUCCESS"))){
                                    if(jsonObject.has("status")){
                                        //Intent intent = new Intent();
                                        Intent intent = getIntent();
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }else if(jsonObject.has("error")){
                                        new AlertDialog.Builder(OTPActivity.this)
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
