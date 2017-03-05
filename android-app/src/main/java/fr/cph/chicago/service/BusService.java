package fr.cph.chicago.service;

import android.content.Context;

import com.annimon.stream.Optional;

import java.util.List;

import fr.cph.chicago.data.BusData;
import fr.cph.chicago.entity.Bus;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.BusDirections;
import fr.cph.chicago.entity.BusPattern;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.entity.BusStop;

public interface BusService {

    List<BusArrival> loadFavoritesBuses(Context context);

    List<BusStop> loadOneBusStop(Context context, String stopId, String bound);

    BusData loadLocalBusData(Context context);

    BusDirections loadBusDirections(Context context, String busRouteId);

    List<BusRoute> loadBusRoutes();

    List<BusArrival> loadFollowBus(Context context, String busId);

    Optional<BusPattern> loadBusPattern(Context context, String busRouteId, String bound);

    List<Bus> loadBus(Context context, int busId, String busRouteId);

    List<BusArrival> loadAroundBusArrivals(Context context, BusStop busStop);
}
