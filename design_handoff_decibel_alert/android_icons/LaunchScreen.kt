// ui/screens/LaunchScreen.kt
package com.yourname.decibelalert.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 런치 스크린 — 흰 배경에 앱 이름만 가운데 정렬.
 * 1.2초 후 onDone() 호출 → 다음 화면으로 전환.
 */
@Composable
fun LaunchScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "I go my baby",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F7868), // 딝 세이지 그린
            )
            Text(
                text = "- 아이고 내새끼 -",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F1D1A), // 검정
            )
        }
    }
}
