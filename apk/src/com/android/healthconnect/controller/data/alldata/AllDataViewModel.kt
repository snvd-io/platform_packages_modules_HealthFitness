/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package com.android.healthconnect.controller.data.alldata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.healthconnect.controller.data.appdata.AppDataUseCase
import com.android.healthconnect.controller.data.appdata.PermissionTypesPerCategory
import com.android.healthconnect.controller.permissions.data.HealthPermissionType
import com.android.healthconnect.controller.shared.HealthDataCategoryExtensions.MEDICAL
import com.android.healthconnect.controller.shared.usecase.UseCaseResults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** View model for the [AllDataFragment] . */
@HiltViewModel
class AllDataViewModel @Inject constructor(private val loadAppDataUseCase: AppDataUseCase) :
    ViewModel() {

    companion object {
        private const val TAG = "AllDataViewModel"
    }

    private val _allData = MutableLiveData<AllDataState>()

    private val _setOfPermissionTypesToBeDeleted = MutableLiveData<Set<HealthPermissionType>>()

    val setOfPermissionTypesToBeDeleted: LiveData<Set<HealthPermissionType>>
        get() = _setOfPermissionTypesToBeDeleted

    private var numOfPermissionTypes: Int = 0

    private var allDataDeletionScreenState: AllDataDeletionScreenState =
        AllDataDeletionScreenState.VIEW

    private val _allPermissionTypesSelected = MutableLiveData<Boolean>()

    val allPermissionTypesSelected: LiveData<Boolean>
        get() = _allPermissionTypesSelected

    /** Provides a list of [PermissionTypesPerCategory]s to be displayed in [AllDataFragment]. */
    val allData: LiveData<AllDataState>
        get() = _allData

    private val _isAnyMedicalData = MutableLiveData(false)

    /** Provides whether there is any medical data stored in HC. */
    val isAnyMedicalData: LiveData<Boolean>
        get() = _isAnyMedicalData

    fun loadAllFitnessData() {
        _allData.postValue(AllDataState.Loading)
        viewModelScope.launch {
            when (val result = loadAppDataUseCase.loadAllFitnessData()) {
                is UseCaseResults.Success -> {
                    _allData.postValue(AllDataState.WithData(result.data))
                    numOfPermissionTypes = result.data.sumOf { it.data.size }
                }
                is UseCaseResults.Failed -> {
                    _allData.postValue(AllDataState.Error)
                }
            }
        }
    }

    fun loadAllMedicalData() {
        _allData.postValue(AllDataState.Loading)
        _isAnyMedicalData.postValue(false)
        viewModelScope.launch {
            when (val result = loadAppDataUseCase.loadAllMedicalData()) {
                is UseCaseResults.Success -> {
                    _allData.postValue(AllDataState.WithData(result.data))
                    numOfPermissionTypes = result.data.sumOf { it.data.size }
                    _isAnyMedicalData.postValue(isAnyMedicalData(result.data))
                }
                is UseCaseResults.Failed -> {
                    _allData.postValue(AllDataState.Error)
                    _isAnyMedicalData.postValue(false)
                }
            }
        }
    }

    private fun isAnyMedicalData(
        permissionTypesPerCategory: List<PermissionTypesPerCategory>
    ): Boolean {
        return permissionTypesPerCategory
            .filter { it.category == MEDICAL }
            .flatMap { it.data }
            .isNotEmpty()
    }

    fun resetDeleteSet() {
        _setOfPermissionTypesToBeDeleted.value = (emptySet())
    }

    fun addToDeleteSet(permissionType: HealthPermissionType) {
        val deleteSet = _setOfPermissionTypesToBeDeleted.value.orEmpty().toMutableSet()
        deleteSet.add(permissionType)
        _setOfPermissionTypesToBeDeleted.value = (deleteSet.toSet())
        if (numOfPermissionTypes == deleteSet.size) {
            _allPermissionTypesSelected.postValue(true)
        }
    }

    fun removeFromDeleteSet(permissionType: HealthPermissionType) {
        val deleteSet = _setOfPermissionTypesToBeDeleted.value.orEmpty().toMutableSet()
        deleteSet.remove(permissionType)
        _setOfPermissionTypesToBeDeleted.value = (deleteSet.toSet())
        if (deleteSet.size != numOfPermissionTypes) {
            _allPermissionTypesSelected.postValue(false)
        }
    }

    fun setScreenState(screenState: AllDataDeletionScreenState) {
        this.allDataDeletionScreenState = screenState
        if (allDataDeletionScreenState == AllDataDeletionScreenState.VIEW) {
            resetDeleteSet()
        }
    }

    fun getScreenState(): AllDataDeletionScreenState {
        return allDataDeletionScreenState
    }

    fun getNumOfPermissionTypes(): Int {
        return numOfPermissionTypes
    }

    sealed class AllDataState {
        object Loading : AllDataState()

        object Error : AllDataState()

        data class WithData(val dataMap: List<PermissionTypesPerCategory>) : AllDataState()
    }

    enum class AllDataDeletionScreenState {
        VIEW,
        DELETE,
    }
}
