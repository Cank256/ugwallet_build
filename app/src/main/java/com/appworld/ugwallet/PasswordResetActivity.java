package com.appworld.ugwallet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class PasswordResetActivity extends AppCompatActivity {

    private static final String TAG = PasswordResetActivity.class.getSimpleName();
    @BindView(R.id.submit_btn) Button submit;
    @BindView(R.id.phone_field) EditText phone_field;
    @BindView(R.id.pin) EditText code_field;
    @BindView(R.id.password1) EditText password_field;

    final int GET_RESET_CODE=1;
    final int VERIFY_RESET_CODE=2;
    final int CHANGE_PASSWORD=3;

    int ACTION=GET_RESET_CODE;

    String phone,code,new_password="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Password Reset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.pin_layout).setVisibility(View.GONE);
        findViewById(R.id.password_layout).setVisibility(View.GONE);
        submit.setText("Send Rest Code Email");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone_field.setError(null);
                password_field.setError(null);
                code_field.setError(null);

                boolean cancelSend = false;
                View focusView = null;
                String url = "";
                if (ACTION==GET_RESET_CODE){
                    url = Utils.CLIENT_FORGOT_PASSWORD_URL;
                    // validate phone
                    if ( TextUtils.isEmpty(phone_field.getText().toString().trim()) )
                    {
                        phone_field.setError(getString(R.string.required_field));
                        cancelSend = true;
                        focusView = phone_field;
                    }

                    if ( phone_field.getText().toString().trim().length() < 10 )
                    {
                        phone_field.setError(getString(R.string.invalid_phone));
                        cancelSend = true;
                        focusView = phone_field;
                    }
                    if(!cancelSend){
                        phone = phone_field.getText().toString().trim();
                    }
                }else if(ACTION==VERIFY_RESET_CODE){
                    url = Utils.CLIENT_VERIFY_CODE_URL;

                    code = code_field.getText().toString().trim();
                    if ( TextUtils.isEmpty(code) ) {
                        code_field.setError(getString(R.string.required_field));
                        cancelSend = true;
                        focusView = code_field;
                    }
                    if ( !(code.length()>4) ) {
                        code_field.setError("The code should be 5 digits");
                        cancelSend = true;
                        focusView = code_field;
                    }
                }else if(ACTION == CHANGE_PASSWORD){
                    url = Utils.CLIENT_CHANGE_PASSWORD_URL;
                    new_password = password_field.getText().toString().trim();
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

                }


                if ( cancelSend )
                {
                    //if there are errors in the form, focus on the field with the error
                    focusView.requestFocus();
                }else {
                    //register client.
                    final ProgressDialog progressDialog = Utils.createProgressDialog(PasswordResetActivity.this, "Processing. Please wait ...", false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    try {
                        Bridge.post(url)
                                .body(
                                        new JSONObject()
                                                .put("platform",Utils.PLATFORM_CODE)
                                                .put("phone",phone)
                                                .put("code",code)
                                                .put("password",new_password)
                                ).asString(new ResponseConvertCallback<String>() {
                            @Override
                            public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                progressDialog.dismiss();
                                if(Utils.DEBUG){
                                    Log.d(TAG, "onResponse: "+s);
                                }
                                if(s==null){
                                    Utils.showToastMessage(PasswordResetActivity.this,"Please check your connection and try again");
                                    return;
                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    String title = "",message="";
                                    switch (ACTION){
                                        case GET_RESET_CODE:
                                            title = "Password Reset";
                                            message = "The password reset code has been sent to your email";
                                            break;
                                        case VERIFY_RESET_CODE:
                                            title = "Password Reset";
                                            message = "The password reset code has been verified";
                                            break;
                                        case CHANGE_PASSWORD:
                                            title = "Password Reset Successful";
                                            message = "Your password has been changed Successfully";
                                            break;
                                    }
                                    if(jsonObject.has("status")){
                                        if(ACTION == GET_RESET_CODE){
                                            findViewById(R.id.phone_layout).setVisibility(View.GONE);
                                            findViewById(R.id.pin_layout).setVisibility(View.VISIBLE);
                                            findViewById(R.id.password_layout).setVisibility(View.GONE);
                                            submit.setText("VERIFY CODE");
                                            ACTION=VERIFY_RESET_CODE;
                                        }else  if ((ACTION == VERIFY_RESET_CODE)){
                                            findViewById(R.id.phone_layout).setVisibility(View.GONE);
                                            findViewById(R.id.pin_layout).setVisibility(View.GONE);
                                            findViewById(R.id.password_layout).setVisibility(View.VISIBLE);
                                            submit.setText("CHANGE PASSWORD");
                                            ACTION=CHANGE_PASSWORD;
                                        }else
                                       if(ACTION==CHANGE_PASSWORD){
                                           new AlertDialog.Builder(PasswordResetActivity.this)
                                                   .setTitle(title)
                                                   .setMessage(message)
                                                   .setCancelable(false)
                                                   .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                       @Override
                                                       public void onClick(DialogInterface dialogInterface, int i) {
                                                           dialogInterface.dismiss();
                                                           onBackPressed();
                                                           finish();
                                                       }
                                                   }).show();
                                       }
                                    }else if(jsonObject.has("error")){
                                        new AlertDialog.Builder(PasswordResetActivity.this)
                                                .setTitle("Password Reset Error")
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

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
