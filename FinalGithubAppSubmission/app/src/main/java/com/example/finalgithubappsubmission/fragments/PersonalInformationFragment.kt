package com.example.finalgithubappsubmission.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.activity.UserDetailActivity
import com.example.finalgithubappsubmission.activity.WebViewActivity
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.DetailUser
import kotlinx.android.synthetic.main.fragment_personal_information.*

class PersonalInformationFragment : Fragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_personal_information, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SettingsComponent.colorizeTaggedViews(context as Context, root_view)
    }
    private fun isActualStringEmpty(actualString: String?): Boolean{
        return actualString == null || actualString == "null" || actualString.isBlank()
    }
    private fun getSpecifiedName(view: TextView, actualString: String?): String{
        with(view){
            text = if (isActualStringEmpty(actualString)){
                getString(R.string.no_information)
            } else{
                actualString
            }
        }
        return view.text.toString()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prepareData()
    }
    override fun onClick(view: View) {
        when(view.id){
            R.id.btn_show_blog -> {
                openBrowserIntent()
            }
        }
    }
    private fun prepareData(){
        val detailUser = arguments?.getParcelable<DetailUser>(UserDetailActivity.DETAIL_USER_KEY)
        tv_username.text = detailUser?.username
        detailUser?.company = getSpecifiedName(tv_company, detailUser?.company)
        detailUser?.location = getSpecifiedName(tv_location, detailUser?.location)
        detailUser?.blogInformation = getSpecifiedName(tv_blog, detailUser?.blogInformation)
        if (detailUser?.blogInformation == getString(R.string.no_information)){
            btn_show_blog.isEnabled = false
        }
        btn_show_blog.setOnClickListener(this)
    }
    private fun openBrowserIntent(){
        var url = tv_blog.text.toString()
        if (!url.startsWith("http://") && !url.startsWith("https://")){
            url = "https://${url}"
        }
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.LINK_BLOG_KEY, url)
        startActivity(intent)
    }
}
