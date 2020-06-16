package app.milanherke.mystudiez

/**
 * Enum class to represent the type of fragment being stored in [FragmentBackStack].
 * [AddEditSubjectActivity], [AddEditLessonActivity] and [AddEditTaskActivity] are missing
 * because no fragment can be opened by interacting with any of them.
 */
enum class Fragments {
    OVERVIEW,
    SUBJECTS,
    TASKS,
    EXAMS,
    SUBJECT_DETAILS,
    LESSON_DETAILS,
    TASK_DETAILS,
    EXAM_DETAILS
}