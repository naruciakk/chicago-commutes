/**
 * Copyright 2014 Carl-Philipp Harmant
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

package fr.cph.chicago.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.entity.BusStop;

public class BusBoundAdapter extends BaseAdapter {

	private List<BusStop> busStops;
	private String stopId;

	public BusBoundAdapter(String stopId) {
		this.busStops = new ArrayList<BusStop>();
		this.stopId = stopId;
	}

	@Override
	public int getCount() {
		return busStops.size();
	}

	@Override
	public Object getItem(int position) {
		return busStops.get(position);
	}

	@Override
	public long getItemId(int position) {
		return busStops.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BusStop busStop = busStops.get(position);

		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.list_bus_bounds, null);

		TextView routNumberView = (TextView) convertView.findViewById(R.id.route_number);
		routNumberView.setText(stopId);

		TextView routNameView = (TextView) convertView.findViewById(R.id.route_name_value);
		routNameView.setText(busStop.getName());

		return convertView;
	}

	public void update(List<BusStop> result) {
		this.busStops = null;
		this.busStops = result;
	}

}
