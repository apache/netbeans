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

package org.netbeans.modules.nativeexecution.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Utilities;

/**
 * A common class for reading rc files in format
 *      # comment
 *      [section1]
 *      key1=value1
 *      key2=value2
 *      ...
 * @author vk155633
 */
public final class RcFile {

    public static class FormatException extends Exception {
        public FormatException(String message) {
            super(message);
        }

    }

    private class Section {

        public final String name;
        private final Map<String, String> map = new TreeMap<>();

        public Section(String name) throws IOException {
            this.name = name;
        }

        public synchronized  String get(String key, String defaultValue) {
            if (map.containsKey(key)) {
                return map.get(key);
            } else {
                return defaultValue;
            }
        }

        public synchronized  Collection<String> getKeys() {
            return new ArrayList<>(map.keySet());
        }

        public synchronized boolean containsKey(String key) {
            return map.containsKey(key);
        }

        private synchronized void put(String key, String value) {
            map.put(key, value);
        }
    }

    private final Map<String, Section> sections = new TreeMap<>();
    private final File file;

    public synchronized String get(String section, String key, String defaultValue) {
        Section sect = sections.get(section);
        return (sect == null) ? defaultValue : sect.get(key, defaultValue);
    }

    public synchronized int get(String section, String key, int defaultValue) {
        String stringValue = get(section, key, "" + defaultValue); //NOI18N
        return Integer.parseInt(stringValue);
    }

    public boolean get(String section, String key, boolean defaultValue) {
        String stringValue = get(section, key, "" + defaultValue); //NOI18N
        return Boolean.valueOf(stringValue);
    }

    public String get(String section, String key) {
        return get(section, key, null);
    }

    public boolean containsKey(String section, String key) {
        Section sect = sections.get(section);
        return (sect == null) ? false : sect.containsKey(key);
    }

    public synchronized Collection<String> getSections() {
        List<String> result = new ArrayList<>();
        for (Section section : sections.values()) {
            result.add(section.name);
        }
        return result;
    }

    public synchronized Collection<String> getKeys(String section) {
        Section sect = sections.get(section);
        return (sect == null) ? Collections.<String>emptyList() : sect.getKeys();
    }

    public static RcFile createDummy() throws IOException, FormatException {
        return new RcFile(new File(Utilities.isWindows() ? "NUL" : "/dev/null"), false);
    }

    public static RcFile create(File file) throws IOException, FormatException {
        return new RcFile(file, true);
    }

    private RcFile(File file, boolean read) throws IOException, FormatException {
        this.file = file;
        try {
            if (read) {
                read();
            }
        } catch (FileNotFoundException e) {
            // no rcFile, no problems ;-)
        }
    }

    private void read() throws IOException, FormatException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String str;
        Pattern commentPattern = Pattern.compile("(#.*)|([ \t]*)"); // NOI18N
        Pattern sectionPattern = Pattern.compile("\\[(.*)\\] *"); // NOI18N
        Pattern valuePattern = Pattern.compile("([^=]+)=(.*)"); //NOI18N
        Pattern justKeyPattern = Pattern.compile("[^=]+"); //NOI18N
        Section currSection = new Section(""); // default section
        while ((str = reader.readLine()) != null) {
            if (commentPattern.matcher(str).matches()) {
                continue;
            }
            if (sectionPattern.matcher(str).matches()) {
                str = str.trim();
                String name = str.substring(1, str.length()-1);
                currSection = new Section(name);
                sections.put(name, currSection);
            } else {
                Matcher m = valuePattern.matcher(str);
                if (m.matches()) {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    currSection.put(key, value);
                } else {
                    if (justKeyPattern.matcher(str).matches()) {
                        String key = str.trim();
                        String value = null;
                        currSection.put(key, value);
                    } else {
                        throw new FormatException(str);
                    }
                }
            }
        }
        reader.close();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + file.getAbsolutePath();
    }

    public synchronized  void dump() {
        dump(System.out);
    }

    public synchronized  void dump(PrintStream ps) {
        for(Section section : sections.values()) {
            ps.printf("[%s]\n", section.name);
            for (String key : section.getKeys()) {
                String value = section.get(key, null);
                ps.printf("%s=%s\n", key, value);
            }
        }
    }

}
