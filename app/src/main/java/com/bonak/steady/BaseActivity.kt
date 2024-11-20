package com.bonak.steady

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.widget.Toolbar

open class BaseActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
    }

    private fun setSupportActionBar(toolbar: Toolbar) {

    }
}