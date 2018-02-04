package fr.cph.chicago.entity.dto

import android.util.SparseArray
import com.fasterxml.jackson.annotation.JsonProperty
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.BusArrival
import fr.cph.chicago.entity.BusRoute
import fr.cph.chicago.entity.TrainArrival
import org.apache.commons.lang3.StringUtils
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator

data class BusArrivalDTO(val busArrivals: List<BusArrival>, val error: Boolean)

data class TrainArrivalDTO(val trainArrivalSparseArray: SparseArray<TrainArrival>, val error: Boolean)

data class FirstLoadDTO(
    val busRoutesError: Boolean,
    val bikeStationsError: Boolean,
    val busRoutes: List<BusRoute>,
    val bikeStations: List<BikeStation>)

data class FavoritesDTO(
    val trainArrivalDTO: TrainArrivalDTO,
    val busArrivalDTO: BusArrivalDTO,
    val bikeError: Boolean,
    val bikeStations: List<BikeStation>)

data class DivvyDTO(@JsonProperty("stationBeanList") val stations: List<BikeStation>)

data class BusFavoriteDTO(val routeId: String, val stopId: String, val bound: String)

data class BusDetailsDTO(
    val busRouteId: String,
    val bound: String,
    val boundTitle: String,
    val stopId: String,
    val routeName: String,
    val stopName: String
)

class BusArrivalStopMappedDTO : TreeMap<String, MutableMap<String, MutableList<BusArrival>>>() {
    // stop name => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.stopName)) {
            val tempMap = get(busArrival.stopName)!!
            if (tempMap.containsKey(busArrival.routeDirection)) {
                tempMap[busArrival.routeDirection]!!.add(busArrival)
            } else {
                tempMap.put(busArrival.routeDirection, mutableListOf(busArrival))
            }
        } else {
            val tempMap = TreeMap<String, MutableList<BusArrival>>()
            val arrivals = mutableListOf(busArrival)
            tempMap.put(busArrival.routeDirection, arrivals)
            put(busArrival.stopName, tempMap)
        }
    }

    fun containsStopNameAndBound(stopName: String, bound: String): Boolean {
        return containsKey(stopName) && get(stopName)!!.containsKey(bound)
    }
}

class BusArrivalRouteDTO(comparator: Comparator<String>) : TreeMap<String, MutableMap<String, MutableList<BusArrival>>>(comparator) {
    // route => { bound => BusArrival }

    fun addBusArrival(busArrival: BusArrival) {
        if (containsKey(busArrival.routeId)) {
            val tempMap = get(busArrival.routeId)!!
            if (tempMap.containsKey(busArrival.routeDirection)) {
                tempMap[busArrival.routeDirection]!!.add(busArrival)
            } else {
                tempMap.put(busArrival.routeDirection, mutableListOf(busArrival))
            }
        } else {
            val tempMap = TreeMap<String, MutableList<BusArrival>>()
            tempMap.put(busArrival.routeDirection, mutableListOf(busArrival))
            put(busArrival.routeId, tempMap)
        }
    }

    companion object {

        private val busRouteIdRegex = Regex("[^0-9]+")

        val busComparator: Comparator<String> = Comparator { key1: String, key2: String ->
            if (key1.matches(busRouteIdRegex) && key2.matches(busRouteIdRegex)) {
                key1.toInt().compareTo(key2.toInt())
            } else {
                key1.replace(busRouteIdRegex, StringUtils.EMPTY).toInt().compareTo(key2.replace(busRouteIdRegex, StringUtils.EMPTY).toInt())
            }
        }
    }
}

data class RoutesAlertsDTO(
    val id: String,
    val routeName: String,
    val routeBackgroundColor: String,
    val routeTextColor: String,
    val routeStatus: String,
    val routeStatusColor: String,
    val alertType: AlertType
)

enum class AlertType {
    TRAIN, BUS
}

data class RouteAlertsDTO(
    val id: String,
    val headLine: String,
    val description: String,
    val impact: String,
    val severityScore: Int,
    val start: String,
    val end: String
)
