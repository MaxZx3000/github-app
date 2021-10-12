package com.example.finalgithubappsubmission.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.activity.UserDetailActivity
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.DetailUser
import kotlinx.android.synthetic.main.fragment_repository.*

class RepositoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repository, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val detailUser = arguments?.getParcelable(UserDetailActivity.DETAIL_USER_KEY) as DetailUser?
        txt_repositories.text = "${detailUser?.repository}"
        txt_gists.text = "${detailUser?.gists}"
        SettingsComponent.colorizeTaggedViews(activity as AppCompatActivity, root_view)
    }
}
