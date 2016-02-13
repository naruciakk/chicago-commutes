/**
 * Copyright 2016 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.connection.GStreetViewConnect;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.Xml;

/**
 * Activity that represents the bus stop
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class BusActivity extends Activity {
	/** Tag **/
	private static final String TAG = "BusActivity";
	/** List of bus arrivals **/
	private List<BusArrival> mBusArrivals;
	/** Buse route id **/
	private String mBusRouteId;
	/** Bound **/
	private String mBound;
	/** Bus stop id **/
	private Integer mBusStopId;
	/** Bus stop name **/
	private String mBusStopName;
	/** Bus route name **/
	private String mBusRouteName;
	/** Position **/
	private Double mLatitude, mLongitude;
	/** Images **/
	private ImageView mStreetViewImage, mMapImage, mDirectionImage, mFavoritesImage;
	/** Street view text **/
	private TextView mStreetViewText;
	/** Stop view **/
	private LinearLayout mStopsView;
	/** First time the activity is loaded **/
	private boolean mFirstLoad = true;
	/** First time the activity is loaded count **/
	private int mFirstLoadCount;
	/** Is added as favorite **/
	private boolean mIsFavorite;
	/** Menu **/
	private Menu mMenu;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(new CalligraphyContextWrapper(newBase));
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChicagoTracker.checkBusData(this);
		if (!this.isFinishing()) {
			// Load right xml
			setContentView(R.layout.activity_bus);

			if (mBusStopId == null && mBusRouteId == null && mBound == null && mBusStopName == null && mBusRouteName == null && mLatitude == null
					&& mLongitude == null) {
				this.mBusStopId = getIntent().getExtras().getInt("busStopId");
				this.mBusRouteId = getIntent().getExtras().getString("busRouteId");
				this.mBound = getIntent().getExtras().getString("bound");

				this.mBusStopName = getIntent().getExtras().getString("busStopName");
				this.mBusRouteName = getIntent().getExtras().getString("busRouteName");

				this.mLatitude = getIntent().getExtras().getDouble("latitude");
				this.mLongitude = getIntent().getExtras().getDouble("longitude");
			}

			Position position = new Position();
			position.setLatitude(mLatitude);
			position.setLongitude(mLongitude);

			this.mIsFavorite = isFavorite();

			this.mStopsView = (LinearLayout) findViewById(R.id.activity_bus_stops);

			TextView busRouteNameView = (TextView) findViewById(R.id.activity_bus_station_name);
			busRouteNameView.setText(mBusStopName);

			TextView busRouteNameView2 = (TextView) findViewById(R.id.activity_bus_station_value);
			busRouteNameView2.setText(mBusRouteName + " (" + mBound + ")");

			mStreetViewImage = (ImageView) findViewById(R.id.activity_bus_streetview_image);
			mStreetViewText = (TextView) findViewById(R.id.activity_bus_steetview_text);
			mMapImage = (ImageView) findViewById(R.id.activity_bus_map_image);

			mDirectionImage = (ImageView) findViewById(R.id.activity_bus_map_direction);

			mFavoritesImage = (ImageView) findViewById(R.id.activity_bus_favorite_star);
			if (mIsFavorite) {
				mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
			}
			mFavoritesImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BusActivity.this.switchFavorite();
				}
			});

			new DisplayGoogleStreetPicture().execute(position);

			new LoadData().execute();

			getActionBar().setDisplayHomeAsUpEnabled(true);

			Util.trackScreen(this, R.string.analytics_bus_details);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mBusStopId = savedInstanceState.getInt("busStopId");
		mBusRouteId = savedInstanceState.getString("busRouteId");
		mBound = savedInstanceState.getString("bound");
		mBusStopName = savedInstanceState.getString("busStopName");
		mBusRouteName = savedInstanceState.getString("busRouteName");
		mLatitude = savedInstanceState.getDouble("latitude");
		mLongitude = savedInstanceState.getDouble("longitude");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("busStopId", mBusStopId);
		savedInstanceState.putString("busRouteId", mBusRouteId);
		savedInstanceState.putString("bound", mBound);
		savedInstanceState.putString("busStopName", mBusStopName);
		savedInstanceState.putString("busRouteName", mBusRouteName);
		savedInstanceState.putDouble("latitude", mLatitude);
		savedInstanceState.putDouble("longitude", mLongitude);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.mMenu = menu;

		// Inflate menu with no search
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_no_search, menu);

		// Modify action bar title
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Bus");

		// Load top bar animation
		MenuItem refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setActionView(R.layout.progressbar);
		refreshMenuItem.expandActionView();

		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			// overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			return true;
		case R.id.action_refresh:

			// Load top bar animation
			MenuItem menuItem = item;
			menuItem.setActionView(R.layout.progressbar);
			menuItem.expandActionView();

			// Load data
			new LoadData().execute();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	/**
	 * Draw arrivals in current layout
	 */
	public final void drawArrivals() {
		if (mBusArrivals != null) {
			Map<String, TextView> mapRes = new HashMap<String, TextView>();
			if (this.mBusArrivals.size() != 0) {
				for (BusArrival arrival : this.mBusArrivals) {
					if (arrival.getRouteDirection().equals(mBound)) {
						String destination = arrival.getBusDestination();
						if (mapRes.containsKey(destination)) {
							TextView arrivalView = mapRes.get(destination);
							if (arrival.getIsDly()) {
								arrivalView.setText(arrivalView.getText() + " Delay");
							} else {
								arrivalView.setText(arrivalView.getText() + " " + arrival.getTimeLeft());
							}
						} else {
							TextView arrivalView = new TextView(ChicagoTracker.getAppContext());
							if (arrival.getIsDly()) {
								arrivalView.setText(arrival.getBusDestination() + ": Delay");
							} else {
								arrivalView.setText(arrival.getBusDestination() + ": " + arrival.getTimeLeft());
							}
							arrivalView.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.grey));
							mapRes.put(destination, arrivalView);
						}
					}
				}
			} else {
				TextView arrivalView = new TextView(ChicagoTracker.getAppContext());
				arrivalView.setTextColor(ChicagoTracker.getAppContext().getResources().getColor(R.color.grey));
				arrivalView.setText("No service scheduled");
				mapRes.put("", arrivalView);
			}
			mStopsView.removeAllViews();
			for (Entry<String, TextView> entry : mapRes.entrySet()) {
				mStopsView.addView(entry.getValue());
			}

		}
	}

	/**
	 * 
	 * @return
	 */
	public final boolean isFavorite() {
		boolean isFavorite = false;
		List<String> favorites = Preferences.getBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS);
		for (String fav : favorites) {
			if (fav.equals(mBusRouteId + "_" + mBusStopId + "_" + mBound)) {
				isFavorite = true;
				break;
			}
		}
		return isFavorite;
	}

	/**
	 * Load data class. Contact Bus CTA api to get arrival buses
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private final class LoadData extends AsyncTask<Void, Void, List<BusArrival>> {

		/** The exception that could potentially been thrown during request **/
		private TrackerException trackerException;

		@Override
		protected List<BusArrival> doInBackground(final Void... params) {
			MultiMap<String, String> reqParams = new MultiValueMap<String, String>();
			reqParams.put("rt", mBusRouteId);
			reqParams.put("stpid", String.valueOf(mBusStopId));
			CtaConnect connect = CtaConnect.getInstance();
			try {
				Xml xml = new Xml();

				// Connect to CTA API bus to get XML result of inc buses
				String xmlResult = connect.connect(CtaRequestType.BUS_ARRIVALS, reqParams);

				// Parse and return arrival buses
				return xml.parseBusArrivals(xmlResult);
			} catch (ParserException e) {
				this.trackerException = e;
			} catch (ConnectException e) {
				this.trackerException = e;
			}
			Util.trackAction(BusActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_bus,
					R.string.analytics_action_get_bus_arrival, 0);
			return null;
		}

		@Override
		protected final void onProgressUpdate(final Void... values) {
			// Get menu item and put it to loading mod
			if (mMenu != null) {
				MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
				refreshMenuItem.setActionView(R.layout.progressbar);
				refreshMenuItem.expandActionView();
			}
		}

		@Override
		protected final void onPostExecute(final List<BusArrival> result) {
			if (trackerException == null) {
				BusActivity.this.mBusArrivals = result;
				BusActivity.this.drawArrivals();
			} else {
				ChicagoTracker.displayError(BusActivity.this, trackerException);
			}
			if (!mFirstLoad) {
				// Stop refresh animation
				MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
				refreshMenuItem.collapseActionView();
				refreshMenuItem.setActionView(null);
			} else {
				setFirstLoad();
				MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
				refreshMenuItem.collapseActionView();
				refreshMenuItem.setActionView(null);
			}
		}
	}

	/**
	 * Load image from google street
	 * 
	 * @author Carl-Philipp Harmant
	 * @version 1
	 */
	private final class DisplayGoogleStreetPicture extends AsyncTask<Position, Void, Drawable> {

		/** Position of the stop **/
		private Position position;

		@Override
		protected final Drawable doInBackground(final Position... params) {
			GStreetViewConnect connect = GStreetViewConnect.getInstance();
			try {
				this.position = params[0];
				Util.trackAction(BusActivity.this, R.string.analytics_category_req, R.string.analytics_action_get_google,
						R.string.analytics_action_get_google_map_street_view, 0);
				return connect.connect(params[0]);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		}

		@Override
		protected final void onPostExecute(final Drawable result) {
			int height = (int) getResources().getDimension(R.dimen.activity_station_street_map_height);
			LayoutParams params = (LayoutParams) BusActivity.this.mStreetViewImage.getLayoutParams();
			ViewGroup.LayoutParams params2 = BusActivity.this.mStreetViewImage.getLayoutParams();
			params2.height = height;
			params2.width = params.width;
			BusActivity.this.mStreetViewText.setText("Street view");
			BusActivity.this.mStreetViewImage.setLayoutParams(params2);
			BusActivity.this.mStreetViewImage.setImageDrawable(result);
			BusActivity.this.mStreetViewImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = String.format(Locale.ENGLISH, "google.streetview:cbll=%f,%f&cbp=1,180,,0,1&mz=1", position.getLatitude(),
							position.getLongitude());
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException ex) {
						// Redirect to browser if the user does not have google map installed
						uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=&layer=c&cbll=%f,%f&cbp=11,0,0,0,0",
								position.getLatitude(), position.getLongitude());
						Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					}
				}
			});
			BusActivity.this.mMapImage.setImageDrawable(ChicagoTracker.getAppContext().getResources().getDrawable(R.drawable.da_turn_arrive));
			BusActivity.this.mMapImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?z=12&t=m&q=loc:" + position.getLatitude() + "+" + position.getLongitude();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});
			BusActivity.this.mDirectionImage.setImageDrawable(ChicagoTracker.getAppContext().getResources()
					.getDrawable(R.drawable.ic_directions_walking));
			BusActivity.this.mDirectionImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/?f=d&daddr=" + position.getLatitude() + "," + position.getLongitude() + "&dirflg=w";
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(i);
				}
			});

			// Stop menu refresh animation
			MenuItem refreshMenuItem = mMenu.findItem(R.id.action_refresh);
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
			setFirstLoad();

		}
	}

	private void setFirstLoad() {
		if (mFirstLoad && mFirstLoadCount == 1) {
			mFirstLoad = false;
		}
		mFirstLoadCount++;
	}

	/**
	 * Add or remove from favorites
	 */
	private final void switchFavorite() {
		if (mIsFavorite) {
			Util.removeFromBusFavorites(mBusRouteId, String.valueOf(mBusStopId), mBound, ChicagoTracker.PREFERENCE_FAVORITES_BUS);
			mIsFavorite = false;
		} else {
			Util.addToBusFavorites(mBusRouteId, String.valueOf(mBusStopId), mBound, ChicagoTracker.PREFERENCE_FAVORITES_BUS);
			Log.i(TAG, "busRouteName: " + mBusRouteName);
			Preferences.addBusRouteNameMapping(String.valueOf(mBusStopId), mBusRouteName);
			Preferences.addBusStopNameMapping(String.valueOf(mBusStopId), mBusStopName);
			mIsFavorite = true;
		}
		if (mIsFavorite) {
			mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_active));
		} else {
			mFavoritesImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_disabled));
		}
	}
}
