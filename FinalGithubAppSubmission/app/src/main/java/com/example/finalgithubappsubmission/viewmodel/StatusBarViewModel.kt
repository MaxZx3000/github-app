package com.example.finalgithubappsubmission.viewmodel

import android.app.Application
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.*
import kotlinx.android.synthetic.main.status_bar_layout.view.*

class StatusBarViewModel(application: Application): AndroidViewModel(application) {
    private var statusBarView = MutableLiveData<View>()
    fun formatStatusBar(statusBarView: View, text: String, drawableColor: Int, displayCircular: Int) {
        val statusBar = statusBarView
        with(statusBar) {
            progress_circular.visibility = displayCircular
            tv_status.text = text
            background = ColorDrawable(drawableColor)
        }
        this.statusBarView.postValue(statusBar)
    }
    fun getStatusBarView(): LiveData<View>{
        return statusBarView
    }
    fun statusBarObserverFunction(originalStatusBarView: View) {
        with(originalStatusBarView) {
            val progressCircular = statusBarView.value?.progress_circular as ProgressBar
            progress_circular.visibility = progressCircular.visibility
            tv_status.text = statusBarView.value?.tv_status?.text
            background = statusBarView.value?.background
        }
    }
}