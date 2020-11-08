package com.sugarspoon.colordetekt.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.sugarspoon.colordetekt.R
import kotlinx.android.synthetic.main.layout_toolbar.view.*
import org.jetbrains.anko.toolbar

open class BaseActivity : AppCompatActivity() {

    var view: ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        view = (window.decorView.rootView as? ViewGroup)
    }

    fun setToolbar(title: String, displayHomeAsUpEnabled: Boolean = false) = view?.run {
        supportActionBar?.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)

        toolbar.toolbar {
            this.title = title
            this.setTitleTextAppearance(context, R.style.AppTheme_AppBarOverlay)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setDisplayHomeAsUpEnabled(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

}
