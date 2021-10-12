package com.example.finalgithubappsubmission.fragments.peoplefragments


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalgithubappsubmission.adapters.people.FollowAdapter
import com.example.finalgithubappsubmission.service.json.JSONStringConverterFromUrl
import com.example.finalgithubappsubmission.notification.NotificationComponent
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.activity.UserDetailActivity
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.MainUser
import com.example.finalgithubappsubmission.viewmodel.MainUsersViewModel
import com.example.finalgithubappsubmission.viewmodel.ServiceRunningViewModel
import com.example.finalgithubappsubmission.viewmodel.StatusBarViewModel
import kotlinx.android.synthetic.main.fragment_follow.*
import kotlinx.android.synthetic.main.people_layout.view.*
import org.json.JSONArray

abstract class PeopleFollowAbstractFragment : Fragment() {
    val TOTAL_PEOPLE_KEY = "TOTAL_PEOPLE_KEY"
    val IMG_LOGO_RES = "IMG_LOGO_RES"
    val PEOPLE_TYPE_KEY = "PEOPLE_TYPE_KEY"
    val URL_PEOPLE_KEY = "URL_PEOPLE_KEY"
    private val CHANNEL_ID = "PEOPLE_FRAGMENT_CHANNEL_ID"
    private val NOTIFICATION_ID = 10
    private lateinit var mainUsersLiveData: MainUsersViewModel
    private lateinit var statusBarLiveData: StatusBarViewModel
    var serviceRunningViewModel: ServiceRunningViewModel? = null
    private var isSavedInstanceState = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follow, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SettingsComponent.colorizeTaggedViews(context as Context, root_view)
        tv_total_people.text = "${arguments?.getInt(TOTAL_PEOPLE_KEY)}"
        tv_people_type.text = arguments?.getString(PEOPLE_TYPE_KEY)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeData()
    }
    override fun onStart() {
        super.onStart()
        val urlPeopleKey: String = arguments?.getString(URL_PEOPLE_KEY) as String
        prepareInternetData(urlPeopleKey)
    }
    override fun onResume() {
        super.onResume()
        eraseNotification()
        mainUsersLiveData.iPrepareConnectionData = object : MainUsersViewModel.PrepareConnectionData{
            override fun prepareInternetData() {
                connectToInternet()
            }
        }
        mainUsersLiveData.getUserData().observe(this, Observer {
                prepareAdapter()
            }
        )
        statusBarLiveData.getStatusBarView().observe(this, Observer {
            statusBarLiveData.statusBarObserverFunction(layout_fragment_user.status_bar)
        })
        if (arguments != null){
            img_logo.setImageResource(arguments?.getInt(IMG_LOGO_RES) as Int)
            val runningState = serviceRunningViewModel?.getRunningState()
            if (mainUsersLiveData.getUserData().value == null && runningState != true){
                mainUsersLiveData.setDataFromInternet()
            }
        }
    }
    private fun eraseNotification(){
        isSavedInstanceState = false
        NotificationComponent.cancelNotification(NOTIFICATION_ID, context as Context)
    }
    private fun initializeData(){
        mainUsersLiveData = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainUsersViewModel::class.java)
        statusBarLiveData = ViewModelProvider(this).get(StatusBarViewModel::class.java)
        serviceRunningViewModel = ViewModelProvider(this).get(ServiceRunningViewModel::class.java)
    }
    private fun getThisFragmentNotificationSettings(titleID: Int, description: String, drawableId: Int): NotificationComponent.NotificationSettings {
        return NotificationComponent.NotificationSettings(context as Context,
        CHANNEL_ID,
        NOTIFICATION_ID,
        NotificationComponent.getPendingIntent(context as Context, UserDetailActivity::class.java, Intent.FLAG_ACTIVITY_SINGLE_TOP),
        LongArray(0){0},
        resources.getString(titleID),
        description,
        drawableId)
    }
    private fun prepareInternetData(url: String){
        if (serviceRunningViewModel?.getJSONStringLiveData() == null) {
            serviceRunningViewModel?.assignJSONStringConverter(url)
        }
        var mainUserArrayList: ArrayList<MainUser>? = null
        serviceRunningViewModel?.getJSONStringLiveData()?.iBeforeConnect =
            object : JSONStringConverterFromUrl.BeforeConnect {
                override fun beforeConnect() {
                    try {
                        if (NotificationComponent.checkIfNotificationExist(
                                NOTIFICATION_ID,
                                context as Context
                            )
                        ) {
                            NotificationComponent.setNotification(getThisFragmentNotificationSettings(R.string.notification_wait_title, resources.getString(R.string.notification_retrieving_data), android.R.drawable.ic_popup_sync))
                        }
                        statusBarLiveData.formatStatusBar(
                            layout_fragment_user.status_bar,
                            getString(
                                R.string.retrieving_user_data,
                                arguments?.getString(PEOPLE_TYPE_KEY)
                            ),
                            SettingsComponent.generatePrimaryDarkColor(),
                            View.VISIBLE
                        )
                    } catch (e: Exception) {
                    }
                }
            }
        serviceRunningViewModel?.getJSONStringLiveData()?.iTransferDataUser =
            object : JSONStringConverterFromUrl.TransferDataUser {
                override fun transferData(jsonObject: String) {
                    val currentStatusBar = layout_fragment_user.status_bar
                    statusBarLiveData.formatStatusBar(
                        currentStatusBar,
                        getString(R.string.parse_data),
                        SettingsComponent.generatePrimaryDarkColor(),
                        View.VISIBLE
                    )
                    mainUserArrayList = parsingJSONData(jsonObject)
                }
            }
        serviceRunningViewModel?.getJSONStringLiveData()?.iFinishedTransfer =
            object : JSONStringConverterFromUrl.FinishedTransfer {
                override fun finishedTransfer() {
                    try {
                        if (NotificationComponent.checkIfNotificationExist(
                                NOTIFICATION_ID,
                                context as Context
                            )
                        ) {
                            NotificationComponent.setNotification(getThisFragmentNotificationSettings(R.string.notification_success_title, resources.getString(R.string.notification_success_description), android.R.drawable.star_on))
                        }
                        mainUsersLiveData.getUserData().postValue(mainUserArrayList)
                        statusBarLiveData.formatStatusBar(
                            layout_fragment_user.status_bar,
                            getString(R.string.data_retrieve_success),
                            SettingsComponent.generatePrimaryDarkColor(),
                            View.GONE
                        )
                    } catch (e: Exception) {
                    }
                }
            }
        serviceRunningViewModel?.getJSONStringLiveData()?.iFailedParse =
            object : JSONStringConverterFromUrl.FailedParse {
                override fun failedParse(message: String) {
                    try {
                        val finalMessage = message + " " + getString(R.string.repeat_5_seconds)
                        statusBarLiveData.formatStatusBar(
                            layout_fragment_user.status_bar,
                            finalMessage,
                            SettingsComponent.generateRedColor(),
                            View.GONE
                        )
                        if (NotificationComponent.checkIfNotificationExist(
                                NOTIFICATION_ID,
                                context as Context
                            )
                        ) {
                            NotificationComponent.setNotification(getThisFragmentNotificationSettings(R.string.notification_error, finalMessage, android.R.drawable.stat_sys_warning))
                        }
                    }
                    catch (e: Exception) {

                    }
                }
        }
    }
    fun connectToInternet(){
        serviceRunningViewModel?.runJSONStringConverter(true)
    }
    fun parsingJSONData(jsonString: String): ArrayList<MainUser>{
        val jsonArray = JSONArray(jsonString)
        val length = jsonArray.length()
        val mainUserArrayList = ArrayList<MainUser>()
        for (i in 0 until length){
            val jsonObject = jsonArray.getJSONObject(i)
            val username = jsonObject.getString("login")
            val type = jsonObject.getString("type")
            val avatarUrl = jsonObject.getString("avatar_url")
            val detailUser = MainUser(username, type, avatarUrl)
            mainUserArrayList.add(detailUser)
        }
        return mainUserArrayList
    }
    private fun prepareAdapter(){
        val mainUsersLiveData = mainUsersLiveData.getUserData().value as ArrayList<MainUser>
        val followAdapter =
            FollowAdapter(
                mainUsersLiveData,
                R.layout.list_follow_item_layout
            )
        followAdapter.iButtonClick = object : FollowAdapter.ButtonClick{
            override fun onClickButton(view: View, position: Int) {
                val clipboardManager = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("username", mainUsersLiveData[position].username)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(view.context, getString(R.string.copied_to_clipboard, mainUsersLiveData[position].username), Toast.LENGTH_SHORT).show()
            }
        }
        layout_fragment_user.recycler_view_user.adapter = followAdapter
        layout_fragment_user.recycler_view_user.visibility = View.VISIBLE
        layout_fragment_user.recycler_view_user.layoutManager = LinearLayoutManager(view?.context)
    }
    protected fun stopBackgroundProcess(){
        serviceRunningViewModel?.stopRunningState()
        if (context != null){
            NotificationComponent.cancelNotification(NOTIFICATION_ID, context as Context)
        }
    }
    override fun onStop() {
        super.onStop()
        if (serviceRunningViewModel?.getRunningState() == true){
            NotificationComponent.setNotification(getThisFragmentNotificationSettings(R.string.notification_wait_title, resources.getString(R.string.notification_retrieving_data), android.R.drawable.ic_popup_sync))
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isSavedInstanceState = true
    }
    override fun onDestroy() {
        super.onDestroy()
        if (!isSavedInstanceState){
            stopBackgroundProcess()
        }
    }
}
