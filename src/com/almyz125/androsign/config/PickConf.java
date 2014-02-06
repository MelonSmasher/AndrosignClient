package com.almyz125.androsign.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.almyz125.androsign.Display;
import com.almyz125.androsign.R;
import com.almyz125.androsign.config.MyLocation.LocationResult;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class PickConf extends Activity implements OnClickListener {

	private ConfigsTask confTask;
	private Context mContext;
	private Button mNextButt, mRefreshButt;
	private Spinner mConfSpinner;
	private TextView tvRSS, tvWaterMark, tvWebContent, tvWeather, tvClock,
			tvRotation, tvTransition;
	private ArrayAdapter<String> spinnerAdapter;
	private JSONArray configs;
	private Intent lastIntent;
	private Location loc;
	private List<Address> addresses;
	private double lat, lng;
	private SharedPreferences prefs;
	private Editor prefsEditor;
	private String selID, selectedConf, zipCode = null;
	private LinearLayout statusLayout;
	private ProgressDialog pd, pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lastIntent = getIntent();
		setContentView(R.layout.activity_pick_conf);
		prefs = getSharedPreferences("Key", 0);
		prefsEditor = prefs.edit();
		mNextButt = (Button) findViewById(R.id.buttConfNext);
		mNextButt.setOnClickListener(this);
		mRefreshButt = (Button) findViewById(R.id.buttRefresh);
		mRefreshButt.setOnClickListener(this);
		mConfSpinner = (Spinner) findViewById(R.id.confSpinner);
		statusLayout = (LinearLayout) findViewById(R.id.statusLayout);
		statusLayout.setVisibility(View.GONE);
		tvRSS = (TextView) findViewById(R.id.tvStatusRSS);
		tvClock = (TextView) findViewById(R.id.tvStatusClock);
		tvWaterMark = (TextView) findViewById(R.id.tvStatusWaterMark);
		tvWebContent = (TextView) findViewById(R.id.tvStatusWebContent);
		tvWeather = (TextView) findViewById(R.id.tvStatusWeather);
		tvRotation = (TextView) findViewById(R.id.tvStatusRotation);
		tvTransition = (TextView) findViewById(R.id.tvStatusTransition);
		spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, android.R.id.text1);
		mConfSpinner.setAdapter(spinnerAdapter);
		mConfSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				if (configs != null) {
					try {
						JSONObject jo = new JSONObject(configs.get(position)
								.toString());
						selID = jo.getString("id");
						// String name = jo.getString("name");
						// String user = jo.getString("user");
						String rss = myDecode(jo.getString("rss"));
						String watermark = myDecode(jo.getString("watermark"));
						String webcontent = myDecode(jo
								.getString("web_content"));
						String weather = myDecode(jo.getString("weather"));
						String clock = myDecode(jo.getString("clock"));
						String rotation = myDecode(jo.getString("rotation"));
						String transition = myDecode(jo.getString("transition"));

						tvRSS.setText("RSS Feed: " + rss);
						tvWaterMark.setText("Watermark: " + watermark);
						tvWebContent.setText("Web Content: " + webcontent);
						tvWeather.setText("Weather: " + weather);
						tvClock.setText("Clock: " + clock);
						tvRotation.setText("Rotation: " + rotation);
						tvTransition.setText("Transition Time: " + transition
								+ " (sec)");
						if (isNetworkAvailable()) {
							if (!weather.contentEquals("false")) {
								getLoc();
							}
						}
						selectedConf = configs.get(position).toString();

						statusLayout.setVisibility(View.VISIBLE);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				statusLayout.setVisibility(View.GONE);
			}

		});
		mContext = PickConf.this;
		confTask = new ConfigsTask();
		confTask.execute(mContext);
	}

	public void getLoc() {
		if (pDialog != null) {
			pDialog.dismiss();
		}
		pDialog = new ProgressDialog(mContext);
		pDialog.setMessage("Getting location for weather...");
		pDialog.setIndeterminate(true);
		pDialog.setCancelable(false);
		pDialog.show();
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {
				loc = location;
				lat = loc.getLatitude();
				lng = loc.getLongitude();
				// after we get a lat an lng lets get an address from it
				new GetGeoInfo().execute("");
			}
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(mContext, locationResult);
	}

	private class GetGeoInfo extends AsyncTask<String, Void, String> {
		@SuppressLint("DefaultLocale")
		@Override
		protected String doInBackground(String... params) {

			try {
				// get location info and form url
				Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
				addresses = geocoder.getFromLocation(lat, lng, 1);
				zipCode = addresses.get(0).getPostalCode().toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "Done";
		}

		@Override
		protected void onPostExecute(String result) {
			if (pDialog != null) {
				pDialog.dismiss();
			}
		}
	}

	public String myDecode(String s) {
		if (s != "false" || s != "true") {
			try {
				s = URLDecoder.decode(s, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return s;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.buttConfNext:
			// commit the current selection to our prefs
			if (selID != null) {
				prefsEditor.putString("ID", selID);
				prefsEditor.commit();
			}
			if (selectedConf != null) {
				prefsEditor.putString("CONF", selectedConf);
				prefsEditor.commit();
				Intent i = new Intent(mContext, Display.class);
				i.putExtra("CONF", selectedConf);
				i.putExtra("URL", lastIntent.getStringExtra("url"));
				zipCode = "03060";
				if (zipCode != null) {
					System.out.println(zipCode);
					i.putExtra("ZIP", zipCode);
				}

				startActivity(i);
			}
			break;
		case R.id.buttRefresh:
			spinnerAdapter.clear();
			confTask = new ConfigsTask();
			confTask.execute(mContext);
			break;
		}

	}

	@Override
	protected void onDestroy() {
		if (pd != null) {
			pd.dismiss();
			mNextButt.setEnabled(true);
		}
		super.onDestroy();
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	protected class ConfigsTask extends AsyncTask<Context, Integer, String> {

		InputStream inputStream = null;
		String result = "";

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(mContext);
			mNextButt.setEnabled(false);
			pd.setTitle("Gatherig Configurations...");
			pd.setMessage("Please wait.");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Context... params) {

			String confAPI = lastIntent.getStringExtra("url") + "/api.php";
			ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
			try {

				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(confAPI);
				httpPost.setEntity(new UrlEncodedFormEntity(param));
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				inputStream = httpEntity.getContent();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(inputStream, "iso-8859-1"), 8);
				StringBuilder sBuilder = new StringBuilder();

				String line = null;
				while ((line = bReader.readLine()) != null) {
					sBuilder.append(line + "\n");
				}

				inputStream.close();
				result = sBuilder.toString();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			try {
				JSONObject jObject = new JSONObject(result);
				configs = jObject.getJSONArray("Configs");
				int i;
				for (i = 0; i < configs.length(); i++) {
					JSONObject jo = new JSONObject(configs.get(i).toString());
					String name = jo.getString("name");
					String user = jo.getString("user");
					String conf = name + " - " + user;
					spinnerAdapter.add(conf);
					spinnerAdapter.notifyDataSetChanged();
				}
				mNextButt.setEnabled(true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Set our last selection as the current selection.
			if (prefs.getString("ID", "") != null) {
				int i;
				String wantID = prefs.getString("ID", "");
				if (wantID.length() > 0) {
					for (i = 0; i < configs.length(); i++) {
						try {
							JSONObject jo = new JSONObject(configs.get(i)
									.toString());
							int math = (Integer.valueOf(jo.getString("id")) - Integer
									.valueOf(wantID));
							if (math == 0) {
								mConfSpinner.setSelection(i);
							} else {
								mConfSpinner.setSelection(0);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				} else {
					mConfSpinner.setSelection(0);
				}
			} else {
				mConfSpinner.setSelection(0);
			}
			pd.dismiss();
			super.onPostExecute(result);
		}

	}
}