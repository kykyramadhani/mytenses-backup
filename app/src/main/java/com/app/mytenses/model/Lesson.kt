package com.app.mytenses.model

data class ApiResponse(
    val counters: Counters? = null,
    val lessons: Map<String, Lesson>? = null,
    val materials: Map<String, Material>? = null,
)

data class Counters(
    val user_id: Int? = null
)

data class Lesson(
    val lesson_id: String,
    val title: String,
    val description: String
)

data class Material(
    val material_id: String,
    val lesson_id: String,
    val chapter_title: String,
    val explanation: String,
    val formulas: List<Formula>? = null,
    val example: String? = null,
    val example_translation: String? = null
)

data class Formula(
    val type: String? = null,
    val formula: String
)

data class Quiz(
    val quiz_id: String,
    val lesson_id: String,
    val title: String,
    val total_points: Int
)

data class Question(
    val question_id: String,
    val quiz_id: String,
    val text: String,
    val options: List<String>,
    val correct_option: String,
    val points: Int
)

data class MaterialsResponse(
    val materials: List<Material>
)



