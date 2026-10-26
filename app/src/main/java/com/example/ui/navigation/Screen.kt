package com.example.ui.navigation

sealed class Screen(val route: String, val titleBengali: String) {
    object Landing : Screen("landing", "স্বাগতম")
    object Dashboard : Screen("dashboard", "ড্যাশবোর্ড")
    object CreateProject : Screen("create_project", "নতুন ক্লিপ")
    object ProcessingStatus : Screen("processing_status/{projectId}", "প্রসেসিং") {
        fun createRoute(projectId: String) = "processing_status/$projectId"
    }
    object ResultEditor : Screen("result_editor/{projectId}", "ফলাফল ও এডিটর") {
        fun createRoute(projectId: String) = "result_editor/$projectId"
    }
    object History : Screen("history", "প্রজেক্ট হিস্ট্রি")
    object Settings : Screen("settings", "সেটিংস ও API")
}
