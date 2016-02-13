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

package fr.cph.chicago.entity;

import java.security.Timestamp;
import java.util.List;

public class Bus {
	private int id;
	private Timestamp timestamp;
	private Position position;
	private int heading;
	private int patternId;
	private int patternDistance;
	private String routeId;
	private String destination;
	private Boolean delay;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getHeading() {
		return heading;
	}

	public void setHeading(int heading) {
		this.heading = heading;
	}

	public int getPatternId() {
		return patternId;
	}

	public void setPatternId(int patternId) {
		this.patternId = patternId;
	}

	public int getPatternDistance() {
		return patternDistance;
	}

	public void setPatternDistance(int patternDistance) {
		this.patternDistance = patternDistance;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Boolean getDelay() {
		return delay;
	}

	public void setDelay(Boolean delay) {
		this.delay = delay;
	}

	public static Position getBestPosition(List<Bus> buses) {
		Position position = new Position();
		double maxLatitude = 0.0;
		double minLatitude = 0.0;
		double maxLongitude = 0.0;
		double minLongitude = 0.0;
		int i = 0;
		for (Bus bus : buses) {
			Position temp = bus.getPosition();
			if (i == 0) {
				maxLatitude = temp.getLatitude();
				minLatitude = temp.getLatitude();
				maxLongitude = temp.getLongitude();
				minLongitude = temp.getLongitude();
			} else {
				if (temp.getLatitude() > maxLatitude) {
					maxLatitude = temp.getLatitude();
				}
				if (temp.getLatitude() < minLatitude) {
					minLatitude = temp.getLatitude();
				}
				if (temp.getLongitude() > maxLongitude) {
					maxLongitude = temp.getLongitude();
				}
				if (temp.getLongitude() < minLongitude) {
					minLongitude = temp.getLongitude();
				}
			}
			i++;
		}
		position.setLatitude((maxLatitude + minLatitude) / 2);
		position.setLongitude((maxLongitude + minLongitude) / 2);
		return position;
	}

}
