package com.berd.qscore.features.shared.secrets

import com.berd.qscore.R
import com.stringcare.library.SC

object SecretManager {
    val giphyKey: String by lazy {
        SC.reveal(R.string.giphy_key)
    }
}
