package com.example.finalgithubappsubmission.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity(), View.OnClickListener {
    companion object{
        const val LINK_BLOG_KEY = "com.example.finalgithubappsubmission"
    }
    private var isSaveInstanceState = false
    private var isActivityDestroyed = false
    private var link: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_web_view)
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.blog)
        SettingsComponent.overrideActivityColor(this, root_view)
        btn_refresh.setOnClickListener(this)
        link = intent.getStringExtra(LINK_BLOG_KEY)
        with(web_view){
            settings.javaScriptEnabled = true
            webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    tv_progress.text = resources.getString(R.string.current_progress, newProgress)
                }
            }
            webViewClient = object : WebViewClient(){
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        setErrorTextView(resources.getString(R.string.error_code, error?.errorCode), ContextCompat.getColor(this@WebViewActivity, R.color.colorRed))
                    }
                }
            }
            if (savedInstanceState == null){
                loadUrl(link.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isSaveInstanceState = false

    }
    override fun onDestroy() {
        super.onDestroy()
        if (!isSaveInstanceState) {
            isActivityDestroyed = true
            val clearViewString = "about:blank"
            web_view.loadUrl(clearViewString)
        }
    }
    override fun onClick(view: View) {
        when(view.id){
            R.id.btn_refresh -> {
                web_view.reload()
                setErrorTextView(resources.getString(R.string.no_errors), ContextCompat.getColor(this@WebViewActivity, android.R.color.black))
            }
        }
    }
    private fun setErrorTextView(message: String, textColor: Int){
        with(tv_error) {
            text = message
            setTextColor(textColor)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isSaveInstanceState = true
        web_view.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        web_view.restoreState(savedInstanceState)
    }
}