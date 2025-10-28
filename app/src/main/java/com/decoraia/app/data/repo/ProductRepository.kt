package com.decoraia.app.data.repo

import com.decoraia.app.data.ProductoAR

interface ProductRepository {
    suspend fun loadBy(style: String, typeValue: String): List<ProductoAR>
}
