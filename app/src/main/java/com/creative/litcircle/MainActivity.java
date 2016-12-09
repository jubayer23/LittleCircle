package com.creative.litcircle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.model.User;
import com.creative.litcircle.utils.ConnectionDetector;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btn_fb, btn_tw;
    private ConnectionDetector cd;
    private ProgressDialog progressDialog;

    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(AppController.getInstance().getPrefManger().getUserProfile() != null){
            Intent intent=new Intent(MainActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }

        init();
    }

    private void init() {
        cd = new ConnectionDetector(this);

        callbackManager=CallbackManager.Factory.create();


        btn_fb = (Button) findViewById(R.id.btn_fb);
        btn_fb.setOnClickListener(this);
        btn_tw = (Button) findViewById(R.id.btn_tw);
        btn_tw.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sign Up...");

    }

    @Override
    public void onClick(View view) {


        int id = view.getId();


        if (!cd.isConnectingToInternet()) {
            AlertDialogForAnything.showAlertDialogWhenComplte(MainActivity.this, "No Internet Connection",
                    "You don't have internet connection.", false);
            return;
        }

        if (id == R.id.btn_fb) {

            progressDialog.show();
            //performFacebookLogin();
            startFBloginProcess();

        }
        if (id == R.id.btn_tw) {

        }
    }

    private void startFBloginProcess(){

        LoginButton dummyFbLoginBtn= (LoginButton)findViewById(R.id.dummy_fb_login_button);

        dummyFbLoginBtn.setReadPermissions("public_profile", "email","user_friends");

        dummyFbLoginBtn.performClick();

        dummyFbLoginBtn.setPressed(true);

        dummyFbLoginBtn.invalidate();

        dummyFbLoginBtn.registerCallback(callbackManager, mCallBack);

        dummyFbLoginBtn.setPressed(false);

        dummyFbLoginBtn.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

           // progressDialog.dismiss();

            // App code
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            Log.e("response: ", response + "");
                            String id="",firstname="",lastname="",email="";
                            try {
                                 email =object.getString("email").toString();
                                id = object.getString("id").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(!id.isEmpty() && !email.isEmpty()){
                                try{
                                    String name[] =object.getString("name").toString().split(" ");
                                    firstname = name[0];
                                }catch (Exception e){
                                    firstname = "NotFound";
                                }
                                try{
                                    String name[] =object.getString("name").toString().split(" ");
                                    lastname = name[1];
                                }catch (Exception e) {
                                    lastname = "NotFound";
                                }
                                hitUrlForSignUp(AppConstant.URL_LOGIN, id, firstname, lastname, email);
                            }
                        }

                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender, birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            progressDialog.dismiss();
        }

        @Override
        public void onError(FacebookException e) {
            progressDialog.dismiss();
        }
    };


    private void hitUrlForSignUp(String url, final String user_id, final String firstname, final String lastname, final String email) {
        // TODO Auto-generated method stub
        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("DEBUG", response);
                        User user = new User(user_id,firstname,lastname,email);
                        AppController.getInstance().getPrefManger().setUserProfile(user);
                        if(progressDialog.isShowing())progressDialog.dismiss();

                        Intent intent=new Intent(MainActivity.this,HomeActivity.class);
                        startActivity(intent);
                        finish();


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(progressDialog.isShowing())progressDialog.dismiss();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("fb_code", user_id);
                params.put("first_name", firstname);
                params.put("last_name", lastname);
                params.put("email", email);
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }
}
