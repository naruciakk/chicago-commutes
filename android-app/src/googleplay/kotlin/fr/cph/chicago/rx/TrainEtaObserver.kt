/**
 * Copyright 2019 Carl-Philipp Harmant
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

package fr.cph.chicago.rx

import android.view.View
import android.widget.ListView
import android.widget.TextView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.map.TrainMapActivity
import fr.cph.chicago.core.adapter.TrainMapSnippetAdapter
import fr.cph.chicago.core.model.TrainEta
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

class TrainEtaObserver(view: View, private val trainMapActivity: TrainMapActivity) : SingleObserver<List<TrainEta>> {

    private val arrivals = view.findViewById<ListView>(R.id.arrivalsTextView)
    private val error = view.findViewById<TextView>(R.id.error)

    override fun onSuccess(trainEtas: List<TrainEta>) {
        // View can be null
        if (trainEtas.isNotEmpty()) {
            val ada = TrainMapSnippetAdapter(trainEtas)
            arrivals.adapter = ada
            arrivals.visibility = ListView.VISIBLE
            error.visibility = TextView.GONE
        } else {
            arrivals.visibility = ListView.GONE
            error.visibility = TextView.VISIBLE
        }
        trainMapActivity.refreshInfoWindow()
    }

    override fun onSubscribe(d: Disposable) {}

    override fun onError(e: Throwable) {
        Timber.e(e)
    }
}
