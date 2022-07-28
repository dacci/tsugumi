/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.aozora;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dacci
 */
final class AozoraAccentMap {

  private static final Map<Pattern, String> ACCENT_MAP;

  static {
    Map<Pattern, String> map = new HashMap<>();

    map.put(Pattern.compile("!@", Pattern.LITERAL), Matcher.quoteReplacement("¡"));
    map.put(Pattern.compile("?@", Pattern.LITERAL), Matcher.quoteReplacement("¿"));
    map.put(Pattern.compile("A`", Pattern.LITERAL), Matcher.quoteReplacement("À"));
    map.put(Pattern.compile("A'", Pattern.LITERAL), Matcher.quoteReplacement("Á"));
    map.put(Pattern.compile("A^", Pattern.LITERAL), Matcher.quoteReplacement("Â"));
    map.put(Pattern.compile("A~", Pattern.LITERAL), Matcher.quoteReplacement("Ã"));
    map.put(Pattern.compile("A:", Pattern.LITERAL), Matcher.quoteReplacement("Ä"));
    map.put(Pattern.compile("A&", Pattern.LITERAL), Matcher.quoteReplacement("Å"));
    map.put(Pattern.compile("AE&", Pattern.LITERAL), Matcher.quoteReplacement("Æ"));
    map.put(Pattern.compile("C,", Pattern.LITERAL), Matcher.quoteReplacement("Ç"));
    map.put(Pattern.compile("E`", Pattern.LITERAL), Matcher.quoteReplacement("È"));
    map.put(Pattern.compile("E'", Pattern.LITERAL), Matcher.quoteReplacement("É"));
    map.put(Pattern.compile("E^", Pattern.LITERAL), Matcher.quoteReplacement("Ê"));
    map.put(Pattern.compile("E:", Pattern.LITERAL), Matcher.quoteReplacement("Ë"));
    map.put(Pattern.compile("I`", Pattern.LITERAL), Matcher.quoteReplacement("Ì"));
    map.put(Pattern.compile("I'", Pattern.LITERAL), Matcher.quoteReplacement("Í"));
    map.put(Pattern.compile("I^", Pattern.LITERAL), Matcher.quoteReplacement("Î"));
    map.put(Pattern.compile("I:", Pattern.LITERAL), Matcher.quoteReplacement("Ï"));
    map.put(Pattern.compile("N~", Pattern.LITERAL), Matcher.quoteReplacement("Ñ"));
    map.put(Pattern.compile("O`", Pattern.LITERAL), Matcher.quoteReplacement("Ò"));
    map.put(Pattern.compile("O'", Pattern.LITERAL), Matcher.quoteReplacement("Ó"));
    map.put(Pattern.compile("O^", Pattern.LITERAL), Matcher.quoteReplacement("Ô"));
    map.put(Pattern.compile("O~", Pattern.LITERAL), Matcher.quoteReplacement("Õ"));
    map.put(Pattern.compile("O:", Pattern.LITERAL), Matcher.quoteReplacement("Ö"));
    map.put(Pattern.compile("O/", Pattern.LITERAL), Matcher.quoteReplacement("Ø"));
    map.put(Pattern.compile("U`", Pattern.LITERAL), Matcher.quoteReplacement("Ù"));
    map.put(Pattern.compile("U'", Pattern.LITERAL), Matcher.quoteReplacement("Ú"));
    map.put(Pattern.compile("U^", Pattern.LITERAL), Matcher.quoteReplacement("Û"));
    map.put(Pattern.compile("U:", Pattern.LITERAL), Matcher.quoteReplacement("Ü"));
    map.put(Pattern.compile("Y'", Pattern.LITERAL), Matcher.quoteReplacement("Ý"));
    map.put(Pattern.compile("s&", Pattern.LITERAL), Matcher.quoteReplacement("ß"));
    map.put(Pattern.compile("a`", Pattern.LITERAL), Matcher.quoteReplacement("à"));
    map.put(Pattern.compile("a'", Pattern.LITERAL), Matcher.quoteReplacement("á"));
    map.put(Pattern.compile("a^", Pattern.LITERAL), Matcher.quoteReplacement("â"));
    map.put(Pattern.compile("a~", Pattern.LITERAL), Matcher.quoteReplacement("ã"));
    map.put(Pattern.compile("a:", Pattern.LITERAL), Matcher.quoteReplacement("ä"));
    map.put(Pattern.compile("a&", Pattern.LITERAL), Matcher.quoteReplacement("å"));
    map.put(Pattern.compile("ae&", Pattern.LITERAL), Matcher.quoteReplacement("æ"));
    map.put(Pattern.compile("c,", Pattern.LITERAL), Matcher.quoteReplacement("ç"));
    map.put(Pattern.compile("e`", Pattern.LITERAL), Matcher.quoteReplacement("è"));
    map.put(Pattern.compile("e'", Pattern.LITERAL), Matcher.quoteReplacement("é"));
    map.put(Pattern.compile("e^", Pattern.LITERAL), Matcher.quoteReplacement("ê"));
    map.put(Pattern.compile("e:", Pattern.LITERAL), Matcher.quoteReplacement("ë"));
    map.put(Pattern.compile("i`", Pattern.LITERAL), Matcher.quoteReplacement("ì"));
    map.put(Pattern.compile("i'", Pattern.LITERAL), Matcher.quoteReplacement("í"));
    map.put(Pattern.compile("i^", Pattern.LITERAL), Matcher.quoteReplacement("î"));
    map.put(Pattern.compile("i:", Pattern.LITERAL), Matcher.quoteReplacement("ï"));
    map.put(Pattern.compile("n~", Pattern.LITERAL), Matcher.quoteReplacement("ñ"));
    map.put(Pattern.compile("o`", Pattern.LITERAL), Matcher.quoteReplacement("ò"));
    map.put(Pattern.compile("o'", Pattern.LITERAL), Matcher.quoteReplacement("ó"));
    map.put(Pattern.compile("o^", Pattern.LITERAL), Matcher.quoteReplacement("ô"));
    map.put(Pattern.compile("o~", Pattern.LITERAL), Matcher.quoteReplacement("õ"));
    map.put(Pattern.compile("o:", Pattern.LITERAL), Matcher.quoteReplacement("ö"));
    map.put(Pattern.compile("o/", Pattern.LITERAL), Matcher.quoteReplacement("ø"));
    map.put(Pattern.compile("u`", Pattern.LITERAL), Matcher.quoteReplacement("ù"));
    map.put(Pattern.compile("u'", Pattern.LITERAL), Matcher.quoteReplacement("ú"));
    map.put(Pattern.compile("u^", Pattern.LITERAL), Matcher.quoteReplacement("û"));
    map.put(Pattern.compile("u:", Pattern.LITERAL), Matcher.quoteReplacement("ü"));
    map.put(Pattern.compile("y'", Pattern.LITERAL), Matcher.quoteReplacement("ý"));
    map.put(Pattern.compile("y:", Pattern.LITERAL), Matcher.quoteReplacement("ÿ"));
    map.put(Pattern.compile("A_", Pattern.LITERAL), Matcher.quoteReplacement("Ā"));
    map.put(Pattern.compile("a_", Pattern.LITERAL), Matcher.quoteReplacement("ā"));
    map.put(Pattern.compile("E_", Pattern.LITERAL), Matcher.quoteReplacement("Ē"));
    map.put(Pattern.compile("e_", Pattern.LITERAL), Matcher.quoteReplacement("ē"));
    map.put(Pattern.compile("I_", Pattern.LITERAL), Matcher.quoteReplacement("Ī"));
    map.put(Pattern.compile("i_", Pattern.LITERAL), Matcher.quoteReplacement("ī"));
    map.put(Pattern.compile("O_", Pattern.LITERAL), Matcher.quoteReplacement("Ō"));
    map.put(Pattern.compile("o_", Pattern.LITERAL), Matcher.quoteReplacement("ō"));
    map.put(Pattern.compile("OE&", Pattern.LITERAL), Matcher.quoteReplacement("Œ"));
    map.put(Pattern.compile("oe&", Pattern.LITERAL), Matcher.quoteReplacement("œ"));
    map.put(Pattern.compile("U_", Pattern.LITERAL), Matcher.quoteReplacement("Ū"));
    map.put(Pattern.compile("u_", Pattern.LITERAL), Matcher.quoteReplacement("ū"));

    ACCENT_MAP = Collections.unmodifiableMap(map);
  }

  public static String replaceAll(String input) {
    for (Map.Entry<Pattern, String> entry : ACCENT_MAP.entrySet()) {
      input = entry.getKey().matcher(input).replaceAll(entry.getValue());
    }

    return input;
  }

  private AozoraAccentMap() {}
}
