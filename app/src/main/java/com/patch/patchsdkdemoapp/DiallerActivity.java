package com.patch.patchsdkdemoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.patch.patchcalling.interfaces.OutgoingCallStatus;
import com.patch.patchcalling.interfaces.PatchResponse;
import com.patch.patchcalling.javaclasses.PatchApiCall;
import com.patch.patchcalling.retrofitresponse.createcontact.Cli;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DiallerActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_context, et_cc, et_phone, et_diallerCuid;
    private Button bt_callVoip, bt_callPstn, bt_callPurePstn, bt_callVoipAutofallback;
    private List<Cli> verifiedNumberList = new ArrayList<>();
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);

        /* You must invoke init() of PatchSdk with required callOptions on each app launch,
            when you place integration of PatchSdk to your app */
        initPatchSdk();

    }

    private void initViews() {
        et_context = findViewById(R.id.et_context);
        et_cc = findViewById(R.id.et_diallercc);
        et_phone = findViewById(R.id.et_diallerPhone);
        et_diallerCuid = findViewById(R.id.et_diallerCuid);
        bt_callVoip = findViewById(R.id.bt_callVoip);
        bt_callPstn = findViewById(R.id.bt_callPstn);
        bt_callPurePstn = findViewById(R.id.bt_callPstnToPstn);
        bt_callVoipAutofallback = findViewById(R.id.bt_callVoipAutofallback);

        bt_callVoip.setOnClickListener(DiallerActivity.this);
        bt_callPstn.setOnClickListener(DiallerActivity.this);
        bt_callPurePstn.setOnClickListener(DiallerActivity.this);
        bt_callVoipAutofallback.setOnClickListener(DiallerActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //getting list of authorised/verified numbers (i.e. list of clis defined in Backend)
        verifiedNumberList = getVerifiedNumbers();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_callVoip:
                DiallerActivity.this.closeKeyboard(view);
                makeVoipCall();
                break;
            case R.id.bt_callVoipAutofallback:
                DiallerActivity.this.closeKeyboard(view);
                makeVoipAutofallbackCall();
                break;
            case R.id.bt_callPstn:
                DiallerActivity.this.closeKeyboard(view);
                makePstnCall();
                break;
            case R.id.bt_callPstnToPstn:
                DiallerActivity.this.closeKeyboard(view);
                makePstnToPstnCall();
                break;
        }
    }


    //Always initialize PatchSdk first, before getting use it
    private void initPatchSdk() {
        showProgressDialog(true);
        PatchApiCall.getInstance().init(DiallerActivity.this, this.getInitOptions(), new PatchResponse() {
            @Override
            public void onSuccess(String response) {
                showProgressDialog(false);
                Toast.makeText(DiallerActivity.this, "Patch Sdk is initialized", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_dialler);
                initViews();
            }

            @Override
            public void onFailure(final String failure) {
                Log.d("Patch", failure);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (failure.equals("100")) {
                            // 100 error code is thrown by PatchSdk when device is not connected of internet.
                            Toast.makeText(DiallerActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            Toast.makeText(DiallerActivity.this, "Patch Sdk not initialized, trying again", Toast.LENGTH_SHORT).show();
                            initPatchSdk();
                        } else {
                            Toast.makeText(DiallerActivity.this, failure, Toast.LENGTH_SHORT).show();
                            initPatchSdk();
                        }
                    }
                });
            }
        });
    }



    /* Patch-Calling methods start here
     * Types of Patch-Calls
     * type 1 : Voip call(without autofallback)
     * type 2 : Voip call(with autofallback)
     * type 3 : Pstn call
     * type 4 : Pstn to Pstn call
     * */
    private void makeVoipCall() {
        try {
            if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() > 0)) {
                JSONObject calleeData = new JSONObject();

                calleeData.put("cc", et_cc.getText().toString());
                calleeData.put("phone", et_phone.getText().toString());
                PatchApiCall.getInstance().call(DiallerActivity.this, calleeData, et_context.getText().toString(), this.getVoipCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", "reason is" + reason);
                        if (reason.equals("callOver")) {
                            Log.d("Patch", "call is over");
                        } else if (reason.equals("declined")) {
                            Log.d("Patch", "call is declined");
                        } else if (reason.equals("missed")) {
                            Log.d("Patch", "call is missed");
                        } else if (reason.equals("answer")) {
                            Log.d("Patch", "call is answered");
                        } else if (reason.equals("cancel")) {
                            Log.d("Patch", "call is cancelled");
                        } else if (reason.equals("Number not rechable")) {
                            Toast.makeText(DiallerActivity.this, "Callee is unavailable. Please use Voip with fallback", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                        if (error.equals("Number not rechable")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DiallerActivity.this, "Callee is unavailable. Please use Voip with fallback", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            } else if (et_diallerCuid.getText().toString().length() > 0 && (et_phone.getText().toString().length() == 0)) {
                PatchApiCall.getInstance().call(DiallerActivity.this, et_diallerCuid.getText().toString(), et_context.getText().toString(), this.getVoipCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", "reason is" + reason);
                        if (reason.equals("callOver")) {
                            Log.d("Patch", "call is over");
                        } else if (reason.equals("declined")) {
                            Log.d("Patch", "call is declined");
                        } else if (reason.equals("missed")) {
                            Log.d("Patch", "call is missed");
                        } else if (reason.equals("answer")) {
                            Log.d("Patch", "call is answered");
                        } else if (reason.equals("cancel")) {
                            Log.d("Patch", "call is cancelled");
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                        if (error.equals("Number not rechable")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DiallerActivity.this, "Callee is unavailable. Please use Voip with fallback", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            } else if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() == 0)) {
                Toast.makeText(DiallerActivity.this, "Please pass country code and phone or Cuid to make call", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(DiallerActivity.this, "Please pass either country code and phone or Cuid, not both", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void makeVoipAutofallbackCall() {
        try {
            if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() > 0)) {
                JSONObject calleeData = new JSONObject();
                calleeData.put("cc", et_cc.getText().toString());
                calleeData.put("phone", et_phone.getText().toString());
                PatchApiCall.getInstance().call(DiallerActivity.this, calleeData, et_context.getText().toString(), this.getVoipAutofallbackCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", "reason is" + reason);
                        if (reason.equals("callOver")) {
                            Log.d("Patch", "call is over");
                        } else if (reason.equals("declined")) {
                            Log.d("Patch", "call is declined");
                        } else if (reason.equals("missed")) {
                            Log.d("Patch", "call is missed");
                        } else if (reason.equals("answer")) {
                            Log.d("Patch", "call is answered");
                        } else if (reason.equals("cancel")) {
                            Log.d("Patch", "call is cancelled");
                        } else if (reason.equals("Number not rechable")) {
                            Toast.makeText(DiallerActivity.this, "Callee is unavailable. Please use Voip with fallback", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                        if (error.equals("Number not rechable")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DiallerActivity.this, "Callee is unavailable. Please use Voip with fallback", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            } else if (et_diallerCuid.getText().toString().length() > 0 && (et_phone.getText().toString().length() == 0)) {
                PatchApiCall.getInstance().call(DiallerActivity.this, et_diallerCuid.getText().toString(), et_context.getText().toString(), this.getVoipAutofallbackCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", "reason is" + reason);
                        if (reason.equals("callOver")) {
                            Log.d("Patch", "call is over");
                        } else if (reason.equals("declined")) {
                            Log.d("Patch", "call is declined");
                        } else if (reason.equals("missed")) {
                            Log.d("Patch", "call is missed");
                        } else if (reason.equals("answer")) {
                            Log.d("Patch", "call is answered");
                        } else if (reason.equals("cancel")) {
                            Log.d("Patch", "call is cancelled");
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                        if (error.equals("Number not rechable")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DiallerActivity.this, "Callee is unavailable. Please use Voip with fallback", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            } else if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() == 0)) {
                Toast.makeText(DiallerActivity.this, "Please pass country code and phone or Cuid to make call", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(DiallerActivity.this, "Please pass either country code and phone or Cuid, not both", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void makePstnCall() {
        try {
            if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() > 0)) {
                JSONObject calleeData = new JSONObject();
                calleeData.put("cc", et_cc.getText().toString());
                calleeData.put("phone", et_phone.getText().toString());
                PatchApiCall.getInstance().call(DiallerActivity.this, calleeData, et_context.getText().toString(), this.getPstnCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", "reason is" + reason);
                        if (reason.equals("callOver")) {
                            Log.d("Patch", "call is over");
                        } else if (reason.equals("declined")) {
                            Log.d("Patch", "call is declined");
                        } else if (reason.equals("missed")) {
                            Log.d("Patch", "call is missed");
                        } else if (reason.equals("answer")) {
                            Log.d("Patch", "call is answered");
                        } else if (reason.equals("cancel")) {
                            Log.d("Patch", "call is cancelled");
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                    }
                });
            } else if (et_diallerCuid.getText().toString().length() > 0 && (et_phone.getText().toString().length() == 0)) {
                PatchApiCall.getInstance().call(DiallerActivity.this, et_diallerCuid.getText().toString(), et_context.getText().toString(), this.getPstnCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", "reason is" + reason);
                        if (reason.equals("Call Over")) {
                            Log.d("Patch", "call is over");
                        } else if (reason.equals("Call Declined")) {
                            Log.d("Patch", "call is declined");
                        } else {
                            Log.d("Patch", "call is missed");
                        }
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                    }
                });
            } else if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() == 0)) {
                Toast.makeText(DiallerActivity.this, "Please pass country code and phone or Cuid to make call", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(DiallerActivity.this, "Please pass either country code and phone or Cuid, not both", Toast.LENGTH_LONG).show();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void makePstnToPstnCall() {
        try {
            if (et_cc.getText().toString().trim().length() > 0 && et_phone.getText().toString().trim().length() > 0) {
                disablePstnToPstnButton();

                JSONObject calleeData = new JSONObject();
                calleeData.put("cc", et_cc.getText().toString());
                calleeData.put("phone", et_phone.getText().toString());
                PatchApiCall.getInstance().pstnToPstnCall(DiallerActivity.this, calleeData, this.getPstnToPstnCallOptions(), new OutgoingCallStatus() {
                    @Override
                    public void callStatus(String reason) {
                        Log.d("Patch", reason);
                    }

                    @Override
                    public void onSuccess(String response) {
                        Log.d("Patch", response);
                    }

                    @Override
                    public void Onfailure(String error) {
                        Log.d("Patch", error);
                    }
                });
            } else if (et_phone.getText().toString().length() == 0) {
                Toast.makeText(DiallerActivity.this, "Please pass country code and phone to make pstn to pstn call", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /* Patch-Calling methods end here*/




    /* Getter mmethods of initOptions/callOptions start here
     * @initOptions - are used in init() method of PatchSdk
     * @callOptions - are used in Patch-calling methods of PatchSdk
     * */
    private JSONObject getInitOptions() {
        JSONObject options = new JSONObject();
        try {
            options.put("accountID", "<Put here AccountId>");
            options.put("apikey", "<Put here ApiKey>");
            options.put("cc", "<Put Country code of whomsoever is calling>");
            options.put("phone", "<Put Phone number of whomsoever is calling>");
            options.put("name", "<Put Name of the caller>");
            options.put("picture", "");                                             //this field is mandatory, although this feature is not available yet, you can put it blank.
            options.put("cuid", "<Put Unique-Userid of whomsoever is calling>");
            options.put("ringtone", "<Put Url of Ringtone to play when call is initiated>");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return options;
    }

    private JSONObject getVoipCallOptions() throws JSONException {
        JSONObject cli = new JSONObject();  // End-user will receive the call from this number
        cli.put("cc", "<Put Country code of cli>");
        cli.put("phone", "<Put Phone number of cli>");
        ArrayList<String> tags = new ArrayList<>(0);
        JSONObject callOptions = new JSONObject();
        callOptions.put("pstn", false);          // false value required for voip call
        callOptions.put("tags", new JSONArray(tags));  // <Put Json Array of tags for a call>
        callOptions.put("var1", "");            // <String of maximum length of 128 characters, used for analytics purpose>
        callOptions.put("var2", "");
        callOptions.put("var3", "");
        callOptions.put("var4", "");
        callOptions.put("var5", "");
        callOptions.put("autoFallback", false);   // false value required for Voip call without AutoFallback
        callOptions.put("recording", "<Put Boolean value true/false>");
        callOptions.put("webhook", "<Put String value. Call data is sent over Webhook after the call gets over>");
        callOptions.put("cli", cli);
        return callOptions;
    }

    private JSONObject getVoipAutofallbackCallOptions() throws JSONException {
        JSONObject cli = new JSONObject();  // End-user will receive the call from this number
        cli.put("cc", "<Put Country code of cli>");
        cli.put("phone", "<Put Phone number of cli>");
        ArrayList<String> tags = new ArrayList<>(0);
        JSONObject callOptions = new JSONObject();
        callOptions.put("pstn", false);          // false value required for voip call
        callOptions.put("tags", new JSONArray(tags));  // <Put Json Array of tags for a call>
        callOptions.put("var1", "");            // <String of maximum length of 128 characters, used for analytics purpose>
        callOptions.put("var2", "");
        callOptions.put("var3", "");
        callOptions.put("var4", "");
        callOptions.put("var5", "");
        callOptions.put("autoFallback", true);   // true value required for Voip call with AutoFallback
        callOptions.put("recording", "<Put Boolean value true/false>");
        callOptions.put("webhook", "<Put String value. Call data is sent over Webhook after the call gets over>");
        callOptions.put("cli", cli);
        return callOptions;
    }

    private JSONObject getPstnCallOptions() throws JSONException {
        JSONObject cli = new JSONObject();  // End-user will receive the call from this number
        cli.put("cc", "<Put Country code of cli>");
        cli.put("phone", "<Put Phone number of cli>");
        ArrayList<String> tags = new ArrayList<>(0);
        JSONObject callOptions = new JSONObject();
        callOptions.put("pstn", true);          // true value required for voip call
        callOptions.put("tags", new JSONArray(tags));  // <Put Json Array of tags for a call>
        callOptions.put("var1", "");            // <String of maximum length of 128 characters, used for analytics purpose>
        callOptions.put("var2", "");
        callOptions.put("var3", "");
        callOptions.put("var4", "");
        callOptions.put("var5", "");
        callOptions.put("recording", "<Put Boolean value true/false>");
        callOptions.put("webhook", "<Put String value. Call data is sent over Webhook after the call gets over>");
        callOptions.put("cli", cli);
        return callOptions;
    }

    private JSONObject getPstnToPstnCallOptions() throws JSONException {
        JSONObject cli = new JSONObject();  // End-user will receive the call from this number
        cli.put("cc", "<Put Country code of cli>");
        cli.put("phone", "<Put Phone number of cli>");
        JSONObject callOptions = new JSONObject();
        callOptions.put("cli", cli);
        return callOptions;
    }
    /* Getter methods of initOptions/callOptions end here*/


    public List<Cli> getVerifiedNumbers() {
        return PatchApiCall.getInstance().getVerifiedNumbers(this);
    }

    public void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    public void showProgressDialog(boolean status){
        if(status == true){
            dialog.setMessage("Please wait,PatchSdk is initializing");
            dialog.show();
        }
        else {
            dialog.dismiss();
        }
    }

    private void disablePstnToPstnButton() {
        bt_callPurePstn.setEnabled(false);
        bt_callPurePstn.setAlpha(0.5f);
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(DiallerActivity.this, "Please wait for 15 seconds while we're placing Pstn to Pstn call", Toast.LENGTH_LONG).show();
                        bt_callPurePstn.setEnabled(true);
                        bt_callPurePstn.setAlpha(1);
                    }
                });
            }
        }, 15000);

    }
}