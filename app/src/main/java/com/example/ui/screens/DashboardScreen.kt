package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectItem
import com.example.data.model.ProjectStatus
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    projects: List<ProjectItem>,
    isApiKeyConfigured: Boolean,
    isDemoMode: Boolean,
    onNavigateToCreate: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onUsePresetUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalClips = projects.sumOf { it.clips.size }
    val avgScore = if (totalClips > 0) {
        val totalScore = projects.flatMap { it.clips }.sumOf { it.viralityScore }
        totalScore / totalClips
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        AppHeader(
            title = "ClipNex AI ড্যাশবোর্ড",
            subtitle = "ভিডিও ক্লিপিং ও ভাইরাল শর্টস অটোমেশন",
            isDemo = isDemoMode || !isApiKeyConfigured
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "মোট প্রজেক্ট",
                        value = "${projects.size}",
                        icon = Icons.Default.Folder,
                        iconTint = BrandPurple,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "মোট ক্লিপ",
                        value = "$totalClips",
                        icon = Icons.Default.MovieFilter,
                        iconTint = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "গড় ভাইরাল স্কোর",
                        value = if (avgScore > 0) "$avgScore%" else "০%",
                        icon = Icons.Default.TrendingUp,
                        iconTint = AccentEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "API সংযোগ",
                        value = if (isApiKeyConfigured && !isDemoMode) "সক্রিয় (লাইভ)" else "ডেমো মোড",
                        icon = if (isApiKeyConfigured && !isDemoMode) Icons.Default.CheckCircle else Icons.Default.Bolt,
                        iconTint = if (isApiKeyConfigured && !isDemoMode) AccentEmerald else AccentAmber,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSettings() }
                    )
                }
            }

            // Quick Action Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(BrandPurpleDark, ElectricBlueDark)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "নতুন ভিডিও ক্লিপিং",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "দীর্ঘ ভিডিওর লিংক দিন এবং মাত্র কয়েক মিনিটে আকর্ষণীয় ভাইরাল শর্টস তৈরি করুন।",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToCreate,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dashboard_create_clip_button")
                            ) {
                                Text(
                                    text = "ভিডিও আপলোড ও সেটিংস",
                                    color = BrandPurpleDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = BrandPurpleDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Preset Samples for Testing
            item {
                Column {
                    Text(
                        text = "টেস্ট করার জন্য নমুনা ভিডিও লিংক:",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "পডকাস্ট ভিডিও",
                            icon = Icons.Default.Mic,
                            onClick = {
                                onUsePresetUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                                onNavigateToCreate()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PresetChip(
                            label = "টেক ইন্টারভিউ",
                            icon = Icons.Default.Computer,
                            onClick = {
                                onUsePresetUrl("https://www.youtube.com/watch?v=k1BneeJTDcU")
                                onNavigateToCreate()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recent Projects Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক প্রজেক্টসমূহ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    if (projects.isNotEmpty()) {
                        TextButton(onClick = onNavigateToHistory) {
                            Text(
                                text = "সব দেখুন (${projects.size})",
                                color = ElectricBlueLight,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Project Items or Empty State
            if (projects.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = NavySurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "এখনও কোনো প্রজেক্ট তৈরি হয়নি",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "উপরে \"ভিডিও আপলোড ও সেটিংস\" বাটনে ক্লিক করে প্রথম ক্লিপ তৈরি করুন।",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(projects.take(4)) { project ->
                    RecentProjectCard(
                        project = project,
                        onClick = { onNavigateToProject(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = Color.Unspecified,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = NavySurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlueLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentProjectCard(
    project: ProjectItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (project.status) {
                            ProjectStatus.COMPLETED -> AccentEmerald.copy(alpha = 0.15f)
                            ProjectStatus.PROCESSING -> ElectricBlue.copy(alpha = 0.15f)
                            ProjectStatus.QUEUED -> AccentAmber.copy(alpha = 0.15f)
                            ProjectStatus.FAILED -> AccentRose.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (project.status) {
                        ProjectStatus.COMPLETED -> Icons.Default.CheckCircle
                        ProjectStatus.PROCESSING -> Icons.Default.HourglassTop
                        ProjectStatus.QUEUED -> Icons.Default.Schedule
                        ProjectStatus.FAILED -> Icons.Default.ErrorOutline
                    },
                    contentDescription = null,
                    tint = when (project.status) {
                        ProjectStatus.COMPLETED -> AccentEmerald
                        ProjectStatus.PROCESSING -> ElectricBlueLight
                        ProjectStatus.QUEUED -> AccentAmber
                        ProjectStatus.FAILED -> AccentRose
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${project.clips.size} টি ক্লিপ • ${project.durationSeconds}s • ${project.aspectRatio}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (project.isDemo) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[ডেমো]",
                            color = AccentAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = project.statusBengali,
                    color = when (project.status) {
                        ProjectStatus.COMPLETED -> AccentEmerald
                        ProjectStatus.PROCESSING -> ElectricBlueLight
                        ProjectStatus.QUEUED -> AccentAmber
                        ProjectStatus.FAILED -> AccentRose
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
