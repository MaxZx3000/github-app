package com.example.finalgithubappsubmission.fragments.peoplefragments

import android.os.Bundle
import com.example.finalgithubappsubmission.activity.UserDetailActivity

class PeopleFollowingFragment : PeopleFollowAbstractFragment(){
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as UserDetailActivity).iStopFollowingFragmentService = object : UserDetailActivity.StopFollowingFragmentService{
            override fun stopFragmentService() {
                stopBackgroundProcess()
            }
        }
    }
}