/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.presentation.loading

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.codepunk.doofenschmirtz.app.AlertDialogFragment
import com.codepunk.doofenschmirtz.app.AlertDialogFragment.OnBuildAlertDialogListener
import com.codepunk.doofenschmirtz.util.CustomDividerItemDecoration
import com.codepunk.doofenschmirtz.util.taskinator.DataUpdate
import com.codepunk.doofenschmirtz.util.taskinator.ProgressUpdate
import com.codepunk.doofenschmirtz.util.taskinator.SuccessUpdate
import dagger.android.support.AndroidSupportInjection
import io.igist.core.BuildConfig.KEY_BOOK_ID
import io.igist.core.BuildConfig.KEY_BOOK_TITLE
import io.igist.core.R
import io.igist.core.databinding.FragmentSelectBookBinding
import io.igist.core.domain.model.Book
import java.util.*
import javax.inject.Inject

// region Constants

/**
 * A request code for the dummy book dialog fragment.
 */
private const val DUMMY_BOOK_DIALOG_FRAGMENT_REQUEST_CODE: Int = 1

// endregion Constants

/**
 * A [Fragment] for displaying and selecting available books.
 */
class SelectBookFragment :
    Fragment(),
    OnClickListener,
    OnBuildAlertDialogListener {

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
    private val adapter: BookAdapter = BookAdapter(this)


    // endregion Properties

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
            Observer { bookId -> onBookSelectionChange(bookId) }
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

        val spanCount = resources.getInteger(R.integer.select_book_span_count)
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(requireContext(), spanCount, VERTICAL, false)
        binding.bookRecycler.layoutManager = layoutManager
        binding.bookRecycler.hasFixedSize()

        val itemDecoration =
            CustomDividerItemDecoration(requireContext(), VERTICAL, false).apply {
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.divider_item_decoration,
                    requireContext().theme
                )?.let { drawable ->
                    setDrawable(drawable)
                }
            }
        binding.bookRecycler.addItemDecoration(itemDecoration)

        binding.bookRecycler.adapter = adapter
    }

    // endregion Lifecycle methods

    // region Implemented methods

    /**
     * Called when a view is clicked.
     */
    override fun onClick(v: View) {
        val tag = v.getTag(R.id.book)
        when (tag) {
            is Book -> {
                when {
                    tag.locked -> showDummyBookDialogFragment(tag)
                    else -> selectBookViewModel.selectBook(tag.id)
                }
            }
        }
    }

    /**
     * Called when a [AlertDialogFragment] is building its alert dialog.
     */
    override fun onBuildAlertDialog(fragment: AlertDialogFragment, builder: AlertDialog.Builder) {
        when (fragment.targetRequestCode) {
            DUMMY_BOOK_DIALOG_FRAGMENT_REQUEST_CODE -> {
                val title = fragment.arguments?.getString(KEY_BOOK_TITLE)?.let {
                    "\"$it\""
                } ?: getString(R.string.select_book_dialog_locked_selected_book)
                builder
                    .setPositiveButton(android.R.string.ok, fragment)
                    .setTitle(R.string.select_book_dialog_locked_title)
                    .setMessage(getString(R.string.select_book_dialog_locked_message, title))
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Called when a list of available books is retrieved (either from the local db or from
     * books.json in the raw resource folder).
     */
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
        }
    }

    /**
     * Shows a dialog informing the user that they selected a "dummy book". Should only be
     * encountered while testing/debugging.
     */
    private fun showDummyBookDialogFragment(book: Book) {
        AlertDialogFragment.show(
            DUMMY_BOOK_DIALOG_FRAGMENT_TAG,
            this,
            DUMMY_BOOK_DIALOG_FRAGMENT_REQUEST_CODE,
            Bundle().apply {
                putString(KEY_BOOK_TITLE, book.title)
            }
        )
    }

    /**
     * Processes a book selection change. This is triggered via the selectedBookIdData live data variable
     * in [SelectBookViewModel].
     */
    private fun onBookSelectionChange(bookId: Long) {
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

    // region Companion object

    companion object {

        // region Properties

        /**
         * A fragment tag for the dummy book dialog fragment.
         */
        @JvmStatic
        private val DUMMY_BOOK_DIALOG_FRAGMENT_TAG: String =
            SelectBookFragment::class.java.name + ".DUMMY_BOOK_DIALOG_FRAGMENT"

        // endregion Properties

    }

    // endregion Companion object

    // region Nested/inner classes

    /**
     * Implementation of [RecyclerView.ViewHolder] that holds a list item representing a [Book].
     */
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // region Properties

        /**
         * The book title [AppCompatTextView].
         */
        private val titleText: AppCompatTextView = itemView.findViewById(R.id.title_txt)

        /**
         * The book preview [AppCompatImageView].
         */
        private val previewImage: AppCompatImageView = itemView.findViewById(R.id.preview_image)

        /**
         * The book description [AppCompatTextView].
         */
        private val descriptionText: AppCompatTextView = itemView.findViewById(R.id.description_txt)

        /**
         * The size of the lock icon relative to the book preview image.
         */
        private val relativeLockIconSize: Float by lazy {
            itemView.context.resources.getFraction(
                R.fraction.select_book_relative_lock_icon_size,
                1,
                1
            )
        }

        // endregion Properties

        // region Methods

        /**
         * Binds a [Book] to this view holder.
         */
        fun bind(book: Book) {
            val context: Context = itemView.context
            val resources: Resources = context.resources
            titleText.text = book.title
            val resId: Int = resources.getIdentifier(
                book.previewImageName,
                "drawable",
                context.packageName
            )
            val thumbnail: Bitmap = BitmapFactory.decodeResource(resources, resId)
            val bitmap = when (book.locked) {
                true -> {
                    val mutable: Bitmap = thumbnail.copy(thumbnail.config, true)
                    val canvas = Canvas(mutable)
                    val paint = Paint().apply {
                        color = ResourcesCompat.getColor(
                            resources,
                            R.color.lockedGray,
                            context.theme
                        )
                        style = Paint.Style.FILL
                    }
                    canvas.drawPaint(paint)

                    ContextCompat.getDrawable(context, R.drawable.ic_lock_white_24dp)?.run {
                        val lockDrawable: Drawable = DrawableCompat.wrap(this).mutate()
                        val start = (1.0f - relativeLockIconSize) / 2.0f
                        val end = start + relativeLockIconSize
                        lockDrawable.setBounds(
                            (canvas.width * start).toInt(),
                            (canvas.height * start).toInt(),
                            (canvas.width * end).toInt(),
                            (canvas.height * end).toInt()
                        )
                        lockDrawable.draw(canvas)
                    }

                    mutable
                }
                else -> thumbnail
            }
            val drawable: RoundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(resources, bitmap)
            drawable.isCircular = true
            previewImage.setImageDrawable(drawable)
            descriptionText.text = book.description
            itemView.setTag(R.id.book, book)
        }

        // endregion Methods

    }

    /**
     * Implementation of [RecyclerView.Adapter] that manages a list of [Book]s.
     */
    class BookAdapter(

        /**
         * The [OnClickListener] implementation that should be called when the user clicks a book.
         */
        private val onClickListener: OnClickListener

    ) : RecyclerView.Adapter<BookViewHolder>() {

        // region Properties

        /**
         * The list of books available for selection.
         */
        var books: List<Book>? = Collections.emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        // endregion Properties

        // region Inherited methods

        /**
         * Returns the number of books in the list.
         */
        override fun getItemCount(): Int = books?.size ?: 0

        /**
         * Creates the [BookViewHolder] representing a [Book].
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder =
            BookViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.listitem_book,
                    parent,
                    false
                ).apply {
                    setOnClickListener(onClickListener)
                }
            )

        /**
         * Binds the [Book] at the supplied [position] to the supplied [holder].
         */
        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            books?.getOrNull(position)?.run {
                holder.bind(this)
            }
        }

        // endregion Inherited methods

    }

    // endregion Nested/inner classes

}
