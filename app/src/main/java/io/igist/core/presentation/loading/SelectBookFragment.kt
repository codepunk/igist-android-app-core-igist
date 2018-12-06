/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import io.igist.core.BuildConfig.DEFAULT_BOOK_ID
import io.igist.core.BuildConfig.KEY_BOOK_ID
import io.igist.core.R
import io.igist.core.databinding.FragmentSelectBookBinding
import javax.inject.Inject

/**
 * A [Fragment] for displaying and selecting available books.
 */
class SelectBookFragment : Fragment() {

    // region Properties

    /**
     * The injected [ViewModelProvider.Factory] that we will use to get an instance of
     * [LoadingViewModel].
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentSelectBookBinding

    // endregion Properties

    /**
     * The [SelectBookViewModel] instance backing this fragment.
     */
    private val loadingViewModel: LoadingViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(LoadingViewModel::class.java)
    }

    // region Lifecycle methods

    /**
     * Performs dependency injection.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        loadingViewModel.bookIdData.observe(
            this,
            Observer { bookId -> onBookSelected(bookId) }
        )
    }

    /**
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_book,
            container,
            false
        )
        return binding.root
    }


    /**
     * Initializes the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
         * NOTE Since we're not currently populating a list of books, clicking
         * the button will just "select" the default book. We can make this more
         * robust in the future with a RecyclerView of books, for example.
         */
        binding.igistBtn.setOnClickListener {
            loadingViewModel.selectBook(DEFAULT_BOOK_ID.toLong())
        }
    }

    // endregion Lifecycle methods

    // region Methods

    /**
     * Processes a book selection.
     */
    private fun onBookSelected(bookId: Long) {
        activity?.apply {
            setResult(
                Activity.RESULT_OK,
                Intent().apply {
                    putExtra(KEY_BOOK_ID, bookId)
                }
            )
            finish()
        }
    }

    // endregion Methods

}
