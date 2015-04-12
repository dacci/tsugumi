/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dacci
 */
public final class Util {

    public static int parseInt(String string) {
        int result = 0;

        for (int i = 0, l = string.length(); i < l; ++i) {
            int digit = Character.getNumericValue(string.codePointAt(i));
            if (digit < 0 || 9 < digit) {
                throw new NumberFormatException(string);
            }

            result = result * 10 + digit;
        }

        return result;
    }

    /**
     * @param pattern
     * @param input
     * @param reverse
     * @return
     */
    public static List<MatchResult> findAll(Pattern pattern,
            CharSequence input, boolean reverse) {
        List<MatchResult> results = new ArrayList<>();

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            results.add(matcher.toMatchResult());
        }

        if (reverse) {
            Collections.reverse(results);
        }

        return results;
    }

    public static String safeFileName(String string) {
        string = string.replace('\\', '_');
        string = string.replace('/', '_');
        string = string.replace(':', '_');
        string = string.replace('*', '_');
        string = string.replace('?', '_');
        string = string.replace('"', '_');
        string = string.replace('<', '_');
        string = string.replace('>', '_');
        string = string.replace('|', '_');
        return string;
    }

    private Util() {
    }
}
