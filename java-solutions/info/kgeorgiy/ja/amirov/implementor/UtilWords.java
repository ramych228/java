package info.kgeorgiy.ja.amirov.implementor;

import java.util.function.Function;

/**
 * Utility class containing constants and helper methods for the Implementor application.
 */
public class UtilWords {
    // Definition of commonly used constants in the code generation process.
    /**
     * EOL (End Of Line) - A system-dependent line separator.
     */
    protected static final String EOL = System.lineSeparator();

    /**
     * SPACE - A single space character.
     */
    protected static final String SPACE = " ";

    /**
     * TAB - A string representing a tab, defined as four space characters.
     */
    protected static final String TAB = SPACE.repeat(4);

    /**
     * SEMICOLON - A semicolon character.
     */
    protected static final String SEMICOLON = ";";

    /**
     * COMMA - A comma character.
     */
    protected static final String COMMA = ",";

    /**
     * DOT - A dot character, typically used in package names and file extensions.
     */
    protected static final String DOT = ".";

    /**
     * SLASH - A slash character, used in file paths.
     */
    protected static final String SLASH = "/";

    /**
     * CASH_MONEY_AP - A dollar sign character, used in inner class names.
     */
    protected static final String CASH_MONEY_AP = "$";

    /**
     * LEFT_BRACE - An opening curly brace character.
     */
    protected static final String LEFT_BRACE = "{";

    /**
     * RIGHT_BRACE - A closing curly brace character.
     */
    protected static final String RIGHT_BRACE = "}";

    /**
     * LEFT_BRACKET - An opening bracket character.
     */
    protected static final String LEFT_BRACKET = "(";

    /**
     * RIGHT_BRACKET - A closing bracket character.
     */
    protected static final String RIGHT_BRACKET = ")";

    /**
     * PUBLIC - The keyword 'public', used in class and method declarations.
     */
    protected static final String PUBLIC = "public";

    /**
     * CLASS - The keyword 'class', used in class declarations.
     */
    protected static final String CLASS = "class";

    /**
     * IMPLEMENTS - The keyword 'implements', used in class declarations to indicate interfaces the class implements.
     */
    protected static final String IMPLEMENTS = "implements";

    /**
     * PACKAGE - The keyword 'package', used at the beginning of source files to specify the package.
     */
    protected static final String PACKAGE = "package";

    /**
     * EXTENDS - The keyword 'extends', used in class declarations to specify the superclass.
     */
    protected static final String EXTENDS = "extends";

    /**
     * IMPL - A suffix used to name implementation classes.
     */
    protected static final String IMPL = "Impl";

    /**
     * EMPTY - An empty string.
     */
    protected static final String EMPTY = "";

    /**
     * SUPER - The keyword 'super', used to refer to the superclass's methods and constructors.
     */
    protected static final String SUPER = "super";

    /**
     * THROWS - The keyword 'throws', used in method declarations to indicate exceptions that can be thrown.
     */
    protected static final String THROWS = "throws";

    /**
     * FALSE - The keyword 'false', representing the boolean value false.
     */
    protected static final String FALSE = "false";

    /**
     * NULL - The keyword 'null', representing the null reference.
     */
    protected static final String NULL = "null";

    /**
     * ZERO - A string representation of the number zero.
     */
    protected static final String ZERO = "0";

    /**
     * ONE - A string representation of the number one.
     */
    protected static final String ONE = "1";

    /**
     * RETURN - The keyword 'return', used in methods to return a value.
     */
    protected static final String RETURN = "return";

    /**
     * JAVA - A string representing the file extension for Java source files.
     */
    protected static final String JAVA = "java";

    /**
     * JAR_CMD_CONST - A command line argument '-jar', used to indicate JAR file creation mode.
     */
    protected static final String JAR_CMD_CONST = "-jar";


    /**
     * Joins elements of an array into a single string with a specified delimiter.
     *
     * @param array The array to join.
     * @param function A function to process each element of the array.
     * @param <E> The type of elements in the array.
     * @return A string representation of the array elements, joined by a delimiter.
     */
    protected static <E> String smartJoin(E[] array, Function<E, String> function) {
        if (array == null || array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(function.apply(array[i]));
            if (i < array.length - 1) {
                sb.append(COMMA).append(SPACE);
            }
        }
        return sb.toString();
    }

    /**
     * Converts a string to its Unicode escape sequence equivalent.
     *
     * @param s The string to convert.
     * @return A string where characters are replaced with their Unicode escape sequences.
     */
    public static String toUnicode(final String s) {
        final StringBuilder b = new StringBuilder();

        for (final char c : s.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04x", (int) c));
            } else {
                b.append(c);
            }
        }

        return b.toString();
    }
}
