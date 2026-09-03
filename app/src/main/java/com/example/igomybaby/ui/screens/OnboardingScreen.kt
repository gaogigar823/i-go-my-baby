package com.example.igomybaby.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.igomybaby.ui.theme.appColors

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    val ctx            = LocalContext.current
    val activity       = ctx as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors         = appColors

    fun checkMic() =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun checkNotif() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else true

    var micGranted   by remember { mutableStateOf(checkMic()) }
    var notifGranted by remember { mutableStateOf(checkNotif()) }
    // 영구 거부 (시스템 다이얼로그가 더 이상 안 뜨는 상태) — 감지되면 시스템 설정으로 보냄
    var micPermanentlyDenied   by remember { mutableStateOf(false) }
    var notifPermanentlyDenied by remember { mutableStateOf(false) }

    // 사용자가 시스템 설정에서 권한을 바꾸고 돌아왔을 때 상태 재동기화
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                micGranted   = checkMic()
                notifGranted = checkNotif()
                if (micGranted)   micPermanentlyDenied   = false
                if (notifGranted) notifPermanentlyDenied = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", ctx.packageName, null)
        }
        ctx.startActivity(intent)
    }

    // notifLauncher 를 micLauncher 보다 먼저 선언해야 micLauncher 콜백에서 cascade 호출 가능
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifGranted = granted
        if (!granted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 거부 콜백 직후 rationale 이 false 면 영구 거부 상태
            notifPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (granted) {
            // 마이크 허용 → 자동으로 알림 권한 cascade (사용자 탭 1회로 양쪽 다 처리)
            if (!notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !notifPermanentlyDenied
            ) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else if (activity != null) {
            micPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.RECORD_AUDIO
            )
        }
    }

    fun requestMic() {
        if (micPermanentlyDenied) openAppSettings()
        else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun requestNotif() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (notifPermanentlyDenied) openAppSettings()
        else notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // 호흡 애니메이션
    val infinite = rememberInfiniteTransition(label = "breathe")
    val outerScale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "outer"
    )
    val midScale by infinite.animateFloat(
        initialValue = 1.08f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "mid"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .systemBarsPadding()
    ) {
        // (1) 상단 스크롤 콘텐츠 — S24 처럼 콘텐츠가 화면보다 길면 위쪽만 스크롤되고
        //     하단 버튼은 항상 노출됨. 하단 버튼 영역 높이만큼 bottom padding 으로 예약.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 호흡 동심원
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(outerScale)
                        .clip(CircleShape)
                        .background(colors.primaryFaint.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(midScale)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = null,
                        tint = colors.primaryOn,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 헤드라인
            Text(
                text = buildAnnotatedString {
                    append("소리를 ")
                    withStyle(SpanStyle(color = colors.primary)) { append("진동으로") }
                    append(" 전해 드릴게요")
                },
                style     = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color     = colors.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "아기의 울음이나 큰 소음이 감지되면\n연결된 워치가 손목을 두드려 알려드려요.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = colors.onSurfaceMed,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 권한 카드
            PermissionRow(
                icon        = Icons.Outlined.Mic,
                title       = "마이크 접근",
                description = "주변 소리를 듣고 데시벨을 측정합니다",
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionRow(
                icon        = Icons.Outlined.Watch,
                title       = "워치 연결",
                description = "미밴드 · 갤럭시워치 · 애플워치",
            )
        }

        // (2) 하단 고정 버튼 영역 — Box.align(BottomCenter) 로 항상 화면 맨 아래에 핀
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 시작하기 버튼 — 마이크는 필수, 알림은 선택사항 (거부해도 진행)
            Button(
                onClick = {
                    when {
                        !micGranted -> requestMic()
                        else        -> onStart()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor   = colors.primaryOn,
                ),
            ) {
                Text(
                    text  = "시작하기",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
) {
    val colors = appColors

    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primaryFaint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector     = icon,
                    contentDescription = null,
                    tint            = colors.primaryDeep,
                    modifier        = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceMed,
                )
            }
        }
    }
}
