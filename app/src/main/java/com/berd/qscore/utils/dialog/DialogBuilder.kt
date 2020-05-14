package com.berd.qscore.utils.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.berd.qscore.utils.dialog.MyDialog.Companion.KEY_ARGS
import com.berd.qscore.utils.dialog.MyDialog.Companion.KEY_CALLED_FROM_FRAGMENT
import com.berd.qscore.utils.dialog.MyDialog.Companion.TAG
import com.berd.qscore.utils.injection.Injector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.Serializable

interface DialogBuilder<T> {
    fun title(title: String)

    fun title(titleResId: Int)

    fun message(message: String)

    fun message(messageResId: Int)

    fun yesButton(block: DialogCallback<T>)

    fun noButton(block: DialogCallback<T>)

    fun yesButtonResId(resId: Int)

    fun noButtonResId(resId: Int)
}

typealias DialogCallback<T> = T.() -> Unit

private class DialogBuilderImpl<T> : DialogBuilder<T> {
    private val appContext = Injector.appContext

    data class DialogArgs<T>(
        val title: String? = null,
        val message: String? = null,
        val yesButton: Serializable? = null,
        val noButton: Serializable? = null,
        val noButtonResId: Int = android.R.string.no,
        val yesButtonResId: Int = android.R.string.yes
    ) : Serializable

    var args = DialogArgs<T>()

    override fun title(title: String) {
        args = args.copy(title = title)
    }

    override fun title(titleResId: Int) {
        args = args.copy(title = appContext.getString(titleResId))
    }

    override fun message(message: String) {
        args = args.copy(message = message)
    }

    override fun message(messageResId: Int) {
        args = args.copy(message = appContext.getString(messageResId))
    }

    override fun yesButton(block: T.() -> Unit) {
        args = args.copy(yesButton = block as Serializable)
    }

    override fun noButton(block: T.() -> Unit) {
        args = args.copy(noButton = block as Serializable)
    }

    override fun yesButtonResId(resId: Int) {
        args = args.copy(yesButtonResId = resId)
    }

    override fun noButtonResId(resId: Int) {
        args = args.copy(noButtonResId = resId)
    }
}


class MyDialog<T> : DialogFragment() {

    private val appContext = Injector.appContext

    companion object {
        val KEY_ARGS = "KEY_ARGS"
        val KEY_CALLED_FROM_FRAGMENT = "KEY_CALLED_FROM_FRAGMENT"
        val TAG = "DIALOG_TAG"
    }

    @Suppress("UNCHECKED_CAST")
    private val args: DialogBuilderImpl.DialogArgs<T> by lazy {
        checkNotNull(arguments?.getSerializable(KEY_ARGS) as DialogBuilderImpl.DialogArgs<T>) { "Args are null!" }
    }

    private val isCalledFromFragment by lazy {
        checkNotNull(arguments?.getBoolean(KEY_CALLED_FROM_FRAGMENT)) { "Args are null!" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getParent(): T {
        return if (isCalledFromFragment) {
            parentFragment as T
        } else {
            activity as T
        }
    }

    @NonNull
    @Suppress("UNCHECKED_CAST")
    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        args.run {
            activity?.let { activity ->
                return MaterialAlertDialogBuilder(activity).let { b ->
                    title?.let { b.setTitle(it) }
                    message?.let { b.setMessage(it) }
                    yesButton?.let {
                        b.setPositiveButton(yesButtonResId) { _, _ ->
                            (it as DialogCallback<T>).invoke(getParent())
                        }
                    }
                    noButton?.let {
                        b.setNegativeButton(noButtonResId) { _, _ ->
                            (it as DialogCallback<T>).invoke(getParent())
                        }
                    }
                    b.show()
                }
            } ?: throw IllegalStateException("Unable to show dialog, activity is null!")
        }
    }
}

fun <A : FragmentActivity> A.showDialogFragment(init: DialogBuilder<A>.() -> Unit) {
    val dialogBuilder = DialogBuilderImpl<A>()
    val dialogFragment = MyDialog<A>()
    init(dialogBuilder)
    dialogFragment.arguments = Bundle().apply {
        putSerializable(KEY_ARGS, dialogBuilder.args)
        putBoolean(KEY_CALLED_FROM_FRAGMENT, false)
    }
    dialogFragment.show(supportFragmentManager, TAG)
}

fun <F : Fragment> F.showDialogFragment(init: DialogBuilder<F>.() -> Unit) {
    val dialogBuilder = DialogBuilderImpl<F>()
    val dialogFragment = MyDialog<F>()
    init(dialogBuilder)
    dialogFragment.arguments = Bundle().apply {
        putSerializable(KEY_ARGS, dialogBuilder.args)
        putBoolean(KEY_CALLED_FROM_FRAGMENT, true)
    }
    dialogFragment.show(childFragmentManager, TAG)
}
