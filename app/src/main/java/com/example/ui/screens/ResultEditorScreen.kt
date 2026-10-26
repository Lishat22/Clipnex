package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipItem
import com.example.data.model.ProjectItem
import com.example.ui.components.AppHeader
import com.example.ui.components.ViralityScoreBadge
import com.example.ui.theme.*

@Composable
fun ResultEditorScreen(
    project: ProjectItem?,
    onSaveClip: (clipId: String, title: String, transcript: String, postCaption: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (project == null || project.clips.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NavyBackground)
        ) {
            AppHeader(title = "ফলাফল পাওয়া যায়নি", subtitle = "")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("এই প্রজেক্টে এখনও কোনো ক্লিপ নেই", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onBack) {
                        Text("হোমে ফিরে যান")
                    }
                }
            }
        }
        return
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentClip = project.clips.getOrNull(selectedIndex) ?: project.clips.first()

    // Editable state for the current clip
    var editedTitle by remember(currentClip.id) { mutableStateOf(currentClip.title) }
    var editedTranscript by remember(currentClip.id) { mutableStateOf(currentClip.transcript) }
    var editedPostCaption by remember(currentClip.id) { mutableStateOf(currentClip.postCaption) }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Custom Top Bar with Back action
        Surface(color = NavySurface, tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ফলাফল ও ক্লিপ এডিটর",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${project.clips.size} টি প্রস্তুত ক্লিপ • ${project.aspectRatio}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                if (project.isDemo) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "ডেমো ফলাফল",
                            color = AccentAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Clip Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(project.clips) { index, clip ->
                val isSelected = index == selectedIndex
                Surface(
                    modifier = Modifier.clickable { selectedIndex = index },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) BrandPurple else NavySurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) BrandPurpleLight else NavyBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ক্লিপ ${index + 1}",
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${clip.viralityScore}%",
                            color = if (isSelected) AccentCyan else ElectricBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Main Editor Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Video Preview & Play Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(NavySurfaceVariant, NavyBackground)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentClip.videoUrl))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "ভিডিও প্লেয়ার খুলতে সমস্যা হচ্ছে", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BrandPurple)
                                    .testTag("play_clip_preview_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Clip",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "প্রিভিউ প্লে করুন (${currentClip.startTime} - ${currentClip.endTime})",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Duration Badge on Preview
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = "${currentClip.durationSeconds} সেকেন্ড",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title & Score Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentClip.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "টাইমস্ট্যাম্প: ${currentClip.startTime} থেকে ${currentClip.endTime}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        ViralityScoreBadge(score = currentClip.viralityScore)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (MP4 Download & Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentClip.downloadUrl))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ডাউনলোড লিংক ব্রাউজারে খোলা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_mp4_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MP4 ডাউনলোড")
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, currentClip.title)
                                    putExtra(Intent.EXTRA_TEXT, "${currentClip.title}\n\n${currentClip.postCaption}\n\nভিডিও লিংক: ${currentClip.videoUrl}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("শেয়ার করুন", color = TextPrimary)
                        }
                    }
                }
            }

            // Score Breakdown Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ভাইরাল পারফরম্যান্স বিশ্লেষণ",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ScoreProgressRow(label = "হুক স্কোর (Hook):", score = currentClip.hookScore, color = BrandPurple)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreProgressRow(label = "প্রবাহ ও রিটেনশন (Flow):", score = currentClip.flowScore, color = ElectricBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreProgressRow(label = "শিক্ষণীয় মান (Value):", score = currentClip.valueScore, color = AccentCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreProgressRow(label = "ট্রেন্ড সামঞ্জস্য (Trend):", score = currentClip.trendScore, color = AccentEmerald)
                }
            }

            // Bengali Subtitles / Transcript (Editable)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বাংলা সাবটাইটেল ও স্ক্রিপ্ট",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Subtitles", editedTranscript))
                            Toast.makeText(context, "সাবটাইটেল কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Subtitles", tint = ElectricBlueLight, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = editedTranscript,
                        onValueChange = {
                            editedTranscript = it
                            isEditing = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            }

            // Ready-to-Post Captions & Hashtags
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সোশ্যাল মিডিয়া পোস্ট ক্যাপশন ও হ্যাশট্যাগ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Post Caption", editedPostCaption))
                            Toast.makeText(context, "পোস্ট ক্যাপশন কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Caption", tint = ElectricBlueLight, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = editedPostCaption,
                        onValueChange = {
                            editedPostCaption = it
                            isEditing = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 4
                    )
                }
            }

            // Save changes button if user made edits
            if (isEditing) {
                Button(
                    onClick = {
                        onSaveClip(currentClip.id, editedTitle, editedTranscript, editedPostCaption)
                        isEditing = false
                        Toast.makeText(context, "পরিবর্তনগুলো সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সম্পাদনা সংরক্ষণ করুন")
                }
            }
        }
    }
}

@Composable
private fun ScoreProgressRow(label: String, score: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 12.sp)
            Text(text = "$score%", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = NavySurfaceVariant
        )
    }
}
