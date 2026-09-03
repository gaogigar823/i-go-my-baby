# Handoff: 아이고 내새끼 (I Go My Baby) — 데시벨 기반 알림 앱

## Overview

청각 장애를 가진 부모를 위한 안드로이드 앱.
앱 이름 "아이고 내새끼"는 중의적 표현:
- **아이고 내새끼** — 한국 부모의 자연스러운 감탄 (애답함 + 애정)
- **I Go My Baby** — "소리 들리면 내가 먼저 간다" (즉각 반응)

스마트폰 마이크로 주변 소음을 실시간 측정하고,
설정한 임계값(dB)을 넘으면 페어링된 웨어러블 밴드
(샤오미 미밴드, 갤럭시핏, 애플워치 등)에 알림을 전송해
손목 진동으로 부모에게 알린다. 시각에만 의존해야 하는
청각 장애 부모가 아기의 울음이나 주변 위험 소음을 놓치지
않도록 보조하는 것이 목적이다.

핵심 기능 3가지:
1. **마이크로 실시간 데시벨 측정**
2. **임계값 초과 시 페어링된 워치에 알림 전송**
3. **민감도(임계값) 사용자 조절 + 알림 테스트**

---

## About the Design Files

이 폴더에 포함된 HTML 파일들은 **디자인 레퍼런스(prototype)** 입니다.
프로덕션에 그대로 복사해서 쓰는 코드가 아니라, 의도된 시각·동작을 보여주는
참고용 mockup입니다.

**구현 환경 권장:** 안드로이드 네이티브 앱을 만드는 것이므로
- **Kotlin + Jetpack Compose (Material 3)** 을 권장합니다.
- 디자인이 M3 토큰 기반으로 설계되어 있어 Compose의
  `MaterialTheme.colorScheme`에 그대로 매핑 가능합니다.
- 라이트/다크 모드는 `dynamicColorScheme` 또는 커스텀 `lightColorScheme()` /
  `darkColorScheme()` 정의로 처리합니다.

기존 코드베이스가 있다면 거기에 맞춰 패턴/라이브러리를 따르고, 새 프로젝트면
위 스택으로 시작하면 됩니다.

---

## Fidelity

**High-fidelity (hi-fi)** — 최종 색상, 타이포그래피, 간격, 모션, 마이크로
인터랙션이 모두 명세된 픽셀 단위 mockup입니다. 색상 hex, 라운드 반경,
폰트 크기, 애니메이션 타이밍은 그대로 사용하세요.

단, 컴포넌트의 픽셀-퍼펙트 재현보다는 **M3 표준 컴포넌트(Card, Slider, FAB,
TopAppBar 등)에 디자인 토큰을 매핑**하는 방식을 권장합니다. 일일이
`Box` + `Modifier`로 만들지 말고, M3 컴포넌트를 우선 활용하세요.

---

## Tech Stack 권장

| 영역 | 추천 라이브러리 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 폰트 | Pretendard (한글) — Google Fonts에 등록되어 있음 |
| 오디오 측정 | `AudioRecord` (PCM raw) — `MediaRecorder.getMaxAmplitude()` 도 가능하지만 정확도 떨어짐 |
| dB 계산 | RMS → 20 × log10(rms / refValue) |
| 워치 연결 — Wear OS | Wear OS Data Layer API (`Wearable.getDataClient`) |
| 워치 연결 — 미밴드/Mi Fitness | 공개 SDK 없음. 사용자가 Mi Fitness 앱에서 "타사 앱 알림" 허용 후 시스템 알림(NotificationChannel)으로 푸시 → 밴드가 그대로 진동 |
| 워치 연결 — 갤럭시핏 | Galaxy Wearable 앱 동일 패턴 |
| 백그라운드 동작 | `ForegroundService` + 영구 알림 (필수, 마이크 백그라운드 사용 시) |
| 권한 | `RECORD_AUDIO`, `POST_NOTIFICATIONS` (Android 13+), `FOREGROUND_SERVICE_MICROPHONE` |
| 상태 관리 | ViewModel + StateFlow |

### dB 측정 핵심 로직 (참고)

```kotlin
// AudioRecord에서 PCM 샘플을 받아 RMS → dB 변환
private fun calculateDb(buffer: ShortArray, readCount: Int): Double {
    var sum = 0.0
    for (i in 0 until readCount) {
        sum += buffer[i] * buffer[i].toDouble()
    }
    val rms = sqrt(sum / readCount)
    // 32767(=Short.MAX) 기준으로 normalize 후 dBFS로 변환,
    // 환경에 따라 +90 정도 보정해서 dB SPL 근사값을 얻는다 (정확한 값은 calibration 필요)
    return 20 * log10(rms / 32767.0) + 90
}
```

