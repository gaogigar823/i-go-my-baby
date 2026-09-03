package com.example.igomybaby.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igomybaby.model.Category
import com.example.igomybaby.model.Detection
import com.example.igomybaby.model.MainState
import com.example.igomybaby.ui.theme.appColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AlertState { QUIET, WARN, ALERT }

/** 임계값 12dB 이내로 접근하면 주의 단계로 표시 */
private const val WARN_MARGIN_DB = 12

@Composable
fun MainScreen(
    state: MainState,
    onTogglePause: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = appColors
    val db  = state.currentDb
    val thr = state.threshold

    val alertState = when {
        db >= thr                  -> AlertState.ALERT
        db >= thr - WARN_MARGIN_DB -> AlertState.WARN
        else                       -> AlertState.QUIET
    }
    val isAlert = alertState == AlertState.ALERT

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {

        // 화면 펄스 — 알림 중 테두리
        if (isAlert) {
            AlertPulseBorder()
        }

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            MainAppBar(onSettings = onSettings)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                NoiseCircle(db = db, threshold = thr, alertState = alertState)
            }

            StatusChip(
                alertState  = alertState,
                db          = db,
                threshold   = thr,
                isAlertSent = state.isAlertSent,
                modifier    = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
            )

            BottomPanel(state = state, onTogglePause = onTogglePause)
        }
    }
}

@Composable
private fun AlertPulseBorder() {
    val colors = appColors
    val infinite = rememberInfiniteTransition(label = "alertPulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(6.dp, colors.alert.copy(alpha = pulseAlpha))
    )
}

@Composable
private fun MainAppBar(onSettings: () -> Unit) {
    val colors = appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primaryFaint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChildCare,
                contentDescription = null,
                tint = colors.primaryDeep,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "아기방",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
            )
            Text(
                text = "감지 중",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceMed,
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "설정",
                tint = colors.onSurfaceMed,
            )
        }
    }
}

