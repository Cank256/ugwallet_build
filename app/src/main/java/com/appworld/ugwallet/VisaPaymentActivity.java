package com.appworld.ugwallet;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appworld.ugwallet.utils.Utils;

public class VisaPaymentActivity extends BaseActivity {

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visa_payment);

        //set the action bar title
        Utils.setActionBarTitle(this, getSupportActionBar(), getString(R.string.visa_payment));

        /*get the payment url from the intent extras*/
        Bundle extras = getIntent().getExtras();
        String paymentUrl = extras.getString("payment_url");

        //get the web view layout and load the page inside it
        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(Utils.CUSTOM_USER_AGENT);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearHistory();
        webView.clearCache(true);
        webView.clearFormData();

        pd = new ProgressDialog(VisaPaymentActivity.this);
        pd.setMessage("Loading. Please wait ...");
        pd.show();
        webView.setWebViewClient(new MyWebViewClient());
        webView.addJavascriptInterface(new WebAppInterface(this), "android");
        webView.loadUrl(paymentUrl);

    }

    private class MyWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url)
        {
            webView.loadUrl(url);

            if (!pd.isShowing()) {
                pd.show();
            }
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request)
        {
            Uri uri = request.getUrl();
            webView.loadUrl(uri.toString());

            if (!pd.isShowing()) {
                pd.show();
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    private class WebAppInterface {
        Context context;
        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            context = c;
        }

        /** go back to the payment form. close the web-view and go back to the form */
        @JavascriptInterface
        public void goBackToPaymentForm(boolean success) {
            finish();
            Intent nextTask;
            if ( success )
            {
                nextTask = new Intent(VisaPaymentActivity.this, HistoryActivity.class);
            }
            else
            {
                nextTask = new Intent(VisaPaymentActivity.this, MainActivity.class);
            }
            startActivity(nextTask);
        }
    }

}
