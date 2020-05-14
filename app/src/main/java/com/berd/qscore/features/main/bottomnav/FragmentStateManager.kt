package com.berd.qscore.features.main.bottomnav

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class FragmentStateManager(private val contentResId: Int, private val fragmentManager: FragmentManager) {

    private val currentFragment get() = fragmentManager.primaryNavigationFragment

    fun changeTab(tab: BottomTab) {
        //Check for existing fragment
        fragmentManager.beginTransaction().apply {
            val fragment = fragmentManager.findTabFragment(tab) ?: addTabFragment(tab)
            makeFragmentVisible(fragment)
            commitNowAllowingStateLoss()
        }
    }

    private fun FragmentManager.findTabFragment(tab: BottomTab): Fragment? {
        return findFragmentByTag(tab.getTag())
    }

    private fun FragmentTransaction.addTabFragment(tab: BottomTab): Fragment {
        val fragment = tab.buildFragment()
        add(contentResId, fragment, tab.getTag())
        return fragment
    }

    private fun FragmentTransaction.makeFragmentVisible(fragment: Fragment) {
        show(fragment)
        if (currentFragment != fragment) {
            currentFragment?.let { hide(it) }
        }
        setPrimaryNavigationFragment(fragment)
    }

    private fun BottomTab.getTag(): String {
        return "tab:$name"
    }
}
