package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectItem
import com.example.data.model.ProjectStatus
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ProcessingStatusScreen(
    project: ProjectItem?,
    onViewResults: () -> Unit,
    onRetry: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Pulse animation for AI processing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Automatically navigate to results when completed
    LaunchedEffect(project?.status) {
        if (project?.status == ProjectStatus.COMPLETED) {
            // Optional slight pause so user sees 100% completion
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        AppHeader(
            title = "প্রসেসিং স্ট্যাটাস",
            subtitle = project?.title ?: "AI ভিডিও ক্লিপিং চলছে",
            isDemo = project?.isDemo ?: false
        )

        if (project == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPurple)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated Radar Circle
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(if (project.status == ProjectStatus.PROCESSING) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                when (project.status) {
                                    ProjectStatus.COMPLETED -> AccentEmerald.copy(alpha = 0.25f)
                                    ProjectStatus.FAILED -> AccentRose.copy(alpha = 0.25f)
                                    else -> BrandPurple.copy(alpha = 0.25f)
                                },
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        2.dp,
                        when (project.status) {
                            ProjectStatus.COMPLETED -> AccentEmerald
                            ProjectStatus.FAILED -> AccentRose
                            else -> BrandPurple
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (project.status) {
                            ProjectStatus.COMPLETED -> Icons.Default.CheckCircle
                            ProjectStatus.FAILED -> Icons.Default.Error
                            else -> Icons.Default.AutoAwesome
                        },
                        contentDescription = null,
                        tint = when (project.status) {
                            ProjectStatus.COMPLETED -> AccentEmerald
                            ProjectStatus.FAILED -> AccentRose
                            else -> BrandPurple
                        },
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${project.progressPercent}%",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Current Status Message
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = project.statusBengali,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = when (project.status) {
                            ProjectStatus.COMPLETED -> AccentEmerald
                            ProjectStatus.FAILED -> AccentRose
                            else -> TextPrimary
                        },
                        textAlign = TextAlign.Center
                    )
                )

                if (project.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentRose.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = project.errorMessage,
                            color = AccentRose,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Linear Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { project.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (project.status) {
                        ProjectStatus.COMPLETED -> AccentEmerald
                        ProjectStatus.FAILED -> AccentRose
                        else -> BrandPurple
                    },
                    trackColor = NavySurfaceVariant,
                )
            }

            // 4-Phase Pipeline Tracker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AI প্রসেসিং পাইপলাইন",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PipelineStageItem(
                        stageNumber = 1,
                        title = "ভিডিও যাচাই ও সার্ভারে সাবমিট",
                        isCompleted = project.progressPercent >= 20,
                        isCurrent = project.progressPercent in 1..19
                    )
                    PipelineStageItem(
                        stageNumber = 2,
                        title = "AI মোমেন্ট ও ভাইরাল হুক ডিটেকশন",
                        isCompleted = project.progressPercent >= 50,
                        isCurrent = project.progressPercent in 20..49
                    )
                    PipelineStageItem(
                        stageNumber = 3,
                        title = "স্পিকার ট্র্যাকিং ও ৯:১৬ স্মার্ট রিফ্রেমিং",
                        isCompleted = project.progressPercent >= 80,
                        isCurrent = project.progressPercent in 50..79
                    )
                    PipelineStageItem(
                        stageNumber = 4,
                        title = "বাংলা সাবটাইটেল জেনারেশন ও রেন্ডারিং",
                        isCompleted = project.progressPercent >= 100,
                        isCurrent = project.progressPercent in 80..99,
                        isLast = true
                    )
                }
            }

            // Job Metadata Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = NavySurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "প্রজেক্ট আইডি:", color = TextSecondary, fontSize = 11.sp)
                        Text(text = project.id, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Project ID", project.id)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "প্রজেক্ট আইডি কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy ID", tint = ElectricBlueLight, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Actions Based on Status
            when (project.status) {
                ProjectStatus.COMPLETED -> {
                    Button(
                        onClick = onViewResults,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("view_results_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ফলাফল ও ক্লিপসমূহ দেখুন (${project.clips.size} টি ক্লিপ)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                ProjectStatus.FAILED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackToHome,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                        ) {
                            Text("হোমে ফিরুন", color = TextSecondary)
                        }
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retry_clipping_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পুনরায় চেষ্টা করুন")
                        }
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onBackToHome,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Text("ব্যাকগ্রাউন্ডে চলতে দিন (হোমে ফিরুন)", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineStageItem(
    stageNumber: Int,
    title: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> AccentEmerald
                        isCurrent -> BrandPurple
                        else -> NavySurfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "$stageNumber",
                    color = if (isCurrent) Color.White else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = when {
                isCompleted -> AccentEmerald
                isCurrent -> TextPrimary
                else -> TextSecondary
            },
            fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