⚠️ 스마트폰 마이크의 dB 측정은 **절대값이 아니라 상대값**입니다. 사용자가
직접 calibration 할 수 있게 하거나, "이 정도면 80dB 근처"라는 라벨로 안내
하는 게 좋습니다 (속삭임 30 / 대화 60 / 울음 80).

### 웨어러블 알림 — 가장 현실적인 방식

스마트밴드의 진동 강도는 앱에서 제어할 수 없으니, **시스템 알림을 발생시키고
밴드가 그것을 받아 진동하게 두는 방식**이 가장 호환성 좋습니다:

```kotlin
NotificationCompat.Builder(context, CHANNEL_HIGH)
    .setSmallIcon(R.drawable.ic_baby_alert)
    .setContentTitle("큰 소음 감지")
    .setContentText("82 dB · 아기방")
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setCategory(NotificationCompat.CATEGORY_ALARM)
    .setVibrate(longArrayOf(0, 400, 200, 400)) // 폰 진동, 밴드는 자체 패턴 사용
    .build()
```

사용자는 Mi Fitness / Galaxy Wearable / Apple Watch 앱에서 우리 앱의
알림을 밴드에 전달하도록 설정해 두기만 하면 됩니다. 온보딩 화면 2번째 카드
("워치 연결")가 이 안내를 담당합니다.

---

## Screens / Views

총 4개 화면 (런치 포함). 모두 세로 모드, 412×892dp 기준.

### 0. Launch (`LaunchScreen`)

**Purpose:** 앱 시작 시 브랜드 노출 (1.2초)

**Layout:** 흰 배경에 텍스트 2줄만 가운데 정렬.

```
        I go my baby
      - 아이고 내새끼 -
```

| 요소 | 값 |
|---|---|
| 배경 | `#FFFFFF` |
| 메인 텍스트 | "I go my baby" — 34sp / weight 700 / `#1F1D1A` / letter-spacing -0.02em |
| 부제 | "- 아이고 내새끼 -" — 17sp / weight 500 / `#6B6258` |
| 세로 간격 | 14dp |
| 노출 시간 | 1200ms |
| 다음 화면 | 첫 실행 → Onboarding / 이후 → Main |

**구현:** 안드로이드 12+ Splash Screen API는 아이콘만 표시 가능하므로,
`themes.xml`의 `windowSplashScreenBackground`를 **흰색**으로 설정해 시스템
splash가 자연스럽게 이어지도록 하고, **LaunchScreen은 Compose 화면으로
구현**한다 (`android_icons/LaunchScreen.kt` 참고).

```kotlin
// MainActivity NavHost
NavHost(navController, startDestination = "launch") {
    composable("launch") {
        LaunchScreen(onDone = {
            navController.navigate(if (isFirstRun) "onboarding" else "main") {
                popUpTo("launch") { inclusive = true }
            }
        })
    }
    // ...
}
```

### 1. Onboarding (`OnboardingScreen`)

**Purpose:** 최초 실행 시 앱 목적 소개 + 마이크 권한 / 워치 연결 안내

**Layout:**
- 상단(여백 40dp): 호흡하는 동심원 시각 + 중앙 wave 아이콘 (height 240dp)
- 헤드라인 H1 + 서브 카피
- 권한 카드 2개 (마이크 / 워치)
- 하단: 페이지 인디케이터(3 dots) + Primary CTA("시작하기", 56dp) + Tertiary("이미 계정이 있어요", 44dp)

**Key components:**
- **호흡 원 비주얼**: 220dp(가장 외곽, primaryFaint) + 160dp(중간, primary @35% opacity) + 108dp(중앙, primary solid) — 외곽/중간이 `breathe-soft` 애니메이션 (4초 ease-in-out infinite, scale 1→1.08)
- **PermissionRow 카드**: surface bg, 1px border, 20dp radius, 14dp padding. 좌측 44×44 아이콘 박스 (primaryFaint bg, 14dp radius). 우측 상태 — `granted=true`면 체크 원, `false`면 "연결" 칩.

