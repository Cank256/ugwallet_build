package com.appworld.ugwallet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.appworld.ugwallet.adapters.PointsAdapter;
import com.appworld.ugwallet.utils.Utils;
import com.appworld.ugwallet.widgets.RecyclerViewEmptySupport;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PointsActivity extends AppCompatActivity {

    private static final String TAG = PointsActivity.class.getSimpleName();
    @BindView(R.id.balance_btn) Button balanceBtn;
    @BindView(R.id.withraw_btn) Button withdrawBtn;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerViewEmptySupport recyclerViewEmptySupport;

    ArrayList<Object> transactions = new ArrayList<>();
    JSONObject clientObject;
    PointsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        get client object from parent intent
        try {
            clientObject = new JSONObject(getIntent().getStringExtra("data"));
            getSupportActionBar().setTitle("Account: "+clientObject.getString("phone"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        withdrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    initiateWithdaw();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        adapter = new PointsAdapter(this,transactions, 0,recyclerViewEmptySupport);
        recyclerViewEmptySupport.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEmptySupport.setLayoutManager(layoutManager);

        recyclerViewEmptySupport.setEmptyView(
                findViewById(R.id.empty_list)
        );
        recyclerViewEmptySupport.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactions();
            }
        });
        getTransactions();

    }

    private void initiateWithdaw() throws JSONException {
        View view = getLayoutInflater().inflate(R.layout.withdraw_layout,null,false);
        final EditText amountField = (EditText) view.findViewById(R.id.amount_field);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        Button submit = (Button) view.findViewById(R.id.submit);
        final AlertDialog withdraw_Alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withdraw_Alert.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = amountField.getText().toString().trim();
                amountField.setError(null);
                if(amt.length()==0){
                    amountField.setError(getString(R.string.required_field));
                    amountField.requestFocus();
                    return;
                }
                final int amount = Integer.parseInt(amt);
                if(amount<1500){
                    amountField.setError("Amount less than minimum");
                    amountField.requestFocus();
                    return;
                }
                if(amount>5000000){
                    amountField.setError("Amount greater than maximum (5000000)");
                    amountField.requestFocus();
                    return;
                }
                //request withdraw here


                final ProgressDialog progressDialog = Utils.createProgressDialog(PointsActivity.this, "Loading data... Please Wait", false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                try {
                    Bridge.post(Utils.CLIENT_TRANSACTION_URL)
                            .body(
                                    new JSONObject()
                                            .put("platform",Utils.PLATFORM_CODE)
                                            .put("phone",clientObject.getString("phone"))
                                            .put("amount",amount)
                                            .put("request_type","withdraw")
                            ).asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                            progressDialog.dismiss();
                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if(s==null){
                                Utils.showToastMessage(PointsActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if(jsonObject.has("pk")){
                                    String message ="";
                                    if (jsonObject.getString("mm_status").equals("SUCCESS")){
                                        message = "Your withdraw request of UGX. "+amount+ " was processed successfully."
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }else  if(jsonObject.getString("mm_status").equals("PENDING")){
                                        message = "Your withdraw request of UGX. "+amount+ " is still being processed."
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }else {
                                        message = "Your withdraw request of UGX. "+amount+ " Faild. Please try again or contact support"
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }

                                    new AlertDialog.Builder(PointsActivity.this)
                                            .setTitle("Notification")
                                            .setMessage(message)
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    withdraw_Alert.dismiss();
                                                    getTransactions();
                                                }
                                            })
                                            .show();

                                }else
                                if(jsonObject.has("error")){
                                    new AlertDialog.Builder(PointsActivity.this)
                                            .setTitle("Withdraw Error")
                                            .setMessage(getErrors(jsonObject.getJSONArray("error")))
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                    return;
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }

                        private String getErrors(JSONArray array) throws JSONException {
                            String str="";
                            for (int i = 0; i < array.length(); i++) {
                                str+=array.getString(i)+"\n";
                            }
                            return str;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private void getTransactions() {
        final ProgressDialog progressDialog = Utils.createProgressDialog(PointsActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
//        progressDialog.show();
        refreshLayout.setRefreshing(true);
        try {
            Bridge.post(Utils.CLIENT_TRANSACTION_URL)
                    .body(
                            new JSONObject()
                                    .put("platform",Utils.PLATFORM_CODE)
                                    .put("phone",clientObject.getString("phone"))
                    ).asString(new ResponseConvertCallback<String>() {
                @Override
                public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                    progressDialog.dismiss();
                    refreshLayout.setRefreshing(false);
                    if(Utils.DEBUG){
                        Log.d(TAG, "onResponse: "+s);
                    }
                    if(s==null){
                        Utils.showToastMessage(PointsActivity.this,"Please check your connection and try again");
                        return;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        if(jsonObject.has("error")){
                            new AlertDialog.Builder(PointsActivity.this)
                                    .setTitle("Error")
                                    .setMessage(jsonObject.getString("error"))
                                    .setCancelable(false)
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                            return;
                        }
                        balanceBtn.setText(String.format(getString(R.string.balance),jsonObject.getDouble("balance")));

//                        trasactions Array
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        transactions.clear();
                        /*//add empty object for header
                        transactions.add(new JSONObject());*/
                        for (int i = 0; i < jsonArray.length(); i++) {
                            transactions.add(jsonArray.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
