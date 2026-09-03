# 앱 아이콘 + 런치 스크린 — 아이고 내새끼 / I Go My Baby

## 앱 정체성

- **앱 이름**: 아이고 내새끼 (영문: I Go My Baby)
- **부제 의미**: "I go" = 내가 (소리 들으면) 간다 + "아이고 내새끼" 한국 부모 정서
- **컨셉**: 청각 장애 부모를 위한 아기 울음 감지 → 워치 진동 알림

## 앱 아이콘

### 디자인

- **모티프**: 사운드웨이브 막대 4개 (좌 2, 우 2) + 중앙 하트 ❤️
  → "소리에서 사랑이 솟아난다"는 메타포
- **색상**: 따뜻한 코랄 그라데이션 `#F2A491` → `#D86A55` (위→아래)
- **전경 색**: 크림 화이트 `#FFFCF7`

### 폴더 구조

```
res/
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml              ← Adaptive icon 설정 (API 26+)
│   └── ic_launcher_round.xml
├── mipmap-mdpi/ic_launcher.png      (48×48)   ← legacy PNG (API 25 이하)
├── mipmap-mdpi/ic_launcher_round.png
├── mipmap-hdpi/...                  (72×72)
├── mipmap-xhdpi/...                 (96×96)
├── mipmap-xxhdpi/...                (144×144)
├── mipmap-xxxhdpi/...               (192×192)
└── drawable/
    ├── ic_launcher_background.xml   ← 코랄 그라데이션 배경
    ├── ic_launcher_foreground.xml   ← 사운드웨이브 + 하트 (전경)
    └── ic_launcher_monochrome.xml   ← Android 13+ 테마 아이콘
```

### Android Studio 설치

**옵션 A — 직접 복사 (가장 간단)**

1. `app/src/main/res/` 에 위 `res/` 내용을 그대로 덮어쓰기
2. `AndroidManifest.xml` 확인:
   ```xml
   <application
       android:icon="@mipmap/ic_launcher"
       android:roundIcon="@mipmap/ic_launcher_round"
       ...>
   ```
3. **Build → Clean Project → Rebuild Project**

**옵션 B — Image Asset Studio로 재생성**

1. `res` 우클릭 → **New → Image Asset**
2. **Launcher Icons (Adaptive and Legacy)** 선택
3. Foreground: `source/ic_launcher_foreground.svg`
4. Background: Color `#D86A55` (또는 `source/ic_launcher_background.svg`)
5. Next → Finish

### Play Store 등록

`play_store_icon_512.png` (512×512 PNG)을 Play Console에 업로드.

---

## 런치 스크린 (Splash Screen)

### 디자인

흰 배경에 텍스트만 가운데 정렬:

```
                I go my baby
              - 아이고 내새끼 -
```

| 요소 | 값 |
|---|---|
| 배경 | `#FFFFFF` (순백) |
| 메인 텍스트 | "I go my baby" — 34sp / weight 700 / color `#1F1D1A` / letter-spacing -0.02em |
| 부제 | "- 아이고 내새끼 -" — 17sp / weight 500 / color `#6B6258` |
| 노출 시간 | 1.2초 |
| 다음 화면 | 첫 실행 → OnboardingScreen / 이후 → MainScreen |

### 구현 방법 — 권장

Android 12+ Splash Screen API는 아이콘만 표시 가능하고 텍스트 배치가
자유롭지 않으므로, **Compose 화면으로 런치 스크린을 직접 구현**하는 것을 권장합니다.

- `LaunchScreen.kt` 파일을 `ui/screens/` 에 추가 (이 폴더에 포함)
- MainActivity의 NavHost 시작 destination을 `launch` 로
- `LaunchedEffect` 안에서 1.2초 delay 후 `navigate("onboarding")` 또는 `navigate("main")`

```kotlin
NavHost(navController, startDestination = "launch") {
    composable("launch") {
        LaunchScreen(onDone = {
            // 첫 실행 여부 체크
            val isFirstRun = ...
            navController.navigate(if (isFirstRun) "onboarding" else "main") {
                popUpTo("launch") { inclusive = true }
            }
        })
    }
    composable("onboarding") { OnboardingScreen(...) }
    composable("main") { MainScreen(...) }
    composable("settings") { SettingsScreen(...) }
}
```

### 시스템 Splash와의 관계

Android 12+ 는 시스템이 강제로 아주 짧은 splash(아이콘 fade-in)를 항상 보여줍니다.
이를 자연스럽게 잇기 위해:

- `themes.xml` 의 `Theme.App.Starting` 에서 `windowSplashScreenBackground` 를
  **흰색**으로 설정 → 시스템 splash도 흰 배경으로 짧게 깜빡인 후 우리의 LaunchScreen이 이어짐
- `splash_screen_guide.xml` 참고

### 들어 있는 파일

- `LaunchScreen.kt` — 그대로 복사해서 사용 가능한 Compose 코드
- `splash_screen_guide.xml` — themes.xml에 추가할 스타일 + 가이드

---

## 컬러 토큰 (앱 전체)

| 토큰 | Light | Dark | 용도 |
|---|---|---|---|
| 브랜드 코랄 (top) | `#F2A491` | — | 아이콘 배경 상단 |
| 브랜드 코랄 (bottom) | `#D86A55` | — | 아이콘 배경 하단, Play Store |
| Primary (sage) | `#5C9989` | `#A3D4C5` | 앱 내부 — 정상/안정 |
| Alert (coral) | `#E07A6A` | `#F0A89B` | 임계값 초과 알림 |

> 아이콘은 **코랄(브랜드/감정)**, 앱 내부 기능 UI는 **세이지 그린(안정/감지)** —
> 정서적 아이덴티티와 기능적 안정감을 분리해서 사용합니다.
