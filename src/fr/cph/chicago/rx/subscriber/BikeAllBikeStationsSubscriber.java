package fr.cph.chicago.rx.subscriber;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.List;

import fr.cph.chicago.R;
import fr.cph.chicago.activity.BikeStationActivity;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.util.Util;
import rx.Subscriber;

public class BikeAllBikeStationsSubscriber extends Subscriber<List<BikeStation>> {

    private final BikeStationActivity activity;
    private final int bikeStationId;
    private final SwipeRefreshLayout swipeRefreshLayout;

    public BikeAllBikeStationsSubscriber(@NonNull final BikeStationActivity activity, final int bikeStationId, @NonNull final SwipeRefreshLayout swipeRefreshLayout){
        this.activity = activity;
        this.bikeStationId = bikeStationId;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public void onNext(final List<BikeStation> onNext) {
        if (onNext != null) {
            for (final BikeStation station : onNext) {
                if (bikeStationId == station.getId()) {
                    activity.refreshStation(station);
                    final Bundle bundle = activity.getIntent().getExtras();
                    bundle.putParcelable(activity.getString(R.string.bundle_bike_station), station);
                    break;
                }
            }
        } else {
            Util.showOopsSomethingWentWrong(swipeRefreshLayout);
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        Util.showOopsSomethingWentWrong(swipeRefreshLayout);
    }

    @Override
    public void onCompleted() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
