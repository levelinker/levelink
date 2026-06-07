package com.zeroorhunderd.app.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.zeroorhunderd.app.ui.theme.CrimsonRed
import com.zeroorhunderd.app.ui.theme.CyberBlack
import com.zeroorhunderd.app.ui.theme.CyberCyan
import com.zeroorhunderd.app.ui.theme.CyberGold
import com.zeroorhunderd.app.ui.theme.CyberSurface
import com.zeroorhunderd.app.ui.theme.NeonGlow
import java.text.DecimalFormat

import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AuctionScreen(
    viewModel: AuctionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val currentPrice by remember(currentTimeMs, uiState) {
        derivedStateOf {
            if (uiState.isSoldOut && uiState.winnerPrice != null) {
                uiState.winnerPrice!!
            } else {
                viewModel.calculateCurrentPrice(
                    startPrice = uiState.startPrice,
                    bottomPrice = uiState.bottomPrice,
                    startTimestampMs = uiState.startTimestampMs,
                    currentTimeMs = currentTimeMs
                )
            }
        }
    }

    val progress by remember(currentTimeMs, uiState) {
        derivedStateOf {
            if (uiState.isSoldOut && uiState.winnerPrice != null) {
                // 낙찰 시점의 진행률 계산 (가격 비율로 역산하거나 단순히 1.0f)
                val totalDrop = uiState.startPrice - uiState.bottomPrice
                if (totalDrop > 0) {
                    ((uiState.startPrice - uiState.winnerPrice!!) / totalDrop).coerceIn(0.0, 1.0).toFloat()
                } else 0f
            } else {
                viewModel.calculateProgress(
                    startTimestampMs = uiState.startTimestampMs,
                    durationMs = uiState.durationMs,
                    currentTimeMs = currentTimeMs
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberBlack
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyberCyan)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuctionHeader(
                        itemId = uiState.itemId,
                        onBack = onBack
                    )
                    
                    if (uiState.imageUrl != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        AsyncImage(
                            model = uiState.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    if (uiState.isSoldOut) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CyberSurface.copy(alpha = 0.5f))
                                .border(1.dp, if (uiState.isUserWinner) CyberCyan else CrimsonRed, RoundedCornerShape(16.dp))
                                .padding(24.dp)
                        ) {
                            Text(
                                text = if (uiState.isUserWinner) "구매 성공" else "판매 완료",
                                color = if (uiState.isUserWinner) CyberCyan else CrimsonRed,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.isUserWinner) "상품이 나의 보관함에 추가되었습니다" else "다른 사용자가 이 상품을 구매했습니다",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (uiState.isUserWinner && uiState.winnerPrice != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                val formatter = DecimalFormat("#,###")
                                val savedAmount = uiState.startPrice - uiState.winnerPrice!!
                                val discountPercent = if (uiState.startPrice > 0) ((savedAmount / uiState.startPrice) * 100).toInt() else 0

                                // Summary Panel instead of Overlay
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("시작가", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                                            Text("${formatter.format(uiState.startPrice)}원", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("구매가", color = CyberCyan.copy(alpha = 0.6f), fontSize = 10.sp)
                                            Text("${formatter.format(uiState.winnerPrice!!)}원", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("절약금액", color = CrimsonRed.copy(alpha = 0.6f), fontSize = 10.sp)
                                            Text("${formatter.format(savedAmount)}원", color = CrimsonRed, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "총 $discountPercent% 할인 혜택을 받으셨습니다",
                                        color = CyberGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PriceDisplaySection(
                        currentPrice = currentPrice,
                        startPrice = uiState.startPrice,
                        isSoldOut = uiState.isSoldOut,
                        errorMessage = uiState.errorMessage
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))

                    if (!uiState.isSoldOut) {
                        Button(
                            onClick = {
                                if (!uiState.isProcessingPurchase) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.attemptPurchase()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed,
                                disabledContainerColor = Color.DarkGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isProcessingPurchase
                        ) {
                            Text(
                                text = if (uiState.isProcessingPurchase) "처리 중..." else "지금 구매하기",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, CyberCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("마켓으로 돌아가기", color = CyberCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceDisplaySection(
    currentPrice: Double,
    startPrice: Double,
    isSoldOut: Boolean,
    errorMessage: String?
) {
    val formatter = DecimalFormat("#,###")
    val discountPercent = if (startPrice > 0) {
        ((1 - currentPrice / startPrice) * 100).toInt()
    } else 0

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isSoldOut) "최종 낙찰가" else "현재 경매가",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatter.format(currentPrice.toLong()),
                color = if (isSoldOut) CyberGold else CyberCyan,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "원",
                color = if (isSoldOut) CyberGold else CyberCyan,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }

        if (discountPercent > 0) {
            Text(
                text = "시작가 대비 $discountPercent% 하락",
                color = CrimsonRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = CrimsonRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AuctionHeader(itemId: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = CyberCyan
            )
        }
        
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ZERO OR HUNDRED",
                color = CyberGold,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = itemId,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}



