package com.example.finalgithubappsubmission.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerFragmentAdapter(private val fragments: Array<Fragment>, private val title: Array<String>, fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }
    override fun getCount(): Int {
        return fragments.size
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return title[position]
    }
}