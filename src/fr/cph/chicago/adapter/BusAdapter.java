/**
 * Copyright 2016 Carl-Philipp Harmant
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

package fr.cph.chicago.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.entity.BusRoute;
import fr.cph.chicago.task.DirectionAsyncTask;

/**
 * Adapter that will handle buses
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public final class BusAdapter extends BaseAdapter {

    private MainActivity mainActivity;
    private List<BusRoute> busRoutes;

    /**
     * Constructor
     *
     * @param activity the main activity
     */
    public BusAdapter(@NonNull final MainActivity activity) {
        this.mainActivity = activity;
        final BusData busData = DataHolder.getInstance().getBusData();
        this.busRoutes = busData.getRoutes();
    }

    @Override
    public final int getCount() {
        return busRoutes.size();
    }

    @Override
    public final Object getItem(final int position) {
        return busRoutes.get(position);
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public final View getView(final int position, View convertView, final ViewGroup parent) {

        final BusRoute route = (BusRoute) getItem(position);

        final TextView routeNameView;
        final TextView routeNumberView;
        final LinearLayout detailsLayout;

        if (convertView == null) {
            final LayoutInflater vi = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_bus, parent, false);

            final ViewHolder holder = new ViewHolder();
            routeNameView = (TextView) convertView.findViewById(R.id.route_name);
            holder.routeNameView = routeNameView;

            routeNumberView = (TextView) convertView.findViewById(R.id.route_number);
            holder.routeNumberView = routeNumberView;

            detailsLayout = (LinearLayout) convertView.findViewById(R.id.route_details);
            holder.detailsLayout = detailsLayout;

            convertView.setTag(holder);
        } else {
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            routeNameView = holder.routeNameView;
            routeNumberView = holder.routeNumberView;
            detailsLayout = holder.detailsLayout;
        }
        routeNameView.setText(route.getName());
        routeNumberView.setText(route.getId());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                detailsLayout.setVisibility(LinearLayout.VISIBLE);
                new DirectionAsyncTask(mainActivity, parent).execute(route, detailsLayout);
            }
        });

        return convertView;
    }

    public void setRoutes(@NonNull final List<BusRoute> busRoutes) {
        this.busRoutes = busRoutes;
    }

    /**
     * DP view holder
     *
     * @author Carl-Philipp Harmant
     * @version 1
     */
    private static class ViewHolder {
        TextView routeNameView;
        TextView routeNumberView;
        LinearLayout detailsLayout;
    }
}
