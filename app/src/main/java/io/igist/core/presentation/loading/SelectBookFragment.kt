/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import dagger.android.support.AndroidSupportInjection
import io.igist.core.BuildConfig.DEFAULT_BOOK_ID
import io.igist.core.BuildConfig.KEY_BOOK_ID
import io.igist.core.R
import io.igist.core.databinding.FragmentSelectBookBinding
import io.igist.core.domain.model.Book
import java.util.*
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
    private val selectBookViewModel: SelectBookViewModel by lazy {
        ViewModelProviders.of(requireActivity(), viewModelFactory)
            .get(SelectBookViewModel::class.java)
    }

    /**
     * The recycle view adapter.
     */
    private val adapter: BookAdapter = BookAdapter()

    // region Lifecycle methods

    /**
     * Performs dependency injection.
     */
    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        selectBookViewModel.books.observe(
            this,
            Observer { books -> onBooks(books) }
        )

        selectBookViewModel.bookIdData.observe(
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
            selectBookViewModel.selectBook(DEFAULT_BOOK_ID)
        }

        val spanCount = resources.getInteger(R.integer.select_book_span_count)
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(requireContext(), spanCount)
        binding.bookRecycler.layoutManager = layoutManager
        binding.bookRecycler.adapter = adapter
    }

    // endregion Lifecycle methods

    // region Methods

    private fun onBooks(update: DataUpdate<List<Book>, List<Book>>) {
        when (update) {
            is ProgressUpdate -> {
                if (update.progress.isNotEmpty()) {
                    adapter.books = update.progress.getOrNull(0)
                }
            }
            is SuccessUpdate -> {
                adapter.books = update.result
            }
            else -> {
                // TODO
            }
        }
    }

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

    // region Nested/inner classes

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // region Properties

        private val titleText: AppCompatTextView = itemView.findViewById(R.id.title_txt)

        private val previewImage: AppCompatImageView = itemView.findViewById(R.id.preview_image)

        private val descriptionText: AppCompatTextView = itemView.findViewById(R.id.description_txt)

        // endregion Properties

        // region Methods

        fun bind(book: Book) {
            val context: Context = itemView.context
            val resources: Resources = context.resources
            titleText.text = book.title
            val resId: Int = resources.getIdentifier(
                book.previewImageName,
                "drawable",
                context.packageName
            )
            val bitmap: Bitmap = BitmapFactory.decodeResource(resources, resId)
            val drawable: RoundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(resources, bitmap)
            drawable.isCircular = true
            previewImage.setImageDrawable(drawable)
            descriptionText.text = book.description
        }

        // endregion Methods

    }

    class BookAdapter : RecyclerView.Adapter<BookViewHolder>() {

        var books: List<Book>? = Collections.emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = books?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder =
            BookViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.listitem_book,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            books?.get(position)?.run {
                holder.bind(this)
            }
        }

    }

    // endregion Nested/inner classes

}
