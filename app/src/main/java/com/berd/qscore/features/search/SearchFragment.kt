package com.berd.qscore.features.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.berd.qscore.R
import com.berd.qscore.databinding.SearchFragmentBinding
import com.berd.qscore.features.search.SearchViewModel.SearchAction.*
import com.berd.qscore.features.search.SearchViewModel.SearchState.LoadingState
import com.berd.qscore.features.shared.activity.BaseFragment
import com.berd.qscore.features.shared.api.models.QUser
import com.berd.qscore.features.shared.user.UserAdapter
import com.berd.qscore.features.user.UserActivity
import com.berd.qscore.utils.extensions.gone
import com.berd.qscore.utils.extensions.invisible
import com.berd.qscore.utils.extensions.setStatusbarColor
import com.berd.qscore.utils.extensions.visible
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchFragment : BaseFragment() {

    private val viewModel by viewModels<SearchViewModel>()

    private val searchAdapter by lazy {
        UserAdapter(::handleSearchItemClicked)
    }

    private val binding: SearchFragmentBinding by lazy {
        SearchFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchBar()
        setupViews()
        setupRecyclerView()
        observeEvents()
        viewModel.onViewCreated()
    }

    private fun setupViews() = with(binding) {
        setStatusbarColor(R.color.colorPrimary)
        clearButton.setOnClickListener { searchField.setText("") }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        setStatusbarColor(R.color.colorPrimary)
    }

    override fun getScreenName() = "search"

    private fun handleSearchItemClicked(user: QUser) {
        activity?.let { activity ->
            val intent = UserActivity.newIntent(activity, user.userId)
            startActivity(intent)
        }
    }

    private fun observeEvents() {
        viewModel.observeActions {
            when (it) {
                is SubmitPagedList -> handleSubmitPagedList(it.pagedList)
                is UpdateLoadingState -> handleLoadingState(it.loadingState)
                is Initialize -> initialize(it.state)
            }
        }
    }

    private fun initialize(state: SearchViewModel.SearchState) {
        handleLoadingState(state.loadingState)
        state.pagedList?.let { handleSubmitPagedList(it) }
    }

    private fun handleLoadingState(loadingState: LoadingState) {
        when (loadingState) {
            LoadingState.LOADING -> handleLoading()
            LoadingState.LOADED -> handleLoaded()
            LoadingState.EMPTY_RESULTS -> handleEmptyResults()
            LoadingState.ERROR -> handleError()
            LoadingState.READY -> handleReady()
        }
    }

    private fun handleSubmitPagedList(pagedList: PagedList<QUser>) {
        searchAdapter.submitList(pagedList)
    }

    private fun handleError() {
        handleEmptyResults()
    }

    private fun handleReady() = with(binding) {
        progressBar.gone()
        clearButton.gone()
        instructions.visible()
        searching.gone()
        noUsersFound.gone()
        robot.visible()
    }

    private fun handleLoading() = with(binding) {
        progressBar.visible()
        clearButton.gone()
        instructions.gone()
        searching.gone()
        noUsersFound.gone()
        robot.gone()
    }

    private fun handleLoaded() = with(binding) {
        recyclerView.visible()
        noUsersFound.gone()
        clearButton.visible()
        progressBar.invisible()
        instructions.gone()
        robot.gone()
        searching.gone()
    }

    private fun handleEmptyResults() = with(binding) {
        recyclerView.gone()
        noUsersFound.visible()
        clearButton.visible()
        progressBar.invisible()
        instructions.gone()
        robot.gone()
        searching.gone()
    }

    private fun setupRecyclerView() = activity?.let { activity ->
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = searchAdapter
    }

    private fun setupSearchBar() = binding.searchField.post {
        binding.searchField
            .afterTextChangeEvents()
            .skip(1)
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
