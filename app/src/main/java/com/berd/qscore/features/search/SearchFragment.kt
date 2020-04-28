package com.berd.qscore.features.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.databinding.SearchFragmentBinding
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.visible
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchFragment : BaseFragment() {

    private val viewModel by viewModels<SearchViewModel>()

    private val searchAdapter = SearchAdapter()

    private val binding: SearchFragmentBinding by lazy {
        SearchFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchBar()
        setupRecyclerView()
        observeEvents()
    }

    private fun observeEvents() {
        viewModel.observeState {
            when (it) {
                is SearchViewModel.SearchState.UsersLoaded -> showUsers(it.users)
                SearchViewModel.SearchState.EmptyResults -> handleEmptyResults()
            }
        }

        viewModel.observeActions {
            when (it) {
                SearchViewModel.SearchAction.ShowProgress -> showProgress()
                SearchViewModel.SearchAction.HideProgress -> hideProgress()
            }
        }
    }

    private fun hideProgress() {
        Timber.d(">>HIDING PROGRESS")
        binding.progressBar.invisible()
    }

    private fun showProgress() {
        Timber.d(">>SHOWING PROGRESS")
        binding.progressBar.visible()
    }

    private fun showUsers(users: List<QUser>) {
        searchAdapter.submitList(users)
        binding.recyclerView.visible()
        binding.noUsersFound.gone()
        binding.progressBar.invisible()
    }

    private fun handleEmptyResults() {
        binding.recyclerView.gone()
        binding.noUsersFound.visible()
        binding.progressBar.invisible()
    }

    private fun setupRecyclerView() = activity?.let { activity ->
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = searchAdapter
    }

    private fun setupSearchBar() {
        binding.searchField
            .afterTextChangeEvents()
            .map { it.editable.toString() }
            .distinctUntilChanged()
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribeBy(onNext = {
                viewModel.onSearch(it)
            }, onError = {
                Timber.d("Unable to handle textChange event: $it")
            }).addTo(compositeDisposable)
    }

    companion object {
        fun newInstance() = SearchFragment()
    }
}
