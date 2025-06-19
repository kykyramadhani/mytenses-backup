package com.app.mytenses.model

data class Lesson(
    val lesson_id: String,
    val title: String,
    val description: String,
    val materials: Map<String, Material>,
    val quizzes: Map<String, Quiz>
)

data class Material(
    val material_id: String,
    val lesson_id: String,
    val chapter_title: String,
    val explanation: List<String>? = null,
    val formulas: List<Formula>? = null,
    val examples: List<Example>? = null
)

data class Example(
    val sentence: String,
    val example_translation: String
)

data class Formula(
    val type: String? = null,
    val formula: String
)

data class Quiz(
    val quiz_id: String,
    val lesson_id: String,
    val title: String,
    val total_points: Int,
    val questions: Map<String, Question>
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