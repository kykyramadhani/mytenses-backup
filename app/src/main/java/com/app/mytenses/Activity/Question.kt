package com.app.mytenses.Activity

data class Question(
    val question_id: String = "",
    val quiz_id: String = "",
    val text: String = "",
    val correct_option: String = "",
    val options: Map<String, String> = emptyMap()
)
