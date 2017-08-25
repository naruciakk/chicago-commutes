/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.activity;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.listener.GoogleMapDirectionOnClickListener;
import fr.cph.chicago.core.listener.GoogleMapOnClickListener;
import fr.cph.chicago.core.listener.GoogleStreetOnClickListener;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.Position;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.exception.TrackerException;
import fr.cph.chicago.service.BusService;
import fr.cph.chicago.service.PreferenceService;
import fr.cph.chicago.util.Util;

import static fr.cph.chicago.Constants.BUSES_PATTERN_URL;

/**
 * Activity that represents the bus stop
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public class BusActivity extends AbstractStationActivity {

    @BindView(R.id.activity_bus_stop_swipe_refresh_layout)
    SwipeRefreshLayout scrollView;
    @BindView(R.id.activity_favorite_star)
    ImageView favoritesImage;
    @BindView(R.id.activity_bus_stops)
    LinearLayout stopsView;
    @BindView(R.id.activity_bus_streetview_image)
    ImageView streetViewImage;
    @BindView(R.id.activity_bus_steetview_text)
    TextView streetViewText;
    @BindView(R.id.activity_map_image)
    ImageView mapImage;
    @BindView(R.id.activity_map_direction)
    ImageView directionImage;
    @BindView(R.id.favorites_container)
    LinearLayout favoritesImageContainer;
    @BindView(R.id.walk_container)
    LinearLayout walkContainer;
    @BindView(R.id.map_container)
    LinearLayout mapContainer;
    @BindView(R.id.activity_bus_station_value)
    TextView busRouteNameView2;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.bundle_bus_stop_id)
    String bundleBusStopId;
    @BindString(R.string.bundle_bus_route_id)
    String bundleBusRouteId;
    @BindString(R.string.bundle_bus_bound)
    String bundleBusBound;
    @BindString(R.string.bundle_bus_bound_title)
    String bundleBusBoundTitle;
    @BindString(R.string.bundle_bus_stop_name)
    String bundleBusStopName;
    @BindString(R.string.bundle_bus_route_name)
    String bundleBusRouteName;
    @BindString(R.string.bundle_bus_latitude)
    String bundleBusLatitude;
    @BindString(R.string.bundle_bus_longitude)
    String bundleBusLongitude;
    @BindString(R.string.bus_activity_no_service)
    String busActivityNoService;
    @BindString(R.string.analytics_bus_details)
    String analyticsBusDetails;
    @BindString(R.string.request_rt)
    String requestRt;
    @BindString(R.string.request_stop_id)
    String requestStopId;

    @BindColor(R.color.grey_5)
    int grey_5;
    @BindColor(R.color.grey)
    int grey;
    @BindColor(R.color.yellowLineDark)
    int yellowLineDark;

    private final Util util;
    private final PreferenceService preferenceService;
    private final BusService busService;

    private List<BusArrival> busArrivals;
    private String busRouteId, bound, boundTitle;
    private Integer busStopId;
    private String busStopName, busRouteName;
    private double latitude, longitude;
    private boolean isFavorite;

    public BusActivity() {
        util = Util.INSTANCE;
        preferenceService = PreferenceService.INSTANCE;
        busService = BusService.INSTANCE;
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.Companion.checkBusData(this);
        if (!this.isFinishing()) {
            setContentView(R.layout.activity_bus);
            ButterKnife.bind(this);

            if (busStopId == null || busRouteId == null || bound == null || busStopName == null || busRouteName == null || boundTitle == null) {
                final Bundle extras = getIntent().getExtras();
                busStopId = extras.getInt(bundleBusStopId);
                busRouteId = extras.getString(bundleBusRouteId);
                bound = extras.getString(bundleBusBound);
                boundTitle = extras.getString(bundleBusBoundTitle);
                busStopName = extras.getString(bundleBusStopName);
                busRouteName = extras.getString(bundleBusRouteName);
                latitude = extras.getDouble(bundleBusLatitude);
                longitude = extras.getDouble(bundleBusLongitude);
            }

            final Position position = new Position(latitude, longitude);

            isFavorite = isFavorite();

            mapImage.setColorFilter(grey_5);
            directionImage.setColorFilter(grey_5);
            favoritesImageContainer.setOnClickListener(v -> switchFavorite());

            if (isFavorite) {
                favoritesImage.setColorFilter(yellowLineDark);
            } else {
                favoritesImage.setColorFilter(grey_5);
            }
            scrollView.setOnRefreshListener(() -> new LoadStationDataTask().execute());
            streetViewImage.setOnClickListener(new GoogleStreetOnClickListener(latitude, longitude));
            mapContainer.setOnClickListener(new GoogleMapOnClickListener(latitude, longitude));
            walkContainer.setOnClickListener(new GoogleMapDirectionOnClickListener(latitude, longitude));

            final String busRouteName2 = busRouteName + " (" + boundTitle + ")";
            busRouteNameView2.setText(busRouteName2);

            // Load google street picture and data
            loadGoogleStreetImage(position, streetViewImage, streetViewText);
            new LoadStationDataTask().execute();

            setToolBar();

            // Google analytics
            util.trackScreen((App) getApplication(), analyticsBusDetails);
        }
    }

    private void setToolBar() {
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener((item -> {
            scrollView.setRefreshing(true);
            new LoadStationDataTask().execute();
            return false;
        }));
        util.setWindowsColor(this, toolbar, TrainLine.NA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }
        toolbar.setTitle(busRouteId + " - " + busStopName);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setOnClickListener(v -> finish());
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        busStopId = savedInstanceState.getInt(bundleBusStopId);
        busRouteId = savedInstanceState.getString(bundleBusRouteId);
        bound = savedInstanceState.getString(bundleBusBound);
        boundTitle = savedInstanceState.getString(bundleBusBoundTitle);
        busStopName = savedInstanceState.getString(bundleBusStopName);
        busRouteName = savedInstanceState.getString(bundleBusRouteName);
        latitude = savedInstanceState.getDouble(bundleBusLatitude);
        longitude = savedInstanceState.getDouble(bundleBusLongitude);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putInt(bundleBusStopId, busStopId);
        savedInstanceState.putString(bundleBusRouteId, busRouteId);
        savedInstanceState.putString(bundleBusBound, bound);
        savedInstanceState.putString(bundleBusBoundTitle, boundTitle);
        savedInstanceState.putString(bundleBusStopName, busStopName);
        savedInstanceState.putString(bundleBusRouteName, busRouteName);
        savedInstanceState.putDouble(bundleBusLatitude, latitude);
        savedInstanceState.putDouble(bundleBusLongitude, longitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void setBusArrivals(@NonNull final List<BusArrival> busArrivals) {
        this.busArrivals = busArrivals;
    }

    /**
     * Draw arrivals in current layout
     */
    public void drawArrivals() {
        final Map<String, List<TextView>> tempMap = new HashMap<>();
        if (busArrivals.isEmpty()) {
            final TextView arrivalView = new TextView(getApplicationContext());
            arrivalView.setTextColor(grey);
            arrivalView.setText(busActivityNoService);
            tempMap.put("", Collections.singletonList(arrivalView));
        } else {
            Stream.of(busArrivals)
                .forEach(arrival -> {
                    final String destination = arrival.getBusDestination();
                    if (tempMap.containsKey(destination)) {
                        final List<TextView> textViews = tempMap.get(destination);
                        final TextView arrivalView = new TextView(getApplicationContext());
                        arrivalView.setText(arrival.isDelay() ? " Delay" : " " + arrival.getTimeLeft());
                        textViews.add(arrivalView);
                    } else {
                        final List<TextView> textViews = new ArrayList<>();
                        final TextView destinationView = new TextView(getApplicationContext());
                        destinationView.setText(destination + ":   ");
                        destinationView.setTextColor(grey);
                        destinationView.setTypeface(null, Typeface.BOLD);
                        final TextView arrivalView = new TextView(getApplicationContext());
                        arrivalView.setText(arrival.isDelay() ? " Delay" : " " + arrival.getTimeLeft());
                        textViews.add(destinationView);
                        textViews.add(arrivalView);
                        tempMap.put(destination, textViews);
                    }
                });
        }
        stopsView.removeAllViews();
        Stream.of(tempMap.entrySet()).flatMap(stringListEntry -> Stream.of(stringListEntry.getValue())).forEach(textView -> stopsView.addView(textView));
    }

    @Override
    protected boolean isFavorite() {
        return preferenceService.isStopFavorite(busRouteId, busStopId, boundTitle);
    }

    /**
     * Add or remove from favorites
     */
    private void switchFavorite() {
        if (isFavorite) {
            preferenceService.removeFromBusFavorites(busRouteId, String.valueOf(busStopId), boundTitle, scrollView);
            favoritesImage.setColorFilter(grey_5);
            isFavorite = false;
        } else {
            preferenceService.addToBusFavorites(busRouteId, String.valueOf(busStopId), boundTitle, scrollView);
            preferenceService.addBusRouteNameMapping(String.valueOf(busStopId), busRouteName);
            preferenceService.addBusStopNameMapping(String.valueOf(busStopId), busStopName);
            favoritesImage.setColorFilter(yellowLineDark);
            isFavorite = true;
        }
    }

    private class LoadStationDataTask extends AsyncTask<Void, Void, List<BusArrival>> {

        private TrackerException trackerException;

        @Override
        protected List<BusArrival> doInBackground(final Void... params) {
            try {
                return busService.loadBusArrivals(requestRt, busRouteId, requestStopId, busStopId,
                    arrival -> arrival.getRouteDirection().equals(bound) || arrival.getRouteDirection().equals(boundTitle));
            } catch (final ParserException | ConnectException e) {
                this.trackerException = e;
            }
            util.trackAction((App) BusActivity.this.getApplication(), R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_PATTERN_URL);
            return null;
        }

        @Override
        protected final void onProgressUpdate(final Void... values) {
        }

        @Override
        protected final void onPostExecute(final List<BusArrival> result) {
            if (trackerException == null) {
                setBusArrivals(result);
                drawArrivals();
            } else {
                util.showNetworkErrorMessage(scrollView);
            }
            scrollView.setRefreshing(false);
        }
    }
}
