/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import io.igist.core.BuildConfig.DEFAULT_BOOK_ID
import io.igist.core.BuildConfig.PREF_KEY_CURRENT_BOOK_ID
import io.igist.core.R
import io.igist.core.databinding.FragmentSelectBookBinding
import javax.inject.Inject

/**
 * A [Fragment] for displaying and selecting available books.
 */
class SelectBookFragment : Fragment() {

    // region Properties

    /**
     * The app [SharedPreferences].
     */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    /**
     * Binding for this fragment.
     */
    private lateinit var binding: FragmentSelectBookBinding

    // endregion Properties

    // region Lifecycle methods

    /**
     * Performs dependency injection.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
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
         * A temporary way to select a book if none is selected.
         */
        binding.igistBtn.setOnClickListener { onBookSelected(DEFAULT_BOOK_ID.toLong()) }
    }

    // endregion Lifecycle methods

    // region Methods

    /**
     * Processes a book selection.
     */
    fun onBookSelected(bookId: Long) {
        sharedPreferences.edit()
            .putLong(PREF_KEY_CURRENT_BOOK_ID, bookId)
            .apply()

        activity?.apply {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    // endregion Methods

}