**Copy (exact):**
- H1 (30sp / weight 700 / line-height 1.25): "소리를 **진동으로** 전해 드릴게요"
- Sub (15.5sp / weight 400 / color onSurfaceMed): "아기의 울음이나 큰 소음이 감지되면 / 연결된 워치가 손목을 두드려 알려드려요."
- 권한 1: "마이크 접근" / "주변 소리를 듣고 데시벨을 측정합니다"
- 권한 2: "워치 연결" / "미밴드 · 갤럭시워치 · 애플워치"
- Primary CTA: "시작하기"
- Tertiary: "이미 계정이 있어요"

---

### 2. Main (`MainScreen`) — 핵심 화면

**Purpose:** 실시간 데시벨 모니터링 + 임계값 초과 시각 알림

**Layout:**
- StatusBar (투명)
- AppBar (8dp / 12dp / 8dp / 20dp 패딩): 좌측 36×36 아이콘 박스 + "아기방" 제목 + "감지 중 · 미밴드 연결됨" 부제(녹색 점). 우측 설정 아이콘 버튼.
- 중앙(flex: 1): 호흡하는 동심원 3겹 + 중앙 dB 수치
- 상태 칩 (pill)
- 하단 sheet (border-radius 28dp 28dp 0 0, surface bg, padding 22/24/18): 최근 감지 리스트 2건 + 일시정지 버튼 (풀폭, 56dp 높이)

**Key components:**

#### 호흡 원 (Breathing Circle) — 이 화면의 핵심 비주얼

3겹 동심원, dB 값에 따라 크기 + 색상이 동적으로 변화.

```
externalRing  280dp circle, dashed 1.5px border, opacity 0.8 (임계값 표시 링)
threshold label  외곽 링 상단에 "임계값 65dB" 칩 (4/10 padding, 100dp radius, surface bg)

outerCircle   260dp, color = circleFaint, opacity 0.7
              transform: scale(circleScale * 0.95)
              animation: breathe-soft 4s (조용함) / breathe-fast 0.8s (알림)

midCircle     200dp, color = circleColor, opacity 0.25 (조용) / 0.45 (알림)
              transform: scale(circleScale)
              animation: breathe-soft 4s reverse / breathe-fast 0.8s reverse

centerCircle  156dp, color = circleColor (solid)
              transform: scale(0.85 + circleScale * 0.15)
              boxShadow: 알림 중이면 0 0 40px alert@33%, 평소엔 shadowLg
              내부: "현재 소음" (11sp, 0.85 opacity)
                    숫자 dB값 (56sp, weight 700, tabular-nums, letter-spacing -0.04em)
                    "dB" (13sp, weight 600)
```

**Scale 계산:**
```
circleScale = clamp(0.4 + (db / 100) * 0.7, 0.4, 1.05)
```

**Color 상태 (3단계):**
| 상태 | 조건 | circleColor | circleFaint |
|---|---|---|---|
| 조용 (quiet) | db < threshold - 12 | primary | primaryFaint |
| 주의 (warn) | threshold-12 ≤ db < threshold | warn | warnFaint |
| 알림 (alert) | db ≥ threshold | alert | alertFaint |

**Screen pulse — 임계값 초과 시 시각 진동:**
- 화면 전체에 `boxShadow: inset 0 0 0 6px alert` 적용
- `screen-pulse 0.7s ease-in-out infinite` 애니메이션 (opacity 0 → 1 → 0)
- 청각 사용자가 화면을 보지 않고 있어도 주변 시야로 감지할 수 있게 강조

#### StatusChip (상태 칩)
- 평상시: primaryFaint bg / primaryDeep text — "조용한 환경 · 정상 감지"
- 주의: warnFaint bg / warn text — "주의 · 임계값 7dB 남음" (남은 dB 동적)
- 알림: alert bg / primaryOn text — "임계값 초과 · 워치 진동 중" (점이 0.5s 깜빡임)

#### 최근 감지 리스트 (DetectionRow)
- 36×36 아이콘 (12dp radius, faint bg)
- 좌: 설명 + 시간 / 우: dB 수치 (tabular-nums, accent color)
- 예시:
  - "아기 울음 추정 · 오늘 9:12 · 82 dB" (alert 색)
  - "높은 소음 · 오늘 8:34 · 71 dB" (warn 색)

#### 일시정지 버튼
- 풀폭, 56dp 높이, 18dp radius
- Primary bg + primaryOn 텍스트
- "⏸ 일시정지"

---

### 3. Settings (`SettingsScreen`) — 민감도/임계값 설정

**Purpose:** 알림 임계값(dB) 조절 + 알림 미리보기

**Layout:**
- 앱바: 좌측 back / 제목 "민감도 설정" / 우측 close
- 임계값 카드 (대형 수치 + 슬라이더 + 참고선)
- 알림 미리보기 버튼

