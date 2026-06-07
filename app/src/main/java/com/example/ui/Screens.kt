package com.example.ui

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MovementEntity
import com.example.data.ProductEntity
import com.example.data.UserEntity
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AppContent(viewModel: InventoryViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationStatus) {
        operationStatus?.let { status ->
            snackbarHostState.showSnackbar(status)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                is Screen.Login -> LoginScreen(viewModel)
                is Screen.Register -> RegisterScreen(viewModel)
                is Screen.Main -> MainDashboardScreen(viewModel)
            }
        }
    }
}

// ---------------- LOGIN SCREEN ----------------
@Composable
fun LoginScreen(viewModel: InventoryViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(BlueBackgroundGradient, Color.White),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(BlueLight, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Logo",
                    tint = BluePrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ciclocar",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = TextDark
            )

            Text(
                text = "Entrar na sua conta",
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Mail") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = RedDanger,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login Trigger Button
            Button(
                onClick = {
                    if (email.trim().isEmpty() || password.trim().isEmpty()) {
                        errorMessage = "Por favor preencha todos os campos."
                        return@Button
                    }
                    viewModel.login(
                        email = email,
                        pass = password,
                        onSuccess = { errorMessage = null },
                        onError = { errorMessage = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Entrar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Icon(Icons.Default.ArrowForward, contentDescription = "Arrow")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Não tem conta? ", color = TextGray)
                Text(
                    text = "Criar conta",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.Register) }
                )
            }
        }
    }
}

// ---------------- REGISTER SCREEN ----------------
@Composable
fun RegisterScreen(viewModel: InventoryViewModel) {
    var activeTabIsNewTeam by remember { mutableStateOf(true) } // true for Nova equipe, false for Entrar com convite
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(BlueBackgroundGradient, Color.White),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Navigation Back Arrow
            AlignBackArrow { viewModel.navigateTo(Screen.Login) }

            Text(
                text = "Criar conta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = TextDark
            )

            Text(
                text = if (activeTabIsNewTeam) "Começar uma nova equipe" else "Entrar em uma equipe existente",
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray
            )

            // Dynamic Tabs Switch matching the custom look in image exactly!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFFEDF2F9), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nova equipe Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeTabIsNewTeam) Color.White else Color.Transparent)
                        .clickable {
                            activeTabIsNewTeam = true
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nova equipe",
                        fontWeight = FontWeight.Bold,
                        color = if (activeTabIsNewTeam) BluePrimary else TextGray
                    )
                }

                // Entrar com convite Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!activeTabIsNewTeam) Color.White else Color.Transparent)
                        .clickable {
                            activeTabIsNewTeam = false
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Entrar com convite",
                        fontWeight = FontWeight.Bold,
                        color = if (!activeTabIsNewTeam) BluePrimary else TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Common inputs
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Seu nome") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Mail") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha (mín. 6)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Pass"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Conditional input
            if (activeTabIsNewTeam) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Nome da equipe (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Company") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("CÓDIGO DO CONVITE") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = "Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = RedDanger,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action trigger button
            Button(
                onClick = {
                    if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                        errorMessage = "Por favor preencha todos os campos obrigatórios."
                        return@Button
                    }
                    if (password.trim().length < 6) {
                        errorMessage = "A senha deve conter no mínimo 6 caracteres."
                        return@Button
                    }

                    if (activeTabIsNewTeam) {
                        viewModel.registerNewTeamUser(
                            name = name,
                            email = email,
                            pass = password,
                            teamName = teamName,
                            onSuccess = { errorMessage = null },
                            onError = { errorMessage = it }
                        )
                    } else {
                        viewModel.registerInviteUser(
                            name = name,
                            email = email,
                            pass = password,
                            inviteCode = inviteCode,
                            onSuccess = { errorMessage = null },
                            onError = { errorMessage = it }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Criar conta",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Icon(Icons.Default.ArrowForward, contentDescription = "Arrow")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text("Já tem conta? ", color = TextGray)
                Text(
                    text = "Entrar",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.Login) }
                )
            }
        }
    }
}

