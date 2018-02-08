package fr.cph.chicago.core.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.AlertRouteAdapter
import fr.cph.chicago.rx.ObservableUtil
import fr.cph.chicago.util.Util

class AlertActivity : Activity() {

    @BindView(R.id.activity_alerts_swipe_refresh_layout)
    lateinit var scrollView: SwipeRefreshLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.alert_route_list)
    lateinit var listView: ListView

    private lateinit var routeId: String
    private lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_alert)
            ButterKnife.bind(this)
            routeId = intent.getStringExtra("routeId")
            title = intent.getStringExtra("title")
            scrollView.setOnRefreshListener({ this.refreshData() })
            setToolBar()
            refreshData()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        routeId = savedInstanceState.getString("routeId")
        title = savedInstanceState.getString("title")
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString("routeId", routeId)
        savedInstanceState.putString("title", title)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun setToolBar() {
        toolbar.inflateMenu(R.menu.main)
        toolbar.setOnMenuItemClickListener { _ ->
            scrollView.isRefreshing = true
            refreshData()
            false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.elevation = 4f
        }
        toolbar.title = title
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setOnClickListener { _ -> finish() }
    }

    private fun refreshData() {
        ObservableUtil.createAlertRouteObservable(routeId).subscribe(
            { routeAlertsDTOS ->
                val ada = AlertRouteAdapter(routeAlertsDTOS)
                listView.adapter = ada
                if (routeAlertsDTOS.isEmpty()) {
                    Util.showSnackBar(listView, this@AlertActivity.getString(R.string.message_no_alerts))
                }
                hideAnimation()
            },
            { onError ->
                Log.e(TAG, onError.message, onError)
                Util.showOopsSomethingWentWrong(listView)
                hideAnimation()
            })
    }

    private fun hideAnimation() {
        if (scrollView.isRefreshing) {
            scrollView.isRefreshing = false
        }
    }

    companion object {
        private val TAG = AlertActivity::class.java.simpleName
    }
}