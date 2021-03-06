package com.seproject.classmate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AddLec extends AppCompatActivity {
    private static final String TAG = "CreateCourse";

    @InjectView(R.id.start_time) EditText _start_time;
    @InjectView(R.id.end_time) EditText _end_time;
    @InjectView(R.id.venue) EditText _venue;
    @InjectView(R.id.btn_create) Button _createButton;
    //@InjectView(R.id.link_login) TextView _loginLink;

    ArrayList<String> list = new ArrayList();
    Dialog dialog;
    String create_notif_url;
    int create_ok1;

    CharSequence box_list[] = {"Tea", "Coffee", "fuck"};

    int create_ok;
    String [] opts ={"Monday","Tuesday","Wednesday","Thursday","Friday"};
    String start_time ;String end_time ;String venue; String subject;
    String create_course_url; String get_sub_url;
    //String email;
    //String password;
    String day;
    ProgressDialog progressDialog;
    //List<NameValuePair> nameValuePair1 = new ArrayList<NameValuePair>(3);

    JSONArray subs = null;
    String[] subjects = new String[50];
    private ProgressDialog pDialog;
    Spinner spnr;Spinner spnr2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addlec);
        ButterKnife.inject(this);

        String ip = getResources().getString(R.string.ip);
        get_sub_url = "http://"+ip+"/classmate/v1/getSub";
        create_course_url = "http://"+ip+"/classmate/v1/putLecInSub/";
        create_notif_url = "http://"+ip+"/classmate/v1/putNotification/";

       // Log.d("Response: ", ">  idhar pocha");
        new get_subject_list().execute();


        spnr = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, opts);

        spnr.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnr.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {

                        int position = spnr.getSelectedItemPosition();
                        // Toast.makeText(getApplicationContext(),"You have selected "+opts[position],Toast.LENGTH_SHORT).show();
                        day = opts[position];
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                }
        );

        _createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject=subject.replace(" ", "_");
                start_time = _start_time.getText().toString();
                end_time = _end_time.getText().toString();
                venue = _venue.getText().toString();
                venue = venue.replace(" ", "_");
                start_time = start_time + ":00";end_time = end_time + ":00";
               // type = _coursetype.getText().toString();
                create_course_url = create_course_url + subject + "/" + day + "/" +start_time + "/" + end_time + "/"+ venue;
                new create().execute();

                String descrip = subject + " lecture added from "+start_time+" - " + end_time + " at " + venue;
                descrip = descrip.replace(" ","_");
                create_notif_url = create_notif_url + subject + "/" + descrip + "/New_Lecture" ;
                Log.d("Response: ", "> " + create_notif_url);
                new createnotif().execute();
                //AsyncTask
            }
        });
    }

    public void onCreateSuccess() {
        _createButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
        NavUtils.navigateUpFromSameTask(AddLec.this);
        //Intent j = new Intent(getBaseContext(), Complete_TT.class);
        //j.putExtra("Main_Activity_call", 1);
        //startActivity(j);
    }

    public void onCreateFailed() {
        Toast.makeText(getBaseContext(), "Lecture could not be added :(", Toast.LENGTH_LONG).show();

        _createButton.setEnabled(true);
    }

    class get_subject_list extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        //@Override
        protected void onPreExecute() {
            super.onPreExecute();
            //This progress Dialog works too fast... :/

            pDialog = new ProgressDialog(AddLec.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            //List<NameValuePair> params = new ArrayList<NameValuePair>();

            ServiceHandler sh = new ServiceHandler();

            //nameValuePair.add(new BasicNameValuePair("email", email));
            // nameValuePair.add(new BasicNameValuePair("password", password));

            // getting JSON string from URL
            String jsonStr = sh.makeServiceCall(get_sub_url, ServiceHandler.GET);

            //Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    subs = jsonObj.getJSONArray("subjects");

                    // looping through All Contacts
                    for (int i = 0; i < subs.length(); i++) {
                        JSONObject c = subs.getJSONObject(i);

                        String sub_name = c.getString("sub_name");

                        subjects[i] = sub_name;
                        subjects[i] = subjects[i].replace("_"," ");
                        Log.d("reachedResponse: ", "> " + subjects[i]);

                        list.add(subjects[i]);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            if (pDialog.isShowing())
                pDialog.dismiss();
           final Button btn = (Button) findViewById(R.id.select_Button);
            box_list = list.toArray(new CharSequence[list.size()]);

            final AlertDialog.Builder builder = new AlertDialog.Builder(AddLec.this);
            builder.setTitle("Select Subject:");

            builder.setSingleChoiceItems(box_list, 0, null)
                    .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                            subject = subjects[selectedPosition];
                            btn.setText(subject);
                            // Do something useful withe the position of the selected radio button
                        }
                    });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Arrays.fill(bl, false);
                    //selected_list.clear();
                    dialog = builder.create();
                    dialog.show();

                    // Toast.makeText(FacDash.this, "Wohoo! See You There!", Toast.LENGTH_SHORT).show();
                }
            });

           /* Log.d("Response: ", "> yaha tak to thee ha");
            //POPULATE SPINNER
            spnr2 = (Spinner)findViewById(R.id.spinner2);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    AddLec.this, android.R.layout.simple_spinner_item, box_list);


            spnr2.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnr2.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {

                            int position = spnr2.getSelectedItemPosition();
                            // Toast.makeText(getApplicationContext(),"You have selected "+opts[position],Toast.LENGTH_SHORT).show();
                            subject = subjects[position];
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    }
            );
            //get_attend_url = "http://"+ip+"/classmate/v1/getAttend/"+Email+"/"+subjects[position];
            //Log.d("URLREQ: ", "> " + get_attend_url);
            //new get_attendence().execute();

            //setListAdapter(adapter);*/
        }

    }
    class createnotif extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        //@Override
        protected void onPreExecute() {
            super.onPreExecute();
            //This progress Dialog works too fast... :/

            progressDialog = new ProgressDialog(AddLec.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Notif...");
            progressDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            //List<NameValuePair> params = new ArrayList<NameValuePair>();

            ServiceHandler sh = new ServiceHandler();
            //nameValuePair1.add(new BasicNameValuePair("name", name));

            //nameValuePair1.add(new BasicNameValuePair("type", type));

            // getting JSON string from URL
            String jsonStr = sh.makeServiceCall(create_notif_url, ServiceHandler.PUT);

            //Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    boolean success = jsonObj.getBoolean("error");
                    if(!success){
                        create_ok1 = 1;
                        //user_name = jsonObj.getString("name");
                        // Log.d("Response: ", "> " + user_name);
                        //boolean success = jsonObj.getBoolean("error");
                    }
                    else{
                        create_ok1 = 0;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }
        protected void onPostExecute(String file_url) {
            progressDialog.dismiss();
            if(create_ok1 == 1){
                onCreateSuccess();
            }
            else
            {onCreateFailed();}


        }

    }

    class create extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        //@Override
        protected void onPreExecute() {
            super.onPreExecute();
            //This progress Dialog works too fast... :/

            progressDialog = new ProgressDialog(AddLec.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            progressDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            //List<NameValuePair> params = new ArrayList<NameValuePair>();

            ServiceHandler sh = new ServiceHandler();
            //nameValuePair1.add(new BasicNameValuePair("name", name));

            //nameValuePair1.add(new BasicNameValuePair("type", type));

            // getting JSON string from URL
            String jsonStr = sh.makeServiceCall(create_course_url, ServiceHandler.PUT);

           // Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    boolean success = jsonObj.getBoolean("error");
                    if(!success){
                       create_ok = 1;
                        //user_name = jsonObj.getString("name");
                       // Log.d("Response: ", "> " + user_name);
                        //boolean success = jsonObj.getBoolean("error");
                    }
                    else{
                       create_ok = 0;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            progressDialog.dismiss();
            if(create_ok == 1){
                onCreateSuccess();
            }
            else
            {onCreateFailed();}


        }

    }
}