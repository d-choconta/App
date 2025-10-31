package com.decoraia.app.data

data class ProductoAR(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val modelUrl: String = "",
    val style: String = "",
    val type: String = "",
    val tags: List<String> = emptyList()
)
