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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;

/**
 * Loads the list of options supported by a platform's {@code java} launcher
 * by running its {@code --help} and {@code --help-extra} (or {@code -help}
 * and {@code -X} on JDK 8 and earlier) and parsing the printed help output.
 *
 * <p>The help output is a help message, not a documented contract, so the
 * parsing is best-effort: an option line is a line whose first non-blank
 * character is a hyphen, a description line is a line indented deeper than
 * the option line (or the text following the option on the same line, as
 * printed by {@code -X}). On any failure an empty list is returned and the
 * caller falls back to the built-in option list.
 */
final class JdkOptions {

    private static final Logger LOG = Logger.getLogger(JdkOptions.class.getName());
    private static final long TIMEOUT_SECONDS = 5; //NOI18N

    private JdkOptions() {
    }

    /**
     * A single option as printed in the help output.
     *
     * @param name the option name without the leading hyphens
     * @param delimiter the character between name and value (e.g. {@code :}
     *        or {@code =}) or an empty string when the value follows
     *        directly
     * @param placeholder the argument placeholder as printed in the help,
     *        e.g. {@code <file>}, or {@code null} for switches
     * @param description the help text, may be {@code null}
     */
    record OptionDefinition(String name, String delimiter,
            @NullAllowed String placeholder, @NullAllowed String description) {
    }

    /**
     * Loads the options supported by the platform's {@code java} launcher.
     *
     * @param platform the platform to query, may be {@code null}
     * @return the options, each one initially without a value; an empty list
     *         if the options cannot be determined (the dialog then offers no
     *         built-in options and only the already present ones)
     */
    static List<JavaVMOption<?>> loadOptions(@NullAllowed JavaPlatform platform) {
        if (platform == null) {
            return List.of();
        }
        Path java = resolveJavaExecutable(platform);
        if (java == null) {
            return List.of();
        }
        // JDK 9 and newer supports the long options, older JDKs the short ones;
        // the other set is tried as a fallback in case the version is wrong.
        boolean modern = isModern(platform);
        String[] first = modern ? new String[] {"--help", "--help-extra"} : new String[] {"-help", "-X"};
        String[] second = modern ? new String[] {"-help", "-X"} : new String[] {"--help", "--help-extra"};
        for (String[] args : new String[][] {first, second}) {
            StringBuilder out = new StringBuilder();
            for (String arg : args) {
                String result = run(java, arg);
                if (result != null) {
                    out.append(result).append('\n');
                }
            }
            List<OptionDefinition> defs = parseHelp(out.toString());
            if (!defs.isEmpty()) {
                return toNodes(defs);
            }
        }
        return List.of();
    }

    /**
     * Whether the platform is a JDK 9 or newer, i.e. supports the long
     * {@code --help}/{@code --help-extra} options.
     */
    private static boolean isModern(JavaPlatform platform) {
        Specification spec = platform.getSpecification();
        return spec != null
                && spec.getVersion() != null
                && spec.getVersion().compareTo(new SpecificationVersion("9")) >= 0;
    }

    /**
     * Resolves the {@code java} executable of the platform: the {@code java}
     * tool if the platform provides one, otherwise the {@code bin/java} of
     * one of the platform's install folders.
     */
    private static Path resolveJavaExecutable(JavaPlatform platform) {
        try {
            FileObject tool = platform.findTool("java"); //NOI18N
            if (tool != null) {
                Path java = FileUtil.toPath(tool);
                if (java != null && Files.exists(java)) {
                    return java;
                }
            }
            String suffix = Utilities.isWindows() ? ".exe" : ""; //NOI18N
            for (FileObject folder : platform.getInstallFolders()) {
                if (folder == null || !folder.isValid()) {
                    continue;
                }
                Path base = FileUtil.toPath(folder);
                if (base == null) {
                    continue;
                }
                Path java = base.resolve("bin").resolve("java" + suffix); //NOI18N
                if (Files.exists(java)) {
                    return java;
                }
            }
        } catch (Exception ex) { //NOI18N
            LOG.log(Level.FINE, "Cannot resolve the java executable of " + platform, ex);
        }
        return null;
    }

