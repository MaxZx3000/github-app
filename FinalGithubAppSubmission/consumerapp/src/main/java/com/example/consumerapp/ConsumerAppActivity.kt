package com.example.consumerapp

import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.consumerapp.adapters.people.FavoriteAdapter
import com.example.consumerapp.contentprovider.FavoriteProvider
import com.example.consumerapp.itemDecoration.RecyclerViewItemDecoration
import com.example.consumerapp.users.MainUser
import com.example.consumerapp.users.database.UserEntity
import com.example.consumerapp.viewmodel.FavoriteViewModel
import kotlinx.android.synthetic.main.activity_consumer_app.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ConsumerAppActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    interface IOnChangeListener{
        fun onChangeListener()
    }
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var favoriteAdapter: FavoriteAdapter
    private var isDelete = false
    private var iOnChangeListener: IOnChangeListener? = null
    private lateinit var contentUri: Uri
    private var iProcessDataCoroutine: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumer_app)
        init()
    }
    private fun init(){
        supportActionBar?.title = resources.getString(R.string.favorite)
        prepareDefaultData()
    }
    private fun setFavoriteViewModelObserver(){
        with(favoriteViewModel) {
            getFavoriteUsers().observe(this@ConsumerAppActivity, Observer { userEntityArrayList ->
                displayFavoriteData(userEntityArrayList)
            })
            getSortOrder().observe(this@ConsumerAppActivity, Observer { sortValue ->
                generateSortOrder(sortValue)
            })
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isDelete = true
    }
    private fun initializeFavoriteViewModel(){
        favoriteViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(FavoriteViewModel::class.java)
    }
    private fun prepareDefaultData() {
        initializeFavoriteViewModel()
        setFavoriteViewModelObserver()
        val handlerThread = HandlerThread("user_favorite_data_observer")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val myObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                iOnChangeListener?.onChangeListener()
            }
        }
        contentUri = FavoriteProvider.getAuthorityPath()
        with(recycler_view_user) {
            addItemDecoration(RecyclerViewItemDecoration(this@ConsumerAppActivity))
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@ConsumerAppActivity, 2)
        }
        contentResolver.registerContentObserver(contentUri, true, myObserver)
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
            runOnUiThread {
                spinner_sort_favorite.onItemSelectedListener = this@ConsumerAppActivity
                container_data.visibility = View.VISIBLE
                container_no_data.visibility = View.GONE
                recycler_view_user.adapter = favoriteAdapter
            }
        }
        else{
            runOnUiThread {
                container_no_data.visibility = View.VISIBLE
                container_data.visibility = View.GONE
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

    override fun onDestroy() {
        super.onDestroy()
        iProcessDataCoroutine?.cancel()
    }
}
