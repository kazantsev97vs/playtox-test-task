package playtox.test.task.utils;

import java.util.Random;

public class RandomValuesGenerator {

    public final static Random random = new Random();

    /**
     * Сгенирировать случайную строку
     * @return строку из случайных букв со случайным регистром для каждой
     */
    public static String generateRandomStringValue() {

        int stringLength = random.nextInt(100);

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < stringLength; i++) {
            int upperOrLowerCase = random.nextInt(2);

            // Если выбрался верхний регистр
            if (upperOrLowerCase == 0) {
                // A-Z = 65-90
                char upperCaseChar = (char) (generateRandomIntegerValue(65, 90));
                stringBuilder.append(upperCaseChar);

            } else { // Если выбрался нижний регистр
                // a-z = 97-122
                char lowerCaseChar = (char) (generateRandomIntegerValue(97, 122));
                stringBuilder.append(lowerCaseChar);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Сгенерировать случайное число из диапазона
     * @param leftBorderRange - диапазон начинается с числа leftBorderRange
     * @param rightBorderRange - диапазон заканчивается числом rightBorderRange
     * @return случайное число в указанном диапазое
     */
    public static int generateRandomIntegerValue(int leftBorderRange, int rightBorderRange) {
        return random.nextInt(rightBorderRange - leftBorderRange + 1) + leftBorderRange;
    }
}
