package com.berd.qscore.features.main.bottomnav

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import timber.log.Timber

class FragmentStateManager(private var container: ViewGroup, private val fragmentManager: FragmentManager) {

    fun changeFragment(bottomTab: BottomTab): Fragment? {
        val tag = makeFragmentName(
            container.id,
            bottomTab.name
        )

        return fragmentManager.beginTransaction().run {
            val fragment = fragmentManager.findFragmentByTag(tag)?.also {
                show(it)
            } ?: bottomTab.fragment.also {
                add(container.id, it, tag)
            }
            if (fragment != fragmentManager.primaryNavigationFragment) {
                fragmentManager.primaryNavigationFragment?.let { hide(it) }
            }
            setPrimaryNavigationFragment(fragment)
            setReorderingAllowed(true)
            commitNowAllowingStateLoss()
            fragment.also { Timber.d(">>FRAGMENT $it") }
        }
    }

    companion object {
        private fun makeFragmentName(viewId: Int, id: String): String {
            return "android:switcher:$viewId:$id"
        }
    }
}
