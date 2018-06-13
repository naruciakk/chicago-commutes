/**
 * Copyright 2018 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.repository

import android.util.Log
import android.util.SparseArray
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import fr.cph.chicago.core.App
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.Stop
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Util
import java.io.IOException
import java.io.InputStreamReader
import java.util.TreeMap

/**
 * Class that handle train data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object TrainRepository {

    private val TAG = TrainRepository::class.java.simpleName
    private const val DEFAULT_RANGE = 0.008
    // https://data.cityofchicago.org/Transportation/CTA-System-Information-List-of-L-Stops/8pix-ypme
    private const val TRAIN_FILE_PATH = "train_stops.csv"

    var error: Boolean = false

    private val parser: CsvParser

    init {
        val settings = CsvParserSettings()
        settings.format.setLineSeparator("\n")
        parser = CsvParser(settings)
    }

    private val inMemoryData: Triple<SparseArray<TrainStation>, SparseArray<Stop>, TreeMap<TrainLine, MutableList<TrainStation>>> by lazy {
        loadInMemoryStationsAndStops()
    }

    val stations: SparseArray<TrainStation>
        get() = inMemoryData.first

    private val stops: SparseArray<Stop>
        get() = inMemoryData.second

    /**
     * Get all stations
     *
     * @return a map containing all the stations ordered line
     */
    val allStations: MutableMap<TrainLine, MutableList<TrainStation>>
        get() = inMemoryData.third

    private fun loadInMemoryStationsAndStops(): Triple<SparseArray<TrainStation>, SparseArray<Stop>, TreeMap<TrainLine, MutableList<TrainStation>>> {
        val stations: SparseArray<TrainStation> = SparseArray()
        val stops: SparseArray<Stop> = SparseArray()
        val stationsOrderByLineMap: TreeMap<TrainLine, MutableList<TrainStation>> = TreeMap()

        var inputStreamReader: InputStreamReader? = null
        try {
            inputStreamReader = InputStreamReader(App.instance.resources.assets.open(TRAIN_FILE_PATH))
            val allRows = parser.parseAll(inputStreamReader)
            for (i in 1 until allRows.size) {
                val row = allRows[i]
                val stopId = row[0].toInt() // STOP_ID
                val direction = TrainDirection.fromString(row[1]) // DIRECTION_ID
                val stopName = row[2] // STOP_NAME
                val stationName = row[3]// STATION_NAME
                // String stationDescription = row[4];//STATION_DESCRIPTIVE_NAME
                val parentStopId = row[5].toInt()// MAP_ID (old PARENT_STOP_ID)
                val ada = row[6].toBoolean()// ADA
                val lines = mutableSetOf<TrainLine>()
                val red = row[7].toBoolean()// Red
                val blue = row[8].toBoolean()// Blue
                val green = row[9].toBoolean()// G
                val brown = row[10].toBoolean()// Brn
                val purple = row[11].toBoolean()// P
                val purpleExp = row[12].toBoolean()// Pexp
                val yellow = row[13].toBoolean()// Y
                val pink = row[14].toBoolean()// Pink
                val orange = row[15].toBoolean()// Org
                if (red) {
                    lines.add(TrainLine.RED)
                }
                if (blue) {
                    lines.add(TrainLine.BLUE)
                }
                if (brown) {
                    lines.add(TrainLine.BROWN)
                }
                if (green) {
                    lines.add(TrainLine.GREEN)
                }
                if (purple || purpleExp) {
                    // Handle both purple and purple express
                    lines.add(TrainLine.PURPLE)
                }
                if (yellow) {
                    lines.add(TrainLine.YELLOW)
                }
                if (pink) {
                    lines.add(TrainLine.PINK)
                }
                if (orange) {
                    lines.add(TrainLine.ORANGE)
                }
                val location = row[16]// Location
                val locationTrunk = location.substring(1)
                val coordinates = locationTrunk.substring(0, locationTrunk.length - 1).split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val latitude = coordinates[0].toDouble()
                val longitude = coordinates[1].toDouble()

                val station = TrainStation(parentStopId, stationName, mutableListOf())
                val stop = Stop(stopId, stopName, direction, Position(latitude, longitude), ada, lines)
                stops.append(stopId, stop)

                val currentStation = stations.get(parentStopId, null)
                if (currentStation == null) {
                    station.stops = mutableListOf(stop)
                    stations.append(parentStopId, station)
                } else {
                    currentStation.stops.add(stop)
                }
                Log.d(TAG, "Load $station")
            }
            stationsOrderByLineMap.putAll(sortStation(stations))
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        } finally {
            Util.closeQuietly(inputStreamReader)
        }
        return Triple(stations, stops, stationsOrderByLineMap)
    }

    /**
     * get a trainStation
     *
     * @param id the id of the train station
     * @return the train station
     */
    fun getStation(id: Int): TrainStation {
        val station = stations.get(id)
        return station ?: TrainStation.buildEmptyStation()
    }

    /**
     * Get a stop
     *
     * @param id the id of the stop
     * @return a stop
     */
    fun getStop(id: Int): Stop {
        return stops.get(id, Stop.buildEmptyStop())
    }

    /**
     * Read near by train station
     *
     * @param position the position
     * @return a list of train station
     */
    fun readNearbyStation(position: Position): List<TrainStation> {
        val latitude = position.latitude
        val longitude = position.longitude

        val latMax = latitude + DEFAULT_RANGE
        val latMin = latitude - DEFAULT_RANGE
        val lonMax = longitude + DEFAULT_RANGE
        val lonMin = longitude - DEFAULT_RANGE

        val nearByStations = mutableListOf<TrainStation>()
        (0 until stations.size())
            .map { stations.valueAt(it) }
            .forEach {
                it.stopsPosition
                    .filter { stopPosition ->
                        val trainLatitude = stopPosition.latitude
                        val trainLongitude = stopPosition.longitude
                        trainLatitude in latMin..latMax && trainLongitude <= lonMax && trainLongitude >= lonMin
                    }
                    .getOrElse(0, { Position() })
                    .also { pos ->
                        if (pos.latitude != 0.0 && pos.longitude != 0.0) {
                            nearByStations.add(it)
                        }
                    }
            }
        return nearByStations
    }

    fun readPattern(line: TrainLine): List<Position> {
        var inputStreamReader: InputStreamReader? = null
        return try {
            inputStreamReader = InputStreamReader(App.instance.resources.assets.open("train_pattern/" + line.toTextString() + "_pattern.csv"))
            val allRows = parser.parseAll(inputStreamReader)
            allRows
                .map { row ->
                    val longitude = row[0].toDouble()
                    val latitude = row[1].toDouble()
                    Position(latitude, longitude)
                }
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            listOf()
        } finally {
            Util.closeQuietly(inputStreamReader)
        }
    }

    private fun sortStation(trainStationNotSorted: SparseArray<TrainStation>): TreeMap<TrainLine, MutableList<TrainStation>> {
        val result = TreeMap<TrainLine, MutableList<TrainStation>>()
        for (i in 0 until trainStationNotSorted.size()) {
            val station = trainStationNotSorted.valueAt(i)
            val trainLines = station.lines
            for (trainLine in trainLines) {
                if (result.containsKey(trainLine)) {
                    val stations = result[trainLine]!!
                    stations.add(station)
                    stations.sort()
                } else {
                    val stations = mutableListOf<TrainStation>()
                    result[trainLine] = stations
                    stations.add(station)
                }
            }
        }
        return result
    }
}
