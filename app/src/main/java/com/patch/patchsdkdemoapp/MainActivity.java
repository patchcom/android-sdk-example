package com.patch.patchsdkdemoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.patch.patchcalling.interfaces.PatchResponse;
import com.patch.patchcalling.javaclasses.PatchApiCall;
import com.patch.patchcalling.retrofitresponse.createcontact.Cli;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText et_name, et_cc, et_phone, et_cuid;
    private Button bt_init;
    private static final String accountId = "5c90e72199dcfd64e18c206c";
    private static final String apikey = "testkey";

    List<Cli> verifiedNumberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //getting list of authorised/verified numbers (i.e. list of clis)
        verifiedNumberList = getVerifiedNumbers();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String accId = sharedPreferences.getString("appAccountID", null);
        String api = sharedPreferences.getString("appApikey", null);
        String name = sharedPreferences.getString("appName", null);
        String cc = sharedPreferences.getString("appCC", null);
        String appPhone = sharedPreferences.getString("appPhone", null);
        String cuid = sharedPreferences.getString("cuid", "");
        Boolean isLogin = sharedPreferences.getBoolean("Login", false);
        if (isLogin) {
            JSONObject options = new JSONObject();
            try {
                options.put("accountID", accId);
                options.put("apikey",api);
                options.put("cc", cc);
                options.put("phone", appPhone);
                options.put("name", name);
                options.put("picture", "");
                options.put("ringtone", "https://s3.ap-south-1.amazonaws.com/sdkassets/outgoing-tone.mp3");
                options.put("cuid",cuid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PatchApiCall.getInstance().init(MainActivity.this, options, new PatchResponse() {
                @Override
                public void onSuccess(String s) {
                    Intent i = new Intent(MainActivity.this, DiallerActivity.class);
                    startActivity(i);
                    finish();

                }

                @Override
                public void onFailure(final String s) {
                    Log.d("Patch", s);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (s.equals("100")) {
                                Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                loadActivity();
                            } else{
                                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                                loadActivity();
                            }
                        }
                    });
                }
            });
        } else {
            loadActivity();
        }
    }

    private  void loadActivity() {
        setContentView(R.layout.activity_main);
        et_name = findViewById(R.id.et_name);
        et_cc = findViewById(R.id.et_cc);
        et_phone = findViewById(R.id.et_phone);
        bt_init = findViewById(R.id.bt_init);
        et_cuid = findViewById(R.id.et_cuid);

        bt_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject options = new JSONObject();
                try {
                    options.put("accountID", accountId);
                    options.put("apikey",apikey);
                    options.put("cc", et_cc.getText().toString());
                    options.put("phone", et_phone.getText().toString());
                    options.put("name",  et_name.getText().toString());
                    options.put("picture", "");
                    options.put("ringtone", "https://s3.ap-south-1.amazonaws.com/sdkassets/outgoing-tone.mp3");
                    options.put("cuid", et_cuid.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PatchApiCall.getInstance().init(MainActivity.this, options, new PatchResponse() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(MainActivity.class.getSimpleName(), "sdk initialized");
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("Login",true);
                        editor.putString("appAccountID", accountId);
                        editor.putString("appApikey", apikey);
                        editor.putString("appName", et_name.getText().toString());
                        editor.putString("appPhone", et_phone.getText().toString());
                        editor.putString("appCC", et_cc.getText().toString());
                        editor.putString("cuid", et_cuid.getText().toString());
                        editor.apply();
                        editor.commit();
                        Intent i = new Intent(MainActivity.this, DiallerActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailure(final String failure) {
                        Log.d("Patch", failure);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (failure.equals("100")) {
                                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                                } else{
                                    Toast.makeText(MainActivity.this, failure, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public List<Cli> getVerifiedNumbers(){
        return PatchApiCall.getInstance().getVerifiedNumbers(this);
    }


}
