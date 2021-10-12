import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.example.finalgithubappsubmission.R

object JSONStringGeneratorOnline{
    private const val TOKEN = "token bc97556ee6c95c46090abcb8e2d70443a09512f7"
    private const val AUTHORIZATION = "Authorization"
    private const val REQUEST_CANCELLED = "requestCancelledError"
    private const val CONNECTION_ERROR = "connectionError"
    private const val ERROR_CODE_NOT_FOUND = 404
    interface IOnResponse{
        fun onResponse(jsonObject: String)
    }
    interface IOnError{
        fun onError(errorMessage: String)
    }
    lateinit var iOnResponse: IOnResponse
    lateinit var iOnError: IOnError
    fun cancelConnect(){
        AndroidNetworking.forceCancelAll()
    }
    fun connectToInternet(url: String, context: Context){
        AndroidNetworking.initialize(context)
        AndroidNetworking.get(url)
            .addHeaders(AUTHORIZATION, TOKEN)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    iOnResponse.onResponse(response)
                }

                override fun onError(anError: ANError) {
                    if (anError.errorDetail != REQUEST_CANCELLED){
                        var errorString = context.resources.getString(R.string.error_messaging, anError.errorCode, anError.errorDetail)
                        errorString += getAdditionalMessage(context, anError)
                        iOnError.onError(errorString)
                    }
                }
            })
        }
        private fun getAdditionalMessage(context: Context, anError: ANError): String{
            if (anError.errorDetail == CONNECTION_ERROR){
                 return context.getString(R.string.connection_error_message)
            }
            if (anError.errorCode == ERROR_CODE_NOT_FOUND){
                 return context.getString(R.string.not_found_error_message)
            }
            return ""
        }
}
