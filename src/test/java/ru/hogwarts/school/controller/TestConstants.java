package ru.hogwarts.school.controller;

public class TestConstants {

    // Faculties
    public static final String GRYFFINDOR_NAME = "Гриффиндор";
    public static final String GRYFFINDOR_COLOR = "Красный";
    public static final String SLYTHERIN_NAME = "Слизерин";
    public static final String SLYTHERIN_COLOR = "Зеленый";
    public static final String RAVENCLAW_NAME = "Когтевран";
    public static final String RAVENCLAW_COLOR = "Синий";
    public static final String HUFFLEPUFF_NAME = "Пуффендуй";
    public static final String HUFFLEPUFF_COLOR = "Желтый";
    public static final String LONGEST_FACULTY_NAME = "Факультет с очень длинным названием";

    // Students
    public static final String HARRY_POTTER_NAME = "Гарри Поттер";
    public static final String HERMIONE_GRANGER_NAME = "Гермиона Грейнджер";
    public static final String RON_WEASLEY_NAME = "Рон Уизли";
    public static final String DRACO_MALFOY_NAME = "Драко Малфой";
    public static final String NEVILLE_LONGBOTTOM_NAME = "Невилл Лонгботтом";
    public static final String ALBUS_DUMBLEDORE_NAME = "Альбус Дамблдор";
    public static final String ANGELINA_JOHNSON_NAME = "Анджелина Джонсон";

    //Numbers
    public static final int STUDENT_AGE_17 = 17;
    public static final int STUDENT_AGE_16 = 16;
    public static final int STUDENT_AGE_18 = 18;
    public static final int MIN_AGE = 15;
    public static final int MAX_AGE = 18;
    public static final long NON_EXISTENT_ID = 9999L;
    public static final double AVERAGE_AGE = 16.8;
    public static final long CALCULATED_SUM = 250000000L;

    // Errors
    public static final String FACULTY_NOT_FOUND_MESSAGE = "Факультет не найден";
    public static final String STUDENT_NOT_FOUND_MESSAGE = "Студент не найден";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";

    private TestConstants() {
    }
}