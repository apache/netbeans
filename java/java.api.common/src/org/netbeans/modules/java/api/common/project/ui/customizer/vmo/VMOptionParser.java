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
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Plain-Java parser for a Java VM options string, e.g.
 * {@code -Xmx512m -Dfoo=bar -server}.
 *
 * <p>The input is a sequence of whitespace separated words. A word that
 * starts with a hyphen is classified as a known VM option (switch,
 * parameterized option, system property and so on); any other word is
 * kept verbatim as an {@link UnknownOption}. Words with a leading hyphen
 * that do not match any known option are kept as an
 * {@link UnrecognizedOption}, so no part of the input is ever lost.
 */
public final class VMOptionParser {

    private static final Set<String> SWITCH_OPTIONS = Set.of(
            "server", "client",
            "esa", "enablesystemassertions",
            "dsa", "disablesystemassertions",
            "ea", "enableassertions",
            "da", "disableassertions",
            "showversion",
            "?", "help",
            "X", "Xint", "Xbatch", "Xfuture", "Xnoclassgc", "Xincgc", "Xprof", "Xrs"
    );

    private static final List<String> PARAM_OPTIONS = List.of(
            "splash", "javaagent", "agentlib", "agentpath",
            "Xbootclasspath", "Xbootclasspath/a", "Xbootclasspath/p"
    );

    private static final Pattern MEM_OPTION = Pattern.compile("X(m[sx]|ss)\\d+[gGmMkK]");

    private VMOptionParser() {}

    /**
     * Parses a VM options string into a list of options.
     *
     * @param input the options string, e.g. {@code -Xmx512m -Dfoo=bar}
     * @return the options parsed from the string; never {@code null}
     */
    public static List<JavaVMOption<?>> parse(String input) {
        List<JavaVMOption<?>> result = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return result;
        }
        String[] words = input.strip().split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.startsWith("-")) {
                result.add(new UnknownOption(word));
                continue;
            }
            String name = word.substring(1);
            if (name.isEmpty()) {
                continue;
            }
            if ("cp".equals(name) || "classpath".equals(name)) {
                // The classpath is the next word, not a ':' or '=' parameter.
                String value = null;
                if (i + 1 < words.length && !words[i + 1].startsWith("-")) {
                    value = unquote(words[i + 1]);
                    i++;
                }
                result.add(new ParametrizedNode(name, " ", value, false));
                continue;
            }
            result.add(classify(name));
        }
        return result;
    }

    private static JavaVMOption<?> classify(String name) {
        if (SWITCH_OPTIONS.contains(name)) {
            return new SwitchNode(name, true);
        }
        if ("verbose".equals(name) || name.startsWith("verbose:")) {
            return new SwitchNode(name, true);
        }
        if ("version".equals(name) || name.startsWith("version:")) {
            int idx = name.indexOf(':');
            if (idx > 0) {
                return new ParametrizedNode(name.substring(0, idx), ":", name.substring(idx + 1));
            }
            return new SwitchNode(name, true);
        }
        if (name.startsWith("Xshare:") || name.startsWith("Xcheck:")) {
            return new SwitchNode(name, true);
        }
        if (name.charAt(0) == 'D') {
            int idx = name.indexOf('=');
            if (idx > 0) {
                return new UserPropertyNode(name.substring(0, idx), unquote(name.substring(idx + 1)));
            }
            return new UnrecognizedOption(name); // -Dfoo without '='
        }
        for (String param : PARAM_OPTIONS) {
            if (name.startsWith(param + ":")) {
                int idx = name.indexOf(':');
                return new ParametrizedNode(name.substring(0, idx), ":", name.substring(idx + 1));
            }
        }
        if (MEM_OPTION.matcher(name).matches()) {
            return new ParametrizedNode(name.substring(0, 3), "", name.substring(3));
        }
        return new UnrecognizedOption(name);
    }

    /**
     * Strips one pair of surrounding single or double quotes, if any.
     */
    private static String unquote(String s) {
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }
}
