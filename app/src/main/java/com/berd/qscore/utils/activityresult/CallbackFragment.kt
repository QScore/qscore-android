package com.berd.qscore.utils.activityresult

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.Serializable

abstract class CallbackFragment<T>(private val block: ActivityResultCallback<T>) : Fragment() {
    init {
        retainInstance = true
    }

    companion object {
        private const val KEY_LAMBDA = "KEY_LAMBDA"
    }

    private var savedBlock = block
    private var savedExtras: Bundle? = null

    abstract fun getParent(): T?

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getParent()?.let {
            savedBlock.invoke(
                it,
                ActivityResult(requestCode, resultCode, data, arguments)
            )
        }
        removeFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isSerializable(block)) {
            outState.putSerializable(KEY_LAMBDA, block as Serializable)
        } else {
            Timber.w("startForResult lambda is not serializable, you may lose state!")
        }
        super.onSaveInstanceState(outState)
    }

    private fun isSerializable(block: Any): Boolean {
        return try {
            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)
            oos.writeObject(block)
            oos.flush()
            val data: ByteArray = bos.toByteArray()
            true
        } catch (e: IOException) {
            Timber.e(e, "Unable to serialize block")
            false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        savedBlock =
            savedInstanceState?.getSerializable(KEY_LAMBDA) as? ActivityResultCallback<T>
                ?: block
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): Nothing? = null

    protected fun removeFragment() = activity?.supportFragmentManager?.let {
        it.beginTransaction().remove(this).commitNowAllowingStateLoss()
        it.executePendingTransactions()
    }
}
