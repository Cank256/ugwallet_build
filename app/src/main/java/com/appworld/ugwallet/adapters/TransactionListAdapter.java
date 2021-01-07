package com.appworld.ugwallet.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.appworld.ugwallet.R;
import com.appworld.ugwallet.models.Transaction;
import com.appworld.ugwallet.utils.Utils;

import java.util.ArrayList;

/**
 * Created by bernard on 1/3/17.
 * list adapter to allow display of transaction items
 */
public class TransactionListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Transaction> transactions;

    public TransactionListAdapter(Context context, ArrayList<Transaction> transactionArrayList)
    {
        this.context = context;
        this.transactions = transactionArrayList;
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public Object getItem(int position) {
        return transactions.get(position);
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
            convertView = mInflater.inflate(R.layout.transaction_item_row, null);
        }

        //get the different text views on the item row
        TextView transactionTitle = (TextView) convertView.findViewById(R.id.transaction_title);
        TextView description = (TextView) convertView.findViewById(R.id.transaction_description);
        TextView dateTime = (TextView) convertView.findViewById(R.id.date_time);

        //get the transaction object and populate the list item
        Transaction transaction = transactions.get(position);
        String title = "Account No: "+transaction.getAccountNo();
        transactionTitle.setText(title);
        String transactionDesc = transaction.getProductName()+ " at UGX. "+ Utils.formatCurrency(transaction.getAmount())+" Using "+transaction.getProvider()+" ("+transaction.getPhone()+")";
        description.setText(transactionDesc);

        dateTime.setText( transaction.getDate() );

        return convertView;
    }
    
}
