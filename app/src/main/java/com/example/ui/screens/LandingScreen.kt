package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        // Hero Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.clipnex_hero_1793034628117),
                contentDescription = "ClipNex AI Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NavyBackground.copy(alpha = 0.8f),
                                NavyBackground
                            )
                        )
                    )
            )

            // Brand tag on hero
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = NavySurface.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandPurple.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ClipNex AI • স্মার্ট ভিডিও ক্লিপার",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hero Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "দীর্ঘ ভিডিও থেকে\nএক ক্লিকে ভাইরাল শর্টস",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "কোনো এডিটিং দক্ষতা ছাড়াই আপনার পডকাস্ট, ইন্টারভিউ বা ভ্লগ থেকে স্বয়ংক্রিয়ভাবে আকর্ষণীয় রিলস ও শর্টস তৈরি করুন। সাথে থাকছে স্বয়ংক্রিয় বাংলা সাবটাইটেল ও ভাইরালিটি স্কোর।",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main CTA Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("landing_get_started_button"),
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
                    Text(
                        text = "ক্লিপ তৈরি শুরু করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Features Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "মূল সুবিধাসমূহ",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            FeatureItem(
                icon = Icons.Default.SmartToy,
                iconColor = BrandPurple,
                title = "AI মোমেন্ট ডিটেকশন",
                description = "সম্পূর্ণ ভিডিও বিশ্লেষণ করে সবচেয়ে রোমাঞ্চকর ও শিক্ষণীয় অংশ খুঁজে বের করে।"
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.CropFree,
                iconColor = ElectricBlue,
                title = "৯:১৬ স্মার্ট রিফ্রেমিং",
                description = "ল্যান্ডস্কেপ ভিডিওর বক্তাকে স্বয়ংক্রিয়ভাবে ফ্রেমের মাঝখানে এনে রিলস ফরম্যাট তৈরি করে।"
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.Subtitles,
                iconColor = AccentCyan,
                title = "স্বয়ংক্রিয় বাংলা সাবটাইটেল",
                description = "স্পষ্ট ও সুন্দর বাংলা সাবটাইটেল যা প্রয়োজনে এডিট করার সুযোগ রয়েছে।"
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.TrendingUp,
                iconColor = AccentEmerald,
                title = "ভাইরালিটি স্কোরিং",
                description = "কোন ক্লিপটি ফেসবুকে বা ইউটিউবে ভাইরাল হওয়ার সম্ভাবনা বেশি তা আগে থেকেই জানুন।"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // How it works section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = NavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "কীভাবে কাজ করে?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                StepRow(stepNumber = "১", text = "ভিডিওর লিংক (ইউটিউব/সরাসরি ফাইল) প্রদান করুন")
                StepRow(stepNumber = "২", text = "ক্লিপের দৈর্ঘ্য ও পছন্দের ফরম্যাট নির্বাচন করুন")
                StepRow(stepNumber = "৩", text = "\"সেরা ক্লিপ তৈরি করুন\" বাটনে ক্লিক করুন")
                StepRow(stepNumber = "৪", text = "তৈরি করা ক্লিপের প্রিভিউ দেখুন ও ডাউনলোড করুন", isLast = true)
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StepRow(
    stepNumber: String,
    text: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(BrandPurple.copy(alpha = 0.2f))
                .border(1.dp, BrandPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = BrandPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
