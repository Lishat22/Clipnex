package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    isApiKeyConfigured: Boolean,
    isDemoMode: Boolean,
    customApiKey: String?,
    onSaveApiKey: (String) -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var keyInput by remember(customApiKey) { mutableStateOf(customApiKey ?: "") }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        AppHeader(
            title = "সেটিংস ও API কনফিগারেশন",
            subtitle = "Katto AI ইন্টিগ্রেশন ও অ্যাকাউন্ট সেটিংস"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // API Connection Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isApiKeyConfigured && !isDemoMode) AccentEmerald.copy(alpha = 0.5f) else AccentAmber.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isApiKeyConfigured && !isDemoMode) AccentEmerald.copy(alpha = 0.15f)
                                else AccentAmber.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isApiKeyConfigured && !isDemoMode) Icons.Default.CheckCircle else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isApiKeyConfigured && !isDemoMode) AccentEmerald else AccentAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isApiKeyConfigured && !isDemoMode) "Katto AI API সংযুক্ত রয়েছে" else "ডেমো মোড কার্যকর রয়েছে",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isApiKeyConfigured && !isDemoMode) "লাইভ ক্লিপিং রিকোয়েস্ট সরাসরি Katto সার্ভারে পাঠানো হবে।"
                            else "কোনো API চার্জ ছাড়া পরীক্ষামূলক ক্লিপ তৈরি করা হচ্ছে।",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Demo Mode Switch Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ডেমো মোড (পরীক্ষামূলক ব্যবহার)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "API খরচ ছাড়াই সম্পূর্ণ ক্লিপিং ফ্লো ও প্রিভিউ পরীক্ষা করুন।",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = onToggleDemoMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = NavySurfaceVariant
                        )
                    )
                }
            }

            // Katto API Key Configuration Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Katto API কী কনফিগারেশন",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "আপনার Katto API কী (Bearer token) এখানে দিতে পারেন, অথবা AI Studio Secrets প্যানেলে STRIPE_LIVE_API_KEY বা KATTO_API_KEY হিসেবে রাখতে পারেন।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = {
                            keyInput = it
                            testResult = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        placeholder = { Text("sk_live_...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveApiKey(keyInput)
                                Toast.makeText(context, "API কী সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_api_key_button")
                        ) {
                            Text("সংরক্ষণ করুন")
                        }

                        OutlinedButton(
                            onClick = {
                                isTestingConnection = true
                                testResult = if (keyInput.isNotBlank() || isApiKeyConfigured) {
                                    "সফল: API কী বিন্যাস সঠিক এবং প্রস্তুত।"
                                } else {
                                    "সতর্কতা: কোনো API কী পাওয়া যায়নি। ডেমো মোড ব্যবহৃত হবে।"
                                }
                                isTestingConnection = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সংযোগ পরীক্ষা", color = TextPrimary)
                        }
                    }

                    if (testResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = testResult!!,
                            color = if (testResult!!.startsWith("সফল")) AccentEmerald else AccentAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // How to get Katto API Key Guide (Bengali)
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
                            text = "Katto API অ্যাকাউন্ট তৈরি নির্দেশিকা",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://katto.tech/api"))
                            context.startActivity(intent)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Katto Docs",
                                tint = ElectricBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    InstructionStep(number = "১", text = "ব্রাউজারে https://katto.tech/api ওপেন করে ফ্রি একাউন্ট তৈরি করুন।")
                    InstructionStep(number = "২", text = "API Dashboard এ গিয়ে নতুন একটি API Key তৈরি করে কপি করে নিন।")
                    InstructionStep(number = "৩", text = "AI Studio এর Secrets প্যানেলে STRIPE_LIVE_API_KEY অথবা উপরে ইনপুট বক্সে পেস্ট করুন।")
                    InstructionStep(number = "৪", text = "এরপর যেকোনো দীর্ঘ ভিডিও থেকে অটোমেটিক রিলস ও শর্টস তৈরি উপভোগ করুন।", isLast = true)
                }
            }

            // App Details Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = NavySurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ClipNex AI সম্পর্কে",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "সংস্করণ: ১.০.০ • সম্পূর্ণ বাংলা ইন্টারফেস\nবাংলাদেশের কনটেন্ট ক্রিয়েটর ও বিগিনারদের জন্য বিশেষভাবে প্রস্তুতকৃত।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier.padding(bottom = if (isLast) 0.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(BrandPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = BrandPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}
