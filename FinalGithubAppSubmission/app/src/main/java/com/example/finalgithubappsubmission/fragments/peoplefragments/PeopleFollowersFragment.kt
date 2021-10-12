package com.example.finalgithubappsubmission.fragments.peoplefragments

import android.os.Bundle
import com.example.finalgithubappsubmission.activity.UserDetailActivity

class PeopleFollowersFragment : PeopleFollowAbstractFragment(){
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as UserDetailActivity).iStopFollowersFragmentService = object : UserDetailActivity.StopFollowersFragmentService{
            override fun stopFragmentService() {
                stopBackgroundProcess()
            }
        }
    }
}