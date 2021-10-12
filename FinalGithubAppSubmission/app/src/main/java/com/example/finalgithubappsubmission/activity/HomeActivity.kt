package com.example.finalgithubappsubmission.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.fragments.IntroDialogFragment
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), View.OnClickListener {
    companion object{
        const val OPTIONS_REQUEST_CODE = 100
    }
    private var isSaveInstanceState = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.title = getString(R.string.home)
        init()
    }

    override fun onResume() {
        super.onResume()
        isSaveInstanceState = false
    }
    private fun initializeButtonListeners(){
        btn_exit.setOnClickListener(this)
        btn_favorite.setOnClickListener(this)
        btn_options.setOnClickListener(this)
        btn_search.setOnClickListener(this)
    }
    private fun init(){
        prepareSettings()
        initializeButtonListeners()
        Glide.with(this).load(R.drawable.octocat).apply(RequestOptions.overrideOf(130, 130)).into(img_octocat)
    }
    override fun onClick(view: View) {
        when(view.id){
            R.id.btn_search -> {
                startActivity(Intent(this, UserFinderActivity::class.java))
            }
            R.id.btn_exit -> {
                finish()
            }
            R.id.btn_favorite -> {
                startActivity(Intent(this, FavoriteActivity::class.java))
            }
            R.id.btn_options -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), OPTIONS_REQUEST_CODE)
            }
        }
    }
    private fun prepareSettings(){
        SettingsComponent.initializePreference(this)
        if (SettingsComponent.settingsPreference.getString(SettingsComponent.NAME_KEY, null) == null){
            showUsernameDialogFragment()
        }
        else{
            tv_username.text = SettingsComponent.settingsPreference.getString(SettingsComponent.NAME_KEY, null)
        }
        SettingsComponent.overrideActivityColor(this, root_view)
    }
    private fun showUsernameDialogFragment(){
        val usernameDialogFragment = IntroDialogFragment()
        usernameDialogFragment.isCancelable = false
        usernameDialogFragment.show(supportFragmentManager, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isSaveInstanceState = true
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            OPTIONS_REQUEST_CODE -> {
                when(resultCode){
                    SettingsActivity.RESET_ACTIVITY_RESULT_CODE -> {
                        GlobalScope.launch(Dispatchers.IO) {
                            delay(300)
                            runOnUiThread{
                                Toast.makeText(this@HomeActivity, getString(R.string.restart), Toast.LENGTH_SHORT).show()
                                recreate()
                            }
                        }
                    }
                }
            }
        }
    }
}