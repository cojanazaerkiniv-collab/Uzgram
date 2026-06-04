package com.uzgram.messenger.features.miniapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.MiniApp
import com.uzgram.messenger.domain.model.MiniAppCategory
import com.uzgram.messenger.domain.repository.MiniAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiniAppsUiState(
    val isLoading: Boolean = false,
    val allApps: List<MiniApp> = emptyList(),
    val featuredApps: List<MiniApp> = emptyList(),
    val installedApps: List<MiniApp> = emptyList(),
    val selectedCategory: MiniAppCategory? = null,
    val error: String? = null
)

@HiltViewModel
class MiniAppsViewModel @Inject constructor(
    private val miniAppRepository: MiniAppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MiniAppsUiState())
    val state: StateFlow<MiniAppsUiState> = _state.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                launch {
                    miniAppRepository.getAllApps()
                        .collect { apps -> _state.update { it.copy(allApps = apps, isLoading = false) } }
                }
                launch {
                    miniAppRepository.getFeaturedApps()
                        .collect { apps -> _state.update { it.copy(featuredApps = apps) } }
                }
                launch {
                    miniAppRepository.getInstalledApps()
                        .collect { apps -> _state.update { it.copy(installedApps = apps) } }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectCategory(category: MiniAppCategory?) {
        _state.update { it.copy(selectedCategory = category) }
        if (category != null) {
            viewModelScope.launch {
                try {
                    miniAppRepository.getAppsByCategory(category)
                        .collect { apps -> _state.update { it.copy(allApps = apps) } }
                } catch (_: Exception) {}
            }
        } else {
            loadApps()
        }
    }

    fun toggleInstall(app: MiniApp) {
        viewModelScope.launch {
            try {
                if (app.isInstalled) {
                    miniAppRepository.uninstallApp(app.id)
                } else {
                    miniAppRepository.installApp(app.id)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
