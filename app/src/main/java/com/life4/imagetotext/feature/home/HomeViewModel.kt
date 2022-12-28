package com.life4.imagetotext.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life4.imagetotext.model.ResultModel
import com.life4.imagetotext.room.ResultDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val resultDao: ResultDao) : ViewModel() {

    private val _textResult = MutableLiveData<String>()
    val textResult: LiveData<String>
        get() = _textResult

    fun setTextResult(result: String) {
        _textResult.postValue(result)
    }

    fun getAllResults(): Flow<List<ResultModel>> {
        return resultDao.getAllResults()
    }

    fun deleteResult(id: Long) {
        viewModelScope.launch {
            resultDao.deleteResult(id)
        }
    }
}