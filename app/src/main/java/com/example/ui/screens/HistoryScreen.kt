package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectItem
import com.example.data.model.ProjectStatus
import com.example.ui.components.AppHeader
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    projects: List<ProjectItem>,
    onSelectProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<ProjectStatus?>(null) }
    var projectToDelete by remember { mutableStateOf<ProjectItem?>(null) }

    val filteredProjects = remember(projects, selectedFilter) {
        if (selectedFilter == null) projects
        else projects.filter { it.status == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        AppHeader(
            title = "প্রজেক্ট হিস্ট্রি",
            subtitle = "সংরক্ষিত ভিডিও ও পূর্ববর্তী ক্লিপসমূহ"
        )

        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("সব (${projects.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPurple,
                        selectedLabelColor = Color.White,
                        containerColor = NavySurface,
                        labelColor = TextSecondary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == ProjectStatus.COMPLETED,
                    onClick = { selectedFilter = ProjectStatus.COMPLETED },
                    label = { Text("সম্পন্ন (${projects.count { it.status == ProjectStatus.COMPLETED }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentEmerald,
                        selectedLabelColor = Color.White,
                        containerColor = NavySurface,
                        labelColor = TextSecondary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == ProjectStatus.PROCESSING,
                    onClick = { selectedFilter = ProjectStatus.PROCESSING },
                    label = { Text("প্রসেসিং (${projects.count { it.status == ProjectStatus.PROCESSING }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue,
                        selectedLabelColor = Color.White,
                        containerColor = NavySurface,
                        labelColor = TextSecondary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == ProjectStatus.FAILED,
                    onClick = { selectedFilter = ProjectStatus.FAILED },
                    label = { Text("ব্যর্থ (${projects.count { it.status == ProjectStatus.FAILED }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentRose,
                        selectedLabelColor = Color.White,
                        containerColor = NavySurface,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "এই ফিল্টারে কোনো প্রজেক্ট পাওয়া যায়নি",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    HistoryProjectItem(
                        project = project,
                        onClick = { onSelectProject(project.id) },
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (projectToDelete != null) {
            ConfirmDeleteDialog(
                projectTitle = projectToDelete?.title ?: "এই প্রজেক্ট",
                onConfirm = {
                    projectToDelete?.id?.let { onDeleteProject(it) }
                    projectToDelete = null
                },
                onDismiss = { projectToDelete = null }
            )
        }
    }
}

@Composable
private fun HistoryProjectItem(
    project: ProjectItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(project.createdAt) { dateFormat.format(Date(project.createdAt)) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_item_${project.id}"),
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
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (project.status) {
                        ProjectStatus.COMPLETED -> AccentEmerald.copy(alpha = 0.15f)
                        ProjectStatus.PROCESSING -> ElectricBlue.copy(alpha = 0.15f)
                        ProjectStatus.QUEUED -> AccentAmber.copy(alpha = 0.15f)
                        ProjectStatus.FAILED -> AccentRose.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = project.status.bengaliText,
                        color = when (project.status) {
                            ProjectStatus.COMPLETED -> AccentEmerald
                            ProjectStatus.PROCESSING -> ElectricBlueLight
                            ProjectStatus.QUEUED -> AccentAmber
                            ProjectStatus.FAILED -> AccentRose
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = AccentRose.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.videoUrl,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = BrandPurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${project.clips.size} টি ক্লিপ",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "• ${project.aspectRatio} • ${project.durationSeconds}s",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = ElectricBlueLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
