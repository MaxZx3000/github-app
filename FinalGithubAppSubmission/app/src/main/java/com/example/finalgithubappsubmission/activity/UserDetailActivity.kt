package com.example.finalgithubappsubmission.activity

import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.database.ContentObserver
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.adapters.ViewPagerFragmentAdapter
import com.example.finalgithubappsubmission.contentprovider.FavoriteProvider
import com.example.finalgithubappsubmission.fragments.PersonalInformationFragment
import com.example.finalgithubappsubmission.fragments.RepositoryFragment
import com.example.finalgithubappsubmission.fragments.RetrieveDataDialogFragment
import com.example.finalgithubappsubmission.fragments.peoplefragments.PeopleFollowAbstractFragment
import com.example.finalgithubappsubmission.fragments.peoplefragments.PeopleFollowersFragment
import com.example.finalgithubappsubmission.fragments.peoplefragments.PeopleFollowingFragment
import com.example.finalgithubappsubmission.service.json.JSONStringConverterFromUrl
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.DetailUser
import com.example.finalgithubappsubmission.users.MainUser
import com.example.finalgithubappsubmission.users.database.UserDAO
import com.example.finalgithubappsubmission.users.database.UserDatabase
import com.example.finalgithubappsubmission.users.database.UserEntity
import com.example.finalgithubappsubmission.viewmodel.DetailUserViewModel
import kotlinx.android.synthetic.main.activity_user_detail.*
import kotlinx.android.synthetic.main.fragment_pop_up.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserDetailActivity : AppCompatActivity() {
    companion object{
        const val MAIN_USER_KEY = "MAIN_USER_KEY"
        const val DETAIL_USER_KEY = "DETAIL_USER_KEY"
        const val UPDATE_FAVORITE_RESULT_KEY = 500
    }
    private lateinit var userDAO: UserDAO
    private lateinit var mainUserData: MainUser
    private lateinit var activityMenu: Menu
    private lateinit var contentObserver: ContentObserver
    private lateinit var handlerThread: HandlerThread
    private lateinit var contentUri: Uri
    private var popUpDialogFragment: RetrieveDataDialogFragment? = null
    private lateinit var detailUserViewModel: DetailUserViewModel
    private var jsonStringConverterFromUrl: JSONStringConverterFromUrl? = null
    private var favoriteCoroutine: Job? = null
    interface StopFollowingFragmentService{
        fun stopFragmentService()
    }
    interface StopFollowersFragmentService{
        fun stopFragmentService()
    }
    var iStopFollowingFragmentService: StopFollowingFragmentService? = null
    var iStopFollowersFragmentService: StopFollowersFragmentService? = null
    interface IStopActivity{
        fun stopActivity()
    }
    private var iStopActivity: IStopActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        initializeContentProvider()
        initializeData()
        prepareAppBar()
    }
    private fun initializeContentProvider(){
        handlerThread = HandlerThread("user_detail_activity")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        contentObserver = object : ContentObserver(handler){}
        contentUri = FavoriteProvider.getAuthorityPath()
        contentResolver.registerContentObserver(contentUri, true, contentObserver)
    }
    private fun initializeData(){
        detailUserViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(DetailUserViewModel::class.java)
        detailUserViewModel.getUserData().observe(this, Observer { detailUserData ->
            prepareTabLayout(detailUserData)
            popUpDialogFragment?.dismiss()
        })
        mainUserData = intent.getParcelableExtra<MainUser>(MAIN_USER_KEY) as MainUser
        userDAO = UserDatabase.getDatabase(this, null).userDAO()
        jsonStringConverterFromUrl =
            JSONStringConverterFromUrl(
                this@UserDetailActivity,
                "https://api.github.com/users/${mainUserData.username}"
            )
    }
    private fun prepareAppBar(){
        setSupportActionBar(toolbar)
        SettingsComponent.colorizeStatusBarOnly(this)
        toolbar.title = "${mainUserData.username} (${mainUserData.type})"
        Glide.with(this).load(mainUserData.imageURL).apply(RequestOptions().override(125, 125)).into(img_user_avatar)
        val colorWhiteStateList = ColorStateList.valueOf(ContextCompat.getColor(this@UserDetailActivity, android.R.color.white))
        navigation_tab.setBackgroundColor(SettingsComponent.generatePrimaryDarkColor())
        with(collapsing_toolbar_layout){
            contentScrim = ColorDrawable(SettingsComponent.generatePrimaryColor())
            setCollapsedTitleTextColor(colorWhiteStateList)
            setExpandedTitleTextColor(colorWhiteStateList)
        }
    }
    private fun gatherDataFromInternet(){
        detailUserViewModel.iPrepareConnectionData = object : DetailUserViewModel.PrepareConnectionData{
            override fun prepareInternetData() {
                prepareInternetConnection()
            }
        }
        if (detailUserViewModel.getUserData().value == null){
            detailUserViewModel.setDataFromInternet()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_detail_menu, menu)
        this.activityMenu = menu
        if (detailUserViewModel.getFavoriteValue() == null) determineIfFavoriteUserExist()
        else initializeFavoriteIcon()
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.btn_share -> {
                sendIntentDetailUser()
            }
            R.id.btn_favorite -> {
                favoriteCoroutine = GlobalScope.launch(Dispatchers.IO) {
                    val userEntity = userDAO.getSpecificUser(mainUserData.username)
                    if (userEntity != null){
                        removeFavorite(item, userEntity)
                    }
                    else{
                        addToFavorite(item)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun notifyFavoriteUser(item: MenuItem, message: String, iconId: Int){
        runOnUiThread {
            Toast.makeText(this@UserDetailActivity, message, Toast.LENGTH_SHORT).show()
            item.icon = ContextCompat.getDrawable(this, iconId)
        }
    }
    private fun removeFavorite(item: MenuItem, userEntity: UserEntity){
        runOnUiThread { detailUserViewModel.setIsFavoriteValue(false) }
        notifyFavoriteUser(item, resources.getString(R.string.remove_favorite, mainUserData.username), R.drawable.ic_favorite_off)
        val deleteUri = Uri.parse("${contentUri}/${FavoriteProvider.DELETE_KEYWORD}/${userEntity.username}")
        with(contentResolver){
            delete(deleteUri, null, null)
            notifyChange(contentUri, contentObserver)
        }
    }
    private fun addToFavorite(item: MenuItem){
        runOnUiThread { detailUserViewModel.setIsFavoriteValue(true) }
        notifyFavoriteUser(item, resources.getString(R.string.add_favorite, mainUserData.username), R.drawable.ic_favorite_on)
        val userContentValue = ContentValues()
        with(userContentValue){
            put(FavoriteProvider.ENTITY_USERNAME_KEY, mainUserData.username)
            put(FavoriteProvider.ENTITY_TYPE_KEY, mainUserData.type)
            put(FavoriteProvider.ENTITY_IMAGEURL_KEY, mainUserData.imageURL)
        }
        with(contentResolver){
            insert(contentUri, userContentValue)
            notifyChange(contentUri, contentObserver)
        }
    }
    private fun sendIntentDetailUser(){
        val intentDestination = Intent(Intent.ACTION_SEND)
        intentDestination.type = "text/plain"
        val detailUserData = detailUserViewModel.getUserData().value
        val textToSend = "${getString(R.string.owner_username)}: ${detailUserData?.username}\n" +
                "${getString(R.string.type)}: ${detailUserData?.type}\n" +
                "${getString(R.string.fullname)}: ${detailUserData?.fullName}\n"+
                "${getString(R.string.company)}: ${detailUserData?.company}\n" +
                "${getString(R.string.location)}: ${detailUserData?.location}\n"+
                "${getString(R.string.blog)}: ${detailUserData?.blogInformation}\n"+
                "${getString(R.string.repository)}: ${detailUserData?.repository}\n" +
                "${getString(R.string.gist)}: ${detailUserData?.gists}\n"+
                "${getString(R.string.followings)}: ${detailUserData?.following}\n"+
                "${getString(R.string.followers)}: ${detailUserData?.followers}\n"
        intentDestination.putExtra(Intent.EXTRA_TEXT, textToSend)
        val intentChooser = Intent.createChooser(intentDestination, getString(R.string.share_user_information))
        startActivity(intentChooser)
    }
    private fun initializeFavoriteIcon(){
        if (detailUserViewModel.getFavoriteValue() == true){
            activityMenu.findItem(R.id.btn_favorite).icon = ContextCompat.getDrawable(this@UserDetailActivity, R.drawable.ic_favorite_on)
        }
        else{
            activityMenu.findItem(R.id.btn_favorite).icon = ContextCompat.getDrawable(this@UserDetailActivity, R.drawable.ic_favorite_off)
        }
    }
    private fun determineIfFavoriteUserExist(){
        favoriteCoroutine = GlobalScope.launch(Dispatchers.IO) {
            if (userDAO.getSpecificUser(mainUserData.username) != null){
                runOnUiThread {
                    detailUserViewModel.setIsFavoriteValue(true)
                    initializeFavoriteIcon()
                }
            }
            else{
                runOnUiThread {
                    detailUserViewModel.setIsFavoriteValue(false)
                    initializeFavoriteIcon()
                }
            }
        }
    }
    fun prepareInternetConnection(){
        if (jsonStringConverterFromUrl?.getIsRunningState() == null || jsonStringConverterFromUrl?.getIsRunningState() == false){
            var detailUserData: DetailUser? = null
            if (popUpDialogFragment == null){
                popUpDialogFragment = RetrieveDataDialogFragment()
            }
            jsonStringConverterFromUrl?.iBeforeConnect = object : JSONStringConverterFromUrl.BeforeConnect{
                override fun beforeConnect() {
                    popUpDialogFragment?.isCancelable = false
                    popUpDialogFragment?.iModificationUI = object : RetrieveDataDialogFragment.ModificationUI {
                        override fun modifyFragmentView(fragmentView: View) {
                            with(fragmentView){
                                text_title.text = context.getString(R.string.please_wait)
                                progress_circular.visibility = View.VISIBLE
                                text_description.text = context.getString(R.string.retrieve_user_detail)
                                text_description.setTextColor(ContextCompat.getColor(this@UserDetailActivity, android.R.color.black))
                                button_cancel.isEnabled = true
                                button_cancel.text = context.getString(R.string.abort)
                                button_ok.isEnabled = false
                                button_ok.text = context.getString(R.string.waiting)
                            }
                        }
                    }
                    iStopActivity = object : IStopActivity{
                        override fun stopActivity() {
                            popUpDialogFragment?.dismiss()
                            popUpDialogFragment = null
                            jsonStringConverterFromUrl?.stopAllCoroutines()
                        }
                    }
                    popUpDialogFragment?.iCancelButton = object :
                        RetrieveDataDialogFragment.CancelButton {
                        override fun cancelButton() {
                            jsonStringConverterFromUrl?.stopAllCoroutines()
                            popUpDialogFragment?.dismiss()
                            popUpDialogFragment = null
                            finish()
                        }
                    }
                    popUpDialogFragment?.iOkButton = object :
                        RetrieveDataDialogFragment.OKButton {
                        override fun okButton() {
                            jsonStringConverterFromUrl?.stopAllCoroutines()
                            popUpDialogFragment?.dismiss()
                            gatherDataFromInternet()
                        }
                    }
                    popUpDialogFragment?.triggerModifyView()
                }
            }
            jsonStringConverterFromUrl?.iTransferDataUser = object : JSONStringConverterFromUrl.TransferDataUser{
                override fun transferData(jsonObject: String) {
                    popUpDialogFragment?.iModificationUI = object :
                        RetrieveDataDialogFragment.ModificationUI {
                        override fun modifyFragmentView(fragmentView: View) {
                            with(fragmentView){
                                text_title.text = context.getString(R.string.almost_there)
                                progress_circular.visibility = View.VISIBLE
                                text_description.text = context.getString(R.string.parse_user_detail)
                            }
                        }
                    }
                    popUpDialogFragment?.triggerModifyView()
                    detailUserData = prepareDataFromInternet(jsonObject)
                }
            }
            jsonStringConverterFromUrl?.iFinishedTransfer = object : JSONStringConverterFromUrl.FinishedTransfer{
                override fun finishedTransfer(){
                    detailUserViewModel.getUserData().postValue(detailUserData)
                    Toast.makeText(this@UserDetailActivity, getString(R.string.data_load_success), Toast.LENGTH_SHORT).show()
                }
            }
            jsonStringConverterFromUrl?.iFailedParse = object : JSONStringConverterFromUrl.FailedParse{
                override fun failedParse(message: String) {
                    popUpDialogFragment?.iModificationUI = object :
                        RetrieveDataDialogFragment.ModificationUI {
                        override fun modifyFragmentView(fragmentView: View) {
                            with(fragmentView){
                                text_title.text = context.getString(R.string.error_title)
                                text_description.text = message
                                text_description.setTextColor(SettingsComponent.generateRedColor())
                                progress_circular.visibility = View.GONE
                                button_ok.isEnabled = true
                                button_ok.text = context.getString(R.string.retry)
                            }
                        }
                    }
                    popUpDialogFragment?.triggerModifyView()
                }
            }
            popUpDialogFragment?.iAction = object : RetrieveDataDialogFragment.Action{
                override fun doAction(){
                    jsonStringConverterFromUrl?.dataRetriever(false)
                }
            }
            popUpDialogFragment?.show(supportFragmentManager, null)
        }
    }
    fun prepareDataFromInternet(jsonString: String): DetailUser{
        val jsonObject = JSONObject(jsonString)
        val fullNameText = jsonObject.getString("name")
        val companyName = jsonObject.getString("company")
        val location = jsonObject.getString("location")
        val blogInformation = jsonObject.getString("blog")
        val followers = jsonObject.getInt("followers")
        val following = jsonObject.getInt("following")
        val publicRepos = jsonObject.getInt("public_repos")
        val publicGists = jsonObject.getInt("public_gists")
        return DetailUser(
            mainUserData,
            fullNameText,
            companyName,
            location,
            blogInformation,
            publicRepos,
            publicGists,
            followers,
            following
        )
    }
    private fun prepareTabLayout(detailUserData: DetailUser){
        val followingFragment = PeopleFollowingFragment()
        followingFragment.arguments = putPeopleBundle(followingFragment,"https://api.github.com/users/${mainUserData.username}/following", detailUserData.following, getString(R.string.followings),
            R.drawable.ic_people_outline
        )
        val followersFragment = PeopleFollowersFragment()
        followersFragment.arguments = putPeopleBundle(followersFragment,"https://api.github.com/users/${mainUserData.username}/followers", detailUserData.followers, getString(R.string.followers),
            R.drawable.ic_people
        )
        val fragments = arrayOf(PersonalInformationFragment(), RepositoryFragment(), followingFragment, followersFragment)
        val stringTitle = arrayOf(getString(R.string.personal), getString(R.string.repository), getString(
                    R.string.followings), getString(R.string.followers))

        val bundleDetailUserData = Bundle()
        bundleDetailUserData.putParcelable(DETAIL_USER_KEY, detailUserData)

        fragments[0].arguments = bundleDetailUserData
        fragments[1].arguments = bundleDetailUserData
        view_pager_user_detail.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }
            override fun onPageSelected(position: Int) {
                when (position) {
                    2 -> iStopFollowersFragmentService?.stopFragmentService()
                    3 -> iStopFollowingFragmentService?.stopFragmentService()
                    else -> {
                        iStopFollowingFragmentService?.stopFragmentService()
                        iStopFollowersFragmentService?.stopFragmentService()
                    }
                }
            }
        })
        view_pager_user_detail.adapter =
            ViewPagerFragmentAdapter(
                fragments,
                stringTitle,
                supportFragmentManager
            )
        navigation_tab.setupWithViewPager(view_pager_user_detail)
    }
    private fun putPeopleBundle(fragment: PeopleFollowAbstractFragment, url: String, numberOfPeople: Int, type: String, resId: Int): Bundle{
        val bundle = Bundle()
        with(bundle){
            putString(fragment.URL_PEOPLE_KEY, url)
            putInt(fragment.TOTAL_PEOPLE_KEY, numberOfPeople)
            putString(fragment.PEOPLE_TYPE_KEY, type)
            putInt(fragment.IMG_LOGO_RES, resId)
        }
        return bundle
    }
    override fun onPause() {
        super.onPause()
        iStopActivity?.stopActivity()
    }
    override fun onResume() {
        super.onResume()
        gatherDataFromInternet()
    }

    override fun onDestroy() {
        favoriteCoroutine?.cancel()
        handlerThread.quitSafely()
        contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()
    }
    override fun onBackPressed() {
        if (popUpDialogFragment?.isVisible == false){
            jsonStringConverterFromUrl?.stopAllCoroutines()
            iStopFollowingFragmentService?.stopFragmentService()
            iStopFollowersFragmentService?.stopFragmentService()
        }
        setResult(UPDATE_FAVORITE_RESULT_KEY)
        finish()
    }
}
