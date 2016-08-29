package com.example.ansem.niceweather;

import org.json.JSONException;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ansem.niceweather.model.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, FragmentManager.OnBackStackChangedListener {

	private static final int REQUEST_CODE_PERMISSIONS = 1;

	private static TextView weatherText;

	private static SeekBar minTempBar;
	private static TextView minTempText;
	private static SeekBar maxTempBar;
	private static TextView maxTempText;
	private static SeekBar cloudsBar;
	private static TextView cloudsText;
	private static SeekBar rainBar;
	private static TextView rainText;

	private static TextView temp;
	private static ImageView imgView;
	private static ProgressBar loadingCircle;

	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;

	public Weather weather;

	private static SharedPreferences prefs;

	private boolean isAnimating = false;

	/**
	 * A handler object, used for deferring UI operations.
	 */
	private Handler mHandler = new Handler();
	/**
	 * Whether or not we're showing the back of the card (otherwise showing the front).
	 */
	private boolean mShowingBack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			// If there is no saved instance state, add a fragment representing the
			// front of the card to this activity. If there is saved instance state,
			// this fragment will have already been added to the activity.
			getFragmentManager()
					.beginTransaction()
					.add(R.id.container, new CardFrontFragment())
					.commit();
		} else {
			mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
		}
		// Monitor back stack changes to ensure the action bar shows the appropriate
		// button (either "photo" or "info").
		getFragmentManager().addOnBackStackChangedListener(this);

		prefs = this.getSharedPreferences("com.example.ansem.niceweather", Context.MODE_PRIVATE);

		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		}
	}

	private void setUpSeekBars() {
		final MainActivity a = this;
		minTempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			int value = -1;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				value = progressValue;
				minTempText.setText("Minimal Temperature: "+ value + "°C");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(value!=-1) {
					//finished sliding, save in preferences
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("com.example.ansem.niceweather.minTemp", value);
					//maxtemp cant be higher than mintemp
					int maxTemp = prefs.getInt("com.example.ansem.niceweather.maxTemp", 30);
					if (maxTemp <= value) {
						editor.putInt("com.example.ansem.niceweather.maxTemp", value + 1);
						maxTempBar.setProgress(value -9);
						maxTempText.setText("Maximal Temperature: " + (value + 1) + "°C");
					}
					editor.apply();
				}
			}
		});

		maxTempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			int value = -1;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				value = progressValue+10;
				maxTempText.setText("Maximal Temperature: "+ (value) + "°C");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(value!=-1) {
					//finished sliding, save in preferences
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("com.example.ansem.niceweather.maxTemp", value);

					//mintemp cant be higher than max temp
					int minTemp = prefs.getInt("com.example.ansem.niceweather.minTemp", 0);
					if (minTemp >= value) {
						editor.putInt("com.example.ansem.niceweather.minTemp", value - 1);
						minTempBar.setProgress(value - 1);
						minTempText.setText("Minimal Temperature: " + (value - 1) + "°C");
					}
					editor.apply();
				}
			}
		});

		cloudsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			int value = -1;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				value = progressValue;
				cloudsText.setText("Maximal amount of clouds: "+ value + "%");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(value!=-1) {
					//finished sliding, save in preferences
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("com.example.ansem.niceweather.maxClouds", value);
					editor.apply();
				}
			}
		});

		rainBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			float value = -1;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				value = ((float)progressValue)/2;
				rainText.setText("Maximal amount of rain the past 3 hours: " + value);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(value!=-1) {
					//finished sliding, save in preferences
					SharedPreferences.Editor editor = prefs.edit();
					editor.putFloat("com.example.ansem.niceweather.maxRain", value);
					editor.apply();
				}
			}
		});
	}

	public void checkWeather(){
		if(weather!=null) {
			//select correct image to display
			int id = weather.currentCondition.getId();
			int drawable;
			if (id >= 300 && id < 400) {
				drawable = R.drawable.slightdrizzle;
			} else if (id >= 500 && id < 600) {
				drawable = R.drawable.drizzle;
			} else if (id >= 600 && id < 700) {
				drawable = R.drawable.snow;
			} else if (id >= 700 && id < 800) {
				drawable = R.drawable.haze;
			} else if (id == 800) {
				drawable = R.drawable.sunny;
			} else if (id > 800 && id < 900) {
				drawable = R.drawable.cloudy;
			} else {
				drawable = R.drawable.thunderstorms;
			}

			imgView.setImageResource(drawable);
			imgView.setVisibility(View.VISIBLE);
			loadingCircle.setVisibility(View.GONE);

			// show the temperature
			temp.setText("" + weather.temperature.getTemp() + "°C");

			//check which text to show
			int minTemp = prefs.getInt("com.example.ansem.niceweather.minTemp", 0);
			int maxTemp = prefs.getInt("com.example.ansem.niceweather.maxTemp", 50);
			int maxClouds = prefs.getInt("com.example.ansem.niceweather.maxClouds", 100);
			float maxRain = prefs.getFloat("com.example.ansem.niceweather.maxRain", 5f);
			if(weather.rain.getAmount()<=maxRain){
				if(weather.clouds.getPerc()<=maxClouds){
					if(weather.temperature.getTemp()>=minTemp){
						if(weather.temperature.getTemp()<=maxTemp){
							weatherText.setText("It's nice outside!");
						} else {
							weatherText.setText("It's very hot..");
						}
					} else {
						weatherText.setText("It's a tad chilly..");
					}
				} else {
					weatherText.setText("It's very cloudy right now");
				}
			} else {
				weatherText.setText("It has been raining a lot..");
			}
			//display animation if it's sunny
			if(weather.currentCondition.getId()==800){
				if(!isAnimating) {
					RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					rotate.setDuration(10000);
					rotate.setRepeatCount(Animation.INFINITE);
					rotate.setInterpolator(new LinearInterpolator());
					imgView.startAnimation(rotate);
					isAnimating = true;
				}
			} else {
				//clear animation in case still running
				imgView.clearAnimation();
				isAnimating=false;
			}
		} else {
			displayError("Something went wrong while fetching your location");
		}
	}

	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add either a "sunny" or "settings" button to the action bar, depending on which page
		// is currently selected.
		MenuItem item = menu.add(Menu.NONE, R.id.action_flip, Menu.NONE,
				mShowingBack
						? R.string.weather
						: R.string.settings);
		item.setIcon(mShowingBack
				? R.drawable.sunny
				: R.drawable.ic_setting_dark);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_flip:
				flipCard();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void flipCard() {
		if (mShowingBack) {
			getFragmentManager().popBackStack();
			return;
		}
		// Flip to the back.
		mShowingBack = true;
		// no longer animating
		imgView.clearAnimation();
		isAnimating = false;
		// Create and commit a new fragment transaction that adds the fragment for the back of
		// the card, uses custom animations, and is part of the fragment manager's back stack.
		getFragmentManager()
				.beginTransaction()
				// Replace the default fragment animations with animator resources representing
				// rotations when switching to the back of the card, as well as animator
				// resources representing rotations when flipping back to the front (e.g. when
				// the system Back button is pressed).
				.setCustomAnimations(
						R.animator.card_flip_right_in, R.animator.card_flip_right_out,
						R.animator.card_flip_left_in, R.animator.card_flip_left_out)
				// Replace any fragments currently in the container view with a fragment
				// representing the next page (indicated by the just-incremented currentPage
				// variable).
				.replace(R.id.container, new CardBackFragment())
				// Add this transaction to the back stack, allowing users to press Back
				// to get to the front of the card.
				.addToBackStack(null)
				// Commit the transaction.
				.commit();
		// Defer an invalidation of the options menu (on modern devices, the action bar). This
		// can't be done immediately because the transaction may not yet be committed. Commits
		// are asynchronous in that they are posted to the main thread's message loop.
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidateOptionsMenu();
			}
		});
	}

	@Override
	public void onBackStackChanged() {
		mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
		// When the back stack changes, invalidate the options menu (action bar).
		invalidateOptionsMenu();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PERMISSIONS);
			return;
		}
		accessLocation();

	}

	private void accessLocation() {
		try {
			mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
					mGoogleApiClient);
			if (mLastLocation != null) {
				WeatherTask task = new WeatherTask(this);
				task.execute(mLastLocation);
			}
		} catch (SecurityException e){
			displayError("Something went wrong while fetching your location");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_PERMISSIONS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    accessLocation();
				} else {
					displayError("This app needs to access your location");
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public void onConnectionSuspended(int i) {}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {	}

	public void displayError(String error){
		imgView.setVisibility(View.GONE);
		loadingCircle.setVisibility(View.GONE);
		weatherText.setText(error);
	}

	private class WeatherTask extends AsyncTask<Location, Void, Weather> {
		public MainActivity activity;

		public WeatherTask(MainActivity a) {
			this.activity = a;
		}

		@Override
		protected Weather doInBackground(Location... params) {
			Weather weather = new Weather();
			if (params.length > 0) {
				String data = ((new WeatherHttpClient()).getWeatherData(params[0].getLatitude(), params[0].getLongitude()));
				if (data != null) {
					try {
						weather = JSONWeatherParser.getWeather(data);
						activity.weather = weather;
					} catch (JSONException e) {
						e.printStackTrace();
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								displayError("Something went wrong while fetching the weather");
							}
						});
					}
				} else {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							displayError("Something went wrong while fetching the weather");
						}
					});
				}
			}
			return weather;
		}

		@Override
		protected void onPostExecute(Weather weather) {
			super.onPostExecute(weather);
			if (weather != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.checkWeather();
					}
				});
			}
		}
	}

	/**
	 * A fragment representing the front of the card.
	 */
	public static class CardFrontFragment extends Fragment {
		public CardFrontFragment() {
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View inflate = inflater.inflate(R.layout.fragment_card_front, container, false);
			weatherText = (TextView) inflate.findViewById(R.id.weatherText);
			temp = (TextView) inflate.findViewById(R.id.temp);
			imgView = (ImageView) inflate.findViewById(R.id.condIcon);
			loadingCircle = (ProgressBar) inflate.findViewById(R.id.loadingCircle);
			MainActivity activity = (MainActivity)getActivity();
			activity.checkWeather();
			return inflate;
		}
	}
	/**
	 * A fragment representing the back of the card.
	 */
	public static class CardBackFragment extends Fragment {
		public CardBackFragment() {
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View inflate = inflater.inflate(R.layout.fragment_card_back, container, false);

			minTempBar = (SeekBar) inflate.findViewById(R.id.minimalTemp);
			minTempText = (TextView) inflate.findViewById(R.id.minimalTempText);
			maxTempBar = (SeekBar) inflate.findViewById(R.id.maximalTemp);
			maxTempText = (TextView) inflate.findViewById(R.id.maximalTempText);
			cloudsBar = (SeekBar) inflate.findViewById(R.id.maximalClouds);
			cloudsText = (TextView) inflate.findViewById(R.id.maximalCloudsText);
			rainBar = (SeekBar) inflate.findViewById(R.id.maximalRain);
			rainText = (TextView) inflate.findViewById(R.id.maximalRainText);

			//init preferences for weather sliders
			int minTemp = prefs.getInt("com.example.ansem.niceweather.minTemp", 0);
			minTempBar.setProgress(minTemp);
			minTempText.setText("Minimal Temperature: "+ minTemp + "°C");
			int maxTemp = prefs.getInt("com.example.ansem.niceweather.maxTemp", 30);
			maxTempBar.setProgress(maxTemp-10);
			maxTempText.setText("Maximal Temperature: "+ maxTemp + "°C");
			int maxClouds = prefs.getInt("com.example.ansem.niceweather.maxClouds", 100);
			cloudsBar.setProgress(maxClouds);
			cloudsText.setText("Maximal amount of clouds: "+ maxClouds + "%");
			float maxRain = prefs.getFloat("com.example.ansem.niceweather.maxRain", 5f);
			rainBar.setProgress(Math.round(maxRain*2));
			rainText.setText("Maximal amount of rain the past 3 hours: " + maxRain);

			MainActivity activity = (MainActivity)getActivity();
			activity.setUpSeekBars();
			return inflate;
		}
	}
}
