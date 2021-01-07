package com.appworld.ugwallet.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.appworld.ugwallet.R;
import com.appworld.ugwallet.models.Purchase;
import com.appworld.ugwallet.utils.Utils;

import java.util.ArrayList;

/**
 * Created by bernard on 1/20/17.
 * list adapter to allow display of purchase items
 */
public class PurchaseListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Purchase> purchases;

    public PurchaseListAdapter(Context context, ArrayList<Purchase> purchaseArrayList)
    {
        this.context = context;
        this.purchases = purchaseArrayList;
    }

    @Override
    public int getCount() {
        return purchases.size();
    }

    @Override
    public Object getItem(int position) {
        return purchases.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.topup_item_row, null);
        }

        //get the different text views on the item row
        TextView token = (TextView) convertView.findViewById(R.id.token);
        TextView amount = (TextView) convertView.findViewById(R.id.amount);
        TextView chargeRate = (TextView) convertView.findViewById(R.id.charge_rate);
        TextView details = (TextView) convertView.findViewById(R.id.acct_details);
        TextView dateTime = (TextView) convertView.findViewById(R.id.transaction_date);

        //get the purchase object and populate the list item
        Purchase purchase = purchases.get(position);
        String title = "Token: "+purchase.getToken();
        token.setText(title);
        String purchaseDesc = "UGX. "+ Utils.formatCurrency(purchase.getAmount())+ "  Units: "+ purchase.getUnits();
        amount.setText(purchaseDesc);

        chargeRate.setText( purchase.getDescription() );

        details.setText( purchase.getAccount() );

        dateTime.setText( purchase.getDate() );

        return convertView;
    }
    
}
