/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */package org.netbeans.conffile;

import static org.netbeans.conffile.Main.NETBEANS_DEFAULT_OPTIONS;
import java.io.IOException;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim Boudreau
 */
public class ConfFile {

    private final Path file;
    final Map<String, List<String>> precedingComments = new HashMap<>();
    final List<String> trailingComments = new ArrayList<>();

    public ConfFile(Path file) {
        assert file != null : "null file";
        this.file = file;
    }

    public void rewriteAs(Map<String, List<String>> newContents) throws IOException {
        rewrite(file, newContents);
    }

    public void rewrite(Path path, Map<String, List<String>> newContents) throws IOException {
        String newBody = rewritten(newContents);
        Files.write(path, newBody.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING, WRITE);
    }

    private static final Pattern SYSTEM_PROP_SETTER
            = Pattern.compile("^-J-D(.*?)=(.*)$");

    public static Set<JVMMemorySetting> allMemorySettings(Map<String, List<String>> items) {
        List<String> l = items.get(NETBEANS_DEFAULT_OPTIONS);
        if (l == null) {
            return null;
        }
        Set<JVMMemorySetting> result = new HashSet<>(5);
        for (String item : l) {
            JVMMemorySetting setting = JVMMemorySetting.parse(item);
            if (setting != null) {
                result.add(setting);
            }
        }
        return result;
    }

    public static String findArgumentTo(String key, Map<String, List<String>> items) {
        List<String> l = items.get(NETBEANS_DEFAULT_OPTIONS);
        if (l == null) {
            return null;
        }
        boolean armed = false;
        for (String item : l) {
            if (key.equals(item) || ("--" + key).equals(item)) {
                armed = true;
                continue;
            }
            if (armed) {
                armed = false;
                return item;
            }
        }
        return null;
    }

    public static MemoryValue findMemorySetting(MemoryValue.Kind kind, Map<String, List<String>> items) {
        List<String> l = items.get(NETBEANS_DEFAULT_OPTIONS);
        if (l == null) {
            return null;
        }
        String prefix = kind.configFilePrefix();
        for (String s : l) {
            if (s.startsWith(prefix)) {
                return MemoryValue.parse(s);
            }
        }
        return null;
    }

    public static String findSystemPropertySetting(String prop, Map<String, List<String>> items) {
        List<String> l = items.get(NETBEANS_DEFAULT_OPTIONS);
        if (l == null) {
            return null;
        }
        for (String s : l) {
            if (s.startsWith("-J-D")) {
                Matcher m = SYSTEM_PROP_SETTER.matcher(s);
                if (m.find()) {
                    if (m.group(1).equals(prop)) {
                        String result = m.group(2);
                        if (result.length() > 1 && result.charAt(0) == '\'' && result.charAt(result.length() - 1) == '\'') {
                            result = result.substring(1, result.length() - 1);
                        } else if (result.length() > 1 && result.charAt(0) == '"' && result.charAt(result.length() - 1) == '"') {
                            result = result.substring(1, result.length() - 1);
                        }
                        return result;
                    }
                }
            }
        }
        return null;
    }

    Set<String> allComments() { // for test
        Set<String> result = new HashSet<>();
        precedingComments.values().forEach(result::addAll);
        return result;
    }

