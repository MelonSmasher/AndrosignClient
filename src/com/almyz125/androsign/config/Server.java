package com.almyz125.androsign.config;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.almyz125.androsign.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Server extends Activity implements OnClickListener {

	private EditText etServer;
	private Button btNext;
	public static String inputURL, finalURL;
	private Context mContext;
	private CheckTask checkTask;
	private SharedPreferences prefs;
	private Editor prefsEditor;
	private ProgressDialog pd;
	private Boolean useURL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		mContext = Server.this;
		etServer = (EditText) findViewById(R.id.serverURL);
		btNext = (Button) findViewById(R.id.buttNext);
		btNext.setOnClickListener(this);
		prefs = getSharedPreferences("Key", 0);
		prefsEditor = prefs.edit();
		if (prefs.getString("URL", "") != null) {
			etServer.setText(prefs.getString("URL", ""));
		}
		if (etServer.length() > 0) {
			btNext.performClick();
		}

	}

	@Override
	protected void onDestroy() {
		if (pd != null) {
			pd.dismiss();
			btNext.setEnabled(true);
		}
		super.onDestroy();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttNext:
			useURL = false;
			inputURL = etServer.getText().toString();
			if (validURL(inputURL)) {
				if (finalURL.toString() != inputURL.toString()) {
					etServer.setText(finalURL.toString());
				}
				System.out.println("Debug: " + finalURL + " is valid.");
				checkTask = new CheckTask();
				checkTask.execute(mContext);
			} else {
				System.out.println("Debug: " + inputURL + " is not valid.");
			}
			break;
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	private Boolean validURL(String url) {
		if (url.length() > 0) {
			if (URLUtil.isValidUrl(url)) {
				finalURL = url;

				return true;
			} else {
				if (!url.startsWith("http://")) {
					url = "http://" + url;
					if (URLUtil.isValidUrl(url)) {
						finalURL = url;

						return true;
					}
				}
				return false;
			}
		} else {
			return false;
		}
	}

	private Boolean hostIsReachable(String host) {
		try {
			int port = 80;
			if (host.startsWith("http://")) {
				host = host.replaceFirst("http://", "");
			}
			if (host.startsWith("https://")) {
				host = host.replaceFirst("https://", "");
				port = 443;
			}
			SocketAddress sockaddr = new InetSocketAddress(host, port);
			Socket sock = new Socket();
			int timeoutMs = 2000;
			sock.connect(sockaddr, timeoutMs);
			sock.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected class CheckTask extends AsyncTask<Context, Integer, String> {
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(mContext);
			btNext.setEnabled(false);
			pd.setTitle("Validating...");
			pd.setMessage("Please wait.");
			pd.setCancelable(false);
			pd.setIndeterminate(true);
			pd.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Context... params) {
			try {
				if (hostIsReachable(finalURL)) {
					System.out.println("Debug: " + finalURL + " is reachable.");
					prefsEditor.putString("URL", finalURL);
					prefsEditor.commit();
					useURL = true;
				} else {
					System.out.println("Debug: " + finalURL
							+ " is not reachable.");
					useURL = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (pd != null) {
				pd.dismiss();
				btNext.setEnabled(true);
				if (useURL) {
					Intent i = new Intent(mContext, PickConf.class);
					i.putExtra("url", finalURL);
					startActivity(i);
				}
			}
			super.onPostExecute(result);
		}

	}

}
