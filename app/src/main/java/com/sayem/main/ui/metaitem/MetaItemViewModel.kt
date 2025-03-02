/*
 * Copyright (C) 2022 The Android Open Source Project
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
 */

package com.sayem.main.ui.metaitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.sayem.main.data.MetaItemRepository
import com.sayem.main.ui.metaitem.MetaItemUiState.Error
import com.sayem.main.ui.metaitem.MetaItemUiState.Loading
import com.sayem.main.ui.metaitem.MetaItemUiState.Success
import javax.inject.Inject

@HiltViewModel
class MetaItemViewModel @Inject constructor(
    private val metaItemRepository: MetaItemRepository
) : ViewModel() {

    val uiState: StateFlow<MetaItemUiState> = metaItemRepository
        .metaItems.map<List<String>, MetaItemUiState>(::Success)
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addMetaItem(name: String) {
        viewModelScope.launch {
            metaItemRepository.add(name)
        }
    }
}

sealed interface MetaItemUiState {
    object Loading : MetaItemUiState
    data class Error(val throwable: Throwable) : MetaItemUiState
    data class Success(val data: List<String>) : MetaItemUiState
}
