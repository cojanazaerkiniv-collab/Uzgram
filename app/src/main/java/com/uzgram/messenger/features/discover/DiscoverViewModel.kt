package com.uzgram.messenger.features.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.MiniApp
import com.uzgram.messenger.domain.model.MiniAppCategory
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.MiniAppRepository
import com.uzgram.messenger.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val foundUsers: List<User> = emptyList(),
    val foundApps: List<MiniApp> = emptyList(),
    val featuredApps: List<MiniApp> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val miniAppRepository: MiniAppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadFeatured()
        loadSuggestedUsers()
        observeSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { q ->
                    _state.update { it.copy(isLoading = true) }
                    try {
                        val users = userRepository.searchUsers(q).first()
                        val apps = miniAppRepository.searchApps(q).first()
                        _state.update { it.copy(foundUsers = users, foundApps = apps, isLoading = false) }
                    } catch (e: Exception) {
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
        }
    }

    private fun loadFeatured() {
        viewModelScope.launch {
            try {
                miniAppRepository.getFeaturedApps()
                    .collect { apps -> _state.update { it.copy(featuredApps = apps) } }
            } catch (_: Exception) {}
        }
    }

    private fun loadSuggestedUsers() {
        viewModelScope.launch {
            try {
                userRepository.getContacts()
                    .collect { users -> _state.update { it.copy(suggestedUsers = users.take(10)) } }
            } catch (_: Exception) {}
        }
    }

    fun search(query: String) {
        _state.update { it.copy(query = query) }
        _searchQuery.value = query
    }

    fun clearSearch() {
        _state.update { it.copy(query = "", foundUsers = emptyList(), foundApps = emptyList()) }
        _searchQuery.value = ""
    }

    fun filterByCategory(category: MiniAppCategory) {
        viewModelScope.launch {
            try {
                miniAppRepository.getAppsByCategory(category)
                    .collect { apps -> _state.update { it.copy(foundApps = apps) } }
            } catch (_: Exception) {}
        }
    }
}
