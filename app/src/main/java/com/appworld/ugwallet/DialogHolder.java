package com.appworld.ugwallet;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;
import com.hbb20.CountryCodePicker;

public class DialogHolder {
    public View view;
    public EditText amountField;
    public EditText accountField;
    public Button cancel;
    public TextView titleTextView;
    public TextView providerTextView;
    public TextView phone_no_TextView;
    public TextView account_no_TextView;
    public Button submit;
    public Button pickSavedNo;
    public Button saveNo;
    Button pickSavedPhone;
    Button savePhone;
    View amount_layout;
    View details_spinner_layout;
    View provider_spinner_layout;
    View account_number_layout;
    AutoCompleteTextView phone;
    Spinner providerSpinner;
    Spinner packageSpinner;
    CountryCodePicker ccp;

    String product_pref = "";
    String product_title = "";

    Context context;

    DialogHolder(Context _context, String product) {
        context = _context;
        bindFields();
        setupEventListeners();
        setVisibility(product);
    }

    void bindFields() {
        view = ((Activity)context).getLayoutInflater().inflate(R.layout.airtime_sale_layout,null,false);
        amountField = (EditText) view.findViewById(R.id.amount_field);
        accountField = (EditText) view.findViewById(R.id.account_number_field);
        cancel = (Button) view.findViewById(R.id.cancel);
        titleTextView = (TextView) view.findViewById(R.id.title);
        providerTextView = (TextView) view.findViewById(R.id.provider_text);
        phone_no_TextView = (TextView) view.findViewById(R.id.phone_no_text);
        account_no_TextView = (TextView) view.findViewById(R.id.txt_account_number);
        submit = (Button) view.findViewById(R.id.submit);
        pickSavedNo = (Button) view.findViewById(R.id.pick_saved_no_btn);
        saveNo = (Button) view.findViewById(R.id.save_no_btn);
        amount_layout = view.findViewById(R.id.amount_layout);
        details_spinner_layout = view.findViewById(R.id.details_spinner_layout);
        provider_spinner_layout = view.findViewById(R.id.provider_spinner_layout);
        account_number_layout = view.findViewById(R.id.account_layout);
        phone = (AutoCompleteTextView) view.findViewById(R.id.phone_number_field);
        providerSpinner = (Spinner) view.findViewById(R.id.provider_spinner);
        packageSpinner = (Spinner) view.findViewById(R.id.package_spinner);
        pickSavedPhone = (Button) view.findViewById(R.id.pick_saved_phone_btn);
        savePhone = (Button) view.findViewById(R.id.save_phone_btn);
        ccp = (CountryCodePicker)view.findViewById(R.id.ccp);
        //ccp.setCountryPreference("UG");
        //ccp.setCustomMasterCountries("UG");
        ccp.setAutoDetectedCountry(false);
        ccp.setDefaultCountryUsingNameCode("UG");
        ccp.setCountryPreference("UG");
        ccp.setCustomMasterCountries("UG");
        ccp.resetToDefaultCountry();

    }

    void setupEventListeners() {
        pickSavedNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.populateContactList(context, accountField);
            }
        });
        saveNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountField.setError(null);
                String meter_number = accountField.getText().toString().trim();
                if( TextUtils.isEmpty(meter_number) )
                {
                    accountField.setError(context.getString(R.string.missing_number));
                    accountField.requestFocus();
                }
                else
                {
                    Utils.savePhoneNumber(context, meter_number.replaceAll("\\D+",""));
                }
            }
        });


        pickSavedPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.populateContactList(context, phone);
            }
        });
        savePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone.setError(null);
                String phone_number = phone.getText().toString().trim();
                if( TextUtils.isEmpty(phone_number) )
                {
                    phone.setError(context.getString(R.string.missing_phone));
                    phone.requestFocus();
                }
                else
                {
                    Utils.savePhoneNumber(context, phone_number.replaceAll("\\D+",""));
                }
            }
        });
    }

    void setVisibility(String product) {
        switch (product){
            case Utils.PRODUCT_AIRTIME:
                product_pref = PrefManager.PREF_SERVICES_AIRTIME;
                product_title = "Airtime Topup";
                details_spinner_layout.setVisibility(View.GONE);
                amount_layout.setVisibility(View.VISIBLE);
                break;
            case Utils.PRODUCT_INTERNET:
                product_pref = PrefManager.PREF_SERVICES_INTERNET;
                product_title = "Internet Data Topup";
                details_spinner_layout.setVisibility(View.VISIBLE);
                amount_layout.setVisibility(View.GONE);
                break;
            case Utils.PRODUCT_SENDMONEY:
                product_pref = "";
                product_title = "Send Money";
                details_spinner_layout.setVisibility(View.GONE);
                amount_layout.setVisibility(View.VISIBLE);
                providerTextView.setText("Select Transfer Option");
                break;
            case Utils.PRODUCT_TV:
                product_pref = PrefManager.PREF_SERVICES_TV;
                product_title = "PayTV Topup";
                details_spinner_layout.setVisibility(View.VISIBLE);
                amount_layout.setVisibility(View.GONE);
                account_no_TextView.setText("Enter Account No.");
                account_number_layout.setVisibility(View.VISIBLE);
                break;
            case Utils.PRODUCT_YAKA:
                product_pref = PrefManager.PREF_SERVICES_YAKA;
                product_title = "Yaka Topup";
                account_number_layout.setVisibility(View.VISIBLE);
                details_spinner_layout.setVisibility(View.GONE);
                provider_spinner_layout.setVisibility(View.GONE);
                account_no_TextView.setText("Enter Yaka Meter Number");
                phone_no_TextView.setText("Enter Phone Eg. 0752200600 (For Token)");
                amount_layout.setVisibility(View.VISIBLE);
                break;
            case Utils.PRODUCT_WATER:
                product_pref = PrefManager.PREF_SERVICES_WATER;
                product_title = "NWSC Water Topup";
                providerTextView.setText("Select Area");
                account_number_layout.setVisibility(View.VISIBLE);
                details_spinner_layout.setVisibility(View.GONE);
                provider_spinner_layout.setVisibility(View.VISIBLE);
                account_no_TextView.setText("Enter NWSC Meter Number");
                phone_no_TextView.setText("Enter Phone Eg. 0752200600 (For NWSC Receipt)");
                amount_layout.setVisibility(View.VISIBLE);
                break;
        }
        titleTextView.setText(product_title);
    }
}
