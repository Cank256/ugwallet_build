package com.appworld.ugwallet;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.appworld.ugwallet.utils.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

public class AgentHistoryActivty extends AppCompatActivity {
    public static JSONObject clientObject;
    public static String TAG = AgentHistoryActivty.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_history_activty);
        clientObject = PrefManager.getLastLoginUser(this);
        try {
            getSupportActionBar().setTitle("Ug Wallet ("+clientObject.getString("phone")+")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b1 = new Bundle();
        b1.putInt("index",0);
        Bundle b2 = new Bundle();
        b2.putInt("index",1);
        Bundle b3 = new Bundle();
        b3.putInt("index",2);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Wallet", DemoFragment.class,b1)
                .add("Transactions", DemoFragment.class,b2)
                .add("Commission", DemoFragment.class,b3)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);

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
