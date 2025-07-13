package top.jgroup.utlis;

public class MessageUtil {

    public static String createJsonMessage(String message) {
        return "{"
                + "\"message\": \"" + escapeJson(message) + "\""
                + "}";
    }

    public static String createJsonMessage(Boolean status, String message) {
        return "{"
                + "\"success\": " + status + ", "
                + "\"message\": \"" + escapeJson(message) + "\""
                + "}";
    }

    public static String createJsonMessage(Boolean status, String textName, String text) {
        return "{"
                + "\"success\": " + status + ", "
                + "\"" + escapeJson(textName) + "\": \"" + escapeJson(text) + "\""
                + "}";
    }

    public static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