**Threshold 카드:**
- 라벨 "알림 임계값" (12sp, 0.4 letter-spacing)
- 큰 수치: `{threshold}` (56sp, weight 700, primaryDeep, tabular-nums) + "dB" + 우측 부제 "이 값 이상이면 / 워치가 진동해요"
- **Gradient slider**:
  - track: 8dp 높이, 4dp radius, surfaceSunken bg
  - fill: `linear-gradient(90deg, primary, warn 70%, alert)` — 좌→우로 안전→주의→위험 시각화
  - thumb: 28dp 원, surface bg, 3px primaryDeep border, shadow
  - range: 30~100 dB
- 눈금 라벨: 30 / 50 / 70 / 100 (10.5sp, dim)
- **참고선 (3줄)**:
  - 30dB - 속삭임
  - 60dB - 일반 대화
  - 80dB - 아기 울음 (highlight, alert 색)
  - 각 줄: dB 라벨(32dp 폭) + 진행 막대(현재 dB / 100 비율) + 텍스트 라벨

**알림 미리보기 버튼:**
- surface bg, 1px border, 20dp radius, 18/20 padding
- 좌측 44×44 종 아이콘 (alertFaint bg, alert color)
- 중앙: "알림 미리보기" + "화면 펄스 + 워치 진동을 테스트해요"
- 우측: "실행" 칩 (primaryFaint bg, primaryDeep text, 100dp radius)
- 클릭 시: `screen-pulse 0.55s × 3회` 애니메이션 + 시스템 알림 1회 발사 (실제 워치 진동 테스트)

---

## Design Tokens

### Color — Light mode

| Token | Hex | Usage |
|---|---|---|
| `bg` | `#FAF6F0` | 화면 배경 (따뜻한 크림) |
| `surface` | `#FFFCF7` | 카드, 시트 |
| `surfaceAlt` | `#F2EBE0` | 보조 surface |
| `surfaceSunken` | `#EFE7DA` | 가라앉은 영역 (슬라이더 track, 리스트 row) |
| `border` | `#E8DFCF` | 카드 테두리 |
| `onSurface` | `#2C2823` | 본문 텍스트 |
| `onSurfaceMed` | `#6B6258` | 부제, 보조 텍스트 |
| `onSurfaceDim` | `#9C9385` | 가장 약한 텍스트 (눈금 등) |
| `primary` | `#5C9989` | 세이지 그린 — 정상/안전 |
| `primaryDeep` | `#3F7868` | primary 진한 변형 (텍스트, 강조) |
| `primaryFaint` | `#DFEFE9` | primary 배경 칩 |
| `primaryOn` | `#FFFFFF` | primary 위 텍스트 |
| `alert` | `#E07A6A` | 코랄 — 임계값 초과 |
| `alertFaint` | `#FBE3DD` | alert 배경 |
| `warn` | `#D4A93E` | 옐로우 — 주의 (임계값 근접) |
| `warnFaint` | `#FBF1D5` | warn 배경 |

### Color — Dark mode

| Token | Hex |
|---|---|
| `bg` | `#1A1815` |
| `surface` | `#252220` |
| `surfaceAlt` | `#2F2B27` |
| `surfaceSunken` | `#1F1D1A` |
| `border` | `#3A3631` |
| `onSurface` | `#F5EFE3` |
| `onSurfaceMed` | `#C8C0B0` |
| `onSurfaceDim` | `#8C8678` |
| `primary` | `#A3D4C5` |
| `primaryDeep` | `#7FB5A9` |
| `primaryFaint` | `#2C3E39` |
| `primaryOn` | `#0D2521` |
| `alert` | `#F0A89B` |
| `alertFaint` | `#3A2622` |
| `warn` | `#E8C26B` |
| `warnFaint` | `#352D18` |

### Typography

| Role | Font | Size | Weight | Line-height | Letter-spacing |
|---|---|---|---|---|---|
| Display (호흡원 dB 숫자) | Pretendard | 56sp | 700 | 1.0 | -0.04em |
| Display (설정 임계값) | Pretendard | 56sp | 700 | 1.0 | -0.04em |
| H1 (온보딩) | Pretendard | 30sp | 700 | 1.25 | -0.02em |
| Title L | Pretendard | 17sp | 600 | 1.2 | 0 |
| Body L | Pretendard | 15.5sp | 400 | 1.55 | 0 |
| Body M (라벨) | Pretendard | 15sp | 600 | 1.2 | 0 |
| Body S (부제) | Pretendard | 13sp | 400 | 1.3 | 0 |
| Caption | Pretendard | 12sp | 500 | 1.3 | 0 |
| Status bar (시간) | Roboto | 14sp | 500 | — | 0.25 |

