package com.example.imrahn.httpdebug;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.request_method_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submit(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String url = editText.getText().toString();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String requestMethod = (String)spinner.getSelectedItem();

        SubmitRequest request = new SubmitRequest();
        request.setUrl(url);
        request.setRequestMethod(requestMethod);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        beginSearch(request);

    }

    public void beginSearch(SubmitRequest request) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            new SubmitAsyncTask().execute(request);
        } else {
            // textView.setText("No network connection available.");
            // TextView textView = (TextView)findViewById(R.id.textView1);
            // textView.setText("No network connection available.");
            Toast.makeText(getApplicationContext(),
                    "No network connection available", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private class SubmitAsyncTask extends AsyncTask<SubmitRequest, Void, SubmitResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected SubmitResponse doInBackground(SubmitRequest... sr) {

            // params comes from the execute() call: params[0] is the url.
            SubmitResponse response = null;
            try {
                response = downloadUrl(sr[0]);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),  "Unable to retrieve url.",
                        Toast.LENGTH_SHORT).show();
                response = new SubmitResponse();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "General exception occured: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                response = new SubmitResponse();
            }
            return response;
        }

        @Override
        protected void onPostExecute(SubmitResponse result) {

            final EditText editText4 = (EditText) findViewById(R.id.editText4);

            try {
                editText4.setText(result.getResponseBody());

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText("Response: " + result.getResponseCode());


        }

        private SubmitResponse downloadUrl(SubmitRequest sr) throws IOException {
            InputStream is = null;
            // Only display the first 10000 characters of the retrieved
            // web page content.
            int len = 10000;
            SubmitResponse response = new SubmitResponse();
            URL url = null;
            HttpURLConnection conn = null;
            try {
                url = new URL(sr.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                String requestMethod = sr.getRequestMethod();
                conn.setRequestMethod(requestMethod);

                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","text/plain");

                if (requestMethod.equals("POST")) {
                    EditText editText3 = (EditText)findViewById(R.id.editText3);

                    conn.setDoOutput(true);
                    //conn.setChunkedStreamingMode(0);

                    //List<NameValuePair> params = new ArrayList<NameValuePair>();
                    //params.add(new BasicNameValuePair("firstParam", editText3.getText().toString()));

                    //OutputStream os = conn.getOutputStream();
                    //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    //String s = getQuery(params);
                    //writer.write(s);
                    //writer.flush();
                    //writer.close();
                    //os.close();

                    OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                    writeIt(out, editText3.getText().toString());
                    out.close();
                }


                conn.connect();
                response.setResponseCode(conn.getResponseCode());
                is = conn.getInputStream();

                response.setResponseBody(readIt(is, len));


            }
            catch (IOException ex){
                is = conn.getErrorStream();
                response.setResponseBody(readIt(is, len));
            }
            finally {

                if (is != null) {
                    is.close();
                }

                conn.disconnect();
            }

            return response;
        }

        public String readIt(InputStream is, int len) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            //Reader reader = new InputStreamReader(stream, "UTF-8");
            //char[] buffer = new char[len];
            //reader.read(buffer);
            //reader.read
            //return new String(buffer);

            //InputStream is = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        }

        public void writeIt(OutputStream stream, String s) throws IOException {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
            writer.write(s);
            writer.flush();
            writer.close();
        }

        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
        {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params)
            {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }

            return result.toString();
        }
    }
}