@Composable
fun AlignBackArrow(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = BluePrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ---------------- MAIN DASHBOARD SCREEN ----------------
@Composable
fun MainDashboardScreen(viewModel: InventoryViewModel) {
    val tab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // High fidelity Header matching Image 4/5 exactly
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = BluePrimary
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Box with standard cube icon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Ciclocar Logo",
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ciclocar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Gestão de Estoque",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Seeding dynamic indicators for Miguel ("M") and Andre ("A") exactly as shown!
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-8).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        users.forEach { user ->
                            val isMe = user.email == currentUser?.email
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF1E293B), CircleShape)
                                    .clickable {
                                        // Simple quick click info
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Online dot
                                if (user.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(GreenSuccess, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Fazer Logout") },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = "Exit") }
                        )
                        DropdownMenuItem(
                            text = { Text("Gerar Carga Estresse") },
                            onClick = {
                                showMenu = false
                                viewModel.generateHighCapacityStress()
                            },
                            leadingIcon = { Icon(Icons.Default.Storage, contentDescription = "Database") }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Produtos
                NavigationBarItem(
                    selected = tab == DashboardTab.PRODUTOS,
                    onClick = { viewModel.setTab(DashboardTab.PRODUTOS) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = "Produtos") },
                    label = { Text("Produtos") }
                )
                // Histórico
                NavigationBarItem(
                    selected = tab == DashboardTab.HISTORICO,
                    onClick = { viewModel.setTab(DashboardTab.HISTORICO) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Histórico") },
                    label = { Text("Histórico") }
                )
                // Equipe
                NavigationBarItem(
                    selected = tab == DashboardTab.EQUIPE,
                    onClick = { viewModel.setTab(DashboardTab.EQUIPE) },
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = "Equipe") },
                    label = { Text("Equipe") }
                )
                // Configurações
                NavigationBarItem(
                    selected = tab == DashboardTab.CONFIGURACOES,
                    onClick = { viewModel.setTab(DashboardTab.CONFIGURACOES) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configurações") },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(BackgroundLight)
        ) {
            when (tab) {
                DashboardTab.PRODUTOS -> TabProdutos(viewModel, onAddNew = { showAddDialog = true })
                DashboardTab.HISTORICO -> TabHistorico(viewModel)
                DashboardTab.EQUIPE -> TabEquipe(viewModel)
                DashboardTab.CONFIGURACOES -> TabConfiguracoes(viewModel)
            }
        }
    }

    if (showAddDialog) {
        ProductDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, cat, desc, cost, sale, stock ->
                viewModel.addProduct(name, sku, cat, desc, cost, sale, stock)
                showAddDialog = false
            }
        )
    }
}

