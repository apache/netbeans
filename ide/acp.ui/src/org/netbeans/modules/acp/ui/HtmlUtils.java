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
package org.netbeans.modules.acp.ui;

public final class HtmlUtils {

    private HtmlUtils() {
    }

    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static String extractProjectName(String text) {
        if (text == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("Project name[*]*:\\s*[*]*(.+?)(?:\\*|$)",
                        java.util.regex.Pattern.MULTILINE)
                .matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    public static String extractProjectDirectory(String text) {
        if (text == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("Project directory[*]*:\\s*[*]*(.+?)(?:\\*|$)",
                        java.util.regex.Pattern.MULTILINE)
                .matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    public static java.util.List<String> extractSourceRoots(String text) {
        if (text == null) {
            return java.util.List.of();
        }
        java.util.List<String> roots = new java.util.ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^\\s+-\\s+`(.+?)`(?:\\s+\\((.+?)\\))?",
                        java.util.regex.Pattern.MULTILINE)
                .matcher(text);
        while (m.find()) {
            String path = m.group(1).trim();
            String name = m.group(2);
            roots.add(name != null ? path + " (" + name.trim() + ")" : path);
        }
        return roots;
    }
}
