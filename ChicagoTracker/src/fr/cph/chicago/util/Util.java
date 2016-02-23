/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.enumeration.TrainLine;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Util {

	private static final String TAG = Util.class.getSimpleName();
	/** **/
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	public static int generateViewId() {
		for (; ; ) {
			final int result = sNextGeneratedId.get();
			// aapt-generated IDs have the high byte nonzero; clamp to the range under that.
			int newValue = result + 1;
			if (newValue > 0x00FFFFFF)
				newValue = 1; // Roll over to 1, not 0.
			if (sNextGeneratedId.compareAndSet(result, newValue)) {
				return result;
			}
		}
	}

	/**
	 * Get property from file
	 *
	 * @param property the property to get
	 * @return the value of the property
	 */
	public static String getProperty(final String property) {
		final Properties prop = new Properties();
		try {
			prop.load(ChicagoTracker.getAppContext().getAssets().open("app.properties"));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		}
		return prop.getProperty(property, null);
	}

	/**
	 * Add to train favorites
	 *
	 * @param stationId  the station id
	 * @param preference the preference
	 */
	public static void addToTrainFavorites(final Integer stationId, final String preference) {
		final List<Integer> favorites = Preferences.getTrainFavorites(preference);
		if (!favorites.contains(stationId)) {
			favorites.add(stationId);
			Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Remove train from favorites
	 *
	 * @param stationId  the station id
	 * @param preference the preference
	 */
	public static void removeFromTrainFavorites(final Integer stationId, final String preference) {
		final List<Integer> favorites = Preferences.getTrainFavorites(preference);
		favorites.remove(stationId);
		Preferences.saveTrainFavorites(ChicagoTracker.PREFERENCE_FAVORITES_TRAIN, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Remove from bus favorites
	 *
	 * @param busRouteId the bus route id
	 * @param busStopId  the bus stop id
	 * @param bound      the bus bound
	 * @param preference the preference
	 */
	public static void removeFromBusFavorites(final String busRouteId, final String busStopId, final String bound, final String preference) {
		final String id = busRouteId + "_" + busStopId + "_" + bound;
		final List<String> favorites = Preferences.getBusFavorites(preference);
		favorites.remove(id);
		Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Add to bus favorites
	 *
	 * @param busRouteId the bus route id
	 * @param busStopId  the bus stop id
	 * @param bound      the bus bound
	 * @param preference the preference
	 */
	public static void addToBusFavorites(final String busRouteId, final String busStopId, final String bound, final String preference) {
		final String id = busRouteId + "_" + busStopId + "_" + bound;
		final List<String> favorites = Preferences.getBusFavorites(preference);
		if (!favorites.contains(id)) {
			favorites.add(id);
			Preferences.saveBusFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BUS, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	public static void addToBikeFavorites(final int stationId, final String preference) {
		final List<String> favorites = Preferences.getBikeFavorites(preference);
		if (!favorites.contains(String.valueOf(stationId))) {
			favorites.add(String.valueOf(stationId));
			Preferences.saveBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE, favorites);
		}
		Toast.makeText(ChicagoTracker.getAppContext(), "Adding to favorites", Toast.LENGTH_SHORT).show();
	}

	public static void removeFromBikeFavorites(final int stationId, final String preference) {
		final List<String> favorites = Preferences.getBikeFavorites(preference);
		favorites.remove(String.valueOf(stationId));
		Preferences.saveBikeFavorites(ChicagoTracker.PREFERENCE_FAVORITES_BIKE, favorites);
		Toast.makeText(ChicagoTracker.getAppContext(), "Removing from favorites", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Decode bus favorites
	 *
	 * @param fav the favorites
	 * @return a tab containing the route id, the stop id and the bound
	 */
	public static String[] decodeBusFavorite(final String fav) {
		final String[] res = new String[3];
		final int first = fav.indexOf('_');
		final String routeId = fav.substring(0, first);
		final int sec = fav.indexOf('_', first + 1);
		final String stopId = fav.substring(first + 1, sec);
		final String bound = fav.substring(sec + 1, fav.length());
		res[0] = routeId;
		res[1] = stopId;
		res[2] = bound;
		return res;
	}

	public static final Comparator<BikeStation> BIKE_COMPARATOR_NAME = new BikeStationComparator();

	private static final class BikeStationComparator implements Comparator<BikeStation> {
		@Override
		public int compare(final BikeStation station1, final BikeStation station2) {
			return station1.getName().compareTo(station2.getName());
		}
	}

	public static boolean isNetworkAvailable() {
		final ConnectivityManager connectivityManager = (ConnectivityManager) ChicagoTracker.getAppContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static int[] getScreenSize() {
		final WindowManager wm = (WindowManager) ChicagoTracker.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		final Display display = wm.getDefaultDisplay();
		final Point size = new Point();
		display.getSize(size);
		return new int[] { size.x, size.y };
	}

	/**
	 * Google analytics track screen
	 *
	 * @param screen the screen name
	 * @param str    the label to send
	 */
	public static void trackScreen(final String screen) {
		final Tracker t = ChicagoTracker.getTracker();
		t.setScreenName(screen);
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	public static void trackAction(Activity activity, int category, int action, int label, int value) {
		final Tracker tracker = ChicagoTracker.getTracker();
		tracker.send(new HitBuilders.EventBuilder().setCategory(activity.getString(category)).setAction(activity.getString(action))
				.setLabel(activity.getString(label)).setValue(value).build());
	}

	public static void setToolbarColor(final Activity activity, final Toolbar toolbar, final TrainLine trainLine) {
		int backgroundColor = 0;
		int statusBarColor = 0;
		switch (trainLine) {
		case BLUE:
			backgroundColor = R.color.blueLine;
			statusBarColor = R.color.blueLineDark;
			break;
		case BROWN:
			backgroundColor = R.color.brownLine;
			statusBarColor = R.color.brownLineDark;
			break;
		case GREEN:
			backgroundColor = R.color.greenLine;
			statusBarColor = R.color.greenLineDark;
			break;
		case ORANGE:
			backgroundColor = R.color.orangeLine;
			statusBarColor = R.color.orangeLineDark;
			break;
		case PINK:
			backgroundColor = R.color.pinkLine;
			statusBarColor = R.color.pinkLineDark;
			break;
		case PURPLE:
			backgroundColor = R.color.purpleLine;
			statusBarColor = R.color.purpleLineDark;
			break;
		case RED:
			backgroundColor = R.color.redLine;
			statusBarColor = R.color.redLineDark;
			break;
		case YELLOW:
			backgroundColor = R.color.yellowLine;
			statusBarColor = R.color.yellowLineDark;
			break;
		case NA:
			backgroundColor = R.color.primaryColor;
			statusBarColor = R.color.primaryColorDark;
			break;
		}
		toolbar.setBackgroundColor(activity.getResources().getColor(backgroundColor));
		toolbar.setTitleTextColor(activity.getResources().getColor(R.color.white));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(activity.getResources().getColor(statusBarColor));
		}
	}
}
