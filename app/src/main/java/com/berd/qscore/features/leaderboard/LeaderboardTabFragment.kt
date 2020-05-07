package com.berd.qscore.features.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.berd.qscore.databinding.LeaderboardTabFragmentBinding
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
            return LeaderboardFragment.newInstance()
        }
    }

    companion object {
        fun newInstance() = LeaderboardTabFragment()

    }
}
