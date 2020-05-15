package com.berd.qscore.features.about

import android.os.Bundle
import com.berd.qscore.databinding.ActivityAboutBinding
import com.berd.qscore.features.shared.activity.BaseActivity

class AboutActivity : BaseActivity() {


    private val binding: ActivityAboutBinding by lazy {
        ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun getScreenName() = "About"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        setupWebView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupWebView() {
        binding.webview.loadUrl("file:///android_asset/about.html")
    }
}
