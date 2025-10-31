package com.decoraia.app.data.repo

import com.decoraia.app.data.ProductoAR

class ProductRepositoryImpl : ProductRepository {
    override suspend fun loadBy(style: String, typeValue: String): List<ProductoAR> {
        return RAProductsRepo.loadProductos(style = style, typeValue = typeValue)
    }
}
