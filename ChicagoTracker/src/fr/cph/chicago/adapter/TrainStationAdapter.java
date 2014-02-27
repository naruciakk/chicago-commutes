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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.cph.chicago.ChicagoTracker;
import fr.cph.chicago.R;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Adapter that will handle Train station list
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class TrainStationAdapter extends BaseAdapter {

	@Override
	public final int getCount() {
		return TrainLine.values().length - 1;
	}

	@Override
	public final Object getItem(final int position) {
		return TrainLine.values()[position];
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		View v = convertView;
		LayoutInflater vi = (LayoutInflater) ChicagoTracker.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.list_train_line, null);

		TextView textView = (TextView) v.findViewById(R.id.station_color_value);
		textView.setBackgroundColor(TrainLine.values()[position].getColor());
		textView.setText("  ");

		textView = (TextView) v.findViewById(R.id.station_name_value);
		textView.setText(TrainLine.values()[position].toString());
		return v;
	}
}
