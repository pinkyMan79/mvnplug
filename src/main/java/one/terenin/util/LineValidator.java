package one.terenin.util;

import one.terenin.configuration.ExclusionConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineValidator {
    public static boolean checkLine(String line, String teamLabel) {
        if (ExclusionConfiguration.warnPhrase.stream().anyMatch(line::contains)) {
            Pattern pattern = Pattern.compile(teamLabel + "-\\d+-[a-zA-Z]+\n");
            Matcher matcher = pattern.matcher(line);
            return matcher.find();
        }
        return false;
    }
}
