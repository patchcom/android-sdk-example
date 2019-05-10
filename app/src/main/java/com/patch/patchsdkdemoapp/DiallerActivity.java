package com.patch.patchsdkdemoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.patch.patchcalling.interfaces.OutgoingCallStatus;
import com.patch.patchcalling.javaclasses.PatchApiCall;
import com.patch.patchcalling.retrofitresponse.createcontact.Cli;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DiallerActivity extends AppCompatActivity {

    private EditText et_context, et_cc, et_phone, et_diallerCuid;
    private Button bt_callVoip, bt_clearData, bt_callPstn, bt_dismiss, bt_callPurePstn, bt_callVoipAutofallback;
    private RelativeLayout rl_overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialler);

        et_context = findViewById(R.id.et_context);
        et_cc = findViewById(R.id.et_diallercc);
        et_phone = findViewById(R.id.et_diallerPhone);
        bt_callVoip = findViewById(R.id.bt_callVoip);
        bt_clearData = findViewById(R.id.bt_clear);
        bt_callPstn = findViewById(R.id.bt_callPstn);
        et_diallerCuid = findViewById(R.id.et_diallerCuid);
        rl_overlay = findViewById(R.id.rl_overlay);
        bt_dismiss = findViewById(R.id.bt_dismiss);
        bt_callPurePstn = findViewById(R.id.bt_callPurePstn);
        bt_callVoipAutofallback = findViewById(R.id.bt_callVoipAutofallback);

        bt_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_overlay.setVisibility(View.GONE);
                bt_callVoip.setVisibility(View.VISIBLE);
                bt_callPstn.setVisibility(View.VISIBLE);
                bt_callPurePstn.setVisibility(View.VISIBLE);
                bt_callVoipAutofallback.setVisibility(View.VISIBLE);
                bt_clearData.setVisibility(View.VISIBLE);
            }
        });

        //Voip Call
        bt_callVoip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    }
                    JSONObject cli = new JSONObject();
                    cli.put("cc", "91");
                    cli.put("phone", "8077756693");
                    ArrayList<String> tags = new ArrayList<>(0);
                    JSONObject callOptions = new JSONObject();
                    callOptions.put("pstn", false);
                    callOptions.put("webhook", "https://webhook.site/efc46406-67ea-4bac-8fb1-53988e0d6f61");
                    callOptions.put("tags", new JSONArray(tags));
                    callOptions.put("recording", false);
                    callOptions.put("var1", null);
                    callOptions.put("var2", "");
                    callOptions.put("var3", "");
                    callOptions.put("var4", "");
                    callOptions.put("var5", "");
                    callOptions.put("autoFallback", "false");
                    callOptions.put("cli", cli);

                    if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() > 0)) {
                        JSONObject calleeData = new JSONObject();
                        calleeData.put("cc", et_cc.getText().toString());
                        calleeData.put("phone", et_phone.getText().toString());
                        PatchApiCall.getInstance().call(DiallerActivity.this, calleeData, et_context.getText().toString(), callOptions, new OutgoingCallStatus() {
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
                                    rl_overlay.setVisibility(View.VISIBLE);
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
                                            rl_overlay.setVisibility(View.VISIBLE);
                                            bt_callVoip.setVisibility(View.INVISIBLE);
                                            bt_callPstn.setVisibility(View.INVISIBLE);
                                            bt_callPurePstn.setVisibility(View.INVISIBLE);
                                            bt_callVoipAutofallback.setVisibility(View.INVISIBLE);
                                            bt_clearData.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    } else if (et_diallerCuid.getText().toString().length() > 0 && (et_phone.getText().toString().length() == 0)) {
                        PatchApiCall.getInstance().call(DiallerActivity.this, et_diallerCuid.getText().toString(), et_context.getText().toString(), callOptions, new OutgoingCallStatus() {
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
                                            rl_overlay.setVisibility(View.VISIBLE);
                                            bt_callVoip.setVisibility(View.INVISIBLE);
                                            bt_callPstn.setVisibility(View.INVISIBLE);
                                            bt_callPurePstn.setVisibility(View.INVISIBLE);
                                            bt_callVoipAutofallback.setVisibility(View.INVISIBLE);
                                            bt_clearData.setVisibility(View.INVISIBLE);
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
        });

        //Voip Call with Autofallback
        bt_callVoipAutofallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    }
                    JSONObject cli = new JSONObject();
                    cli.put("cc", "91");
                    cli.put("phone", "8077756693");
                    ArrayList<String> tags = new ArrayList<>(0);
                    JSONObject options = new JSONObject();
                    options.put("pstn", false);
                    options.put("webhook", "https://webhook.site/efc46406-67ea-4bac-8fb1-53988e0d6f61");
                    options.put("tags", new JSONArray(tags));
                    options.put("recording", false);
                    options.put("var1", null);
                    options.put("var2", "");
                    options.put("var3", "");
                    options.put("var4", "");
                    options.put("var5", "");
                    options.put("autoFallback", "true");
                    options.put("cli", cli);

                    if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() > 0)) {
                        JSONObject calleeData = new JSONObject();
                        calleeData.put("cc", et_cc.getText().toString());
                        calleeData.put("phone", et_phone.getText().toString());
                        PatchApiCall.getInstance().call(DiallerActivity.this, calleeData, et_context.getText().toString(), options, new OutgoingCallStatus() {
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
                                    rl_overlay.setVisibility(View.VISIBLE);
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
                                            rl_overlay.setVisibility(View.VISIBLE);
                                            bt_callVoip.setVisibility(View.INVISIBLE);
                                            bt_callPstn.setVisibility(View.INVISIBLE);
                                            bt_callPurePstn.setVisibility(View.INVISIBLE);
                                            bt_clearData.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    } else if (et_diallerCuid.getText().toString().length() > 0 && (et_phone.getText().toString().length() == 0)) {
                        PatchApiCall.getInstance().call(DiallerActivity.this, et_diallerCuid.getText().toString(), et_context.getText().toString(), options, new OutgoingCallStatus() {
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
                                            rl_overlay.setVisibility(View.VISIBLE);
                                            bt_callVoip.setVisibility(View.INVISIBLE);
                                            bt_callPstn.setVisibility(View.INVISIBLE);
                                            bt_callPurePstn.setVisibility(View.INVISIBLE);
                                            bt_clearData.setVisibility(View.INVISIBLE);
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
        });


        //Pstn Call
        bt_callPstn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject cli = new JSONObject();
                    cli.put("cc", "91");
                    cli.put("phone", "8077756693");
                    ArrayList<String> tags = new ArrayList<>(0);
                    JSONObject options = new JSONObject();
                    options.put("pstn", true);
                    options.put("webhook", "https://webhook.site/efc46406-67ea-4bac-8fb1-53988e0d6f61");
                    options.put("tags", new JSONArray(tags));
                    options.put("recording", true);
                    options.put("var1", "");
                    options.put("var2", "");
                    options.put("var3", "");
                    options.put("var4", "");
                    options.put("var5", "");
                    options.put("cli", cli);
                    if (et_diallerCuid.getText().toString().length() == 0 && (et_phone.getText().toString().length() > 0)) {
                        JSONObject calleeData = new JSONObject();
                        calleeData.put("cc", et_cc.getText().toString());
                        calleeData.put("phone", et_phone.getText().toString());
                        PatchApiCall.getInstance().call(DiallerActivity.this, calleeData, et_context.getText().toString(), options, new OutgoingCallStatus() {
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
                        PatchApiCall.getInstance().call(DiallerActivity.this, et_diallerCuid.getText().toString(), et_context.getText().toString(), options, new OutgoingCallStatus() {
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
        });

        //Pstn to Pstn Call
        bt_callPurePstn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                disablePstnToPstnButton();

                try {
                    JSONObject cli = new JSONObject();
                    cli.put("cc", "91");
                    cli.put("phone", "8824965636");
                    JSONObject options = new JSONObject();
                    options.put("pstn", true);
                    options.put("cli", cli);
                    if (et_cc.getText().toString().trim().length() > 0 && et_phone.getText().toString().trim().length() > 0) {
                        JSONObject calleeData = new JSONObject();
                        calleeData.put("cc", et_cc.getText().toString());
                        calleeData.put("phone", et_phone.getText().toString());
                        PatchApiCall.getInstance().pstnToPstnCall(DiallerActivity.this, calleeData, options, new OutgoingCallStatus() {
                            @Override
                            public void callStatus(String reason) {
                                Log.d("Patch",  reason);
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
                    }
                    else if (et_phone.getText().toString().length() == 0) {
                        Toast.makeText(DiallerActivity.this, "Please pass country code and phone to make pstn to pstn call", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        bt_clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatchApiCall.getInstance().logout(DiallerActivity.this);
                Intent i = new Intent(DiallerActivity.this, MainActivity.class);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DiallerActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                startActivity(i);
                finish();
            }
        });
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
                        bt_callPurePstn.setEnabled(true);
                        bt_callPurePstn.setAlpha(1);
                    }
                });
            }
        }, 10000);

    }
}