// ---------------- TAB: PRODUTOS ----------------
@Composable
fun TabProdutos(viewModel: InventoryViewModel, onAddNew: () -> Unit) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val query by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header Row: Title & Novo Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Produtos",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "${products.size} no estoque",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }

            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Novo", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search panel
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.productSearchQuery.value = it },
            placeholder = { Text("Buscar nome, SKU ou categoria") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.productSearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Nenhum produto cadastrado",
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    Text(
                        text = "Agilize adicionando ou gerando massa de teste de alta movimentação.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onIncrement = { viewModel.adjustStock(product, 1) },
                        onDecrement = { viewModel.adjustStock(product, -1) },
                        onEdit = { editingProduct = product },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
    }

    if (editingProduct != null) {
        val prod = editingProduct!!
        ProductDialog(
            product = prod,
            onDismiss = { editingProduct = null },
            onSave = { name, sku, cat, desc, cost, sale, stock ->
                val updated = prod.copy(
                    name = name,
                    sku = sku,
                    category = cat.uppercase(),
                    description = desc,
                    costPrice = cost,
                    salePrice = sale,
                    stockQuantity = stock
                )
                viewModel.updateProduct(updated, prod)
                editingProduct = null
            }
        )
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category & Edit/Delete Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.category}  ·  SKU ${product.sku}".uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Product",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Product",
                            tint = RedDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Name & Description
            Column {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            // Prices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "VENDA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "R$ ${String.format(Locale.US, "%.2f", product.salePrice)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BluePrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custo R$ ",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", product.costPrice),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }
            }

            // Real-time stock controller (Matching screenshot exactly)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTOQUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Outlined minus circle
                    IconButton(
                        onClick = { if (product.stockQuantity > 0) onDecrement() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Reduce Stock",
                            tint = BluePrimary
                        )
                    }

                    Text(
                        text = "${product.stockQuantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    // Solid Blue Plus Button
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(32.dp)
                            .background(BluePrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Stock",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------------- TAB: HISTÓRICO ----------------
@Composable
fun TabHistorico(viewModel: InventoryViewModel) {
    val movements by viewModel.movements.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Histórico",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(
            text = "${movements.size} movimentações registradas",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (movements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Empty Log",
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Histórico vazio",
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                    Text(
                        text = "As movimentações de estoque aparecerão aqui.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(movements) { item ->
                    MovementRowItem(item)
                }
            }
        }
    }
}

@Composable
fun MovementRowItem(movement: MovementEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction Icon matching the exact colors of the screenshot
            val bgColor = when (movement.type) {
                "ENTRADA" -> Color(0xFFE8FDF5)
                "SAÍDA" -> Color(0xFFFEE2E2)
                "CARGA" -> Color(0xFFE0F2FE)
                else -> Color(0xFFFEF3C7) // AJUSTE
            }
            val iconTint = when (movement.type) {
                "ENTRADA" -> GreenSuccess
                "SAÍDA" -> RedDanger
                "CARGA" -> BluePrimary
                else -> YellowWarn
            }
            val vector = when (movement.type) {
                "ENTRADA" -> Icons.Default.ArrowUpward
                "SAÍDA" -> Icons.Default.ArrowDownward
                "CARGA" -> Icons.Default.Upload
                else -> Icons.Default.SwapVert
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vector,
                    contentDescription = movement.type,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = movement.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = movement.type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = iconTint
                    )
                    Text(
                        text = "·",
                        color = TextMuted
                    )
                    Text(
                        text = if (movement.type == "CARGA") "Cadastro inicial" else "Ajuste Manual",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = "por ${movement.authorName}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${movement.quantity}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "un",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                
                // Display timestamp relative to screenshot format nicely
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = formatMovementTime(movement.timestamp),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

fun formatMovementTime(timeMs: Long): String {
    // Check if matching April 26th exactly to align with images format
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = timeMs }
    val month = cal.get(java.util.Calendar.MONTH)
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val hour = String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY))
    val min = String.format("%02d", cal.get(java.util.Calendar.MINUTE))
    
    return if (month == java.util.Calendar.APRIL && day == 26) {
        "26 de abr., $hour:$min"
    } else {
        val sdf = SimpleDateFormat("dd 'de' MMM., HH:mm", Locale("pt", "BR"))
        sdf.format(Date(timeMs)).lowercase()
    }
}

