package com.berd.qscore.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.berd.qscore.R
import com.berd.qscore.databinding.LeaderboardTabFragmentBinding
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.google.android.material.tabs.TabLayoutMediator

class LeaderboardTabFragment : Fragment() {

    val binding by lazy {
        LeaderboardTabFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTabs()
        setStatusbarColor(R.color.colorPrimary)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setStatusbarColor(R.color.colorPrimary)
    }

    private fun setupTabs() {
        binding.pager.adapter = DemoCollectionAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Friends"
                else -> "Global"
            }
        }.attach()
    }

    class DemoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            // Return a NEW fragment instance in createFragment(int)
            val type = when (position) {
                0 -> LeaderboardType.SOCIAL
                else -> LeaderboardType.GLOBAL
            }
            return LeaderboardFragment.newInstance(type)
        }
    }

    companion object {
        fun newInstance() = LeaderboardTabFragment()
    }
}
