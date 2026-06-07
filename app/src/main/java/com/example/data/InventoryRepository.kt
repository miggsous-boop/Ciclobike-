package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InventoryRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao
    private val productDao = database.productDao
    private val movementDao = database.movementDao

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Observable flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allMovements: Flow<List<MovementEntity>> = movementDao.getAllMovements()

    // Initialization check and seeding
    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        try {
            val existingUsers = userDao.getAllUsers().first()
            if (existingUsers.isEmpty()) {
                Log.d("InventoryRepository", "Database is empty. Seeding initial data...")
                seedInitialData()
            }
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error seeding database: ${e.message}", e)
        }
    }

    private suspend fun seedInitialData() {
        val users = listOf(
            UserEntity(
                name = "Miguel",
                email = "miggsous@gmail.com",
                password = "123",
                role = "Admin",
                teamName = "Ciclocar S/A",
                isOnline = true
            ),
            UserEntity(
                name = "Andre",
                email = "mignasto1001@gmail.com",
                password = "123",
                role = "Membro",
                teamName = "Ciclocar S/A",
                isOnline = true
            )
        )
        userDao.insertUsers(users)

        val products = listOf(
            ProductEntity(
                name = "Pneu 29",
                sku = "101",
                category = "BORRACHA",
                description = "29×2.10",
                costPrice = 35.0,
                salePrice = 45.50,
                stockQuantity = 21
            ),
            ProductEntity(
                name = "Catraca",
                sku = "102",
                category = "PEÇA",
                description = "102", // SKU 102 description
                costPrice = 10.0,
                salePrice = 13.00,
                stockQuantity = 10
            )
        )
        productDao.insertProducts(products)

        // Seed movements with specific custom timestamps relative to April 26
        val calendar = Calendar.getInstance()
        // Set date to April 26th
        calendar.set(Calendar.MONTH, Calendar.APRIL)
        calendar.set(Calendar.DAY_OF_MONTH, 26)

        // Let's create timestamps matching the hours in the screenshot:
        // 1. CARGA (Cadastro inicial) - 25 units - Miguel - April 26, 06:54
        calendar.set(Calendar.HOUR_OF_DAY, 6)
        calendar.set(Calendar.MINUTE, 54)
        val t1 = calendar.timeInMillis

        // 2. AJUSTE (Ajuste Manual) - 4 units - Miguel - April 26, 06:55
        calendar.set(Calendar.MINUTE, 55)
        val t2 = calendar.timeInMillis

        // 3. CARGA (Cadastro inicial) - 10 units - Miguel - April 26, 06:58 (Catraca)
        calendar.set(Calendar.MINUTE, 58)
        val t3 = calendar.timeInMillis

        // 4. SAÍDA - 1 unit - Andre - April 26, 07:02
        calendar.set(Calendar.MINUTE, 2)
        calendar.set(Calendar.HOUR_OF_DAY, 7)
        val t4 = calendar.timeInMillis

        // 5. ENTRADA - 1 unit - Andre - April 26, 07:02
        val t5 = calendar.timeInMillis

        val movements = listOf(
            MovementEntity(
                productId = 1,
                productName = "Pneu 29",
                type = "CARGA",
                quantity = 25,
                authorName = "Miguel",
                timestamp = t1
            ),
            MovementEntity(
                productId = 1,
                productName = "Pneu 29",
                type = "AJUSTE",
                quantity = 4,
                authorName = "Miguel",
                timestamp = t2
            ),
            MovementEntity(
                productId = 2,
                productName = "Catraca",
                type = "CARGA",
                quantity = 10,
                authorName = "Miguel",
                timestamp = t3
            ),
            MovementEntity(
                productId = 1,
                productName = "Pneu 29",
                type = "SAÍDA",
                quantity = 1,
                authorName = "Andre",
                timestamp = t4
            ),
            MovementEntity(
                productId = 1,
                productName = "Pneu 29",
                type = "ENTRADA",
                quantity = 1,
                authorName = "Andre",
                timestamp = t5
            )
        )
        movementDao.insertMovements(movements)
    }

    // High Capacity Generation & Storage in Internal Folder
    private fun getBackupFolder(): File {
        val folder = File(context.filesDir, "ciclocar_inventario")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    suspend fun getExportedFilesList(): List<File> = withContext(Dispatchers.IO) {
        val folder = getBackupFolder()
        folder.listFiles { file -> file.extension == "json" }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    // Export products to internal folder as JSON
    suspend fun exportProductsToInternalStorage(): File = withContext(Dispatchers.IO) {
        val products = productDao.getAllProducts().first()
        val type = Types.newParameterizedType(List::class.java, ProductEntity::class.java)
        val adapter = moshi.adapter<List<ProductEntity>>(type)
        val jsonString = adapter.toJson(products)

        val folder = getBackupFolder()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().time)
        val file = File(folder, "produtos_backup_$timestamp.json")
        file.writeText(jsonString)
        file
    }

    // Import products from a specific JSON file in internal folder
    suspend fun importProductsFromBackup(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext 0
        val jsonString = file.readText()
        val type = Types.newParameterizedType(List::class.java, ProductEntity::class.java)
        val adapter = moshi.adapter<List<ProductEntity>>(type)
        val list = adapter.fromJson(jsonString) ?: return@withContext 0

        // Insert into database and add a CARGA movement for each
        productDao.insertProducts(list)
        val movements = list.map {
            MovementEntity(
                productId = it.id,
                productName = it.name,
                type = "CARGA",
                quantity = it.stockQuantity,
                authorName = "Sistema (Backup)"
            )
        }
        movementDao.insertMovements(movements)
        list.size
    }

    // High capacity mass generator: simulates/saves a huge inventory of products in JSON and loads it
    suspend fun generateAndLoadHighCapacityProducts(count: Int = 1000, author: String): File = withContext(Dispatchers.IO) {
        val categories = listOf("PEÇA", "BORRACHA", "ACESSÓRIO", "VESTUÁRIO", "FERRAMENTA")
        val prefixes = listOf("Câmara de Ar", "Corrente", "Guidão", "Selim", "Pedal", "Farol", "Manopla", "Freio a Disco", "Câmbio Traseiro", "Pneu Extra")
        val descriptors = listOf("Aluminio 29", "Carbono Lite", "Performance Max", "Gel Confort", "Shimano Comp", "Led Superfire", "SRAM Eagle", "Grip Pro")

        val generatedList = mutableListOf<ProductEntity>()
        for (i in 1..count) {
            val prefix = prefixes.random()
            val desc = descriptors.random()
            val category = categories.random()
            val sku = "${200 + i}"
            val costPrice = (15 + (i % 150)).toDouble()
            val salePrice = costPrice * 1.35 // 35% margin

            val product = ProductEntity(
                id = 0, // database auto-generate
                name = "$prefix $desc",
                sku = sku,
                category = category,
                description = "Lote Especial $sku - Alta Capacidade",
                costPrice = String.format(Locale.US, "%.2f", costPrice).toDouble(),
                salePrice = String.format(Locale.US, "%.2f", salePrice).toDouble(),
                stockQuantity = (5..100).random()
            )
            generatedList.add(product)
        }

        // Save generated mass to a specialized internal file
        val type = Types.newParameterizedType(List::class.java, ProductEntity::class.java)
        val adapter = moshi.adapter<List<ProductEntity>>(type)
        val jsonString = adapter.toJson(generatedList)

        val folder = getBackupFolder()
        val file = File(folder, "carga_massa_${count}_intensiva.json")
        file.writeText(jsonString)

        // Perform fast ROOM batch transaction (inserting products in bulk)
        productDao.insertProducts(generatedList)

        // Generate a single aggregate movement for the bulk cargo to keep history usable
        movementDao.insertMovement(
            MovementEntity(
                productId = -1,
                productName = "Carga em Lote ($count Itens)",
                type = "CARGA",
                quantity = generatedList.sumOf { it.stockQuantity },
                authorName = author
            )
        )

        file
    }

    // Product stock adjustments (Real-time update)
    suspend fun adjustStock(product: ProductEntity, delta: Int, type: String, author: String) = withContext(Dispatchers.IO) {
        val newQuantity = product.stockQuantity + delta
        if (newQuantity >= 0) {
            val updatedProduct = product.copy(stockQuantity = newQuantity)
            productDao.updateProduct(updatedProduct)

            // Register the movement
            movementDao.insertMovement(
                MovementEntity(
                    productId = product.id,
                    productName = product.name,
                    type = type,
                    quantity = if (delta < 0) -delta else delta,
                    authorName = author
                )
            )
        }
    }

    suspend fun addProduct(product: ProductEntity, author: String) = withContext(Dispatchers.IO) {
        val newId = productDao.insertProduct(product).toInt()
        movementDao.insertMovement(
            MovementEntity(
                productId = newId,
                productName = product.name,
                type = "CARGA",
                quantity = product.stockQuantity,
                authorName = author
            )
        )
    }

    suspend fun updateProductDetails(product: ProductEntity, oldProduct: ProductEntity, author: String) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        if (product.stockQuantity != oldProduct.stockQuantity) {
            val delta = product.stockQuantity - oldProduct.stockQuantity
            val type = if (delta > 0) "ENTRADA" else if (delta < 0) "SAÍDA" else "AJUSTE"
            val qty = if (delta < 0) -delta else delta
            if (qty > 0) {
                movementDao.insertMovement(
                    MovementEntity(
                        productId = product.id,
                        productName = product.name,
                        type = type,
                        quantity = qty,
                        authorName = author
                    )
                )
            }
        } else {
            // General modification record
            movementDao.insertMovement(
                MovementEntity(
                    productId = product.id,
                    productName = product.name,
                    type = "AJUSTE",
                    quantity = 0,
                    authorName = author
                )
            )
        }
    }

    suspend fun deleteProduct(product: ProductEntity, author: String) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
        movementDao.insertMovement(
            MovementEntity(
                productId = product.id,
                productName = product.name,
                type = "SAÍDA",
                quantity = product.stockQuantity,
                authorName = "$author (Removido)"
            )
        )
    }

    // User authentication & registrations
    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun registerUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun setUserOnlineStatus(email: String, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            userDao.updateUser(user.copy(isOnline = isOnline))
        }
    }

    // Clear db
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        productDao.clearAllProducts()
        movementDao.clearAllMovements()
    }
}
