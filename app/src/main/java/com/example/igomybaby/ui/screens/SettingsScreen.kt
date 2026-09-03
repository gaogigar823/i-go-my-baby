package com.example.igomybaby.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igomybaby.model.NoiseDefaults
import com.example.igomybaby.model.SettingsState
import com.example.igomybaby.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onThresholdChange: (Int) -> Unit,
    onPreview: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = appColors

    val previewAlpha = remember { Animatable(0f) }
    var triggerPreview by remember { mutableStateOf(false) }

    LaunchedEffect(triggerPreview) {
        if (triggerPreview) {
            repeat(3) {
                previewAlpha.animateTo(1f, tween(275, easing = FastOutSlowInEasing))
                previewAlpha.animateTo(0f, tween(275, easing = FastOutSlowInEasing))
            }
            previewAlpha.snapTo(0f)
            triggerPreview = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {

        // 미리보기 화면 펄스
        if (previewAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(6.dp, colors.alert.copy(alpha = previewAlpha.value))
            )
        }

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            SettingsAppBar(onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ThresholdCard(
                    threshold = state.threshold,
                    onThresholdChange = onThresholdChange,
                )

                Spacer(modifier = Modifier.height(16.dp))

                PreviewButton(
                    onClick = {
                        triggerPreview = true
                        onPreview()
                    },
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SettingsAppBar(onBack: () -> Unit) {
    val colors = appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "뒤로",
                tint               = colors.onSurface,
            )
        }
        Text(
            text     = "민감도 설정",
            style    = MaterialTheme.typography.titleLarge,
            color    = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.Outlined.Close,
                contentDescription = "닫기",
                tint               = colors.onSurfaceMed,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThresholdCard(
    threshold: Int,
    onThresholdChange: (Int) -> Unit,
) {
    val colors = appColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Text(
            text          = "알림 임계값",
            style         = MaterialTheme.typography.labelLarge,
            letterSpacing = 0.4.sp,
            color         = colors.onSurfaceMed,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = threshold.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = colors.primaryDeep,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text       = "dB",
                style      = MaterialTheme.typography.titleLarge,
                fontSize   = 20.sp,
                color      = colors.primaryDeep,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text       = "이 값 이상이면\n워치가 진동해요",
                style      = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color      = colors.onSurfaceMed,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 그라디언트 슬라이더
        Slider(
            value           = threshold.toFloat(),
            onValueChange   = { onThresholdChange(it.toInt()) },
            valueRange      = 30f..100f,
            steps           = 69,
            modifier        = Modifier.fillMaxWidth(),
            colors          = SliderDefaults.colors(
                thumbColor         = colors.surface,
                activeTrackColor   = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTickColor    = Color.Transparent,
                inactiveTickColor  = Color.Transparent,
            ),
            track = { sliderState ->
                GradientSliderTrack(
                    fraction       = ((sliderState.value - 30f) / 70f).coerceIn(0f, 1f),
                    surfaceSunken  = colors.surfaceSunken,
                    gradientColors = listOf(colors.primary, colors.warn, colors.alert),
                )
            },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(3.dp, colors.primaryDeep, CircleShape)
                )
            },
        )

        // 눈금 라벨
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("30", "50", "70", "100").forEach { label ->
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceDim,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 참고선
        listOf(
            Triple(30, "30dB", "속삭임"),
            Triple(60, "60dB", "일반 대화"),
            Triple(NoiseDefaults.BABY_CRY_DB, "${NoiseDefaults.BABY_CRY_DB}dB", "아기 울음"),
        ).forEach { (refDb, label, desc) ->
            ReferenceLevelRow(refDb = refDb, label = label, desc = desc)
        }
    }
}

@Composable
private fun ReferenceLevelRow(refDb: Int, label: String, desc: String) {
    val colors      = appColors
    val isHighlight = refDb == NoiseDefaults.BABY_CRY_DB
    val barColor    = if (isHighlight) colors.alert else colors.primary.copy(alpha = 0.55f)
    val textColor   = if (isHighlight) colors.alert else colors.onSurfaceDim

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color    = textColor,
            modifier = Modifier.width(40.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(CircleShape)
                .background(colors.surfaceSunken),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(refDb / 100f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(barColor),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text     = desc,
            style    = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color    = textColor,
        )
    }
}

@Composable
private fun PreviewButton(onClick: () -> Unit) {
    val colors = appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.alertFaint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Notifications,
                contentDescription = null,
                tint               = colors.alert,
                modifier           = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "알림 미리보기",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
            Text(
                text  = "화면 펄스 + 워치 진동을 테스트해요",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceMed,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.primaryFaint)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = "실행",
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color      = colors.primaryDeep,
            )
        }
    }
}

@Composable
private fun GradientSliderTrack(
    fraction: Float,
    surfaceSunken: Color,
    gradientColors: List<Color>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(surfaceSunken),
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(colors = gradientColors)
                    ),
            )
        }
    }
}
