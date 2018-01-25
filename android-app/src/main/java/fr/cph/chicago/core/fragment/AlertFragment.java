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

package fr.cph.chicago.core.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindString;
import butterknife.BindView;
import fr.cph.chicago.R;
import fr.cph.chicago.core.adapter.AlertAdapter;
import fr.cph.chicago.rx.ObservableUtil;
import fr.cph.chicago.util.Util;

/**
 * Train Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@SuppressWarnings("WeakerAccess")
public final class AlertFragment extends AbstractFragment {

    @BindView(R.id.alert_list)
    ListView listView;
    @BindString(R.string.bundle_train_line)
    String bundleTrainLine;

    private final Util util;

    public AlertFragment() {
        util = Util.INSTANCE;
    }

    /**
     * Returns a new instance of this fragment for the given section number.
     *
     * @param sectionNumber the section number
     * @return a train fragment
     */
    public static AlertFragment newInstance(final int sectionNumber) {
        return (AlertFragment) fragmentWithBundle(new AlertFragment(), sectionNumber);
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util.trackScreen(getString(R.string.analytics_cta_alert_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_alert, container, false);
        setBinder(rootView);
        ObservableUtil.INSTANCE.createAlertRoutesObservable()
            .subscribe(routeAlertsDTOS -> {
                Log.i("AlertFragment", "Alert Routes: " + routeAlertsDTOS);
                final AlertAdapter ada = new AlertAdapter(routeAlertsDTOS);
                listView.setAdapter(ada);
                listView.setOnItemClickListener((parentView, childView, position, id) -> {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(this.getContext(), android.R.style.Theme);
                    } else {
                        builder = new AlertDialog.Builder(this.getContext());
                    }
                    builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                });
            });
/*        listView.setOnItemClickListener((parentView, childView, position, id) -> {
            final Intent intent = new Intent(getContext(), TrainListStationActivity.class);
            final Bundle extras = new Bundle();
            final String line = TrainLine.values()[position].toString();
            extras.putString(bundleTrainLine, line);
            intent.putExtras(extras);
            startActivity(intent);
        });*/
        return rootView;
    }
}
