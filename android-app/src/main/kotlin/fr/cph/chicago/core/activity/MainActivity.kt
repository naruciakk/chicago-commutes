/**
 * Copyright 2017 Carl-Philipp Harmant
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

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import butterknife.BindColor
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.Constants.Companion.BUSES_ARRIVAL_URL
import fr.cph.chicago.Constants.Companion.BUSES_ROUTES_URL
import fr.cph.chicago.Constants.Companion.TRAINS_ARRIVALS_URL
import fr.cph.chicago.R
import fr.cph.chicago.core.fragment.*
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.service.BusService
import fr.cph.chicago.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.container)
    lateinit var frameLayout: FrameLayout
    @BindView(R.id.main_drawer)
    lateinit var drawer: NavigationView
    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout

    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStations: String
    @BindString(R.string.bundle_title)
    lateinit var bundleTitle: String
    @BindString(R.string.favorites)
    lateinit var favorites: String
    @BindString(R.string.train)
    lateinit var train: String
    @BindString(R.string.bus)
    lateinit var bus: String
    @BindString(R.string.divvy)
    lateinit var divvy: String
    @BindString(R.string.nearby)
    lateinit var nearby: String
    @BindString(R.string.cta_map)
    lateinit var ctaMap: String
    @BindString(R.string.cta_alert)
    lateinit var ctaAlert: String
    @BindString(R.string.settings)
    lateinit var settings: String

    @JvmField
    @BindColor(R.color.primaryColorDarker)
    internal var primaryColorDarker: Int = 0

    private val observableUtil: ObservableUtil = ObservableUtil
    private val util: Util = Util
    private val busService: BusService = BusService

    private var currentPosition: Int = 0

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var menuItem: MenuItem

    private var favoritesFragment: FavoritesFragment? = null
    private var trainFragment: TrainFragment? = null
    private var busFragment: BusFragment? = null
    private var bikeFragment: BikeFragment? = null
    private var nearbyFragment: NearbyFragment? = null
    private var ctaMapFragment: CtaMapFragment? = null
    private var alertFragment: AlertFragment? = null
    private var settingsFragment: SettingsFragment? = null

    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isFinishing) {
            if (savedInstanceState != null) {
                reloadData()
            }
            setContentView(R.layout.activity_main)
            ButterKnife.bind(this)

            loadFirstData()

            frameLayout.foreground.alpha = 0

            initView()
            setToolbar()

            drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawerLayout.addDrawerListener(drawerToggle)
            drawerToggle.syncState()

            currentPosition = savedInstanceState?.getInt(SELECTED_ID) ?: R.id.navigation_favorites
            itemSelection(currentPosition)

            checkForErrorInBundle()
        }
    }

    private fun checkForErrorInBundle() {
        val isTrainError = intent.getBooleanExtra(getString(R.string.bundle_train_error), false)
        val isBusError = intent.getBooleanExtra(getString(R.string.bundle_bus_error), false)
        // FIXME The snackbar does not move up the search button
        if (isTrainError && isBusError) {
            util.showSnackBar(this, R.string.message_something_went_wrong, Snackbar.LENGTH_SHORT)
        } else {
            if (isTrainError) {
                util.showSnackBar(this, R.string.message_error_train_favorites, Snackbar.LENGTH_SHORT)
            } else if (isBusError) {
                util.showSnackBar(this, R.string.message_error_bus_favorites, Snackbar.LENGTH_SHORT)
            }
        }
    }

    override fun onBackPressed() {
        if (currentPosition == R.id.navigation_favorites) {
            finish()
        } else {
            onNavigationItemSelected(menuItem)
        }
    }

    override fun onResume() {
        super.onResume()
        if(title != null)
            setBarTitle(title!!)
    }

    private fun initView() {
        drawer.setNavigationItemSelectedListener(this)
        menuItem = drawer.menu.getItem(0)
    }

    private fun setToolbar() {
        toolbar.setOnMenuItemClickListener { _ ->
            // Favorite fragment
            favoritesFragment!!.startRefreshing()

            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ARRIVAL_URL)
            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_train, TRAINS_ARRIVALS_URL)
            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_divvy, applicationContext.getString(R.string.analytics_action_get_divvy_all))
            util.trackAction(R.string.analytics_category_ui, R.string.analytics_action_press, applicationContext.getString(R.string.analytics_action_refresh_fav))

            if (util.isNetworkAvailable()) {
                if (busService.getBusRoutes().size == 0
                    || intent.getParcelableArrayListExtra<Parcelable>(bundleBikeStations) == null
                    || intent.getParcelableArrayListExtra<Parcelable>(bundleBikeStations).size == 0) {
                    loadFirstData()
                }
                val zipped = observableUtil.createAllDataObservable(application)
                zipped.subscribe({ favoritesResult -> favoritesFragment!!.reloadData(favoritesResult) })
                { onError ->
                    Log.e(TAG, onError.message, onError)
                    favoritesFragment!!.displayError(R.string.message_something_went_wrong)
                }
            } else {
                favoritesFragment!!.displayError(R.string.message_network_error)
            }
            true
        }
        toolbar.inflateMenu(R.menu.main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
            window.navigationBarColor = primaryColorDarker
        }
    }

    fun loadFirstData() {
        observableUtil.createOnFirstLoadObservable().subscribe(
            { (busRoutesError, bikeStationsError, busRoutes, bikeStations) ->
                busService.setBusRoutes(busRoutes)
                refreshFirstLoadData(bikeStations)
                if (bikeStationsError || busRoutesError) {
                    util.showSnackBar(this, R.string.message_something_went_wrong, Snackbar.LENGTH_SHORT)
                }
            },
            { _ -> util.showSnackBar(this, R.string.message_something_went_wrong, Snackbar.LENGTH_SHORT) }
        ) {
            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_bus, BUSES_ROUTES_URL)
            util.trackAction(R.string.analytics_category_req, R.string.analytics_action_get_divvy, applicationContext.getString(R.string.analytics_action_get_divvy_all))
        }
    }

    private fun refreshFirstLoadData(bikeStations: List<BikeStation>) {
        intent.putParcelableArrayListExtra(bundleBikeStations, bikeStations as ArrayList<BikeStation>)
        onNewIntent(intent)
        if (favoritesFragment != null) {
            favoritesFragment!!.setBikeStations(bikeStations)
        }
        if (bikeFragment != null) {
            bikeFragment!!.setBikeStations(bikeStations)
        }
        if (currentPosition == POSITION_BUS && busFragment != null) {
            busFragment!!.update()
        }
    }

    private fun reloadData() {
        // Might want to reload train csv data if needed here
    }

    private fun setBarTitle(title: String) {
        this.title = title
        toolbar.title = title
    }

    private fun itemSelection(position: Int) {
        val fragmentManager = supportFragmentManager
        currentPosition = position
        when (position) {
            R.id.navigation_favorites -> {
                setBarTitle(favorites)
                favoritesFragment = if (favoritesFragment == null) FavoritesFragment.newInstance(position + 1) else favoritesFragment
                fragmentManager.beginTransaction().replace(R.id.container, favoritesFragment).commit()
                closeDrawerAndUpdateActionBar(true)
            }
            R.id.navigation_train -> {
                setBarTitle(train)
                trainFragment = if (trainFragment == null) TrainFragment.newInstance(position + 1) else trainFragment
                fragmentManager.beginTransaction().replace(R.id.container, trainFragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.navigation_bus -> {
                setBarTitle(bus)
                busFragment = if (busFragment == null) BusFragment.newInstance(position + 1) else busFragment
                fragmentManager.beginTransaction().replace(R.id.container, busFragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.navigation_bike -> {
                setBarTitle(divvy)
                bikeFragment = if (bikeFragment == null) BikeFragment.newInstance(position + 1) else bikeFragment
                fragmentManager.beginTransaction().replace(R.id.container, bikeFragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.navigation_nearby -> {
                setBarTitle(nearby)
                nearbyFragment = if (nearbyFragment == null) NearbyFragment.newInstance(position + 1) else nearbyFragment
                Observable.create<Any> { subscriber ->
                    drawerLayout.closeDrawer(GravityCompat.START)
                    subscriber.onNext(Any())
                    subscriber.onComplete()
                }
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { throwable -> Log.e(TAG, throwable.message, throwable) }
                    .subscribe { _ -> fragmentManager.beginTransaction().replace(R.id.container, nearbyFragment).commitAllowingStateLoss() }
                drawerLayout.closeDrawer(GravityCompat.START)
                hideActionBarMenu()
            }
            R.id.navigation_cta_map -> {
                setBarTitle(ctaMap)
                ctaMapFragment = if (ctaMapFragment == null) CtaMapFragment.newInstance(position + 1) else ctaMapFragment
                fragmentManager.beginTransaction().replace(R.id.container, ctaMapFragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.alert_cta -> {
                setBarTitle(ctaAlert)
                alertFragment = if (alertFragment == null) AlertFragment.newInstance(position + 1) else alertFragment
                fragmentManager.beginTransaction().replace(R.id.container, alertFragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
            R.id.rate_this_app -> util.rateThisApp(this)
            R.id.settings -> {
                setBarTitle(settings)
                settingsFragment = if (settingsFragment == null) SettingsFragment.newInstance(position + 1) else settingsFragment
                fragmentManager.beginTransaction().replace(R.id.container, settingsFragment).commit()
                closeDrawerAndUpdateActionBar(false)
            }
        }
    }

    private fun closeDrawerAndUpdateActionBar(showActionBarMenu: Boolean) {
        drawerLayout.closeDrawer(GravityCompat.START)
        if (showActionBarMenu)
            showActionBarMenu()
        else
            hideActionBarMenu()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true
        currentPosition = menuItem.itemId
        if (!isFinishing)
            itemSelection(currentPosition)
        return true
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState!!.putInt(SELECTED_ID, currentPosition)
        savedInstanceState.putString(bundleTitle, title)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        title = savedInstanceState.getString(bundleTitle)
        currentPosition = savedInstanceState.getInt(SELECTED_ID)
    }

    private fun hideActionBarMenu() {
        if (toolbar.menu.getItem(0).isVisible) {
            showHideActionBarMenu(false)
        }
    }

    private fun showActionBarMenu() {
        if (!toolbar.menu.getItem(0).isVisible) {
            showHideActionBarMenu(true)
        }
    }

    private fun showHideActionBarMenu(bool: Boolean) {
        toolbar.menu.getItem(0).isVisible = bool
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val SELECTED_ID = "SELECTED_ID"
        private const val POSITION_BUS = 2
    }
}