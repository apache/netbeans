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
 */
package org.netbeans.conffile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Quick'n'dirty command-line argument handling. Note - all of the add methods
 * that mutate the parser return new instances, and are intended to be invoked
 * fluently.
 *
 * @author Tim Boudreau
 */
public final class ArgsParser {

    private final Set<ArgEntry> all = new HashSet<>();
    private static final int TAB_WIDTH = 4;

    ArgsParser copy() {
        ArgsParser p = new ArgsParser();
        for (ArgEntry ae : all) {
            p.all.add(ae.copy(p));
        }
        return p;
    }

    ArgsParser add(ArgEntry entry) {
        ArgsParser result = copy();
        result.all.add(entry.copy(result));
        return result;
    }

    /**
     * Parse command line arguments.
     *
     * @param args command-line arguments
     * @return A result
     */
    public ArgsResult parse(String... args) {
        List<String> unhandled = new ArrayList<>(Arrays.asList(args));
        ArgsResult result = new ArgsResult(unhandled, new ArrayList<>(this.all));
        for (ArgEntry e : all) {
            for (int i = 0; i < args.length; i++) {
                int next = e.match(args, i, result);
                if (next != i) {
                    unhandled.remove(args[i]);
                    if (next > i + 1) {
                        unhandled.remove(args[i + 1]);
                    }
                    break;
                }
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        printHelp(sb, null, true);
        return sb.toString();
    }

    public void printHelpAndExit(int exitCode, String prefix) {
        printHelpAndExit(exitCode, prefix, null);
    }

    public void printHelpAndExit(int exitCode, String prefix, String openingLineEnd) {
        StringBuilder sb = prefix == null ? new StringBuilder() : new StringBuilder(prefix);
        printHelp(sb, openingLineEnd, false);
        System.err.println(sb);
        if (exitCode >= 0) {
            System.exit(exitCode);
        }
    }

    public StringBuilder printHelp(StringBuilder sb, String openingLineEnd, boolean includeUndocumented) {
        List<ArgEntry> entries = new ArrayList<>(all);
        Collections.sort(entries);
        int maxNameItemWidth = 0;
        if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.append(' ');
        }
        for (Iterator<ArgEntry> it = entries.iterator(); it.hasNext();) {
            ArgEntry e = it.next();
            maxNameItemWidth = e.justify(maxNameItemWidth);
            sb.append(e);
            if (it.hasNext() || openingLineEnd != null) {
                sb.append(' ');
            }
        }
        if (openingLineEnd != null) {
            sb.append(openingLineEnd);
        }
        sb.append('\n');
        int stops = (maxNameItemWidth / TAB_WIDTH) + 1;
        for (ArgEntry e : entries) {
            e.printHelp(sb, TAB_WIDTH, stops, includeUndocumented);
        }
        sb.append('\n');
        return sb;
    }

    public ArgEntry add(String longName) {
        if (longName == null || longName.trim().isEmpty()) {
            throw new IllegalArgumentException("Null or empty name '"
                    + longName + "'");
        }
        for (ArgEntry e : all) {
            if (longName.equals(e.longName)) {
                throw new IllegalArgumentException("An entry for '"
                        + longName + "' was already added.");
            }
        }
        ArgEntry e = new ArgEntry(this, longName.trim());
        return e;
    }

    public static final class ArgsResult {

        private final Set<String> switches = new HashSet<>();
        private final Map<String, String> args = new HashMap<>();
        private final List<String> unhandled;
        private final List<ArgEntry> createdFrom;

        ArgsResult(List<String> unh, List<ArgEntry> createdFrom) {
            unhandled = unh;
            this.createdFrom = createdFrom;
            Collections.sort(createdFrom);
        }

        /**
         * Validate that required values are present and any entries that have a
         * predicate to test against pass.
         *
         * @param allowUnknownArguments If false, and unhandled arguments are
         * present, treat that as failure.
         * @return A string of errors if failed, else null
         */
        public String validate(boolean allowUnknownArguments) {
            StringBuilder sb = new StringBuilder();
            boolean result = true;
            for (ArgEntry e : createdFrom) {
                result &= e.validate(this, sb);
            }
            if (!allowUnknownArguments && !unhandled.isEmpty()) {
                for (String un : unhandled) {
                    sb.append("Unknown argument: '").append(un).append("'\n");
                }
                result = false;
            }
            if (!result) {
                return sb.toString();
            }
            return null;
        }

        public ArgsResult forEach(BiConsumer<String, String> argConsumer) {
            switches.forEach((s) -> {
                argConsumer.accept(s, "true");
            });
            args.entrySet().forEach((e) -> {
                argConsumer.accept(e.getKey(), e.getValue());
            });
            return this;
        }

        public boolean hasUnknownArguments() {
            return unhandled != null && unhandled.size() > 0;
        }

        public String unhandledArgument() {
            return unhandled.isEmpty() ? null : unhandled.get(0);
        }

        public List<String> unhandled() {
            return unhandled;
        }

        void add(String arg) {
            args.remove(arg);
            switches.add(arg);
        }

        void add(String name, String value) {
            switches.remove(name);
            args.put(name, value);
        }

        public boolean isSet(String sw) {
            return switches.contains(sw) || args.containsKey(sw);
        }

        public Path get(String name, Supplier<Path> fallback) {
            if (isSet(name)) {
                return Paths.get(name);
            }
            return fallback.get();
        }

        public String get(String sw) {
            if (args.containsKey(sw)) {
                return args.get(sw);
            }
            if (switches.contains(sw)) {
                return "true";
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String s : switches) {
                sb.append(s).append('=').append("true").append(' ');
            }
            for (Map.Entry<String, String> e : args.entrySet()) {
                sb.append(e.getKey()).append('=').append('"').append(e.getValue()).append('"').append(' ');
            }
            return sb.toString();
        }
    }

    public static final class ArgEntry implements Comparable<ArgEntry> {

        char shortName;
        private final ArgsParser parser;
        String longName;
        boolean takesArgument;
        String helpText;
        boolean required;
        BiPredicate<String, StringBuilder> matching;
        Set<String> requiredIf = new HashSet<>();
        Set<String> incompatibleWith = new HashSet<>();

        ArgEntry(ArgsParser parser, String longName) {
            this.parser = parser;
            this.longName = longName;
        }

        ArgEntry copy(ArgsParser parser) {
            ArgEntry nue = new ArgEntry(parser, longName);
            nue.shortName = shortName;
            nue.takesArgument = takesArgument;
            nue.helpText = helpText;
            nue.required = required;
            nue.requiredIf.addAll(requiredIf);
            nue.incompatibleWith.addAll(incompatibleWith);
            return nue;
        }

        /**
         * Mark this entry as required if the passed value is present.
         *
         * @param otherValue Another value
         * @return this
         */
        public ArgEntry requiredIf(String otherValue) {
            if (otherValue == null) {
                throw new IllegalArgumentException("Null otherValue");
            }
            if (otherValue.equals(longName)) {
                throw new IllegalArgumentException("Cannot require self");
            }
            if (incompatibleWith.contains(otherValue)) {
                throw new IllegalArgumentException("Cannot both be incompatible with '" + otherValue + "' and require it");
            }
            this.requiredIf.add(otherValue);
            return this;
        }

        /**
         * Mark this entry as being mutually exclusive with the passed value.
         *
         * @param otherValue Another value
         * @return this
         */
        public ArgEntry incompatibleWith(String otherValue) {
            if (otherValue == null) {
                throw new IllegalArgumentException("Null otherValue");
            }
            if (otherValue.equals(longName)) {
                throw new IllegalArgumentException("Cannot require self");
            }
            if (requiredIf.contains(otherValue)) {
                throw new IllegalArgumentException("Cannot both be incompatible with '" + otherValue + "' and require it");
            }
            this.incompatibleWith.add(otherValue);
            return this;
        }

        boolean validate(ArgsResult res, StringBuilder errors) {
            String value = res.get(longName);
            boolean req = required;
            if (value == null && !req && !requiredIf.isEmpty()) {
                for (String r : requiredIf) {
                    if (res.isSet(r)) {
                        errors.append(this).append(" is required when '")
                                .append(r).append("' is set\n");
                        return false;
                    }
                }
            }
            if (required && value == null) {
                errors.append(this).append(" is required.\n");
                return false;
            }
            if (value != null && matching != null && !matching.test(value, errors)) {
                return false;
            }
            if (value != null && !incompatibleWith.isEmpty()) {
                for (String inc : incompatibleWith) {
                    if (res.isSet(inc)) {
                        errors.append(longName).append(" and ")
                                .append(inc).append(" are mutually exclusive\n");
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * Provide a test of the argument value which can append an error
         * message into the StringBuilder it will be passed.
         *
         * @param test A test
         * @return A new parser that includes a copy of this entry in its
         * current state
         */
        public ArgsParser matching(BiPredicate<String, StringBuilder> test) {
            if (test == null) {
                throw new IllegalArgumentException("Null test");
            }
            takesArgument = true;
            if (this.matching == null) {
                this.matching = test;
            } else {
                this.matching = this.matching.or(test);
            }
            return addToParser();
        }

        /**
         * Set this entry to take an argument, and store the passed predicate to
         * validate arguments.
         *
         * @param test A test
         * @return A new parser that includes a copy of this entry in its
         * current state
         */
        public ArgsParser matching(Predicate<String> test) {
            if (test == null) {
                throw new IllegalArgumentException("Null test");
            }
            takesArgument = true;
            if (this.matching != null) {
                this.matching = this.matching.or((String val, StringBuilder sb) -> {
                    if (!test.test(val)) {
                        sb.append("Invalid value for ").append(this).append('\n');
                        return false;
                    }
                    return true;
                });
            } else {
                this.matching = (val, sb) -> {
                    if (!test.test(val)) {
                        sb.append("Invalid value for ").append(this).append('\n');
                        return false;
                    }
                    return true;
                };
            }
            return addToParser();
        }

        private ArgsParser addToParser() {
            return parser.add(this);
        }

        /**
         * Mark this entry as required.
         *
         * @return This
         */
        public ArgEntry required() {
            required = true;
            return this;
        }

        @Override
        public String toString() {
            if (shortName != 0) {
                if (takesArgument) {
                    return "[--" + longName + " / " + "-" + shortName + "] "
                            + (required ? "<required>" : "<value>");
                } else {
                    return "[--" + longName + " / " + "-" + shortName + "]";
                }
            } else {
                if (takesArgument) {
                    return "--" + longName + " " + (required ? "<required>" : "<value>");
                } else {
                    return "--" + longName;
                }
            }
        }

        int match(String[] args, int index, ArgsResult res) {
            String val = args[index];
            if (takesArgument && index == args.length - 1) {
                return index;
            }
            if (("--" + longName).equals(val) || ("-" + shortName).equals(val)) {
                if (takesArgument) {
                    res.add(longName, args[index + 1]);
                    return index + 2;
                } else {
                    res.add(longName);
                    return index + 1;
                }
            }
            return index;
        }

        /**
         * Set help text for this argument.
         *
         * @param txt Some text
         * @return this
         */
        public ArgEntry withHelpText(String txt) {
            if (this.helpText != null && !this.helpText.equals(txt)) {
                throw new IllegalStateException("Short name already set to '"
                        + this.helpText + "'");
            }
            this.helpText = txt;
            return this;
        }

        /**
         * Provide a short line switch, e.g. -f for --file.
         *
         * @param c A character
         * @return this
         */
        public ArgEntry shortName(char c) {
            if (shortName != 0 && shortName != c) {
                throw new IllegalStateException("Short name already set to '"
                        + shortName + "'");
            }
            shortName = c;
            return this;
        }

        String switchText() {
            String result = shortName != 0 ? "--" + longName + " / " + "-" + shortName
                    : "--" + longName;
            if (takesArgument) {
                result += " <" + shortName + ">";
            }
            return result;
        }

        int justify(int currentWidth) {
            int len = switchText().length();
            return Math.max(currentWidth, len);
        }

        StringBuilder printHelp(StringBuilder into, int tabWidth, int nameTabStops, boolean includeUndocumented) {
            if (helpText == null && !includeUndocumented) {
                return into;
            }
            into.append('\n');
            spaces(tabWidth, into);
            String item = switchText();
            into.append(item);
            int count = (tabWidth * nameTabStops) - item.length();
            spaces(count, into);
            into.append(":");
            int beginLength = tabWidth + item.length() + count;
            int lineLength = beginLength;
            String ht = helpText == null ? "(undocumented)" : helpText;
            if (required) {
                ht = "Required. " + helpText;
            }
            for (String word : ht.split("\\s")) {
                if (lineLength + word.length() + 1 > 80) {
                    into.append('\n');
                    spaces(beginLength, into);
                    lineLength = beginLength;
                }
                into.append(' ').append(word);
            }
            return into;
        }

        /**
         * Mark this entry as taking an argument, and return a new parser that
         * contains it.
         *
         * @return A new parser that includes a copy of this entry in its
         * current state
         */
        public ArgsParser takesArgument() {
            takesArgument = true;
            return addToParser();
        }

        /**
         * Mark this entry as taking no argument, and return a new parser that
         * includes it.
         *
         * @return A new parser that includes a copy of this entry in its
         * current state
         */
        public ArgsParser withNoArgument() {
            takesArgument = false;
            return addToParser();
        }

        /**
         * Compare case-insensitively on long name.
         *
         * @param o Another entry
         * @return an int
         */
        @Override
        public int compareTo(ArgEntry o) {
            return longName.compareToIgnoreCase(o.longName);
        }
    }

    private static StringBuilder spaces(int count, StringBuilder into) {
        if (count <= 0) {
            return into;
        }
        char[] chars = new char[count];
        Arrays.fill(chars, ' ');
        into.append(chars);
        return into;
    }

}
