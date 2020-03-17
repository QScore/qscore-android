package com.berd.qscore.utils.activityresult

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.berd.qscore.utils.activityresult.ActivityResultManager.REQUEST_CODE
import java.io.Serializable


typealias ActivityResultCallback<T> = T.(ActivityResult) -> Unit

data class ActivityResult(
    val requestCode: Int,
    val resultCode: Int,
    val dataIntent: Intent?,
    val extras: Bundle?
) : Serializable {
    val isResultCanceled get() = resultCode == RESULT_CANCELED
    val isResultOk get() = resultCode == RESULT_OK
}

private object ActivityResultManager {
    const val FRAGMENT_TAG = "CallbackFragment"
    const val REQUEST_CODE = 12345
    val handler = Handler()

    private fun FragmentManager.addFragment(fragment: Fragment) {
        beginTransaction().add(fragment, FRAGMENT_TAG).commitNowAllowingStateLoss()
        executePendingTransactions()
    }

    @Suppress("UNCHECKED_CAST")
    class CallbackFragmentWithActivity<A : FragmentActivity> constructor(block: ActivityResultCallback<A>) :
        CallbackFragment<A>(block) {

        constructor() : this({})

        override fun getParent() = activity as? A

        companion object {
            fun <A : FragmentActivity> create(
                supportFragmentManager: FragmentManager,
                block: ActivityResultCallback<A>
            ): CallbackFragmentWithActivity<A> {
                return supportFragmentManager.findCallbackFragment() as? CallbackFragmentWithActivity<A>
                    ?: CallbackFragmentWithActivity(block).also {
                        supportFragmentManager.addFragment(
                            it
                        )
                    }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class CallbackFragmentWithId<F : Fragment> constructor(
        val fragmentId: Int,
        block: ActivityResultCallback<F>
    ) : CallbackFragment<F>(block) {

        constructor() : this(0, {})

        override fun getParent() = findFragmentById(fragmentId)

        private fun findFragmentById(id: Int) = activity?.supportFragmentManager?.let {
            it.findFragmentById(id) as F
        }

        companion object {
            fun <F : Fragment> create(
                supportFragmentManager: FragmentManager,
                id: Int,
                block: ActivityResultCallback<F>
            ): CallbackFragmentWithId<F> {
                return supportFragmentManager.findCallbackFragment() as? CallbackFragmentWithId<F>
                    ?: CallbackFragmentWithId(id, block).also {
                        supportFragmentManager.addFragment(
                            it
                        )
                    }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <A : FragmentActivity> A.startForResult(
    intent: Intent,
    extras: Bundle? = null,
    block: ActivityResultCallback<A>
) {
    ActivityResultManager.handler.post {
        val fragment =
            ActivityResultManager.CallbackFragmentWithActivity.create(supportFragmentManager, block)
        extras?.let { fragment.arguments = extras }
        fragment.startActivityForResult(intent, REQUEST_CODE, null)
    }
}

@Suppress("UNCHECKED_CAST")
fun <F : Fragment> F.startForResult(
    intent: Intent,
    extras: Bundle? = null,
    block: ActivityResultCallback<F>
) = activity?.let {
    ActivityResultManager.handler.post {
        val fragment = ActivityResultManager.CallbackFragmentWithId.create(
            it.supportFragmentManager,
            id,
            block
        )
        extras?.let { fragment.arguments = extras }
        fragment.startActivityForResult(intent, REQUEST_CODE, null)
    }
}

@Suppress("UNCHECKED_CAST")
fun <A : FragmentActivity> A.intentSenderForResult(
    intent: IntentSender,
    fillInIntent: Intent? = null,
    flagsMask: Int = 0,
    flagsValues: Int = 0,
    extraFlags: Int = 0,
    options: Bundle? = null,
    extras: Bundle? = null,
    block: ActivityResultCallback<A>
) {
    ActivityResultManager.handler.post {
        val fragment =
            ActivityResultManager.CallbackFragmentWithActivity.create(supportFragmentManager, block)
        extras?.let { fragment.arguments = extras }
        fragment.startIntentSenderForResult(
            intent,
            REQUEST_CODE,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options
        )
    }
}

@Suppress("UNCHECKED_CAST")
fun <F : Fragment> F.intentSenderForResult(
    intent: IntentSender,
    fillInIntent: Intent? = null,
    flagsMask: Int = 0,
    flagsValues: Int = 0,
    extraFlags: Int = 0,
    options: Bundle? = null,
    extras: Bundle? = null,
    block: ActivityResultCallback<F>
) = activity?.let {
    ActivityResultManager.handler.post {
        val fragment = ActivityResultManager.CallbackFragmentWithId.create(
            it.supportFragmentManager,
            id,
            block
        )
        extras?.let { fragment.arguments = extras }
        fragment.startIntentSenderForResult(
            intent,
            REQUEST_CODE,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options
        )
    }
}

private fun FragmentManager.findCallbackFragment() =
    findFragmentByTag(ActivityResultManager.FRAGMENT_TAG)
