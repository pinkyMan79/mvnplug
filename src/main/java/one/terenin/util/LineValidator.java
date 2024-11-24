package one.terenin.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineValidator {
    public static boolean checkLine(String line, String teamLabel, String warnPhrases) {
        List<String> warnPhrasesList = List.of(warnPhrases.split(","));
        if (warnPhrasesList.stream().anyMatch(line::contains)) {
            Pattern pattern = Pattern.compile(teamLabel + "-\\d+-[a-zA-Z]+\n");
            Matcher matcher = pattern.matcher(line);
            return matcher.find();
        }
        return false;
    }
}
