package com.example.finalgithubappsubmission.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.finalgithubappsubmission.R
import kotlinx.android.synthetic.main.fragment_pop_up.*
class RetrieveDataDialogFragment : DialogFragment(), View.OnClickListener {
    interface ModificationUI{
        fun modifyFragmentView(fragmentView: View)
    }
    interface CancelButton{
        fun cancelButton()
    }
    interface OKButton{
        fun okButton()
    }
    interface Action{
        fun doAction()
    }
    private lateinit var popUpView: View
    var iModificationUI: ModificationUI? = null
    var iCancelButton: CancelButton? = null
    var iOkButton: OKButton? = null
    var iAction: Action? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        popUpView = inflater.inflate(R.layout.fragment_pop_up, container, false)
        return popUpView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_ok.setOnClickListener(this)
        button_cancel.setOnClickListener(this)
        triggerModifyView()
        doDialogFragmentAction()
    }
    private fun doDialogFragmentAction(){
        iAction?.doAction()
    }
    fun triggerModifyView(){
        iModificationUI?.modifyFragmentView(popUpView)
    }
    override fun onClick(view: View) {
        when(view.id){
            R.id.button_ok -> {
                iOkButton?.okButton()
            }
            R.id.button_cancel -> {
                iCancelButton?.cancelButton()
            }
        }
    }
}
