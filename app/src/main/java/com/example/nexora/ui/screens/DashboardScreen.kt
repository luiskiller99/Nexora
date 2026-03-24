package com.example.nexora.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onClientesClick: () -> Unit,
    onProductosClick: () -> Unit,
    onPedidosClick: () -> Unit,
    onCreditoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF0B1220), Color(0xFF0F172A))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFFE5E7EB),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Acceso rapido a modulos clave",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9CA3AF))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Clientes",
                    accent = listOf(Color(0xFF60A5FA), Color(0xFF6366F1)),
                    onClick = onClientesClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Productos",
                    accent = listOf(Color(0xFF22D3EE), Color(0xFF6366F1)),
                    onClick = onProductosClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Pedidos",
                    accent = listOf(Color(0xFF8B5CF6), Color(0xFF2563EB)),
                    onClick = onPedidosClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Credito",
                    accent = listOf(Color(0xFF14B8A6), Color(0xFF0EA5E9)),
                    onClick = onCreditoClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    accent: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = accent + Color.Transparent,
                            radius = 120f
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.linearGradient(accent),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                )
            }
        }
    }
}

@Preview(
    name = "Dashboard Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun DashboardPreview() {
    DashboardScreen(
        onClientesClick = {},
        onProductosClick = {},
        onPedidosClick = {},
        onCreditoClick = {}
    )
}
