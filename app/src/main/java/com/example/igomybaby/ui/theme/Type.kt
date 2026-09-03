package com.example.igomybaby.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 폰트 설정.
 *
 * Pretendard를 적용하려면::q
 *
 * 1. https://github.com/orioncactus/pretendard/releases 에서 OTF/TTF 다운로드
 * 2. app/src/main/res/font/ 폴더에 아래 파일 복사:
 *      pretendard_regular.ttf   (Weight 400)
 *      pretendard_medium.ttf    (Weight 500)
 *      pretendard_semibold.ttf  (Weight 600)
 *      pretendard_bold.ttf      (Weight 700)
 * 3. PretendardFamily를 아래로 교체:
 *      FontFamily(
 *          Font(R.font.pretendard_regular,  FontWeight.Normal),
 *          Font(R.font.pretendard_medium,   FontWeight.Medium),
 *          Font(R.font.pretendard_semibold, FontWeight.SemiBold),
 *          Font(R.font.pretendard_bold,     FontWeight.Bold),
 *      )
 *
 * 현재는 시스템 기본 한글 폰트(Noto Sans KR)를 사용합니다.
 */
val PretendardFamily: FontFamily = FontFamily.Default

val Typography = Typography(
    // 56sp bold — dB 숫자 / 설정 임계값
    displayLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 56.sp,
        lineHeight    = 56.sp,
        letterSpacing = (-0.04).em,
    ),
    // 30sp bold — 온보딩 H1
    headlineLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 37.5.sp,
        letterSpacing = (-0.02).em,
    ),
    // 17sp semibold — Title L
    titleLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 20.4.sp,
        letterSpacing = 0.sp,
    ),
    // 15.5sp regular — Body L
    bodyLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.5.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    ),
    // 15sp semibold — Body M 라벨
    bodyMedium = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    ),
    // 13sp regular — Body S 부제
    bodySmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 16.9.sp,
        letterSpacing = 0.sp,
    ),
    // 12sp medium — Caption
    labelLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 15.6.sp,
        letterSpacing = 0.sp,
    ),
    // 10.5sp — 눈금
    labelSmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.5.sp,
        lineHeight    = 13.65.sp,
        letterSpacing = 0.sp,
    ),
)
