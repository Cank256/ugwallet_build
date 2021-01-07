package com.appworld.ugwallet.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.appworld.ugwallet.R;
import com.appworld.ugwallet.models.PhoneNumber;
import com.appworld.ugwallet.utils.PrefManager;

import java.util.ArrayList;

/**
 * Created by bernard on 1/2/17.
 * adapter class to enable rendering of contact items on the list
 */
public class ContactListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<PhoneNumber> phoneNumbers;

    public ContactListAdapter(Context context, ArrayList<PhoneNumber> phoneNumberArrayList)
    {
        this.context = context;
        this.phoneNumbers = phoneNumberArrayList;
    }

    @Override
    public int getCount() {
        return phoneNumbers.size();
    }

    @Override
    public Object getItem(int position) {
        return phoneNumbers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.contact_item_row, null);
        }

        //Handle TextView and display string from your list
        TextView itemTitle = (TextView) convertView.findViewById(R.id.list_item_title);
        final PhoneNumber phoneNumber = phoneNumbers.get(position);
        String contactDetails = phoneNumber.getName()+" - "+phoneNumber.getNumber();
        itemTitle.setText(contactDetails);

        //handle the button and set click listener
        Button deleteBtn = (Button) convertView.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //determine whether the user really wants to delete the contact
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder
                        .setTitle(context.getString(R.string.confirm_delete))
                        .setMessage(context.getString(R.string.confirm_delete_msg))
                        .setCancelable(false)
                        .setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        //delete phone number from list and from shared prefs
                                        PrefManager prefManager = new PrefManager(context);
                                        prefManager.deletePhoneNumber(position);
                                        phoneNumbers.remove(position);
                                        notifyDataSetChanged();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                builder.create().show();

            }
        });

        return convertView;
    }

}