### Spacing & radius

- 컨테이너 padding: 20–28dp (화면 좌우)
- 카드 padding: 18–22dp
- 카드 radius: 24dp (설정 카드) / 20dp (권한, 미리보기) / 16dp (DetectionRow)
- 시트 radius: 28dp 28dp 0 0
- 버튼 radius (large CTA): 100dp (pill) — 온보딩 시작하기
- 버튼 radius (action): 18dp — 메인 일시정지
- 칩 radius: 100dp (pill)
- 아이콘 박스 radius: 12dp / 14dp

### Shadows

- `shadow` (light): `0 1px 2px rgba(60, 45, 25, 0.06)`
- `shadowLg` (light): `0 8px 24px rgba(60, 45, 25, 0.08)`
- `shadow` (dark): `0 1px 2px rgba(0, 0, 0, 0.4)`
- `shadowLg` (dark): `0 8px 24px rgba(0, 0, 0, 0.5)`

### Motion

| Animation | Duration | Easing | Use |
|---|---|---|---|
| `breathe-soft` | 4000ms | ease-in-out infinite | 평상시 호흡원, 온보딩 동심원 (scale 1 ↔ 1.08) |
| `breathe-fast` | 800ms | ease-in-out infinite | 알림 중 호흡원 (scale + opacity) |
| `screen-pulse` | 700ms | ease-in-out infinite | 알림 중 화면 테두리 (opacity 0 ↔ 1) |
| `screen-pulse` (preview) | 550ms × 3 | ease-in-out | 설정 화면 미리보기 |
| `blink` | 500ms | infinite | 알림 칩의 작은 점 (opacity 1 ↔ 0.3) |

Compose에서: `rememberInfiniteTransition()` + `animateFloat` (RepeatMode.Reverse)로 구현.

---

## Interactions & Behavior

### 핵심 상태 머신 — MainScreen

```
[측정중] --(db ≥ threshold-12)--> [주의]
[측정중] <--(db < threshold-12)-- [주의]
[주의]   --(db ≥ threshold)--> [알림] → 워치 알림 발사 + 화면 펄스
[알림]   --(db < threshold)--> [주의] (cooldown 권장: 5초)
일시정지 토글: 측정 ↔ 정지
```

**Debounce / cooldown 권장:**
- 알림 발사 후 5초간 재발사 금지 (배경 노이즈로 인한 연속 알림 방지)
- 임계값 진입은 0.3초 이상 지속될 때만 트리거 (순간적 박수 등 무시)

### Settings

- 슬라이더: 30~100, step 1, 드래그/탭 둘 다 지원
- 알림 미리보기: 클릭 시 화면 펄스 0.55s × 3회 + 실제 시스템 알림 1회 발사 (워치가 받음)

### Permissions

- 최초 실행: `RECORD_AUDIO` 요청 (rationale 다이얼로그 권장 — 청각 사용자 대상이므로 이유 설명 필수)
- Android 13+: `POST_NOTIFICATIONS` 추가 요청
- ForegroundService 시작 시 `FOREGROUND_SERVICE_MICROPHONE` 선언 (manifest)

### Notification channels

```kotlin
NotificationChannel("decibel_alert", "소음 감지 알림", IMPORTANCE_HIGH).apply {
    enableVibration(true)
    vibrationPattern = longArrayOf(0, 400, 200, 400)
    setBypassDnd(true) // 사용자 옵션으로 노출 권장
}
```

---

## State Management

### MainViewModel

```kotlin
data class MainState(
    val currentDb: Float = 0f,
    val threshold: Int = 65,
    val isPaused: Boolean = false,
    val isConnected: Boolean = false,         // 워치 페어링 상태
    val recentDetections: List<Detection> = emptyList(),
    val lastAlertAt: Long = 0L,               // cooldown 추적
)

data class Detection(
    val timestamp: Long,
    val peakDb: Int,
    val category: Category,                   // BABY_CRY, LOUD, etc
)

enum class Category { BABY_CRY, LOUD, NORMAL }
```

- `currentDb`는 ForegroundService에서 100ms 주기로 emit, ViewModel에서
  StateFlow로 collect
- 추후 ML 추가 시 `BABY_CRY` 분류는 YAMNet (TFLite) 등 사용