    private void checkEvenQuoteCount(String in, char... quotes) {
        int max = in.length();
        int[] counts = new int[quotes.length];
        for (int i = 0; i < max; i++) {
            char c = in.charAt(i);
            for (int j = 0; j < quotes.length; j++) {
                char q = quotes[j];
                if (c == q) {
                    counts[j]++;
                }
            }
        }
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] % 2 != 0) {
                throw new IllegalArgumentException("Unbalanced " + quotes[i] + " characters in <" + in + ">");
            }
        }
    }

    private void checkContents(List<String> all) {
        all.forEach((s) -> {
            checkEvenQuoteCount(s);
        });
    }

    private boolean allDigits(CharSequence s) {
        int max = s.length();
        boolean result = max > 0;
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) {
                result = false;
                break;
            }
        }
        return result;
    }

    public String rewritten(Map<String, List<String>> newContents) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : newContents.entrySet()) {
            checkContents(e.getValue());
            List<String> toPrepend = precedingComments.get(e.getKey());
            if (toPrepend != null) {
                toPrepend.forEach(cmt -> {
                    sb.append(cmt).append(lineSeparator());
                });
            } else {
                sb.append('\n');
            }
            sb.append(e.getKey()).append('=').append('"');
            String prefix = "";
            if (e.getValue().size() > 1) {
                sb.append(lineSeparator());
                prefix = "\t";
            }
            if (e.getValue().isEmpty()) {
                sb.append('"').append(lineSeparator());
            }
            for (ListIterator<String> it = e.getValue().listIterator(); it.hasNext();) {
                String sw = it.next().trim();
                if (allDigits(sw)) {
                    sb.append(' ').append(sw);
                } else {
                    sb.append(prefix).append(sw);
                }
                if (it.hasNext()) {
                    String nxt = it.next();
                    it.previous();
                    if (allDigits(nxt)) {
                        sb.append(' ');
                    } else {
                        sb.append(lineSeparator());
                    }
                } else {
                    if (e.getValue().size() > 1) {
                        sb.append(lineSeparator());
                    }
                    sb.append('"').append(lineSeparator());
                }
            }
        }
        if (!trailingComments.isEmpty()) {
            for (String tr : trailingComments) {
                sb.append(tr).append(lineSeparator());
            }
        }
        return sb.toString();
    }

    public Map<String, List<String>> parse() throws IOException {
        Map<String, List<String>> result = new LinkedHashMap<>(5);
        parse(result::put);
        return result;
    }

    public void parse(BiConsumer<String, List<String>> variableAndContents) throws IOException {
        parse(file, variableAndContents);
    }

    void parse(Path file, BiConsumer<String, List<String>> variableAndContents) throws IOException {
        VariablesParser vp = new VariablesParser();
        List<String> currComments = new ArrayList<>(64);
        forEachLine(file, (line, isLast) -> {
            handleLine(line, currComments, vp, isLast, variableAndContents);
        });
        if (!currComments.isEmpty()) {
            trailingComments.addAll(currComments);
        }
    }

    void handleLine(String line, List<String> currComments, VariablesParser vp, Boolean isLast, BiConsumer<String, List<String>> variableAndContents) {
        line = line.trim();
        if (line.isEmpty()) {
            currComments.add("");
        } else if (line.charAt(0) == '#') {
            currComments.add(line);
        } else {
            vp.accept(line, isLast, (var, items) -> {
                precedingComments.put(var, new ArrayList<>(currComments));
                variableAndContents.accept(var, items);
                currComments.clear();
            });
        }
    }

    private static void forEachLine(Path file, BiConsumer<String, Boolean> lines) throws IOException {
        List<String> all = Files.readAllLines(file, UTF_8);
        forEachLine(all, lines);
    }

    static void forEachLine(List<String> all, BiConsumer<String, Boolean> lines) throws IOException { // pp for tests
        for (Iterator<String> it = all.iterator(); it.hasNext();) {
            String line = it.next();
            lines.accept(line, !it.hasNext());
        }
    }

    static final class VariablesParser {

        private String name;
        private State state = State.AWAITING_NAME;
        private final StringBuilder buffer = new StringBuilder();
        private final QuoteChar quot = new QuoteChar();
        List<String> itemsBuffer = new ArrayList<>(30);

        private void reset() {
            name = null;
            itemsBuffer.clear();
        }

        @SuppressWarnings("UnnecessaryReturnStatement") // bogus warning but netbeans 11 shows a hint for it
        public void accept(String line, Boolean last, BiConsumer<String, List<String>> varNameAndItems) {
            int max = line.length();
            for (int i = 0; i < max; i++) {
                char c = line.charAt(i);
                State oldState = state;
                boolean endOfLine = i == max - 1;
                state = state.handle(buffer, quot, c, endOfLine, item -> {
                    switch (oldState) {
                        case AWAITING_NAME:
                            name = item;
                            itemsBuffer.clear();
                            break;
                        case IN_ENTRY:
                            itemsBuffer.add(item);
                            break;
                        case DONE:
                            itemsBuffer.add(item);
                            varNameAndItems.accept(name, new ArrayList<>(itemsBuffer));
                            reset();
                            return;
                    }
                });
                if (oldState != state) {
                    switch (state) {
                        case AWAITING_NAME:
                        case DONE:
                            varNameAndItems.accept(name, new ArrayList<>(itemsBuffer));
                            reset();
                            break;
                    }
                }
            }
            if (last) {
                if (name != null && !itemsBuffer.isEmpty()) {
                    varNameAndItems.accept(name, new ArrayList<>(itemsBuffer));
                    reset();
                }
            }
        }

        private enum State {
            AWAITING_NAME() {
                @Override
                State handle(StringBuilder current, QuoteChar quot, char ch, boolean endOfLine, Consumer<String> item) {
                    if (Character.isWhitespace(ch)) {
                        return this;
                    } else if (ch == '=') {
                        sendAndWipe(current, item);
                        return AWAITING_OPENING_QUOTE;
                    }
                    current.append(ch);
                    return this;
                }
            },
            AWAITING_OPENING_QUOTE() {
                @Override
                State handle(StringBuilder current, QuoteChar quot, char ch, boolean endOfLine, Consumer<String> item) {
                    if (Character.isWhitespace(ch)) {
                        return this;
                    }
                    if (quot.set(ch)) {
                        return IN_ENTRY;
                    }
                    return this;
                }
            },
            IN_ENTRY() {
                @Override
                State handle(StringBuilder current, QuoteChar quot, char ch, boolean endOfLine, Consumer<String> item) {
                    boolean sec = !quot.inSecondaryQuote() && quot.toggleSecondary(ch);
                    if (sec) {
                        current.append(ch);
                        if (endOfLine) {
                            sendAndWipe(current, item);
                        }
                        return this;
                    }
                    if (Character.isWhitespace(ch)) {
                        if (!quot.inSecondaryQuote()) {
                            sendAndWipe(current, item);
                            return this;
                        }
                    } else if (!sec && quot.clear(ch)) {
                        sendAndWipe(current, item);
                        return AWAITING_NAME;
                    }
                    current.append(ch);
                    boolean cleared = !sec && quot.inSecondaryQuote() && quot.toggleSecondary(ch);
                    if (!cleared && quot.clear(ch)) {
                        sendAndWipe(current, item);
                        return AWAITING_NAME;
                    }
                    if (endOfLine) {
                        sendAndWipe(current, item);
                    }
                    return this;
                }
            },
            DONE() {
                @Override
                State handle(StringBuilder current, QuoteChar quot, char ch, boolean endOfLine, Consumer<String> item) {
                    if (current.length() > 0) {
                        sendAndWipe(current, item);
                    }
                    return this;
                }
            };

            abstract State handle(StringBuilder current, QuoteChar quot, char ch, boolean endOfLine, Consumer<String> item);

            static void sendAndWipe(StringBuilder buf, Consumer<String> item) {
                if (buf.length() > 0) {
                    item.accept(buf.toString());
                    buf.setLength(0);
                }
            }
        }
    }

    private static final class QuoteChar {

        private char primary = 0;
        private char secondary = 0;

        @Override
        public String toString() {
            if (primary == 0 && secondary == 0) {
                return "<not in quotes>";
            } else {
                StringBuilder sb = new StringBuilder();
                if (primary != 0) {
                    sb.append("primary <").append(primary).append('>');
                }
                if (secondary != 0) {
                    if (sb.length() > 0) {
                        sb.append("; ");
                    }
                    sb.append("secondary <").append(secondary).append('>');
                }
                return sb.toString();
            }
        }

        boolean inSecondaryQuote() {
            return secondary != 0;
        }

        boolean toggleSecondary(char ch) {
            if (primary == 0) {
                return false;
            }
            if (ch == primary) {
                return false;
            }
            switch (ch) {
                case '\'':
                case '"':
                    if (secondary == ch) {
                        secondary = 0;
                        return true;
                    } else {
                        secondary = ch;
                        return true;
                    }
            }
            return false;
        }

        boolean set(char ch) {
            if (primary != 0) {
                switch (ch) {
                    case '\'':
                    case '"':
                        secondary = ch;
                        return false;
                }
            }
            switch (ch) {
                case '\'':
                case '"':
                    primary = ch;
                    return true;
                default:
                    return false;
            }
        }

        char get() {
            return primary;
        }

        boolean clear(char ch) {
            if (ch == secondary) {
                secondary = 0;
                return false;
            }
            if (ch == primary) {
                primary = 0;
                return true;
            }
            return false;
        }
    }
}
