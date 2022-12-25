package com.life4.imagetotext.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _textResult = MutableLiveData<String>()
    val textResult: LiveData<String>
        get() = _textResult

    fun setTextResult(result: String) {
        _textResult.postValue(result)
    }

}