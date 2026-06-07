package com.zeroorhunderd.app.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zeroorhunderd.app.domain.model.Auction
import com.zeroorhunderd.app.ui.theme.CrimsonRed
import com.zeroorhunderd.app.ui.theme.CyberBlack
import com.zeroorhunderd.app.ui.theme.CyberCyan
import com.zeroorhunderd.app.ui.theme.CyberGold
import com.zeroorhunderd.app.ui.theme.CyberSurface
import com.zeroorhunderd.app.ui.theme.GlassWhite
import kotlinx.coroutines.delay
import java.text.DecimalFormat

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.zeroorhunderd.app.domain.usecase.CalculateCurrentPriceUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateProgressUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionListScreen(
    viewModel: AuctionListViewModel,
    onAuctionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userCredits by viewModel.userCredits.collectAsStateWithLifecycle()
    val purchaseMessage by viewModel.purchaseMessage.collectAsStateWithLifecycle()
    val serverTimeOffsetMs by viewModel.serverTimeOffsetMs.collectAsStateWithLifecycle()
    val filterByWinner by viewModel.isHistoryMode.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val calculateCurrentPriceUseCase = remember { CalculateCurrentPriceUseCase() }
    val calculateProgressUseCase = remember { CalculateProgressUseCase() }
    
    LaunchedEffect(purchaseMessage) {
        purchaseMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearPurchaseMessage()
        }
    }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showChargeDialog by remember { mutableStateOf(false) }

    val formatter = remember { DecimalFormat("#,###") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberBlack,
        topBar = {
            AuctionListHeader(
                credits = formatter.format(userCredits),
                isHistoryMode = filterByWinner,
                onAddClick = { showCreateSheet = true }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Filter Button (My Purchases)
                FloatingActionButton(
                    onClick = { viewModel.toggleFilterMode() },
                    containerColor = if (filterByWinner) CyberGold else CyberSurface,
                    contentColor = if (filterByWinner) CyberBlack else CyberGold,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (filterByWinner) Icons.Default.Home else Icons.AutoMirrored.Filled.List,
                        contentDescription = "History"
                    )
                }
                
                // Charge Button
                FloatingActionButton(
                    onClick = { showChargeDialog = true },
                    containerColor = CyberGold,
                    contentColor = CyberBlack,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("$", fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            ScanlinesEffect()

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyberCyan)
                }
            } else {
                val displayList = if (filterByWinner) {
                    // 내가 구매한 상품만 표시
                    uiState.auctions.filter { it.winnerUid == viewModel.userUid }
                } else {
                    // 아직 판매되지 않은(입찰 가능한) 상품만 표시
                    uiState.auctions.filter { it.winnerUid == null }
                }

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (filterByWinner) "구매한 내역이 없습니다" else "진행 중인 경매가 없습니다",
                            color = Color.White.copy(alpha = 0.3f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayList, key = { it.auctionId }) { auction ->
                        ModernAuctionItem(
                            auction = auction,
                            serverTimeOffsetMs = serverTimeOffsetMs,
                            calculateCurrentPriceUseCase = calculateCurrentPriceUseCase,
                            calculateProgressUseCase = calculateProgressUseCase,
                            onBuyClick = { viewModel.attemptPurchase(auction) },
                            onDetailClick = { onAuctionClick(auction.auctionId) }
                        )
                    }
                }
            }

            if (showCreateSheet) {
                CreateAuctionSheet(
                    onDismiss = { showCreateSheet = false },
                    onCreate = { itemId, startPrice, bottomPrice, imageUri ->
                        viewModel.createNewAuction(itemId, startPrice, bottomPrice, imageUri?.toString())
                        showCreateSheet = false
                    }
                )
            }

            if (showChargeDialog) {
                ChargeCreditsDialog(
                    onDismiss = { showChargeDialog = false },
                    onCharge = { amount ->
                        viewModel.chargeCredits(amount)
                        showChargeDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAuctionSheet(
    onDismiss: () -> Unit,
    onCreate: (String, Double, Double, Uri?) -> Unit
) {
    var itemId by remember { mutableStateOf("") }
    var startPrice by remember { mutableStateOf("") }
    var bottomPrice by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // 데모용: 실제 앱에서는 이미지를 저장하고 URI를 가져와야 함
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        scrimColor = Color.Black.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "새 경매 시작",
                color = CyberCyan,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            )

            // Image Selection Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CyberCyan)
                        Text("이미지 선택", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            CyberTextField(value = itemId, onValueChange = { itemId = it }, label = "상품명 / ID")
            CyberTextField(value = startPrice, onValueChange = { startPrice = it }, label = "시작 가격", isNumber = true)
            CyberTextField(value = bottomPrice, onValueChange = { bottomPrice = it }, label = "최저 가격", isNumber = true)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val sPrice = startPrice.toDoubleOrNull() ?: 0.0
                    val bPrice = bottomPrice.toDoubleOrNull() ?: 0.0
                    if (itemId.isNotBlank() && sPrice > 0) {
                        onCreate(itemId, sPrice, bPrice, selectedImageUri)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("경매 등록하기", color = CyberBlack, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false
) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text),
            singleLine = true
        )
    }
}

@Composable
fun ScanlinesEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanlines")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val lineSpacing = 10f
        
        for (i in 0 until (canvasHeight / lineSpacing).toInt()) {
            val y = (i * lineSpacing + offsetY) % canvasHeight
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun AuctionListHeader(credits: String, isHistoryMode: Boolean, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberBlack, Color.Transparent)
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = if (isHistoryMode) "나의 구매 목록" else "제로 오어 헌드레드",
                    color = if (isHistoryMode) CyberGold else CyberCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (isHistoryMode) "낙찰받은 자산 목록" else "실시간 역경매 네트워크",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Wallet Display
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "보유 잔액", color = CyberGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "$credits 원", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberCyan.copy(alpha = 0.1f))
                    .border(1.dp, CyberCyan, RoundedCornerShape(4.dp))
                    .clickable { onAddClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "INITIALIZE",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CyberCyan, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun ChargeCreditsDialog(onDismiss: () -> Unit, onCharge: (Double) -> Unit) {
    var amount by remember { mutableStateOf("100000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("잔액 충전", color = CyberCyan, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("충전할 금액을 입력하세요 (원)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CyberTextField(value = amount, onValueChange = { amount = it }, label = "충전 금액", isNumber = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onCharge(amount.toDoubleOrNull() ?: 0.0) }) {
                Text("충전하기", color = CyberCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Gray)
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ModernAuctionItem(
    auction: Auction,
    serverTimeOffsetMs: Long,
    calculateCurrentPriceUseCase: CalculateCurrentPriceUseCase,
    calculateProgressUseCase: CalculateProgressUseCase,
    onBuyClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val formatter = DecimalFormat("#,###")
    var currentPrice by remember { mutableStateOf(auction.startPrice) }
    var progress by remember { mutableStateOf(0f) }

    // List screen realtime preview
    LaunchedEffect(auction, serverTimeOffsetMs) {
        if (auction.winnerUid == null) {
            while (true) {
                val now = System.currentTimeMillis() + serverTimeOffsetMs
                
                currentPrice = calculateCurrentPriceUseCase.execute(
                    startPrice = auction.startPrice,
                    bottomPrice = auction.bottomPrice,
                    startTimestampMs = auction.startTimestampMs,
                    currentTimeMs = now
                )
                
                // Duration 대신 고정된 1시간 기준으로 진행률 계산
                val dropDurationMs = 3600_000L
                val elapsed = now - auction.startTimestampMs
                progress = (elapsed.toFloat() / dropDurationMs).coerceIn(0f, 1f)
                
                delay(100L)
            }
        } else {
            currentPrice = auction.winnerPrice ?: auction.bottomPrice
            progress = 1.0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface)
            .border(
                BorderStroke(
                    1.dp,
                    if (auction.winnerUid != null) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)))
                    else Brush.horizontalGradient(listOf(CrimsonRed, CyberCyan.copy(alpha = 0.5f)))
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = if (auction.winnerUid == null) onBuyClick else onDetailClick)
    ) {
        // Progress background
        if (auction.winnerUid == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(CrimsonRed.copy(alpha = 0.05f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon / Image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (auction.winnerUid != null) Color.White.copy(alpha = 0.05f) else CrimsonRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (auction.imageUrl != null) {
                    AsyncImage(
                        model = auction.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (auction.winnerUid != null) "OFF" else "ON",
                        color = if (auction.winnerUid != null) Color.Gray else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = auction.itemId,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (auction.winnerUid != null) "거래 종료" else "현재가: ${formatter.format(currentPrice.toLong())}원",
                    color = if (auction.winnerUid != null) Color.Gray else CyberGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (auction.winnerUid == null) {
                    Button(
                        onClick = onBuyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("구매", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "판매완료",
                        color = Color.Gray,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                if (auction.winnerUid == null) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
