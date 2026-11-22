package com.sukoon.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukoon.launcher.data.LauncherState
import com.sukoon.launcher.data.QuranPage
import com.sukoon.launcher.data.formatDigits
import com.sukoon.launcher.data.toArabicDigits
import com.sukoon.launcher.ui.theme.AmiriFont
import com.sukoon.launcher.ui.theme.LocalAppFont
import com.sukoon.launcher.ui.theme.LocalAppStrings
import com.sukoon.launcher.ui.theme.LocalSukoonColors

@Composable
fun QuranScreen(
    state: LauncherState,
    pages: List<QuranPage>,
    onGoToPage: (Int) -> Unit,
    onAdjustFont: (Float) -> Unit,
    onTogglePaper: () -> Unit,
    onToggleDone: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = LocalAppFont.current
    val strings = LocalAppStrings.current
    val currentPage = pages.getOrNull((state.quranPage - 1).coerceIn(0, (pages.size - 1).coerceAtLeast(0)))
    val scrollState = rememberScrollState()
    var showJumpInput by remember { mutableStateOf(false) }
    var jumpText by remember { mutableStateOf("") }

    LaunchedEffect(state.quranPage) {
        scrollState.scrollTo(0)
    }

    val sheetBg = if (state.quranPaperMode) Color(0xFFF5EEDF) else colors.mushafBg
    val sheetText = if (state.quranPaperMode) Color(0xFF1F1A14) else colors.mushafText
    val sheetAccent = if (state.quranPaperMode) Color(0xFF8C5D27) else colors.accentWarm
    val sheetBorder = if (state.quranPaperMode) Color(0x558C5D27) else colors.mushafBorder

    // Smart auto-fit font size based on page character density
    // User's manual adjustment (offset from default 22f) is preserved on top of auto-fit.
    val userFontOffset = state.quranFontSizeSp - 22f
    val totalChars = remember(currentPage) { currentPage?.a?.sumOf { it.t.length } ?: 600 }
    val baseAutoSize = remember(totalChars) {
        when {
            totalChars <= 420 -> 22f
            totalChars <= 700 -> 19f
            totalChars <= 950 -> 17f
            totalChars <= 1180 -> 15.6f
            else -> 14.5f
        }
    }
    val effectiveFontSp = (baseAutoSize + userFontOffset).coerceIn(12.5f, 30f)
    val lineHeightMultiplier = state.quranLineHeightMultiplier.coerceIn(1.6f, 2.9f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Ultra-slim Header (Surah, Juz, Page + Controls)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.clickable {
                    jumpText = state.quranPage.toString()
                    showJumpInput = !showJumpInput
                }
            ) {
                Text(
                    text = currentPage?.s ?: strings.holyQuran,
                    fontFamily = appFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = strings.juzPageFmt(
                        currentPage?.j ?: 1,
                        state.quranPage
                    ).let { if (strings.isArabic) it.toArabicDigits() else it },
                    fontFamily = appFont,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SmallChip(strings.fontMinus) { onAdjustFont(-1.5f) }
                SmallChip(strings.fontPlus) { onAdjustFont(1.5f) }
                SmallChip(strings.paperMode, active = state.quranPaperMode) { onTogglePaper() }
            }
        }

        // Jump-to-page bar (when tapping page number)
        if (showJumpInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(strings.jumpToPageLabel, fontFamily = appFont, fontSize = 12.sp, color = colors.textSecondary)
                BasicTextField(
                    value = jumpText,
                    onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) jumpText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        fontFamily = appFont,
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(colors.accentWarm),
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.background)
                        .padding(vertical = 6.dp)
                )
                Spacer(Modifier.weight(1f))
                SmallChip(strings.go, active = true) {
                    jumpText.toIntOrNull()?.let { onGoToPage(it) }
                    showJumpInput = false
                }
            }
        }

        // Full-Height Mushaf Sheet (Always RTL for Arabic Quran script)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sheetBg)
                .border(1.dp, sheetBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 15.dp, vertical = 14.dp)
        ) {
            if (currentPage == null) {
                Text(
                    text = strings.loadingMushaf,
                    fontFamily = appFont,
                    color = colors.textMuted,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val blocks = remember(currentPage) {
                            val result = mutableListOf<Triple<String, Boolean, MutableList<Pair<Int, String>>>>()
                            for (ayah in currentPage.a) {
                                if (ayah.sn.isNotEmpty() || result.isEmpty()) {
                                    result.add(Triple(ayah.sn, ayah.sb, mutableListOf()))
                                }
                                result.last().third.add(ayah.n to ayah.t)
                            }
                            result
                        }

                        blocks.forEachIndexed { idx, (surahBanner, showBasmala, ayahs) ->
                            if (surahBanner.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (idx == 0) 0.dp else 10.dp, bottom = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(sheetAccent.copy(alpha = 0.1f))
                                        .border(1.dp, sheetBorder, RoundedCornerShape(8.dp))
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = surahBanner,
                                        fontFamily = AmiriFont,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sheetAccent
                                    )
                                }
                            }
                            if (showBasmala) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontFamily = AmiriFont,
                                    fontSize = (effectiveFontSp - 1f).sp,
                                    lineHeight = ((effectiveFontSp - 1f) * lineHeightMultiplier).sp,
                                    color = sheetAccent,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                )
                            }
                            val annotated = remember(ayahs, sheetAccent) {
                                buildAnnotatedString {
                                    ayahs.forEach { (num, text) ->
                                        append(text)
                                        append(" ")
                                        withStyle(SpanStyle(color = sheetAccent)) {
                                            append("﴿${num.toArabicDigits()}﴾ ")
                                        }
                                    }
                                }
                            }
                            Text(
                                text = annotated,
                                fontFamily = AmiriFont,
                                fontSize = effectiveFontSp.sp,
                                lineHeight = (effectiveFontSp * lineHeightMultiplier).sp,
                                color = sheetText,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Clean Bottom Page Flip Bar (Previous | Page Number Jump | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .clickable(enabled = state.quranPage > 1) { onGoToPage(state.quranPage - 1) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.prevPage,
                    fontFamily = appFont,
                    fontSize = 13.sp,
                    color = if (state.quranPage > 1) colors.textPrimary else colors.textMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accentWarmSoft)
                    .border(1.dp, colors.accentWarm.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable {
                        jumpText = state.quranPage.toString()
                        showJumpInput = !showJumpInput
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${state.quranPage.formatDigits(strings.isArabic)} / ${604.formatDigits(strings.isArabic)}",
                    fontFamily = appFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accentWarm
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .clickable(enabled = state.quranPage < 604) { onGoToPage(state.quranPage + 1) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.nextPage,
                    fontFamily = appFont,
                    fontSize = 13.sp,
                    color = if (state.quranPage < 604) colors.textPrimary else colors.textMuted
                )
            }
        }
    }
}

@Composable
fun SmallChip(
    label: String,
    active: Boolean = false,
    danger: Boolean = false,
    fontOverride: FontFamily? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalSukoonColors.current
    val appFont = fontOverride ?: LocalAppFont.current
    val bg = when {
        danger -> colors.dangerSoft.copy(alpha = 0.12f)
        active -> colors.accentWarmSoft
        else -> colors.surface
    }
    val border = when {
        danger -> colors.dangerSoft.copy(alpha = 0.4f)
        active -> colors.accentWarm
        else -> colors.borderSubtle
    }
    val fg = when {
        danger -> colors.dangerSoft
        active -> colors.accentWarm
        else -> colors.textSecondary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontFamily = appFont, fontSize = 12.sp, color = fg)
    }
}
