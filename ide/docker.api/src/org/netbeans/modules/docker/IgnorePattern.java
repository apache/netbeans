/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.docker;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public class IgnorePattern {

    private final List<Rule> rules;

    private final boolean negative;

    private IgnorePattern(List<Rule> rules, boolean negative) {
        this.rules = rules;
        this.negative = negative;
    }

    @CheckForNull
    public static IgnorePattern compile(String pattern, Character separator, boolean exclusionSupported) {
        String trimmed = pattern.trim();
        boolean negative = false;
        if (exclusionSupported && trimmed.startsWith("!")) {
            negative = true;
            trimmed = trimmed.substring(1).trim();
        }
        char sep = separator != null ? separator : File.separatorChar;
        String preprocessed = preprocess(trimmed, sep);
        // remove the leading / or \ we will match the relative paths
        if (preprocessed.startsWith(Character.toString(sep))) {
            preprocessed = preprocessed.substring(1);
        }
        return compilePattern(preprocessed, sep, negative);
    }

    static String preprocess(String pattern, char separator) {
        String sep = Character.toString(separator);
        String trimmed = pattern.trim();
        String volume = getVolume(trimmed, separator);
        String path = trimmed.substring(volume.length());
        String ret = path.replaceAll("(" + Pattern.quote(sep) + "){2,}", Matcher.quoteReplacement(sep))
                .replaceAll("(" + Pattern.quote(sep) + "\\.)+(" + Pattern.quote(sep) + "|$)", Matcher.quoteReplacement(sep));
        if (ret.endsWith(sep) && ret.length() > 1) {
            ret = ret.substring(0, ret.length() - sep.length());
        }
        String[] parts = ret.split(Pattern.quote(sep));
        if (parts.length > 1) {
            boolean root = false;
            StringBuilder removed = new StringBuilder();
            int count = 0;
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i].isEmpty()) {
                    root = true;
                    break;
                }
                if (parts[i].equals("..")) {
                    count++;
                } else {
                    if (count == 0) {
                        if (removed.length() > 0) {
                            removed.insert(0, sep);
                        }
                        removed.insert(0, parts[i]);
                    } else {
                        count--;
                    }
                }
            }

            for (int i = 0; i < count; i++) {
                if (removed.length() > 0) {
                    removed.insert(0, sep);
                }
                removed.insert(0, "..");
            }
            if (root) {
                removed.insert(0, sep);
            }
            ret = removed.toString();
        }

        ret = ret.replaceAll("^(" + Pattern.quote(sep) + "\\.\\.)+(" + Pattern.quote(sep) +")?", Matcher.quoteReplacement(sep))
                .replaceAll("/", Matcher.quoteReplacement(sep));
        if (ret.isEmpty()) {
            ret = ".";
        }
        return volume + ret;
    }

    static IgnorePattern compilePattern(String pattern, char separator, boolean negative) {
        String trimmed = pattern.trim();
        List<Rule> ret = new ArrayList<>();
        char[] patternChars = trimmed.toCharArray();
        List<Character> buffer = new ArrayList<>();
        for (int i = 0; i < patternChars.length; i++) {
            char c = patternChars[i];
            switch (c) {
                case '*':
                    addCharacterListRule(ret, buffer);
                    if (ret.isEmpty() || !(ret.get(ret.size() - 1) instanceof StarRule)) {
                        ret.add(new StarRule(separator));
                    }
                    break;
                case '?':
                    addCharacterListRule(ret, buffer);
                    ret.add(new QuestionRule(separator));
                    break;
                case '[':
                    addCharacterListRule(ret, buffer);
                    Pair<? extends Rule, Integer> p = createRange(patternChars, i, separator);
                    ret.add(p.first());
                    if (p.second() < 0) {
                        return new IgnorePattern(ret, negative);
                    }
                    i = p.second();
                    break;
                case '\\':
                    if (separator == '\\') {
                        buffer.add(patternChars[i]);
                    } else {
                        if (i < patternChars.length - 1) {
                            buffer.add(patternChars[++i]);
                        } else {
                            addCharacterListRule(ret, buffer);
                            ret.add(new ErrorRule(trimmed, i));
                            return new IgnorePattern(ret, negative);
                        }
                    }
                    break;
                default:
                    buffer.add(patternChars[i]);
                    break;
            }
        }
        addCharacterListRule(ret, buffer);
        return new IgnorePattern(ret, negative);
    }

    private static void addCharacterListRule(List<Rule> rules, List<Character> buffer) {
        if (!buffer.isEmpty()) {
            rules.add(new CharacterListRule(buffer));
            buffer.clear();
        }
    }

    public boolean matches(String input) throws PatternSyntaxException {
        return matches(rules, input);
    }

    public boolean isNegative() {
        return negative;
    }

    boolean isError() {
        for (Rule r : rules) {
            if (r instanceof ErrorRule) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(List<Rule> rules, String input) throws PatternSyntaxException {
        char[] inputChars = input.toCharArray();
        int i = 0;
        int listIndex = 0;
        for (Iterator<Rule> it = rules.iterator(); it.hasNext();) {
            Rule r = it.next();
            //try {
                if (inputChars.length == 0) {
                    // star matches even empty string
                    return rules.size() == 1 && r.matchesEmpty();
                }
                int[] test = r.consume(inputChars, i);
                if (test == null) {
                    return false;
                }

                if (test.length == 1) {
                    i = test[0];
                } else if (listIndex == rules.size() - 1
                        && test[test.length - 1] >= input.length()) {
                    // last rule - take the longest one
                    i = test[test.length - 1];
                } else {
                    for (int j = test.length - 1; j >= 0; j--) {
                        if (matches(rules.subList(listIndex + 1, rules.size()), input.substring(test[j]))) {
                            return true;
                        }
                    }
                    return false;
                }
//            } catch (PatternSyntaxException ex) {
//                return false;
//            }
            listIndex++;
            if (i >= inputChars.length) {
                if (!it.hasNext()) {
                    return true;
                } else {
                    return it.next().matchesEmpty();
                }
            }
        }
        return i >= inputChars.length;
    }

    private static Pair<? extends Rule, Integer> createRange(char[] chars, int offset, char separator) {
        if (chars[offset] != '[' || offset >= chars.length - 1) {
            return Pair.of(new ErrorRule(new String(chars), offset), -1);
            //throw new PatternSyntaxException("Malformed range", new String(chars), offset);
        }

        boolean negated = false;
        int start = offset + 1;
        char first = chars[offset + 1];
        if (first == '^') {
            negated = true;
            start++;
        }

        if (start >= chars.length - 1) {
            return Pair.of(new ErrorRule(new String(chars), start), -1);
            //throw new PatternSyntaxException("Malformed range", new String(chars), start);
        }

        Character last = null;
        LinkedList<Character> singles = new LinkedList<>();
        List<Pair<Character, Character>> ranges = new LinkedList<>();
        boolean inRange = false;
        for (int i = start; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\\':
                    if (separator == '\\') {
                        char l = chars[i];
                        if (inRange) {
                            ranges.add(Pair.of(last, l));
                            inRange = false;
                            last = null;
                        } else {
                            last = l;
                            singles.add(l);
                        }
                    } else {
                        if (i < chars.length - 1) {
                            char l = chars[++i];
                            // XXX is backslash allowed in range ?
                            if (inRange) {
                                ranges.add(Pair.of(last, l));
                                inRange = false;
                                last = null;
                            } else {
                                last = l;
                                singles.add(l);
                            }
                        } else {
                            return Pair.of(new ErrorRule(new String(chars), i), -1);
                            //throw new PatternSyntaxException("Malformed range", new String(chars), i);
                        }
                    }
                    break;
                case ']':
                    if (inRange || i == start) {
                        return Pair.of(new ErrorRule(new String(chars), i), -1);
                        //throw new PatternSyntaxException("Malformed range", new String(chars), i);
                    }
                    return Pair.of(new RangeRule(negated, ranges, singles), i);
                case '-':
                    if (last == null) {
                        return Pair.of(new ErrorRule(new String(chars), i), -1);
                        //throw new PatternSyntaxException("Malformed range", new String(chars), i);
                    }
                    singles.removeLast();
                    inRange = true;
                    break;
                default:
                    char l = chars[i];
                    if (inRange) {
                        ranges.add(Pair.of(last, l));
                        inRange = false;
                        last = null;
                    } else {
                        last = l;
                        singles.add(l);
                    }
                    break;
            }
        }
        return Pair.of(new ErrorRule(new String(chars), chars.length - 1), -1);
        //throw new PatternSyntaxException("Malformed range", new String(chars), chars.length - 1);
    }

    private static String getVolume(String path, char separator) {
        if (separator != '\\') {
            return "";
        }
        if (path.length() < 2) {
            return "";
        }
        char drive = path.charAt(0);
        if (path.charAt(1) == ':' && ('a' <= drive && drive <= 'z' || 'A' <= drive && drive <= 'Z')) { // NOI18N
            return path.substring(0, 2);
        }
        // FIXME UNC

        return "";
    }

    private static interface Rule {

        int[] consume(char[] chars, int offset);

        boolean matchesEmpty();

    }

    private static class StarRule implements Rule {

        private final char separator;

        public StarRule(char separator) {
            this.separator = separator;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }

            int limit = -1;
            for (int i = offset; i < chars.length; i++) {
                if (chars[i] == separator) {
                    limit = i;
                    break;
                }
            }
            if (limit < 0) {
                limit = chars.length;
            }
            int[] ret = new int[limit - offset + 1];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = offset + i;
            }
            return ret;
        }

        @Override
        public boolean matchesEmpty() {
            return true;
        }
    }

    private static class QuestionRule implements Rule {

        private final char separator;

        public QuestionRule(char separator) {
            this.separator = separator;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }
            if (chars[offset] == separator) {
                return null;
            }
            return new int[]{offset + 1};
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }
    }

    private static class RangeRule implements Rule {

        private final boolean negated;

        private final List<Pair<Character, Character>> ranges;

        private final List<Character> singles;

        public RangeRule(boolean negated, List<Pair<Character, Character>> ranges, List<Character> singles) {
            this.negated = negated;
            this.ranges = ranges;
            this.singles = singles;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }
            boolean ok = check(chars[offset]);
            if (negated) {
                ok = !ok;
            }
            if (!ok) {
                return null;
            }
            return new int[]{offset + 1};
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }

        private boolean check(char c) {
            for (Character s : singles) {
                if (s == c) {
                    return true;
                }
            }
            for (Pair<Character, Character> r : ranges) {
                if (r.first() <= c && c <= r.second()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class CharacterListRule implements Rule {

        private final List<Character> array;

        public CharacterListRule(List<Character> array) {
            this.array = new ArrayList<>(array);
        }

        @Override
        public int[] consume(char[] chars, int offset) throws IllegalStateException {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != chars[offset + i]) {
                    return null;
                }
            }
            return new int[]{offset + array.size()};
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }
    }

    private static class ErrorRule implements Rule {

        private final String regex;

        private final int index;

        public ErrorRule(String regex, int index) {
            this.regex = regex;
            this.index = index;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            throw new PatternSyntaxException("Malformed pattern", regex, index);
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }
    }
}
