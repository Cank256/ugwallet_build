package com.appworld.ugwallet.adapters;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.appworld.ugwallet.R;
import com.appworld.ugwallet.models.OnLoadMoreListener;
import com.appworld.ugwallet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import static com.appworld.ugwallet.AgentHistoryActivty.TAG;

/**
 * Created by osalia on 1/31/18.
 */

public class PointsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Object> itemData;
    Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    int frag;
    private OnLoadMoreListener onLoadMoreListener;


    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    // data is passed into the constructor
    public PointsAdapter(Context context, ArrayList<Object> itemData, int frag,RecyclerView recyclerView) {

        this.itemData = itemData;
        this.context = context;
        this.frag = frag;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }
    public final static String APP_NAME ="Ug Wallet";

    // inflates the cell layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_ITEM) {
            RecyclerView.ViewHolder vh;
            View view = LayoutInflater.from(context).inflate(R.layout.points_item,null,false);
            return new ViewHolder(view);
        }
        throw new RuntimeException("No match for " + viewType + ".");



    }

    @Override
    public int getItemViewType(int position) {
        return itemData.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }


    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
            return;
        }
        ViewHolder  holder1= (ViewHolder)holder;
        final JSONObject jo = (JSONObject) getItem(position);

        try {
            String timestamp = "";
            if (jo.has("time_stamp"))
                timestamp = jo.getString("time_stamp");
            else
                timestamp = jo.getString("timestamp");
            holder1.date.setText("Date: "+Utils.formart_date(timestamp));
            switch (frag){
                case 2:
                    /*
                    if(
                            jo.getJSONObject("transaction").getString("transaction_type").equals("4")||
                            jo.getJSONObject("transaction").getString("transaction_type").equals("6")
                            ){
                        String product = jo.getJSONObject("transaction").isNull("wallet_purchase_service") ? jo.getJSONObject("transaction").getJSONObject("wallet_purchase_service_price_list_item").getString("name") : jo.getJSONObject("transaction").getJSONObject("wallet_purchase_service").getString("name");

                        String txt = "Topup of "+product
                                +" on A/C ("+jo.getJSONObject("transaction").getString("mm_msisdn")+") of Ugx. "+jo.getJSONObject("transaction")
                                .getString("trans_amount")+"."
                                +". You earned ";
                        if(jo.getJSONObject("transaction").getInt("client")== clientObject.getInt("id")){
                            holder1.points_earned.setText(txt+"UGX. "
                                    +jo.getString("trans_client_points")
                                    +". Your Commission Balance is UGX. "+
                                    jo.getString("trans_client_balance")+
                                    ". Transaction ID: "+jo.getJSONObject("transaction").getString("mm_transaction_id")
                            );
                        }else{
                            JSONObject balance  = null;
                            JSONArray jsonArray = jo.getJSONArray("beneficiary_clients");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if(clientObject.getInt("id")== jsonArray.getJSONObject(i).getInt("client")){
                                    balance = jsonArray.getJSONObject(i);
                                    break;
                                }
                            }
                            holder1.points_earned.setText(
                                    txt+"UGX. "+jo.getString("points_earned_each")
                                            +". Your Commission Balance is UGX. "+
                                            balance.getString("balance")+
                                            ". Transaction ID: "+jo.getJSONObject("transaction").getString("mm_transaction_id"));
                        }

                    }else if(jo.getJSONObject("transaction").getString("transaction_type").equals("7")
                            ||jo.getJSONObject("transaction").getString("transaction_type").equals("8")
                            ){
                        String txt = "Transfer of UGx. "+jo.getJSONObject("transaction")
                                .getString("trans_amount")
                                +" to A/C ("+jo.getJSONObject("transaction").getString("mm_msisdn")+")"
                                +". You earned ";
                        if(jo.getJSONObject("transaction").getInt("client")== clientObject.getInt("id")){
                            holder1.points_earned.setText(txt+"UGX. "
                                    +jo.getString("trans_client_points")
                                    +". Your Commission Balance is UGX. "+
                                    jo.getString("trans_client_balance")+
                                    ". Transaction ID: "+jo.getJSONObject("transaction").getString("mm_transaction_id")
                            );
                        }else{
                            JSONObject balance  = null;
                            JSONArray jsonArray = jo.getJSONArray("beneficiary_clients");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if(clientObject.getInt("id")== jsonArray.getJSONObject(i).getInt("client")){
                                    balance = jsonArray.getJSONObject(i);
                                    break;
                                }
                            }
                            holder1.points_earned.setText(
                                    txt+"UGX. "+jo.getString("points_earned_each")
                                            +". Your Commission Balance is UGX. "+
                                            balance.getString("balance")+
                                            ". Transaction ID: "+jo.getJSONObject("transaction").getString("mm_transaction_id"));
                        }

                    }
                        else {
                        String txt = "Withdraw of UGX. "+jo.getJSONObject("transaction").getString("trans_amount")+". Your Commission Balance is UGX. "+jo.getString("client_balance_after_withdraw")+". "+
                                " Transaction ID: "+jo.getJSONObject("transaction").getString("mm_transaction_id")
                                ;
                        holder1.points_earned.setText(txt);
                    }
                    */
                    String txt2 =jo.getString("transaction_type").replace(" Transaction", "");
                    txt2 += " of Ugx ";
                    txt2 +=jo.getString("amount")
                            +" from ("+jo.getString("paying_phone")+")."
                            +" Your Commission Balance is Ugx. "+jo.getString("running_balance")
                            +". Transaction ID: "+jo.getString("transaction_id")
                    ;
                    holder1.points_earned.setText(txt2);
                    break;
                case 1:
                    /*
                    if(jo.getString("transaction_type").equals("7")
                            ||jo.getString("transaction_type").equals("8")
                            ){
                        String mssid = jo.isNull("mm_msisdn") ? jo.getString("phone") : jo.getString("mm_msisdn");
                        String txt = "Transfer of UGx.  "+jo.getString("trans_amount")
                                +" on A/C ("+mssid+"), "
                                +"Transaction Charge Ugx. "+jo.optInt("trans_charge",0)+
                                ". Your Wallet Balance is Ugx. "+jo.getString("client_balance_after_wallet_sell")
                                +" Transaction ID: "+jo.getString("mm_transaction_id")
                                ;
                        holder1.points_earned.setText(txt);
                    }else{
                        String product = jo.isNull("wallet_purchase_service") ? jo.getJSONObject("wallet_purchase_service_price_list_item").getString("name") : jo.getJSONObject("wallet_purchase_service").getString("name");

                        String txt = "Topup "+product
                                +" on A/C ("+jo.getString("mm_msisdn")+") of Ugx. "+jo.getString("trans_amount")+", "
                                +"Transaction Charge Ugx. "+jo.optInt("mm_tariff_charge",0)+
                                ". Your Wallet Balance is Ugx. "+jo.getString("client_balance_after_wallet_sell")
                                +" Transaction ID: "+jo.getString("mm_transaction_id")
                                ;
                        holder1.points_earned.setText(txt);
                    }
                    */
                    String txt =jo.getString("transaction_type").replace(" Transaction", "");
                    txt += " of Ugx ";
                    txt +=jo.getString("amount")
                            +" using ("+jo.getString("paying_phone")+")."
                            + " Charge: Ugx " + jo.get("transaction_charge") + "."
                            +" Your Wallet Balance is Ugx. "+jo.getString("running_balance")
                            +". Transaction ID: "+jo.getString("transaction_id") +".";
                    if (jo.has("aggregator_reference")) {
                        txt += " Reference: " + jo.getString("aggregator_reference") + ".";
                    }
                    if (jo.has("receipt")) {
                        txt += " Receipt: " + jo.getString("receipt") + ".";
                    }
                    if (jo.has("account_number")) {
                        txt += " Account No.: " + jo.getString("account_number") + ".";
                    }

                    holder1.points_earned.setText(txt);
                    break;
                case 0:
                    /*
                    "transaction_type": "Topup",
            "amount": "2000.00",
            "transaction_charge": "0.00",
            "paying_phone": "0701806449",
            "running_balance": 2912,
            "transaction_id": 2181,
            "timestamp": "2019-01-03T13:24:18Z"

                     */
                    String txt1 ="";
                    /*
                    if(jo.getString("transaction_type").contains("Topup")) {
                        txt1 = "Deposit of Ugx ";
                    } else {
                        txt1 = "Withdrawal of Ugx ";
                    }
                    */
                    txt1 = jo.getString("transaction_type");
                    txt1 += " of Ugx ";
                    txt1 +=jo.getString("amount")
                            +" using ("+jo.getString("paying_phone")+")."
                            + " Charge: Ugx " + jo.get("transaction_charge") + "."
                            +" Your Wallet Balance is Ugx. "+jo.getString("running_balance")
                            +". Transaction ID: "+jo.getString("transaction_id")
                            ;
                    holder1.points_earned.setText(txt1);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "onBindViewHolder: "+e.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView date, points_earned;

        ViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.date);
            points_earned = (TextView) v.findViewById(R.id.points);

        }


    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {


        HeaderViewHolder(View v) {
            super(v);

        }


    }

    // "Loading item" ViewHolder
    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }

    // convenience method for getting data at click position
    Object getItem(int id) {
        return itemData.get(id);
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public int getItemCount() {
        return itemData == null ? 0 : itemData.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

}