    /**
     * Runs the java launcher with the given argument and returns its output
     * (both streams merged).
     *
     * @return the output or {@code null} if the launcher could not be run
     */
    private static String run(Path javaExe, String arg) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(javaExe.toAbsolutePath().toString(), arg);
            pb.redirectErrorStream(true);
            process = pb.start();
            // The help output is small enough to fit into the pipe buffer,
            // so it is safe to read it after the process has finished.
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                LOG.log(Level.FINE, "Timed out running {0} {1}", new Object[] {javaExe, arg});
                return null;
            }
            try (InputStream in = process.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.log(Level.FINE, "Cannot run " + javaExe + " " + arg, ex);
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Parses a help output of the form printed by {@code java --help},
     * {@code java --help-extra} or {@code java -X}.
     *
     * @param output the help output, may be {@code null}
     * @return the options; never {@code null}, may be empty
     */
    static List<OptionDefinition> parseHelp(@NullAllowed String output) {
        if (output == null || output.isBlank()) {
            return List.of();
        }
        Map<String, OptionDefinition> byName = new LinkedHashMap<>();
        String currentName = null;
        OptionDefinition current = null;
        StringBuilder description = null;
        int optionIndent = -1;
        for (String line : output.lines().toList()) {
            if (line.isBlank()) {
                currentName = null;
                current = null;
                description = null;
                optionIndent = -1;
                continue;
            }
            int indent = 0;
            while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                indent++;
            }
            String trimmed = line.substring(indent).strip();
            if (trimmed.startsWith("-")) {
                OptionDefinition def = parseOptionLine(trimmed);
                if (def == null) {
                    currentName = null;
                    current = null;
                    description = null;
                    optionIndent = -1;
                    continue;
                }
                OptionDefinition prev = byName.get(def.name());
                if (prev != null) {
                    def = new OptionDefinition(
                        def.name(),
                        !def.delimiter().isEmpty() ? def.delimiter() : prev.delimiter(),
                        def.placeholder() != null ? def.placeholder() : prev.placeholder(),
                        def.description() != null ? def.description() : prev.description()
                    );
                }
                byName.put(def.name(), def);
                current = def;
                currentName = def.name();
                description = def.description() != null ? new StringBuilder(def.description()) : new StringBuilder();
                optionIndent = indent;
            } else if (optionIndent >= 0 && indent > optionIndent) {
                if (description != null && currentName != null) {
                    if (description.length() > 0) {
                        description.append(' ');
                    }
                    description.append(trimmed);
                    byName.put(
                        currentName,
                        new OptionDefinition(currentName, current.delimiter(), current.placeholder(), description.toString())
                    );
                }
            } else {
                // section header or other non-option line
                currentName = null;
                current = null;
                description = null;
                optionIndent = -1;
            }
        }
        return List.copyOf(byName.values());
    }

    /**
     * Parses a single option line.
     *
     * @param line the trimmed line, first character a hyphen
     * @return the option or {@code null} if the line is not a usable option
     */
    private static OptionDefinition parseOptionLine(String line) {
        // the first whitespace outside a <...> or [...] placeholder
        // separates the option name from the rest of the line
        int sep = -1;
        int depth = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '<' || c == '[') {
                depth++;
            } else if (c == '>' || c == ']') {
                depth = Math.max(0, depth - 1);
            } else if (depth == 0 && (c == ' ' || c == '\t')) {
                sep = i;
                break;
            }
        }
        String token = sep < 0 ? line : line.substring(0, sep);
        String rest = sep < 0 ? "" : line.substring(sep).strip();
        String raw = token.substring(1);
        while (raw.startsWith("-")) {
            raw = raw.substring(1);
        }
        int cut = raw.indexOf('<');
        int bracket = raw.indexOf('[');
        if (bracket >= 0 && (cut < 0 || bracket < cut)) {
            cut = bracket;
        }
        String name = cut < 0 ? raw : raw.substring(0, cut);
        String placeholder = cut < 0 ? null : raw.substring(cut);
        String delimiter = "";
        if (placeholder != null && placeholder.startsWith("[")) {
            // the argument is optional, e.g. -ea[:<packagename>...]: the
            // base form is a plain switch
            while (name.endsWith(":") || name.endsWith("=")) {
                name = name.substring(0, name.length() - 1);
            }
            placeholder = null;
        } else if (placeholder != null && (name.endsWith(":") || name.endsWith("="))) {
            // the placeholder follows directly after the delimiter,
            // e.g. -Xbootclasspath/a:<directories...>
            delimiter = String.valueOf(name.charAt(name.length() - 1));
            name = name.substring(0, name.length() - 1);
        }
        if (name.isEmpty()) {
            return null;
        }
        String description = null;
        if (!rest.isEmpty()) {
            if (rest.startsWith("<") || rest.startsWith("[")) {
                if (placeholder == null) {
                    placeholder = rest;
                }
            } else {
                description = rest;
                if (description.startsWith("|")) {
                    // e.g. -esa | -enablesystemassertions
                    description = description.substring(1).strip();
                }
            }
        }
        return new OptionDefinition(name, delimiter, placeholder, description);
    }

    /**
     * Creates option nodes from the parsed help options.
     *
     * @param defs the definitions obtained from {@link #parseHelp(String)}
     * @return the nodes; each one initially without a value
     */
    static List<JavaVMOption<?>> toNodes(List<OptionDefinition> defs) {
        List<JavaVMOption<?>> result = new ArrayList<>(defs.size());
        for (OptionDefinition def : defs) {
            String name = def.name();
            if ("D".equals(name)) {
                continue; // the -D option is offered by the free form row
            }
            JavaVMOption<?> node;
            if (def.placeholder() == null) {
                node = new SwitchNode(name);
            } else {
                String delimiter = def.delimiter();
                if ("cp".equals(name) || "classpath".equals(name) || "class-path".equals(name)) {
                    // the classpath is the following word, not a : parameter
                    delimiter = " ";
                }
                node = new ParametrizedNode(name, delimiter);
                ParametrizedNode pn = (ParametrizedNode) node;
                pn.setHint(def.placeholder());
            }
            if (def.description() != null && !def.description().isBlank()) {
                node.setDescription(def.description());
            }
            result.add(node);
        }
        return result;
    }

}
