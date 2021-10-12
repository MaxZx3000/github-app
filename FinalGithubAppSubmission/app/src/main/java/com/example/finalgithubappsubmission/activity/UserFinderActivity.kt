package com.example.finalgithubappsubmission.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.CursorAdapter
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.adapters.people.MainUserAdapter
import com.example.finalgithubappsubmission.itemdecoration.RecyclerViewItemDecoration
import com.example.finalgithubappsubmission.notification.NotificationComponent
import com.example.finalgithubappsubmission.service.json.JSONStringConverterFromUrl
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.MainUser
import com.example.finalgithubappsubmission.viewmodel.MainUsersViewModel
import com.example.finalgithubappsubmission.viewmodel.ServiceRunningViewModel
import com.example.finalgithubappsubmission.viewmodel.StatusBarViewModel
import kotlinx.android.synthetic.main.activity_user_finder.*
import org.json.JSONObject

class UserFinderActivity : AppCompatActivity(), SearchView.OnCloseListener, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, View.OnClickListener {
    private lateinit var userViewModel: MainUsersViewModel
    private lateinit var statusBarViewModel: StatusBarViewModel
    private lateinit var serviceRunningViewModel: ServiceRunningViewModel
    private var isSavedInstanceState: Boolean = false
    private var numberOfUsers = 0
    val NOTIFICATION_ID = 0
    val CHANNEL_ID = "CHANNEL_MAIN_ACTIVITY_ID"
    val PEOPLE_NAME = "people_name"
    private lateinit var cursorAdapter: SimpleCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_finder)
        initializeData()
        if (savedInstanceState == null) {
            initializeFirstSetup()
        }
        SettingsComponent.overrideActivityColor(this, root_view)
        supportActionBar?.title = resources.getString(R.string.search)
        initializeSearchFunction()
        userViewModel.getUserData().observe(this@UserFinderActivity, Observer { listMainUserData ->
            if (listMainUserData != null) {
                numberOfUsers = listMainUserData.size
                generateAdapter(listMainUserData)
            }
        })
        prepareDataFromInternet("")
        statusBarViewModel.getStatusBarView().observe(this, Observer {
            statusBarViewModel.statusBarObserverFunction(status_bar)
        })
    }
    private fun initializeData(){
        val from = arrayOf(PEOPLE_NAME)
        val to = intArrayOf(android.R.id.text1)
        cursorAdapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainUsersViewModel::class.java)
        statusBarViewModel = ViewModelProvider(this).get(StatusBarViewModel::class.java)
        serviceRunningViewModel = ViewModelProvider(this).get(ServiceRunningViewModel::class.java)
    }
    override fun onQueryTextChange(string: String): Boolean {
        populateSearch(string)
        return true
    }
    private fun populateSearch(userString: String){
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, PEOPLE_NAME))
        val arrayKeywords = SettingsComponent.settingsPreference.getStringSet(SettingsComponent.KEYWORDS_SET_KEY, null)
        if (arrayKeywords != null){
            var index = 0
            for (i in arrayKeywords){
                if (i.startsWith(userString)){
                    cursor.addRow(arrayOf(index++, i))
                }
            }
        }
        cursorAdapter.changeCursor(cursor)
    }
    override fun onStart() {
        super.onStart()
        eraseNotification()
    }
    override fun onClick(v: View) {
        when(v.id){
            R.id.search_fab -> {
                search_user.visibility = View.VISIBLE
                search_fab.visibility = View.GONE
            }
        }
    }
    override fun onQueryTextSubmit(text: String): Boolean {
        if (text.trim().isNotEmpty()){
            if (text.contains(" ")){
                statusBarViewModel.formatStatusBar(status_bar, getString(R.string.invalid_space), SettingsComponent.generateRedColor(), View.GONE)
                return false
            }
            SettingsComponent.addHistoryData(text)
            userViewModel.iPrepareConnectionData = object : MainUsersViewModel.PrepareConnectionData{
                override fun prepareInternetData() {
                    prepareDataFromInternet(text)
                    connectToInternet()
                }
            }
            userViewModel.setDataFromInternet()
            search_user.visibility = View.GONE
            search_fab.visibility = View.VISIBLE
            return true
        }
        else{
            statusBarViewModel.formatStatusBar(status_bar, getString(R.string.invalid_empty), R.drawable.red_background, View.GONE)
            return false
        }
    }
    private fun eraseNotification(){
        isSavedInstanceState = false
        NotificationComponent.cancelNotification(NOTIFICATION_ID, this)
    }
    private fun getCurrentActivityNotificationSetting(titleID: Int, description: String, drawableID: Int): NotificationComponent.NotificationSettings{
        return NotificationComponent.NotificationSettings(this,
            CHANNEL_ID,
            NOTIFICATION_ID,
            NotificationComponent.getPendingIntent(this, UserFinderActivity::class.java, Intent.FLAG_ACTIVITY_SINGLE_TOP),
            LongArray(1){0},
            resources.getString(titleID),
            description,
            drawableID
        )
    }
    private fun prepareDataFromInternet(username: String){
        if (username.isNotEmpty()){
            serviceRunningViewModel.stopRunningState()
            serviceRunningViewModel.assignJSONStringConverter("https://api.github.com/search/users?q=${username}")
        }
        var listUserData: ArrayList<MainUser>? = null
        serviceRunningViewModel.getJSONStringLiveData()?.iBeforeConnect = object : JSONStringConverterFromUrl.BeforeConnect{
            override fun beforeConnect() {
                try{
                    if (NotificationComponent.checkIfNotificationExist(NOTIFICATION_ID, this@UserFinderActivity)){
                        NotificationComponent.setNotification(getCurrentActivityNotificationSetting(R.string.notification_wait_title, resources.getString(R.string.notification_retrieving_data), android.R.drawable.ic_popup_sync))
                    }
                    statusBarViewModel.formatStatusBar(status_bar, getString(R.string.waiting_data), SettingsComponent.generatePrimaryDarkColor(), View.VISIBLE)
                }
                catch(e: Exception){

                }
            }
        }
        serviceRunningViewModel.getJSONStringLiveData()?.iTransferDataUser = object : JSONStringConverterFromUrl.TransferDataUser{
            override fun transferData(jsonObject: String) {
                listUserData = retrieveDataFromURL(jsonObject)
            }
        }
        serviceRunningViewModel.getJSONStringLiveData()?.iFinishedTransfer = object : JSONStringConverterFromUrl.FinishedTransfer{
            override fun finishedTransfer() {
                try{
                    userViewModel.getUserData().postValue(listUserData)
                    if (NotificationComponent.checkIfNotificationExist(NOTIFICATION_ID, this@UserFinderActivity)){
                        NotificationComponent.setNotification(getCurrentActivityNotificationSetting(R.string.notification_success_title, resources.getString(R.string.notification_success_description), android.R.drawable.star_on))
                    }
                    showUsersQuantityStatusBar()
                }
                catch(e: Exception){

                }
            }
        }
        serviceRunningViewModel.getJSONStringLiveData()?.iFailedParse = object : JSONStringConverterFromUrl.FailedParse{
            override fun failedParse(message: String) {
                try{
                    val finalMessage = message + " " + getString(R.string.repeat_5_seconds)
                    statusBarViewModel.formatStatusBar(status_bar, finalMessage, SettingsComponent.generateRedColor(), View.GONE)
                    if (NotificationComponent.checkIfNotificationExist(NOTIFICATION_ID, this@UserFinderActivity)){
                        NotificationComponent.setNotification(getCurrentActivityNotificationSetting(R.string.notification_error, finalMessage, android.R.drawable.stat_sys_warning))
                    }
                }
                catch(e: Exception){

                }
            }
        }

    }
    fun connectToInternet(){
        serviceRunningViewModel.runJSONStringConverter(true)
    }
    fun showUsersQuantityStatusBar(){
        if (numberOfUsers > 0){
            statusBarViewModel.formatStatusBar(status_bar, getString(R.string.users_found, numberOfUsers), SettingsComponent.generatePrimaryDarkColor(), View.GONE)
        }
        else{
            statusBarViewModel.formatStatusBar(status_bar, getString(R.string.no_user_data), SettingsComponent.generatePrimaryDarkColor(), View.GONE)
        }
    }
    fun retrieveDataFromURL(jsonString: String): ArrayList<MainUser>{
        val jsonObject = JSONObject(jsonString)
        val itemsArray = jsonObject.getJSONArray("items")
        numberOfUsers = itemsArray.length()
        val listUserData = ArrayList<MainUser>()
        for(i in 0 until numberOfUsers){
            val itemObject = itemsArray.getJSONObject(i)
            val login = itemObject.getString("login")
            val type = itemObject.getString("type")
            val avatarUrl = itemObject.getString("avatar_url")
            listUserData.add(MainUser(login, type, avatarUrl))
        }
        return listUserData
    }
    private fun generateAdapter(listMainUserData: ArrayList<MainUser>){
        val adapter =
            MainUserAdapter(
                listMainUserData,
                R.layout.list_main_item_layout
            )
        adapter.iClickBehaviour = object : MainUserAdapter.IClickBehaviour{
            override fun onClick(position: Int) {
                showUsersQuantityStatusBar()
                serviceRunningViewModel.stopRunningState()
                val intent = Intent(this@UserFinderActivity, UserDetailActivity::class.java)
                intent.putExtra(UserDetailActivity.MAIN_USER_KEY, listMainUserData[position])
                startActivity(intent)
            }
        }
        with(recycler_view_user){
            this.adapter = adapter
            layoutManager = GridLayoutManager(this@UserFinderActivity, 2)
            addItemDecoration(RecyclerViewItemDecoration(this@UserFinderActivity))
            visibility = View.VISIBLE
        }
    }
    private fun initializeFirstSetup(){
        recycler_view_user.visibility = View.GONE
        statusBarViewModel.formatStatusBar(status_bar, getString(R.string.welcome_message), SettingsComponent.generatePrimaryDarkColor(), View.GONE)
    }
    private fun initializeSearchFunction(){
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        with(search_user){
            setOnQueryTextListener(this@UserFinderActivity)
            setOnCloseListener(this@UserFinderActivity)
            setOnSearchClickListener(this@UserFinderActivity)
            setOnSuggestionListener(this@UserFinderActivity)
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            suggestionsAdapter = cursorAdapter
            requestFocus(1)
            queryHint = getString(R.string.search_bar_hint)
            if (visibility == View.VISIBLE){
                search_fab.visibility = View.GONE
            }
            else{
                search_fab.visibility = View.VISIBLE
            }
        }
        search_fab.setOnClickListener(this)
    }
    private fun stopProcess(){
        serviceRunningViewModel.stopRunningState()
        NotificationComponent.cancelNotification(NOTIFICATION_ID, this@UserFinderActivity)

    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isSavedInstanceState = true
    }
    override fun onClose(): Boolean {
        search_user.visibility = View.GONE
        search_fab.visibility = View.VISIBLE
        return true
    }
    override fun onStop() {
        super.onStop()
        if (serviceRunningViewModel.getRunningState() == true){
            val notificationSettings = NotificationComponent.NotificationSettings(
                this,
                CHANNEL_ID,
                NOTIFICATION_ID,
                NotificationComponent.getPendingIntent(this, UserFinderActivity::class.java, Intent.FLAG_ACTIVITY_SINGLE_TOP),
                LongArray(1){0},
                getString(R.string.notification_wait_title),
                getString(R.string.notification_retrieving_data),
                android.R.drawable.ic_popup_sync
            )
            NotificationComponent.setNotification(notificationSettings)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (!isSavedInstanceState){
            stopProcess()
        }
    }

    override fun onSuggestionSelect(index: Int): Boolean {
        return true
    }

    override fun onSuggestionClick(index: Int): Boolean {
        val cursor = search_user.suggestionsAdapter.cursor
        cursor.moveToPosition(index)
        val item = cursor.getString(cursor.getColumnIndexOrThrow(PEOPLE_NAME))
        search_user.setQuery(item, true)
        return true
    }
}
