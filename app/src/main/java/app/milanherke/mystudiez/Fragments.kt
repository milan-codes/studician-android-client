package app.milanherke.mystudiez

/**
 * Simple enum class to represent the type of fragment being stored in [FragmentsStack].
 * [AddEditSubjectFragment], [AddEditLessonFragment] and [AddEditTaskFragment] are missing
 * because no fragment can be opened by interacting with any of them.
 */
enum class Fragments {
    SUBJECTS,
    TASKS,
    EXAMS,
    SUBJECT_DETAILS,
    LESSON_DETAILS,
    TASK_DETAILS,
    EXAM_DETAILS
}