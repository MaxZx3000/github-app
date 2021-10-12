package com.example.finalgithubappsubmission.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_intro_dialog.*
import kotlinx.android.synthetic.main.fragment_intro_dialog.root_view
import kotlinx.android.synthetic.main.fragment_intro_dialog.view.*

class IntroDialogFragment : DialogFragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(view).load(R.drawable.octocat).apply(RequestOptions.overrideOf(150, 150)).into(view.img_octocat)
        SettingsComponent.colorizeTaggedViews(context as Context, root_view)
        btn_abort.setOnClickListener(this)
        btn_ok.setOnClickListener(this)
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }
    override fun onClick(view: View) {
        when(view.id){
            R.id.btn_abort -> {
                activity?.finish()
            }
            R.id.btn_ok -> {
                SettingsComponent.settingsPreference.edit()
                    .putString(SettingsComponent.NAME_KEY, edt_username.text.toString())
                    .apply()
                activity?.tv_username?.text = edt_username.text
                dismiss()
            }
        }
    }
}