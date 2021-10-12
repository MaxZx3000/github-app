package com.example.finalgithubappsubmission.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.adapters.people.FavoriteAdapter
import com.example.finalgithubappsubmission.contentprovider.FavoriteProvider
import com.example.finalgithubappsubmission.itemdecoration.RecyclerViewItemDecoration
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.MainUser
import com.example.finalgithubappsubmission.users.database.UserEntity
import com.example.finalgithubappsubmission.viewmodel.FavoriteViewModel
import com.example.finalgithubappsubmission.viewmodel.SelectedFavoriteViewModel
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.people_layout.recycler_view_user
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, ActionMode.Callback{
    companion object{
        private const val UPDATE_REQUEST_CODE = 200
    }
    interface IOnChangeListener{
        fun onChangeListener()
    }
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var selectedFavoriteViewModel: SelectedFavoriteViewModel
    private lateinit var favoriteActionMode: ActionMode
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var handlerThread: HandlerThread
    private lateinit var contentObserver: ContentObserver
    private var isDelete = false
    private var iOnChangeListener: IOnChangeListener? = null
    private lateinit var contentUri: Uri
    private var iProcessDataCoroutine: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        init()
    }
    private fun init(){
        supportActionBar?.title = resources.getString(R.string.favorite)
        SettingsComponent.overrideActivityColor(this, root_view)
        prepareDefaultData()
    }
    private fun setSelectedFavoriteViewModelObserver(){
        selectedFavoriteViewModel.getIsActionMode().observe(this, Observer { isActionMode ->
            if (isActionMode){
                startActionMode(this)
            }
            else{
                selectedFavoriteViewModel.clearFavoriteUsersData()
                try{
                    favoriteActionMode.finish()
                    favoriteAdapter.notifyDataSetChanged()
                }
                catch (e: Exception){

                }
            }
        })
    }
    private fun setFavoriteViewModelObserver(){
        with(favoriteViewModel) {
            getFavoriteUsers().observe(this@FavoriteActivity, Observer { userEntityArrayList ->
                displayFavoriteData(userEntityArrayList)
            })
            getSortOrder().observe(this@FavoriteActivity, Observer { sortValue ->
                generateSortOrder(sortValue)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        isDelete = false
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isDelete = true
    }
    private fun initializeFavoriteViewModel(){
        favoriteViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(FavoriteViewModel::class.java)
        selectedFavoriteViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(SelectedFavoriteViewModel::class.java)
    }
    private fun prepareDefaultData() {
        initializeFavoriteViewModel()
        setSelectedFavoriteViewModelObserver()
        setFavoriteViewModelObserver()
        val keywordStringSet = SettingsComponent.settingsPreference.getStringSet(SettingsComponent.KEYWORDS_SET_KEY, null)
        tv_number_of_keywords.text = keywordStringSet?.size?.toString() ?: "0"
        handlerThread = HandlerThread("user_favorite_data_observer")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        contentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                iOnChangeListener?.onChangeListener()
            }
        }
        contentUri = FavoriteProvider.getAuthorityPath()
        with(recycler_view_user) {
            addItemDecoration(RecyclerViewItemDecoration(this@FavoriteActivity))
            layoutManager = GridLayoutManager(this@FavoriteActivity, 2)
        }
        contentResolver.registerContentObserver(contentUri, true, contentObserver)
        if (favoriteViewModel.getSortOrder().value == null) {
            val selectedValue = 2
            spinner_sort_favorite.setSelection(selectedValue, false)
            favoriteViewModel.applySortOrder(spinner_sort_favorite.selectedItem.toString())
        }
    }
    private fun getQueryCursor(uri: Uri): Cursor? = contentResolver?.query(uri, null, null, null, null, null)
    private fun generateLatestFavoriteData(){
        iProcessDataCoroutine = GlobalScope.launch(Dispatchers.IO) {
            val cursor = getQueryCursor(contentUri)
            favoriteViewModel.convertCursorToArrayListLiveData(cursor)
        }
    }
    private fun generateAscendingFavoriteData(){
        iProcessDataCoroutine = GlobalScope.launch(Dispatchers.IO) {
            val uriAscending = Uri.parse("$contentUri/${FavoriteProvider.ALL_USER_FAVORITES_ASC}")
            val cursor = getQueryCursor(uriAscending)
            favoriteViewModel.convertCursorToArrayListLiveData(cursor)
        }
    }
    private fun generateDescendingFavoriteData(){
        iProcessDataCoroutine = GlobalScope.launch(Dispatchers.IO) {
            val uriDescending = Uri.parse("$contentUri/${FavoriteProvider.ALL_USER_FAVORITES_DESC}")
            val cursor = getQueryCursor(uriDescending)
            favoriteViewModel.convertCursorToArrayListLiveData(cursor)
        }
    }
    private fun displayFavoriteData(favoriteUserEntityData: ArrayList<UserEntity>){
        runOnUiThread {tv_number_of_favorite.text = favoriteUserEntityData.size.toString()}
        if (favoriteUserEntityData.isNotEmpty()){
            favoriteAdapter = FavoriteAdapter(favoriteUserEntityData as ArrayList<MainUser>, R.layout.list_main_item_layout)
            favoriteAdapter.iShowCheckBehaviour = object : FavoriteAdapter.IShowCheckBehaviour{
                override fun checkBehaviour(checkView: View, mainUserData: MainUser) {
                    if (selectedFavoriteViewModel.getFavoriteSelectedUsersValue()?.contains(mainUserData.username) == true){
                        checkView.visibility = View.VISIBLE
                    }
                    else{
                        checkView.visibility = View.GONE
                    }
                }
            }
            favoriteAdapter.iLongHoldBehavior = object : FavoriteAdapter.ILongClickBehaviour{
                override fun longClickBehaviour(checkView: View, mainUserData: MainUser, position: Int) {
                    val isActionModeValue = selectedFavoriteViewModel.getIsActionMode().value
                    if (isActionModeValue == false || isActionModeValue == null){
                        selectedFavoriteViewModel.addFavoriteUsersLiveData(mainUserData.username)
                        checkView.visibility = View.VISIBLE
                        selectedFavoriteViewModel.setIsActionMode(true)
                    }
                }
            }
            favoriteAdapter.iFavoriteClickBehaviour = object : FavoriteAdapter.IClickBehaviour{
                override fun onClick(checkView: View, position: Int, mainUserData: MainUser) =
                    if (selectedFavoriteViewModel.getIsActionMode().value == true){
                        if (selectedFavoriteViewModel.getFavoriteSelectedUsersValue()?.contains(mainUserData.username) == false){
                            selectedFavoriteViewModel.addFavoriteUsersLiveData(mainUserData.username)
                            checkView.visibility = View.VISIBLE
                        }
                        else{
                            checkView.visibility = View.GONE
                            selectedFavoriteViewModel.removeFavoriteUsersLiveData(mainUserData.username)
                        }
                        favoriteActionMode.title = resources.getString(R.string.favorite_action_mode_title, selectedFavoriteViewModel.getFavoriteSelectedUsersValue()?.size)
                    }
                    else{
                        val intent = Intent(this@FavoriteActivity, UserDetailActivity::class.java)
                        intent.putExtra(UserDetailActivity.MAIN_USER_KEY, favoriteUserEntityData[position])
                        startActivityForResult(intent, UPDATE_REQUEST_CODE)
                    }
            }
            runOnUiThread {
                spinner_sort_favorite.onItemSelectedListener = this@FavoriteActivity
                container_data.visibility = View.VISIBLE
                container_no_data.visibility = View.GONE
                tv_long_click.setTextColor(SettingsComponent.getDarkAccentColor())
                recycler_view_user.adapter = favoriteAdapter
            }
        }
        else{
            runOnUiThread {
                container_no_data.visibility = View.VISIBLE
                container_data.visibility = View.GONE
                img_no_data.imageTintList = ColorStateList.valueOf(SettingsComponent.getAccentColor())
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            UPDATE_REQUEST_CODE -> {
                if(resultCode == UserDetailActivity.UPDATE_FAVORITE_RESULT_KEY){
                    contentResolver.notifyChange(contentUri, null)
                }
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
    private fun generateSortOrder(sortOrder: String){
        when(sortOrder){
            resources.getString(R.string.ascending)-> {
                iOnChangeListener = object : IOnChangeListener{
                    override fun onChangeListener() { generateAscendingFavoriteData() }
                }
                generateAscendingFavoriteData()
            }
            resources.getString(R.string.descending) -> {
                iOnChangeListener = object : IOnChangeListener{
                    override fun onChangeListener() { generateDescendingFavoriteData() }
                }
                generateDescendingFavoriteData()
            }
            resources.getString(R.string.latest) -> {
                iOnChangeListener = object : IOnChangeListener{
                    override fun onChangeListener() { generateLatestFavoriteData() }
                }
                generateLatestFavoriteData()
            }
        }
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
        favoriteViewModel.applySortOrder(parent?.getItemAtPosition(position).toString())
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
        actionMode.menuInflater?.inflate(R.menu.delete_user_favorite_menu, menu)
        favoriteActionMode = actionMode
        favoriteActionMode.title = resources.getString(R.string.favorite_action_mode_title, selectedFavoriteViewModel.getFavoriteSelectedUsersValue()?.size)
        favoriteActionMode.subtitle = getString(R.string.delete_users)
        return true
    }
    override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
        when(menu?.itemId){
            R.id.btn_delete -> {
                isDelete = true
                if (selectedFavoriteViewModel.getFavoriteSelectedUsersValue() != null){
                    val selectedFavoriteArrayList = selectedFavoriteViewModel.getFavoriteSelectedUsersValue() as ArrayList<String>
                    var numberOfDeletions = 0
                    iProcessDataCoroutine = GlobalScope.launch(Dispatchers.IO) {
                        for (user in selectedFavoriteArrayList){
                            val uri = Uri.parse("$contentUri/${FavoriteProvider.DELETE_KEYWORD}/$user")
                            numberOfDeletions += contentResolver?.delete(uri, null, null) as Int
                        }
                        runOnUiThread {Toast.makeText(this@FavoriteActivity, resources.getString(R.string.favorite_number_of_delete, numberOfDeletions), Toast.LENGTH_SHORT).show() }
                        contentResolver.notifyChange(contentUri, null)
                        selectedFavoriteViewModel.setIsActionMode(false)
                    }
                }
            }
        }
        return true
    }
    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
        return true
    }
    override fun onDestroyActionMode(actionMode: ActionMode?) {
        if (!isDelete) {
            selectedFavoriteViewModel.setIsActionMode(false)
        }
        isDelete = false
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
        handlerThread.quitSafely()
        iProcessDataCoroutine?.cancel()
    }
}