// ---------------- TAB: EQUIPE ----------------
@Composable
fun TabEquipe(viewModel: InventoryViewModel) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var inviteCodeGenerated by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Equipe",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = "${users.size} membros  ·  ${users.count { it.isOnline }} online",
                fontSize = 14.sp,
                color = TextGray
            )
        }

        // Invite Box matching Image 5
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular user plus logo representation
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Invite Icon",
                        tint = BluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Convidar pela equipe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                    Text(
                        text = "Gere um código e compartilhe o link",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Code label
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (inviteCodeGenerated != null) "CÓDIGO: $inviteCodeGenerated" else "Nenhum convite pendente",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                }

                Button(
                    onClick = { inviteCodeGenerated = "CICLO100" },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Add user")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gerar", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Members list label
        Text(
            text = "Membros",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextDark
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { usr ->
                val isMe = usr.email == currentUser?.email
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = usr.name.take(1).uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary,
                                fontSize = 16.sp
                            )
                            // online dot
                            if (usr.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(GreenSuccess, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = usr.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark
                                )
                                if (isMe) {
                                    Box(
                                        modifier = Modifier
                                            .background(BlueLight, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "você",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = usr.email,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }

                        // Badge Role Admin vs Membro
                        Box(
                            modifier = Modifier
                                .background(
                                    if (usr.role == "Admin") Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (usr.role == "Admin") Icons.Default.Star else Icons.Default.Person,
                                    contentDescription = usr.role,
                                    tint = if (usr.role == "Admin") BluePrimary else TextGray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = usr.role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (usr.role == "Admin") BluePrimary else TextGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB: CONFIGURAÇÕES ----------------
@Composable
fun TabConfiguracoes(viewModel: InventoryViewModel) {
    val backups by viewModel.backupsList.collectAsStateWithLifecycle()
    
    // Auto-reload files list
    LaunchedEffect(Unit) {
        viewModel.refreshBackups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configurações",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Armazenamento Interno
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = "Folder", tint = BluePrimary)
                            Text(
                                text = "Armazenamento Local",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                        }

                        Text(
                            text = "Gere arquivos físicos de backup compactados no armazenamento interno privados do aplicativo para exportação ou portabilidade.",
                            fontSize = 13.sp,
                            color = TextGray
                        )

                        Button(
                            onClick = { viewModel.exportProducts() },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Backup")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Novo Arquivo de Backup (.json)")
                        }
                    }
                }
            }

            // Seção: Backups Disponíveis
            item {
                Text(
                    text = "Arquivos em ciclocar_inventario/",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            if (backups.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum backup encontrado na pasta interna.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                items(backups) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "${file.length() / 1024} KB",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            
                            Button(
                                onClick = { viewModel.importProducts(file) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Unarchive, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restaurar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Seção: Estresse e Demonstração de Alta Capacidade
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "Performance", tint = YellowWarn)
                            Text(
                                text = "Alta Capacidade (Estresse)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                        }

                        Text(
                            text = "Fácil simulação em lote. Adicione 1200+ peças de reposição e bikes de forma instantânea às tabelas SQLite, testando eficiência em tempo real de filtragem.",
                            fontSize = 13.sp,
                            color = TextGray
                        )

                        Button(
                            onClick = { viewModel.generateHighCapacityStress() },
                            colors = ButtonDefaults.buttonColors(containerColor = YellowWarn),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Stress")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gerar 1200 Produtos & Importar")
                        }
                    }
                }
            }

            // Seção: Perigo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Danger", tint = RedDanger)
                            Text(
                                text = "Zona de Perigo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = RedDanger
                            )
                        }

                        Button(
                            onClick = { viewModel.clearAllDataAndMovements() },
                            colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Clear")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Limpar Catálogo de Estoque")
                        }
                    }
                }
            }
        }
    }
}

// ---------------- DIALOGS ----------------
@Composable
fun ProductDialog(
    product: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, sku: String, category: String, description: String, cost: Double, sale: Double, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var costPriceStr by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var salePriceStr by remember { mutableStateOf(product?.salePrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "") }

    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (product == null) "Novo Produto" else "Editar Produto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Produto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Código") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoria") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mesa, Borracha, etc") },
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Medidas") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: 29x2.10, Alumínio") },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("Custo (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it },
                        label = { Text("Venda (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Quantidade em Estoque") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = RedDanger,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextGray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.trim().isEmpty() || sku.trim().isEmpty() || category.trim().isEmpty()) {
                                validationError = "Preencha Nome, SKU e Categoria."
                                return@Button
                            }
                            val cost = costPriceStr.toDoubleOrNull() ?: 0.0
                            val sale = salePriceStr.toDoubleOrNull() ?: 0.0
                            val stock = stockStr.toIntOrNull() ?: 0
                            
                            onSave(name, sku, category, description, cost, sale, stock)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}