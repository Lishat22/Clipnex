package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioOption
import com.example.data.model.CaptionLanguageOption
import com.example.data.model.DurationOption
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun CreateProjectScreen(
    inputUrl: String,
    selectedDuration: DurationOption,
    selectedClipCount: Int,
    selectedAspectRatio: AspectRatioOption,
    selectedCaptionLanguage: CaptionLanguageOption,
    isApiKeyConfigured: Boolean,
    isDemoMode: Boolean,
    errorMessage: String?,
    onUrlChange: (String) -> Unit,
    onDurationChange: (DurationOption) -> Unit,
    onClipCountChange: (Int) -> Unit,
    onAspectRatioChange: (AspectRatioOption) -> Unit,
    onCaptionLanguageChange: (CaptionLanguageOption) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        AppHeader(
            title = "ভিডিও আপলোড ও সেটিংস",
            subtitle = "ক্লিপিং প্যারামিটার নির্ধারণ করুন",
            isDemo = isDemoMode || !isApiKeyConfigured
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Tab Selector (URL vs Local Upload note)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavySurface,
                contentColor = BrandPurple
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ভিডিও লিংক (URL)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ফাইল আপলোড", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 1) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = NavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = ElectricBlueLight,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "পাবলিক ক্লাউড ফাইল লিঙ্ক সমর্থন",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Katto AI এর উচ্চগতির ক্লাউড প্রসেসিংয়ের জন্য সরাসরি অ্যাক্সেসযোগ্য MP4 ভিডিও লিঙ্ক অথবা ইউটিউব লিঙ্ক প্রদান করা সবচেয়ে কার্যকর। নিচে একটি টেস্ট লিঙ্ক যুক্ত করা হয়েছে।",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onUrlChange("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                                selectedTab = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("নমুনা ডিরেক্ট MP4 লিঙ্ক ব্যবহার করুন")
                        }
                    }
                }
            }

            // URL Input Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ভিডিওর লিঙ্ক প্রদান করুন (ইউটিউব, ভিমিও বা MP4)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = onUrlChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_url_input"),
                        placeholder = {
                            Text("যেমন: https://www.youtube.com/watch?v=...", color = TextMuted)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (inputUrl.isNotBlank()) {
                                    IconButton(onClick = { onUrlChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                    }
                                }
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = clipboard.primaryClip
                                    if (clipData != null && clipData.itemCount > 0) {
                                        val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
                                        if (pasteText.isNotBlank()) onUrlChange(pasteText)
                                    }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = ElectricBlueLight)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = BrandPurple
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetUrlPill(
                            label = "পডকাস্ট ডেমো",
                            onClick = { onUrlChange("https://www.youtube.com/watch?v=k1BneeJTDcU") }
                        )
                        PresetUrlPill(
                            label = "মোটিভেশন ডেমো",
                            onClick = { onUrlChange("https://www.youtube.com/watch?v=dQw4w9WgXcQ") }
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = errorMessage, color = AccentRose, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Duration Selection (15s, 30s, 45s, 60s)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ক্লিপের দৈর্ঘ্য নির্বাচন করুন",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DurationOption.values().forEach { option ->
                            SelectableChip(
                                label = option.labelBengali,
                                isSelected = selectedDuration == option,
                                onClick = { onDurationChange(option) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Number of Clips Selection (1, 3, 5, 10)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "কতটি ক্লিপ তৈরি করতে চান?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 5, 10).forEach { count ->
                            SelectableChip(
                                label = "$count টি",
                                isSelected = selectedClipCount == count,
                                onClick = { onClipCountChange(count) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Aspect Ratio Selection (9:16, 1:1, 16:9)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আউটপুট অনুপাত (Aspect Ratio)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AspectRatioOption.values().forEach { ratio ->
                            SelectableRatioRow(
                                ratio = ratio,
                                isSelected = selectedAspectRatio == ratio,
                                onClick = { onAspectRatioChange(ratio) }
                            )
                        }
                    }
                }
            }

            // Subtitle / Caption Language (বাংলা, ইংরেজি, স্বয়ংক্রিয়)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "সাবটাইটেল ও ক্যাপশন ভাষা",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CaptionLanguageOption.values().forEach { lang ->
                            SelectableChip(
                                label = lang.titleBengali.substringBefore(" "),
                                isSelected = selectedCaptionLanguage == lang,
                                onClick = { onCaptionLanguageChange(lang) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // API Mode Notice
            if (!isApiKeyConfigured && !isDemoMode) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentAmber.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "লাইভ Katto API কী কনফিগার করা নেই। এই ক্লিপটি পরীক্ষামূলক ডেমো মোডে তৈরি হবে। আসল এপিআই ব্যবহার করতে সেটিংস পেজ দেখুন।",
                            color = AccentAmber,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_generate_clips_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPurple
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "সেরা ক্লিপ তৈরি করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PresetUrlPill(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = NavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = ElectricBlueLight, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandPurple else NavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) BrandPurpleLight else NavyBorder
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SelectableRatioRow(
    ratio: AspectRatioOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandPurple.copy(alpha = 0.15f) else NavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) BrandPurple else NavyBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = BrandPurple,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = ratio.ratioLabel,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = ratio.bengaliDescription,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
