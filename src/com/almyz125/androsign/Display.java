package com.almyz125.androsign;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.squareup.picasso.Picasso;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Display extends Activity {

	private ImageView iv, iv2, iv3;
	private List<String> urls = new LinkedList<String>();
	private int choice;
	private Thread wpThread = null, rssThread, bgThread;
	private DoPOST mDoPOST;
	private Picasso.Builder builder;
	private Picasso picasso;
	private static String data;
	private Intent lastIntent;
	private RelativeLayout weatherLayOut;
	public static int visible;
	private CustomTextView text;
	private TextView rssTV, weatherTV, forecastTV;
	private String baseURL, apiURL, confSTR, user, weather, clock, rss,
			watermark, webcontent, rotation, transition; // , name, id;
	private DigitalClock digClock;
	private int weatherImg;
	private String newsString, forecast, weatherString;
	private Document newsDoc, weatherDoc;
	private Toast toast;
	private WebView wv;
	private boolean first, shouldShowWebView = false,
			shouldShowWatermark = false, shouldShowClock = false,
			shouldShowNews = false, shouldShowWeather = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lastIntent = getIntent();
		setFullScreen();
		setContentView(R.layout.activity_display);
		init();
		parseConf();
		enableElements();
		defineGUI();
		initElements();
	}

	private void init() {
		visible = 1;
		choice = 0;
		first = true;
		toast = new Toast(Display.this);
		builder = new Picasso.Builder(getBaseContext());
		picasso = builder.build();
		picasso.setDebugging(false);
		text = new CustomTextView(Display.this);
	}

	private void parseConf() {
		confSTR = lastIntent.getStringExtra("CONF");
		try {
			JSONObject jo = new JSONObject(confSTR);
			// get all values from json.
			// id = jo.getString("id");
			user = jo.getString("user");
			// name = jo.getString("name");
			rss = myDecode(jo.getString("rss"));
			watermark = myDecode(jo.getString("watermark"));
			webcontent = myDecode(jo.getString("web_content"));
			clock = myDecode(jo.getString("clock"));
			weather = myDecode(jo.getString("weather"));
			rotation = jo.getString("rotation");
			transition = jo.getString("transition");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		baseURL = lastIntent.getStringExtra("URL") + "/upload/" + user + "/";
		apiURL = lastIntent.getStringExtra("URL") + "/api.php?kind=photo&user="
				+ user;
	}

	@SuppressLint("CutPasteId")
	private void defineGUI() {
		weatherLayOut = (RelativeLayout) findViewById(R.id.weatherContainer);
		rssTV = (TextView) findViewById(R.id.rssTV);
		weatherTV = (TextView) findViewById(R.id.weatherTV);
		wv = (WebView) findViewById(R.id.webView1);
		forecastTV = (TextView) findViewById(R.id.forecastTV);
		iv = (ImageView) findViewById(R.id.imageView1);
		iv2 = (ImageView) findViewById(R.id.imageView2);
		iv3 = (ImageView) findViewById(R.id.weatherIV);
		digClock = (DigitalClock) findViewById(R.id.digitalClock);
	}

	private void enableElements() {

		if (!rss.contentEquals("false")) {
			shouldShowNews = true;
		} else {
			shouldShowNews = false;
		}
		if (!watermark.contentEquals("false")) {
			shouldShowWatermark = true;
		} else {
			shouldShowWatermark = false;
		}
		if (!webcontent.contentEquals("false")) {
			shouldShowWebView = true;
		} else {
			shouldShowWebView = false;
		}
		if (!weather.contentEquals("false")) {
			shouldShowWeather = true;
		} else {
			shouldShowWeather = false;
		}
		if (!clock.contentEquals("false")) {
			shouldShowClock = true;
		} else {
			shouldShowClock = false;
		}
	}

	private void initElements() {
		if (shouldShowWebView) {
			wv.loadUrl(webcontent);
		} else {
			wv.setVisibility(View.GONE);
		}
		if (shouldShowWatermark) {
			ivParams();
		} else {
			iv2.setVisibility(View.GONE);
		}
		if (shouldShowClock) {
			clockParams();
		} else {
			digClock.setVisibility(View.GONE);
		}
		if (shouldShowNews) {
			rssTVParams();
			startNewsThread();
		} else {
			rssTV.setVisibility(View.GONE);
		}
		if (shouldShowWeather) {
			startWeatherThread();
		} else {
			weatherLayOut.setVisibility(View.GONE);
		}
		startWallpaperThread();
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

	private void rssTVParams() {
		rssTV.setRotation(Integer.valueOf(rotation));
		// get Density
		final int marginSide = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10, getResources()
						.getDisplayMetrics());
		final int marginTB = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 0, getResources()
						.getDisplayMetrics());

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		switch (Integer.valueOf(rotation)) {
		case 0:
			break;
		case 90:
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.setMargins(marginTB, marginSide, marginTB, marginSide);
			rssTV.setLayoutParams(lp);
			break;
		case 180:
			break;
		case 270:
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.setMargins(marginTB, marginSide, marginTB, marginSide);
			rssTV.setLayoutParams(lp);
			break;
		}
	}

	private void clockParams() {

		DigitalClock dc = (DigitalClock) findViewById(R.id.digitalClock);
		dc.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.charAt(4) == ':') {
					s.delete(4, 7);
				} else if (s.charAt(5) == ':') {
					s.delete(5, 8);
				}
			}
		});

		digClock.setRotation(Integer.valueOf(rotation));
		// get Density
		final int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
						.getDisplayMetrics());
		final int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 100, getResources()
						.getDisplayMetrics());
		final int marginSide = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10, getResources()
						.getDisplayMetrics());
		final int marginTB = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 0, getResources()
						.getDisplayMetrics());

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width,
				height);
		switch (Integer.valueOf(rotation)) {
		case 0:
			break;
		case 90:
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.setMargins(marginTB, marginSide, marginTB, marginSide);
			digClock.setLayoutParams(lp);
			break;
		case 180:
			break;
		case 270:
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.setMargins(marginTB, marginSide, marginTB, marginSide);
			digClock.setLayoutParams(lp);
			break;
		}
	}

	private void ivParams() {

		iv2.setRotation(Integer.valueOf(rotation));
		// get Density
		final int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
						.getDisplayMetrics());
		final int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 100, getResources()
						.getDisplayMetrics());
		final int marginSide = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 50, getResources()
						.getDisplayMetrics());
		final int marginTB = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 0, getResources()
						.getDisplayMetrics());

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width,
				height);
		switch (Integer.valueOf(rotation)) {
		case 0:
			break;
		case 90:
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.setMargins(marginTB, marginSide, marginTB, marginSide);
			iv2.setLayoutParams(lp);
			break;
		case 180:
			break;
		case 270:
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.setMargins(marginTB, marginSide, marginTB, marginSide);
			iv2.setLayoutParams(lp);
			break;

		}
		picasso.load(watermark).into(iv2);
		// iv2.setAlpha(100);
	}

	private void setFullScreen() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void setImage() throws IndexOutOfBoundsException {
		if (choice >= urls.size()) {
			choice = 0;
			try {
				picasso.load(urls.get(choice))
						.rotate(Integer.valueOf(rotation)).into(iv);
			} catch (Exception e) {
				e.printStackTrace();
			}
			choice++;
		} else {
			try {
				picasso.load(urls.get(choice))
						.rotate(Integer.valueOf(rotation)).into(iv);
			} catch (Exception e) {
				e.printStackTrace();
			}
			choice++;
		}
	}

	private class DoPOST extends AsyncTask<String, Void, Boolean> {
		// Context mContext = null;
		Exception exception = null;

		DoPOST(Context context, String nameToSearch) {
			// mContext = context;
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				if (!urls.isEmpty()) {
					urls.clear();
				}
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams
						.setConnectionTimeout(httpParameters, 15000);
				HttpConnectionParams.setSoTimeout(httpParameters, 15000);
				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpGet httppost = new HttpGet(apiURL);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				JSONObject jsonObject = new JSONObject(result);
				JSONObject jsonPhotos = jsonObject.getJSONObject("Images");
				Iterator<?> keys = jsonPhotos.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (jsonPhotos.get(key) instanceof JSONObject) {
						JSONObject jsonPhoto = (JSONObject) jsonPhotos.get(key);
						String image = jsonPhoto.getString("file");
						urls.add(baseURL + image);
					}
				}
				System.out.println("Debug: Wallpaper update.");

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("", "Error:", e);
				exception = e;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean valid) {
			if (exception != null) {
				text.SetText("Lost connection to host!");
				toast.setView(text);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();
			}
			setImage();
		}

	}

	private News parseNews(Document srcDoc) {
		News myNews = new News();

		myNews.headline = "\t\t\t\t" + "Latest Headlines:" + "\t\t\t\t"
				+ srcDoc.getElementsByTagName("title").item(2).getTextContent()
				+ "\t\t\t\t"
				+ srcDoc.getElementsByTagName("title").item(3).getTextContent()
				+ "\t\t\t\t"
				+ srcDoc.getElementsByTagName("title").item(4).getTextContent()
				+ "\t\t\t\t"
				+ srcDoc.getElementsByTagName("title").item(5).getTextContent()
				+ "\t\t\t\t";

		return myNews;
	}

	private Document convertNewsStringToDocument(String src) {

		Document dest = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dest;
	}

	private String QueryYahooNews() {

		String qResult = "";
		String queryString = rss;

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");
				}

				qResult = stringBuilder.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return qResult;
	}

	private Weather parseWeather(Document srcDoc) {
		int currentCode;
		Weather myWeather = new Weather();

		// <description>Yahoo! Weather for New York, NY</description>
		// myWeather.description = "Current Weather: Troy NY";

		myWeather.description = srcDoc.getElementsByTagName("description")
				.item(0).getTextContent();

		// <yweather:location.../>

		Node locationNode = srcDoc.getElementsByTagName("yweather:location")
				.item(0);
		myWeather.city = locationNode.getAttributes().getNamedItem("city")
				.getNodeValue().toString();
		myWeather.region = locationNode.getAttributes().getNamedItem("region")
				.getNodeValue().toString();
		myWeather.country = locationNode.getAttributes()
				.getNamedItem("country").getNodeValue().toString();

		// <yweather:condition.../>
		Node conditionNode = srcDoc.getElementsByTagName("yweather:condition")
				.item(0);
		myWeather.conditiontext = conditionNode.getAttributes()
				.getNamedItem("text").getNodeValue().toString();
		myWeather.conditiondate = conditionNode.getAttributes()
				.getNamedItem("date").getNodeValue().toString();
		currentCode = Integer
				.valueOf(myWeather.conditioncode = conditionNode
						.getAttributes().getNamedItem("code").getNodeValue()
						.toString());
		System.out.println("Debug: Weather code is " + currentCode + ".");
		setWeatherIcon(currentCode);

		// <yweather:wind.../>
		Node windNode = srcDoc.getElementsByTagName("yweather:wind").item(0);
		myWeather.windChill = windNode.getAttributes().getNamedItem("chill")
				.getNodeValue().toString();
		myWeather.windDirection = windNode.getAttributes()
				.getNamedItem("direction").getNodeValue().toString();
		myWeather.windSpeed = windNode.getAttributes().getNamedItem("speed")
				.getNodeValue().toString();

		// <yweather:astronomy.../>
		Node astronomyNode = srcDoc.getElementsByTagName("yweather:astronomy")
				.item(0);
		myWeather.sunrise = astronomyNode.getAttributes()
				.getNamedItem("sunrise").getNodeValue().toString();
		myWeather.sunset = astronomyNode.getAttributes().getNamedItem("sunset")
				.getNodeValue().toString();
		// forecast info
		Node forecastNode1 = srcDoc.getElementsByTagName("yweather:forecast")
				.item(0);
		myWeather.day1 = "\t\t"
				+ forecastNode1.getAttributes().getNamedItem("day")
						.getNodeValue().toString()
				+ ": "
				+ forecastNode1.getAttributes().getNamedItem("high")
						.getNodeValue().toString()
				+ "��F "
				+ forecastNode1.getAttributes().getNamedItem("text")
						.getNodeValue().toString() + "\n";

		Node forecastNode2 = srcDoc.getElementsByTagName("yweather:forecast")
				.item(1);

		myWeather.day2 = "\t\t"
				+ forecastNode2.getAttributes().getNamedItem("day")
						.getNodeValue().toString()
				+ ": "
				+ forecastNode2.getAttributes().getNamedItem("high")
						.getNodeValue().toString()
				+ "��F "
				+ forecastNode2.getAttributes().getNamedItem("text")
						.getNodeValue().toString() + "\n";

		Node forecastNode3 = srcDoc.getElementsByTagName("yweather:forecast")
				.item(2);

		myWeather.day3 = "\t\t"
				+ forecastNode3.getAttributes().getNamedItem("day")
						.getNodeValue().toString()
				+ ": "
				+ forecastNode3.getAttributes().getNamedItem("high")
						.getNodeValue().toString()
				+ "��F "
				+ forecastNode3.getAttributes().getNamedItem("text")
						.getNodeValue().toString() + "\n";

		Node forecastNode4 = srcDoc.getElementsByTagName("yweather:forecast")
				.item(3);

		myWeather.day4 = "\t\t"
				+ forecastNode4.getAttributes().getNamedItem("day")
						.getNodeValue().toString()
				+ ": "
				+ forecastNode4.getAttributes().getNamedItem("high")
						.getNodeValue().toString()
				+ "��F "
				+ forecastNode4.getAttributes().getNamedItem("text")
						.getNodeValue().toString() + "\n";

		Node forecastNode5 = srcDoc.getElementsByTagName("yweather:forecast")
				.item(4);

		myWeather.day5 = "\t\t"
				+ forecastNode5.getAttributes().getNamedItem("day")
						.getNodeValue().toString()
				+ ": "
				+ forecastNode5.getAttributes().getNamedItem("high")
						.getNodeValue().toString()
				+ "��F "
				+ forecastNode5.getAttributes().getNamedItem("text")
						.getNodeValue().toString() + "\n";

		forecast = "\t\t" + myWeather.day1 + myWeather.day2 + myWeather.day3
				+ myWeather.day4 + myWeather.day5 + "\t\t";

		return myWeather;
	}

	private void startWeatherThread() {
		bgThread = new Thread(new Runnable() {
			public void run() {
				while (visible == 1) {
					weatherString = QueryYahooWeather();
					weatherDoc = convertStringToDocument(weatherString);
					final Weather weatherResult = parseWeather(weatherDoc);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							weatherTV.invalidate();
							weatherTV.setText(weatherResult.toString());
							Picasso.with(getBaseContext()).load(weatherImg)
									.error(R.drawable.dunno).into(iv3);
							forecastTV.invalidate();
							forecastTV.setText(forecast);
							forecastTV.setSelected(true);
							System.out.println("Debug: Weather update.");
						}
					});
					try {
						Thread.sleep(600000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		bgThread.start();
	}

	private Document convertStringToDocument(String src) {

		Document dest = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dest;
	}

	private void startNewsThread() {
		rssThread = new Thread(new Runnable() {
			public void run() {
				while (visible == 1) {
					newsString = QueryYahooNews();
					newsDoc = convertNewsStringToDocument(newsString);
					final News newsResult = parseNews(newsDoc);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							rssTV.invalidate();
							rssTV.setText(newsResult.toString());
							rssTV.setSelected(true);
							System.out.println("Debug: News update.");
						}
					});
					try {
						Thread.sleep(600000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		rssThread.start();
	}

	private void startWallpaperThread() {
		wpThread = new Thread(new Runnable() {
			public void run() {
				while (visible == 1) {
					try {
						if (first) {
							first = false;
							mDoPOST = new DoPOST(Display.this, data);
							mDoPOST.execute("");
							Thread.sleep(Integer.valueOf(transition) * 1000);
						} else {
							Thread.sleep(Integer.valueOf(transition) * 1000);
							mDoPOST = new DoPOST(Display.this, data);
							mDoPOST.execute("");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		wpThread.start();
	}

	private String QueryYahooWeather() {

		String qResult = "";
		String queryString = getResources().getString(R.string.yahooWeatherAPI)
				.toString() + "2508215";

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");
				}

				qResult = stringBuilder.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return qResult;
	}

	// use our icons based on yahoo weather code.
	public void setWeatherIcon(int currentCode) {
		// using switch-case fall through to keep things short and neat.
		switch (currentCode) {
		// t-storm 3
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			weatherImg = R.drawable.tstorm3;
			break;
		// sleet
		case 5:
		case 6:
		case 7:
		case 8:
		case 10:
		case 18:
			weatherImg = R.drawable.sleet;
			break;
		// light rain
		case 9:
			weatherImg = R.drawable.light_rain;
			break;
		// shower 3
		case 11:
		case 12:
			weatherImg = R.drawable.shower3;
			break;
		// snow 4
		case 13:
		case 14:
		case 15:
		case 46:
			weatherImg = R.drawable.snow4;
			break;
		// snow 5
		case 16:
			weatherImg = R.drawable.snow5;
			break;
		// hail
		case 17:
		case 35:
			weatherImg = R.drawable.hail;
			break;
		// mist
		case 19:
		case 21:
			weatherImg = R.drawable.mist;
			break;
		// fog
		case 20:
		case 22:
			weatherImg = R.drawable.fog;
			break;
		// sunny
		case 23:
		case 24:
		case 25:
		case 32:
		case 34:
		case 36:
			weatherImg = R.drawable.sunny;
			break;
		// overcast
		case 26:
			weatherImg = R.drawable.overcast;
			break;
		// cloudy 4 night
		case 27:
			weatherImg = R.drawable.cloudy4_night;
			break;
		// cloudy 4
		case 28:
			weatherImg = R.drawable.cloudy4;
			break;
		// cloudy 1 night
		case 29:
			weatherImg = R.drawable.cloudy1_night;
			break;
		// cloudy 1
		case 30:
			weatherImg = R.drawable.cloudy1;
			break;
		// suny night
		case 31:
		case 33:
			weatherImg = R.drawable.sunny_night;
			break;
		// tstorm 2
		case 37:
		case 38:
		case 39:
		case 45:
		case 47:
			weatherImg = R.drawable.tstorm2;
			break;
		// shower 1
		case 40:
			weatherImg = R.drawable.shower1;
			break;
		// snow 5
		case 41:
		case 43:
			weatherImg = R.drawable.snow5;
			break;
		// snow 3
		case 42:
			weatherImg = R.drawable.snow3;
			break;
		// cloudy 2
		case 44:
			weatherImg = R.drawable.cloudy2;
			break;
		// not sure
		case 3200:
			weatherImg = R.drawable.dunno;
			break;

		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}