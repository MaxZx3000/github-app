package com.example.finalgithubappsubmission.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.fragments.PreferenceFragment
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    companion object{
        const val RESET_ACTIVITY_RESULT_CODE = 102
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = getString(R.string.options)
        SettingsComponent.overrideActivityColor(this, settings_holder)
        supportFragmentManager.beginTransaction().replace(R.id.settings_holder,
            PreferenceFragment()
        ).commit()
    }


    override fun onBackPressed() {
        setResult(RESET_ACTIVITY_RESULT_CODE)
        super.onBackPressed()
    }
}