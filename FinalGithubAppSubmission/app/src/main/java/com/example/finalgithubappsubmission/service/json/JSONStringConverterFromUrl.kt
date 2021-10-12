package com.example.finalgithubappsubmission.service.json

import JSONStringGeneratorOnline
import android.content.Context
import kotlinx.coroutines.*

class JSONStringConverterFromUrl(val context: Context, private val url: String){
    companion object{
        private const val DELAY_TIME: Long = 5000
    }
    interface BeforeConnect{
        fun beforeConnect()
    }
    interface TransferDataUser{
        fun transferData(jsonObject: String)
    }
    interface FinishedTransfer{
        fun finishedTransfer()
    }
    interface FailedParse{
        fun failedParse(message: String)
    }
    var iTransferDataUser: TransferDataUser? = null
    var iBeforeConnect: BeforeConnect? = null
    var iFinishedTransfer: FinishedTransfer? = null
    var iFailedParse: FailedParse? = null
    private var dataRetrieverCoroutine: Job? = null
    private var dataTransferCoroutine: Job? = null
    private var dataErrorCoroutine: Job? = null
    private var isProcessRunning = false
    private suspend fun failedParseContainer(message: String, allowRetry: Boolean){
        withContext(Dispatchers.Main){
            iFailedParse?.failedParse(message)
        }
        if (allowRetry){
            delay(DELAY_TIME)
            dataRetriever(allowRetry)
        }
    }
    fun dataRetriever(allowRetry: Boolean){
        stopAllCoroutines()
        dataRetrieverCoroutine = GlobalScope.launch(Dispatchers.IO) {
            isProcessRunning = true
            withContext(Dispatchers.Main) {
                iBeforeConnect?.beforeConnect()
            }
            JSONStringGeneratorOnline.iOnResponse = object : JSONStringGeneratorOnline.IOnResponse {
                override fun onResponse(jsonObject: String) {
                    dataTransferCoroutine = GlobalScope.launch(Dispatchers.IO) {
                        withContext(Dispatchers.Main) {
                            iTransferDataUser?.transferData(jsonObject)
                            iFinishedTransfer?.finishedTransfer()
                            isProcessRunning = false
                        }
                    }
                }
            }
            JSONStringGeneratorOnline.iOnError = object : JSONStringGeneratorOnline.IOnError {
                override fun onError(errorMessage: String) {
                    dataErrorCoroutine = GlobalScope.launch(Dispatchers.IO) {
                        failedParseContainer(errorMessage, allowRetry)
                    }
                }
            }
            JSONStringGeneratorOnline.connectToInternet(url, context)
        }
    }
    fun stopAllCoroutines(){
        dataTransferCoroutine?.cancel()
        dataErrorCoroutine?.cancel()
        dataRetrieverCoroutine?.cancel()
        JSONStringGeneratorOnline.cancelConnect()
        isProcessRunning = false
    }
    fun getIsRunningState(): Boolean{
        return isProcessRunning
    }
}
