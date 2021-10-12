package com.example.finalgithubappsubmission.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.android.synthetic.main.fragment_color_dialog.*

class ColorDialogFragment : DialogFragment(), SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    interface IOKButton{
        fun okButtonAction(colorInt: Int)
    }
    var iOKButton: IOKButton? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_color_dialog, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeListeners()
        generateCurrentValues()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }
    private fun initializeListeners(){
        seek_bar_blue.setOnSeekBarChangeListener(this)
        seek_bar_green.setOnSeekBarChangeListener(this)
        seek_bar_red.setOnSeekBarChangeListener(this)
        btn_abort.setOnClickListener(this)
        btn_apply.setOnClickListener(this)
    }
    private fun generateCurrentValues(){
        seek_bar_red.progress = SettingsComponent.settingsPreference.getInt(SettingsComponent.RED_KEY, 0)
        seek_bar_green.progress = SettingsComponent.settingsPreference.getInt(SettingsComponent.GREEN_KEY, 0)
        seek_bar_blue.progress = SettingsComponent.settingsPreference.getInt(SettingsComponent.BLUE_KEY, 0)
        updateColorImagePreview()
    }
    override fun onProgressChanged(seekBar: SeekBar, value: Int, p2: Boolean) {
        updateColorImagePreview()
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }
    override fun onClick(view: View) {
        when (view.id){
            R.id.btn_abort -> {
                dismiss()
            }
            R.id.btn_apply -> {
                SettingsComponent.settingsPreference.edit()
                    .putInt(SettingsComponent.RED_KEY, seek_bar_red.progress)
                    .putInt(SettingsComponent.GREEN_KEY, seek_bar_green.progress)
                    .putInt(SettingsComponent.BLUE_KEY, seek_bar_blue.progress)
                    .apply()
                iOKButton?.okButtonAction(getPickedColor())
                dismiss()
            }
        }
    }
    private fun getPickedColor(): Int{
        return Color.rgb(seek_bar_red.progress, seek_bar_green.progress, seek_bar_blue.progress)
    }
    private fun updateColorImagePreview(){
        img_color_preview.setBackgroundColor(getPickedColor())
    }
}