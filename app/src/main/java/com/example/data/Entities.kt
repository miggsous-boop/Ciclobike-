package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: String, // "Admin" or "Membro"
    val teamName: String,
    val inviteCodeUsed: String? = null,
    val isOnline: Boolean = false
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String,
    val category: String,
    val description: String,
    val costPrice: Double,
    val salePrice: Double,
    val stockQuantity: Int
)

@Entity(tableName = "movements")
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val type: String, // "ENTRADA", "SAÍDA", "CARGA", "AJUSTE"
    val quantity: Int,
    val authorName: String,
    val timestamp: Long = System.currentTimeMillis()
)
