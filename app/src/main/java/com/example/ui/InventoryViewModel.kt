package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InventoryRepository
import com.example.data.MovementEntity
import com.example.data.ProductEntity
import com.example.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface Screen {
    object Login : Screen
    object Register : Screen
    object Main : Screen
}

enum class DashboardTab {
    PRODUTOS, HISTORICO, EQUIPE, CONFIGURACOES
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InventoryRepository(application)

    // Current screen navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Main) // Default to Main for instant preview load (Andre logged in)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Current active dashboard tab
    private val _currentTab = MutableStateFlow(DashboardTab.PRODUTOS)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    // Active session user state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Filter properties for Product list
    val productSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null)

    // Observe DB arrays
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movements: StateFlow<List<MovementEntity>> = repository.allMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allDbProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered products list
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allDbProducts,
        productSearchQuery,
        selectedCategoryFilter
    ) { products, query, cat ->
        products.filter { prod ->
            val matchQuery = query.isEmpty() || 
                    prod.name.contains(query, ignoreCase = true) || 
                    prod.sku.contains(query, ignoreCase = true) || 
                    prod.category.contains(query, ignoreCase = true)
            
            val matchCat = cat == null || prod.category.equals(cat, ignoreCase = true)
            
            matchQuery && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Internal backups tracking
    private val _backupsList = MutableStateFlow<List<File>>(emptyList())
    val backupsList: StateFlow<List<File>> = _backupsList.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    init {
        viewModelScope.launch {
            // Check & seed database with mockup records
            repository.checkAndSeedDatabase()
            
            // Auto login Andre on first launch to deliver a beautiful instantly ready experience
            val defaultUser = repository.getUserByEmail("mignasto1001@gmail.com")
            if (defaultUser != null) {
                _currentUser.value = defaultUser
                repository.setUserOnlineStatus(defaultUser.email, true)
            }
            // Update lists
            refreshBackups()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setTab(tab: DashboardTab) {
        _currentTab.value = tab
    }

    fun clearStatus() {
        _operationStatus.value = null
    }

    // Login Action
    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                onError("Usuário não encontrado.")
                return@launch
            }
            if (user.password != pass) {
                onError("Senha incorreta.")
                return@launch
            }
            _currentUser.value = user
            repository.setUserOnlineStatus(user.email, true)
            _currentScreen.value = Screen.Main
            _currentTab.value = DashboardTab.PRODUTOS
            onSuccess()
        }
    }

    // Register User Action
    fun registerNewTeamUser(name: String, email: String, pass: String, teamName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onError("Email já cadastrado.")
                return@launch
            }
            val finalTeam = if (teamName.trim().isEmpty()) "Minha Equipe" else teamName
            val newUser = UserEntity(
                name = name,
                email = email,
                password = pass,
                role = "Admin",
                teamName = finalTeam,
                isOnline = true
            )
            repository.registerUser(newUser)
            _currentUser.value = newUser
            _currentScreen.value = Screen.Main
            _currentTab.value = DashboardTab.PRODUTOS
            onSuccess()
        }
    }

    fun registerInviteUser(name: String, email: String, pass: String, inviteCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (inviteCode.trim().isEmpty()) {
                onError("Insira o código do convite.")
                return@launch
            }
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onError("Email já cadastrado.")
                return@launch
            }
            // Derive team name based on the invite code if match
            val teamName = if (inviteCode.uppercase() == "CICLO100") "Ciclocar S/A" else "Equipe Convite"
            val newUser = UserEntity(
                name = name,
                email = email,
                password = pass,
                role = "Membro",
                teamName = teamName,
                inviteCodeUsed = inviteCode.uppercase(),
                isOnline = true
            )
            repository.registerUser(newUser)
            _currentUser.value = newUser
            _currentScreen.value = Screen.Main
            _currentTab.value = DashboardTab.PRODUTOS
            onSuccess()
        }
    }

    // Logout Action
    fun logout() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                repository.setUserOnlineStatus(user.email, false)
            }
            _currentUser.value = null
            _currentScreen.value = Screen.Login
        }
    }

    // Add Product
    fun addProduct(name: String, sku: String, category: String, description: String, cost: Double, sale: Double, stock: Int) {
        viewModelScope.launch {
            val author = _currentUser.value?.name ?: "Sistema"
            val product = ProductEntity(
                name = name,
                sku = sku,
                category = category.uppercase(),
                description = description,
                costPrice = cost,
                salePrice = sale,
                stockQuantity = stock
            )
            repository.addProduct(product, author)
            _operationStatus.value = "Produto adicionado com sucesso!"
        }
    }

    // Edit Product
    fun updateProduct(product: ProductEntity, oldProduct: ProductEntity) {
        viewModelScope.launch {
            val author = _currentUser.value?.name ?: "Sistema"
            repository.updateProductDetails(product, oldProduct, author)
            _operationStatus.value = "Produto atualizado!"
        }
    }

    // Inline Stock Fast Plus/Minus
    fun adjustStock(product: ProductEntity, delta: Int) {
        viewModelScope.launch {
            val author = _currentUser.value?.name ?: "Sistema"
            val type = if (delta > 0) "ENTRADA" else if (delta < 0) "SAÍDA" else "AJUSTE"
            repository.adjustStock(product, delta, type, author)
        }
    }

    // Delete Product
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            val author = _currentUser.value?.name ?: "Sistema"
            repository.deleteProduct(product, author)
            _operationStatus.value = "Produto removido!"
        }
    }

    // Backups management
    fun refreshBackups() {
        viewModelScope.launch {
            _backupsList.value = repository.getExportedFilesList()
        }
    }

    fun exportProducts() {
        viewModelScope.launch {
            try {
                val file = repository.exportProductsToInternalStorage()
                _operationStatus.value = "Backup compactado salvo em: ${file.name}"
                refreshBackups()
            } catch (e: Exception) {
                _operationStatus.value = "Falha ao exportar: ${e.message}"
            }
        }
    }

    fun importProducts(file: File) {
        viewModelScope.launch {
            try {
                val count = repository.importProductsFromBackup(file)
                _operationStatus.value = "Restaurados $count produtos com sucesso!"
            } catch (e: Exception) {
                _operationStatus.value = "Falha ao restaurar: ${e.message}"
            }
        }
    }

    // Generating 1200 items in internal folder for intensive data stress test (high volume products)
    fun generateHighCapacityStress(count: Int = 1200) {
        viewModelScope.launch {
            try {
                val author = _currentUser.value?.name ?: "Massa"
                val file = repository.generateAndLoadHighCapacityProducts(count, author)
                _operationStatus.value = "Carregados $count produtos no banco! Dados salvos em ${file.name}"
                refreshBackups()
            } catch (e: Exception) {
                _operationStatus.value = "Falha no estresse: ${e.message}"
            }
        }
    }

    fun clearAllDataAndMovements() {
        viewModelScope.launch {
            repository.clearAllData()
            _operationStatus.value = "Catálogo de estoque limpo!"
        }
    }
}
