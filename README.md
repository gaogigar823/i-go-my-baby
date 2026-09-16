# I Go My Baby

청각장애 부모를 위한 안드로이드 소음 감지 알림 앱입니다. 스마트폰 마이크로 주변 소리의 크기를 실시간 측정하고, 설정한 임계값을 넘으면 시스템 알림을 보냅니다. 연결된 워치·스마트밴드에서 이 앱의 알림을 허용하면 손목 진동으로도 알림을 받을 수 있습니다.

## 주요 기능

- `AudioRecord` 기반 실시간 데시벨 추정 및 화면 표시
- 30~100 dB 범위의 알림 임계값 설정
- 임계값 초과 시 고중요도 알림 전송
- 앱이 백그라운드에 있어도 측정을 지속하는 마이크 포그라운드 서비스
- 첫 실행 온보딩에서 마이크·알림 권한 및 웨어러블 알림 연동 안내

## 기술 구성

- Kotlin, Jetpack Compose, Material 3
- ViewModel + StateFlow
- DataStore Preferences
- Android SDK 26 이상 (target SDK 36)

## 실행 방법

1. Android Studio에서 이 저장소를 엽니다.
2. Android SDK 36이 설치된 기기 또는 에뮬레이터를 선택합니다.
3. 앱을 실행한 후 마이크와 알림 권한을 허용합니다.

명령줄에서는 다음을 실행할 수 있습니다.

```bash
./gradlew installDebug
```

## 권한

| 권한 | 용도 |
| --- | --- |
| `RECORD_AUDIO` | 주변 소리 측정 |
| `POST_NOTIFICATIONS` | 소음 감지 알림 표시 (Android 13 이상) |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` | 백그라운드 마이크 측정 유지 |

## 워치·스마트밴드 알림

앱은 특정 웨어러블 SDK에 직접 연결하지 않고 Android 시스템 알림을 사용합니다. Mi Fitness, Galaxy Wearable, Wear OS 등에서 **I Go My Baby의 알림 전달**을 켜면 기기의 정책에 따라 워치 또는 밴드가 진동합니다.

## 참고 사항

표시되는 dB 값은 스마트폰 마이크의 PCM 신호를 바탕으로 계산한 **상대적인 추정치**입니다. 기기별 마이크 특성과 주변 환경에 따라 실제 음압과 차이가 날 수 있으므로, 필요한 환경에서 임계값을 직접 조절해 사용하세요. 이 앱은 의료기기나 안전장비를 대체하지 않습니다.

## 테스트

```bash
./gradlew test
```

## APK 릴리스 자동화

`v1.0.0`처럼 `v`로 시작하는 태그를 푸시하면 GitHub Actions가 서명된 release APK를 빌드하고 GitHub Release에 `app-release.apk` 자산으로 업로드합니다. 태그 없이도 GitHub의 **Actions → Release APK → Run workflow**에서 릴리스 태그를 입력해 실행할 수 있습니다.

처음 한 번은 저장소의 **Settings → Secrets and variables → Actions**에 다음 Repository secrets를 등록해야 합니다. 서명키 원본 파일이나 비밀번호는 저장소에 커밋하지 마세요.

| Secret | 값 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | release keystore 파일을 Base64로 인코딩한 값 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 비밀번호 |
| `ANDROID_KEY_ALIAS` | key alias |
| `ANDROID_KEY_PASSWORD` | key 비밀번호 |

macOS에서는 키스토어를 다음처럼 Base64로 변환할 수 있습니다.

```bash
base64 -i release.jks -o release.jks.base64
```

릴리스 태그를 만들고 푸시하는 예시는 다음과 같습니다.

```bash
git tag v1.0.0
git push origin v1.0.0
```
