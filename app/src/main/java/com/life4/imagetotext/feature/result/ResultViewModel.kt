package com.life4.imagetotext.feature.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life4.imagetotext.model.ResultModel
import com.life4.imagetotext.room.ResultDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(private val resultDao: ResultDao) : ViewModel() {
    var lastInsertedResultID: Long? = null

    fun insertResultToRoom(resultModel: ResultModel) {
        viewModelScope.launch {
            lastInsertedResultID = resultDao.insertResult(resultModel)
        }
    }

    fun updateResultToRoom(resultModel: ResultModel) {
        viewModelScope.launch {
            lastInsertedResultID?.let {
                resultDao.updateResult(it, resultModel.content)
            }
        }
    }
}