### SettingsViewModel

```kotlin
data class SettingsState(
    val threshold: Int = 65,
    val cooldownSeconds: Int = 5,
    val minDurationMs: Int = 300,
)
```

- DataStore Preferences로 영속화

---

## Brand assets

### 앱 아이콘

- 컴셉: **사운드웨이브 막대 4개 + 중앙 하트** — "소리에서 사랑이 솟아난다"
- 배경: 코랄 그라데이션 `#F2A491` → `#D86A55` (위→아래)
- 전경: 크림 화이트 `#FFFCF7`
- Android 13+ 테마 아이콘 (`<monochrome>`) 지원

파일 위치: `android_icons/` 서브폴더 — README와 설치 방법 포함.

### Splash / Launch

위 "Screen 0" 참고. `android_icons/LaunchScreen.kt` 그대로 복사 가능.

## Assets

이 prototype에는 외부 이미지 자산이 없고, 모든 아이콘은 인라인 SVG입니다.
실제 구현에서는:
- **Material Symbols** (Outlined, weight 400) 사용 권장 — 아래 매핑 참고
- 폰트: **Pretendard** (한글) — `androidx.compose.ui.text.googlefonts` 로 동적 로드

### Icon mapping (HTML → Material Symbols)

| HTML name | Material Symbol |
|---|---|
| `mic` | `mic` |
| `bell` / `bellOff` | `notifications` / `notifications_off` |
| `watch` | `watch` |
| `baby` | `child_care` |
| `waveform` | `graphic_eq` |
| `vibrate` | `vibration` |
| `settings` | `settings` |
| `chevronLeft` | `chevron_left` |
| `check` | `check` |
| `history` | `history` |
| `play` / `pause` | `play_arrow` / `pause` |

---

## Files in this bundle

- `데시벨 알림 앱.html` — 메인 prototype (5 아트보드: 브랜드 2 + 앱 화면 3)
- `app.jsx` — React 컴포넌트 정의 (LaunchScreen, IconShowcase, AppIcon, AppIconMono, OnboardingScreen, MainScreen, SettingsScreen, 색상 토큰 함수 `t(mode)`)
- `design-canvas.jsx`, `tweaks-panel.jsx` — prototype 호스팅용 헬퍼 (구현 시 불필요)
- `android_icons/` — **안드로이드 프로젝트에 바로 넣을 수 있는 아이콘 파일 세트**
  - `res/` — mipmap-* + drawable/* (Android Studio `res/` 에 덮어쓰기)
  - `LaunchScreen.kt` — 바로 쓸 수 있는 Compose 코드
  - `splash_screen_guide.xml` — themes.xml 추가 가이드
  - `play_store_icon_512.png` — Play Store 제출용
  - `source/*.svg` — 원본 SVG (디자이너 재편집용)
  - `preview_*.png` — 미리보기 (독립 마스크별)
  - `README.md` — 설치/구현 가이드

브라우저에서 `데시벨 알림 앱.html`을 열면 4개 아트보드(온보딩 / 메인 조용함 / 메인 알림 / 설정)가 나란히 보이고,
우상단 Tweaks 패널로 라이트/다크, 임계값, 알림 상태를 토글할 수 있습니다.

---

## Acceptance criteria

구현 완료 시 다음이 되어야 합니다:

- [ ] 앱 아이콘 — 코랄 그라데이션 + 사운드웨이브 + 하트 (독립 아이콘, 테마 아이콘 모두)
- [ ] LaunchScreen — 흰 배경에 "I go my baby / - 아이고 내새끼 -" 1.2초 노출 후 다음 화면으로
- [ ] 마이크 권한 요청 + ForegroundService로 백그라운드 dB 측정
- [ ] MainScreen — 호흡 원 비주얼이 dB 값에 따라 실시간 scale / color 변화
- [ ] 임계값 초과 시 — 화면 펄스 + 시스템 알림 (HIGH priority) 발사 + 5초 cooldown
- [ ] SettingsScreen — 슬라이더로 임계값 조정 (30~100), DataStore 영속화
- [ ] 알림 미리보기 버튼 — 실제 알림 1회 발사 (사용자가 페어링된 밴드에서 진동 확인 가능)
- [ ] 라이트/다크 테마 둘 다 동작
- [ ] 한국어 UI (모든 카피는 위 명세 그대로)
- [ ] 청각 사용자 대상 — 모든 권한 rationale 다이얼로그 + 워치 연결 안내가 시각적으로 충분히 강조
