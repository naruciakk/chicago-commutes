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

package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.TrainAdapter
import fr.cph.chicago.core.model.enumeration.TrainLine
import fr.cph.chicago.util.Util
import kotlinx.android.synthetic.main.activity_train_station.listView
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.apache.commons.lang3.StringUtils

/**
 * Activity the list of train stations
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainListStationActivity : AppCompatActivity() {

    companion object {
        private val util = Util
    }

    private lateinit var trainLine: TrainLine
    private lateinit var lineParam: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_train_station)
            // Load data
            lineParam = if (savedInstanceState != null) savedInstanceState.getString(getString(R.string.bundle_train_line))
                ?: StringUtils.EMPTY else intent.getStringExtra(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY

            trainLine = TrainLine.fromString(lineParam)
            title = trainLine.toStringWithLine()

            util.setWindowsColor(this, toolbar, trainLine)
            toolbar.title = trainLine.toStringWithLine()

            toolbar.navigationIcon = getDrawable(R.drawable.ic_arrow_back_white_24dp)
            toolbar.setOnClickListener { finish() }

            listView.adapter = TrainAdapter(trainLine)
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        lineParam = savedInstanceState.getString(getString(R.string.bundle_train_line)) ?: StringUtils.EMPTY
        trainLine = TrainLine.fromString(lineParam)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (::lineParam.isInitialized) savedInstanceState.putString(getString(R.string.bundle_train_line), lineParam)
        super.onSaveInstanceState(savedInstanceState)
    }
}
