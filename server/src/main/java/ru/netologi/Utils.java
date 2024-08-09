package ru.netologi;
import java.util.HashSet;
import java.util.Set;
public class Utils {

    public static String makeCommandsList(HashSet<String> commands) {
        StringBuilder sb = new StringBuilder();
        for (String cmd : commands) {
            if (sb.length() > 0) sb.append("\n");  // добавляем разделитель между командами
            sb.append(cmd);
        }
        return sb.toString();
    }

}
