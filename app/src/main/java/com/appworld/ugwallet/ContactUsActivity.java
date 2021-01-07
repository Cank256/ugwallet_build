package com.appworld.ugwallet;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.appworld.ugwallet.utils.Utils;

public class ContactUsActivity extends BaseActivity {

    private RadioGroup feedbackTypesRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        //set the action bar title
        Utils.setActionBarTitle(this, getSupportActionBar(), getString(R.string.contact_us));

        feedbackTypesRadioGroup = (RadioGroup) findViewById(R.id.feedback_types_radio_group);
        Button writeEmailButton = (Button) findViewById(R.id.write_email_button);
        writeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedbackEmail(getSelectedFeedbackType());
            }
        });
        findViewById(R.id.terms_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactUsActivity.this,TermsActivity.class));
            }
        });
    }

    private void sendFeedbackEmail(String feedbackType) {
        Resources resources = this.getResources();
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { resources
                .getText(R.string.fdb_support_email).toString() });
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, feedbackType);
        intentEmail.putExtra(Intent.EXTRA_TEXT,
                (java.io.Serializable) Utils.getDeviseInfoForFeedback(this));
        intentEmail.setType(Utils.TYPE_OF_EMAIL);
        this.startActivity(Intent.createChooser(intentEmail,
                resources.getText(R.string.fdb_choose_email_provider)));
    }

    private String getSelectedFeedbackType() {
        int radioButtonID = feedbackTypesRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) feedbackTypesRadioGroup
                .findViewById(radioButtonID);
        return radioButton.getText().toString();
    }
}
