package com.example.finalgithubappsubmission.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.finalgithubappsubmission.service.json.JSONStringConverterFromUrl

class ServiceRunningViewModel(application: Application): AndroidViewModel(application) {
    private var jsonStringConverterFromUrl = MutableLiveData<JSONStringConverterFromUrl>()
    fun getJSONStringLiveData(): JSONStringConverterFromUrl?{
        return jsonStringConverterFromUrl.value
    }
    fun assignJSONStringConverter(url: String){
        this.jsonStringConverterFromUrl.value =
            JSONStringConverterFromUrl(
                getApplication<Application>().applicationContext,
                url
            )
    }
    fun runJSONStringConverter(allowRetry: Boolean){
        jsonStringConverterFromUrl.value?.dataRetriever(allowRetry)
    }
    fun getRunningState(): Boolean?{
        return jsonStringConverterFromUrl.value?.getIsRunningState()
    }
    fun stopRunningState(){
         jsonStringConverterFromUrl.value?.stopAllCoroutines()
    }
}