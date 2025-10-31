package com.decoraia.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoraia.app.R
import com.decoraia.app.data.repo.RAProductsRepo
import com.decoraia.app.ui.theme.MuseoModerno

private val Cream = Color(0xFFFBF3E3)
private val Terracotta = Color(0xFFE1A172)
private val Cocoa = Color(0xFFB2754E)

@Composable
private fun HeaderObjetos(
    @DrawableRes banner: Int,
    title: String,
    onBack: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Terracotta)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 30.dp)
                .fillMaxWidth()
                .height(135.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(banner),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.25f)))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = MuseoModerno,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 40.sp,
                    color = Color.White
                )
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 17.dp, top = 17.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.White
            )
        }
    }
}

private data class CategoriaItem(
    val label: String,
    @DrawableRes val image: Int,
    val categoria: RAProductsRepo.Categoria
)

@Composable
private fun CategoriaCard(
    item: CategoriaItem,
    onClick: (RAProductsRepo.Categoria) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick(item.categoria) }
    ) {
        Image(
            painter = painterResource(item.image),
            contentDescription = item.label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.28f)))
        Text(
            text = item.label,
            style = TextStyle(
                fontFamily = MuseoModerno,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun BottomActionsObjetos(onHome: () -> Unit, onProfile: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
        ) {
            Icon(
                Icons.Filled.Home,
                contentDescription = "Inicio",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Box(
            Modifier
                .weight(1f)
                .height(56.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.45f))
        )

        IconButton(
            onClick = onProfile,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cocoa.copy(alpha = 0.9f))
                .border(2.dp, Terracotta, CircleShape)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Perfil",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun RAObjetosScreenUI(
    styleTitle: String,
    onBack: () -> Unit,
    onSelectCategoria: (RAProductsRepo.Categoria) -> Unit,
    onHome: () -> Unit,
    onProfile: () -> Unit,
    @DrawableRes headerBanner: Int = R.drawable.objetos_banner,
    @DrawableRes imgJarrones: Int = R.drawable.cat_jarrones,
    @DrawableRes imgCuadros: Int = R.drawable.cat_cuadros,
    @DrawableRes imgLamparas: Int = R.drawable.cat_lamparas,
    @DrawableRes imgSofas: Int = R.drawable.cat_sofas
) {
    fun toItem(cat: RAProductsRepo.Categoria): CategoriaItem {
        return when (cat) {
            RAProductsRepo.Categoria.JARRONES -> CategoriaItem(cat.label, imgJarrones, cat)
            RAProductsRepo.Categoria.CUADROS -> CategoriaItem(cat.label, imgCuadros, cat)
            RAProductsRepo.Categoria.LAMPARAS -> CategoriaItem(cat.label, imgLamparas, cat)
            RAProductsRepo.Categoria.SOFAS -> CategoriaItem(cat.label, imgSofas, cat)
        }
    }

    val items = RAProductsRepo.categoriasFijas.map(::toItem)

    Surface(color = Cream) {
        Column(Modifier.fillMaxSize()) {
            HeaderObjetos(
                banner = headerBanner,
                title = "Objetos",
                onBack = onBack
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items) { item ->
                    CategoriaCard(item = item, onClick = onSelectCategoria)
                }
            }

            BottomActionsObjetos(
                onHome = onHome,
                onProfile = onProfile
            )
        }
    }
}