@Composable
private fun NoiseCircle(
    db: Float,
    threshold: Int,
    alertState: AlertState,
) {
    val colors = appColors
    val isAlert = alertState == AlertState.ALERT

    // 원 색상 애니메이션
    val circleColor by animateColorAsState(
        targetValue = when (alertState) {
            AlertState.QUIET -> colors.primary
            AlertState.WARN  -> colors.warn
            AlertState.ALERT -> colors.alert
        },
        animationSpec = tween(300),
        label = "circleColor",
    )
    val faintColor by animateColorAsState(
        targetValue = when (alertState) {
            AlertState.QUIET -> colors.primaryFaint
            AlertState.WARN  -> colors.warnFaint
            AlertState.ALERT -> colors.alertFaint
        },
        animationSpec = tween(300),
        label = "faintColor",
    )

    // dB 기반 스케일
    val targetScale = (0.4f + (db / 100f) * 0.7f).coerceIn(0.4f, 1.05f)
    val dbScale by animateFloatAsState(targetValue = targetScale, animationSpec = tween(120), label = "dbScale")

    // 호흡 애니메이션 (조용 4s / 알림 0.8s — 둘 다 실행하여 상태에 따라 선택)
    val infinite = rememberInfiniteTransition(label = "breath")
    val quietBreath by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "quietBreath",
    )
    val alertBreath by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alertBreath",
    )
    val breathe = if (isAlert) alertBreath else quietBreath
    val midOpacity = if (isAlert) 0.45f else 0.25f

    Box(
        modifier = Modifier.size(320.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 외곽 점선 링 (임계값 표시)
        Box(
            modifier = Modifier
                .size(280.dp)
                .drawBehind {
                    drawCircle(
                        color  = colors.onSurfaceDim.copy(alpha = 0.8f),
                        radius = size.minDimension / 2f,
                        style  = Stroke(
                            width      = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                        ),
                    )
                }
        )

        // 임계값 칩
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.border, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "임계값 ${threshold}dB",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = colors.onSurfaceDim,
            )
        }

        // Outer circle 260dp
        Box(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer {
                    val s = breathe * dbScale * 0.95f
                    scaleX = s; scaleY = s; alpha = 0.7f
                }
                .clip(CircleShape)
                .background(faintColor)
        )

        // Mid circle 200dp
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    val s = (if (isAlert) (2f - breathe) else breathe) * dbScale
                    scaleX = s; scaleY = s; alpha = midOpacity
                }
                .clip(CircleShape)
                .background(circleColor)
        )

        // Center circle 156dp — dB 수치
        Box(
            modifier = Modifier
                .size(156.dp)
                .graphicsLayer {
                    val s = 0.85f + dbScale * 0.15f
                    scaleX = s; scaleY = s
                    shadowElevation = if (isAlert) 0f else 24.dp.toPx()
                    shape = CircleShape
                    clip  = true
                }
                .background(circleColor),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "현재 소음",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.primaryOn.copy(alpha = 0.85f),
                )
                Text(
                    text = db.toInt().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = colors.primaryOn,
                )
                Text(
                    text = "dB",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryOn,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    alertState: AlertState,
    db: Float,
    threshold: Int,
    isAlertSent: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = appColors
    val isAlert = alertState == AlertState.ALERT

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                when (alertState) {
                    AlertState.QUIET -> colors.primaryFaint
                    AlertState.WARN  -> colors.warnFaint
                    AlertState.ALERT -> colors.alert
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAlert) {
                val infinite = rememberInfiniteTransition(label = "chipBlink")
                val blinkAlpha by infinite.animateFloat(
                    initialValue = 1f, targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                    label = "blink",
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.primaryOn.copy(alpha = blinkAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = when (alertState) {
                    AlertState.QUIET -> "조용한 환경 · 정상 감지"
                    AlertState.WARN  -> "주의 · 임계값 ${(threshold - db).toInt()}dB 남음"
                    AlertState.ALERT -> if (isAlertSent) {
                        "임계값 초과 · 알림 전송됨"
                    } else {
                        "임계값 초과 · 감지 확인 중"
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = when (alertState) {
                    AlertState.QUIET -> colors.primaryDeep
                    AlertState.WARN  -> colors.warn
                    AlertState.ALERT -> colors.primaryOn
                },
            )
        }
    }
}

@Composable
private fun BottomPanel(
    state: MainState,
    onTogglePause: () -> Unit,
) {
    val colors = appColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(colors.surface)
            .border(
                1.dp, colors.border,
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(start = 22.dp, end = 22.dp, top = 20.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = "최근 감지",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceMed,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        if (state.recentDetections.isEmpty()) {
            Text(
                text = "아직 감지된 소음이 없어요",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = colors.onSurfaceDim,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            state.recentDetections.take(2).forEach { detection ->
                DetectionRow(detection = detection)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onTogglePause,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor   = colors.primaryOn,
            ),
        ) {
            Icon(
                imageVector = if (state.isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isPaused) "재개" else "일시정지",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun DetectionRow(detection: Detection) {
    val colors  = appColors
    val timeStr = remember(detection.timestamp) { formatDetectionTime(detection.timestamp) }
    val dbColor = if (detection.category == Category.BABY_CRY) colors.alert else colors.warn

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceSunken)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(dbColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (detection.category == Category.BABY_CRY)
                    Icons.Outlined.ChildCare else Icons.Outlined.Notifications,
                contentDescription = null,
                tint = dbColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (detection.category) {
                    Category.BABY_CRY -> "아기 울음 추정"
                    Category.LOUD     -> "높은 소음"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = colors.onSurface,
            )
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Normal,
                color = colors.onSurfaceMed,
            )
        }
        Text(
            text = "${detection.peakDb} dB",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = dbColor,
        )
    }
}

/** 오늘 감지면 "오늘 H:mm", 아니면 "M/d H:mm" */
private fun formatDetectionTime(timestamp: Long): String {
    val locale  = Locale.KOREAN
    val dayKey  = SimpleDateFormat("yyyyMMdd", locale)
    val date    = Date(timestamp)
    return if (dayKey.format(date) == dayKey.format(Date())) {
        "오늘 " + SimpleDateFormat("H:mm", locale).format(date)
    } else {
        SimpleDateFormat("M/d H:mm", locale).format(date)
    }
}
