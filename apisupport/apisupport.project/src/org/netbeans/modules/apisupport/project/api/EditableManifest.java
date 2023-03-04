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

package org.netbeans.modules.apisupport.project.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Similar to {@link java.util.jar.Manifest} but preserves all formatting when changes are made.
 * Methods which take a section name can accept null for the main section.
 * (This style is in contrast to that used in {@link java.util.jar.Attributes}.
 * Does not implement esoteric aspects of the manifest spec such as signature recognition
 * and line wrapping after 72 characters, but does produce output which can be read
 * by {@link java.util.jar.Manifest} and Ant's <code>&lt;jar&gt;</code> task.
 * Will not touch the formatting of any line unless you ask to change it, nor
 * reorder lines, etc., except to correct line endings or insert a final newline.
 * Newly added sections and attributes are inserted in alphabetical order.
 * @author Jesse Glick
 */
public final class EditableManifest {
    
    private static final String MANIFEST_VERSION = "Manifest-Version"; // NOI18N
    private static final String MANIFEST_VERSION_VALUE = "1.0"; // NOI18N
    
    private final Section mainSection;
    private final List<Section> sections;
    
    /**
     * Creates an almost empty manifest.
     * Contains just <code>Manifest-Version: 1.0</code>.
     */
    public EditableManifest() {
        try {
            mainSection = new Section(Collections.singletonList(new Line(MANIFEST_VERSION, MANIFEST_VERSION_VALUE)), true, 1);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        sections = new ArrayList<Section>();
    }
    
    /**
     * Creates a manifest object from an existing manifest file.
     * @param is a stream to load content from (in UTF-8 encoding)
     * @throws IOException if reading the stream failed, or the contents were syntactically malformed
     */
    public EditableManifest(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        sections = new LinkedList<Section>();
        String text;
        int blankLines = 0;
        List<Line> lines = new ArrayList<Line>();
        Section _mainSection = null;
        while (true) {
            text = r.readLine();
            if (text == null || (text.length() > 0 && blankLines > 0)) {
                Section s = new Section(lines, _mainSection == null, blankLines);
                if (_mainSection == null) {
                    _mainSection = s;
                } else {
                    sections.add(s);
                }
                lines.clear();
                blankLines = 0;
            }
            if (text != null) {
                if (text.length() > 0) {
                    Line line;
                    if (text.charAt(0) == ' ') {
                        if (lines.isEmpty()) {
                            throw new IOException("Continuation lines only allowed for attributes"); // NOI18N
                        }
                        Line prev = lines.remove(lines.size() - 1);
                        line = new Line(prev.name, prev.value + text.substring(1), prev.text + System.getProperty("line.separator") + text);
                    } else {
                        line = new Line(text);
                    }
                    lines.add(line);
                } else {
                    blankLines++;
                }
            } else {
                break;
            }
        }
        mainSection = _mainSection;
        Set<String> names = new HashSet<String>();
        for (Section s : sections) {
            if (!names.add(s.name)) {
                throw new IOException("Duplicated section names: " + s.name); // NOI18N
            }
        }
    }
    
    /**
     * Stores the manifest to a file.
     * @param os a stream to write content to (in UTF-8 encoding, using platform default line endings)
     * @throws IOException if writing to the stream failed
     */
    public void write(OutputStream os) throws IOException {
        Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        mainSection.write(w, !sections.isEmpty());
        Iterator<Section> it = sections.iterator();
        while (it.hasNext()) {
            it.next().write(w, it.hasNext());
        }
        w.flush();
    }
    
    /**
     * Adds a new section.
     * It will be added in alphabetical order relative to other sections, if they
     * are already alphabetized.
     * @param name the new section name
     * @throws IllegalArgumentException if a section with that name already existed
     */
    public void addSection(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (findSection(name) != null) {
            throw new IllegalArgumentException(name);
        }
        int i;
        for (i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            if (s.name.compareTo(name) > 0) {
                break;
            }
        }
        sections.add(i, new Section(name));
    }

    /**
     * Removes a section.
     * @param name the section name to delete
     * @throws IllegalArgumentException if there was no such section
     */
    public void removeSection(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        Iterator<Section> it = sections.iterator();
        while (it.hasNext()) {
            Section s = it.next();
            if (s.name.equals(name)) {
                it.remove();
                return;
            }
        }
        throw new IllegalArgumentException(name);
    }
    
    /**
     * Gets a list of all named sections (not including the main section).
     * @return a list of section names
     */
    public Set<String> getSectionNames() {
        Set<String> names = new HashSet<String>();
        for (Section s : sections) {
            names.add(s.name);
        }
        return names;
    }
    
    private Section findSection(String section) {
        if (section == null) {
            return mainSection;
        } else {
            for (Section s : sections) {
                if (s.name.equals(section)) {
                    return s;
                }
            }
            return null;
        }
    }
    
    /**
     * Find the value of an attribute.
     * @param name the attribute name (case-insensitive)
     * @param section the name of the section to look in, or null for the main section
     * @return the attribute value, or null if not defined
     * @throws IllegalArgumentException if the named section does not exist
     */
    public String getAttribute(String name, String section) throws IllegalArgumentException {
        Section s = findSection(section);
        if (s == null) {
            throw new IllegalArgumentException(section);
        }
        return s.getAttribute(name);
    }
    
    /**
     * Changes the value of an attribute, or adds the attribute if it does not yet exist.
     * If it is being added, it will be added in alphabetical order relative to
     * other attributes in the same section, if they are already alphabetized.
     * @param name the attribute name (case-insensitive if it already exists)
     * @param value the new attribute value
     * @param section the name of the section to add it to, or null for the main section
     * @throws IllegalArgumentException if the named section does not exist
     */
    public void setAttribute(String name, String value, String section) throws IllegalArgumentException {
        Section s = findSection(section);
        if (s == null) {
            throw new IllegalArgumentException(section);
        }
        s.setAttribute(name, value);
    }
    
    /**
     * Removes an attribute.
     * @param name the attribute name to delete (case-insensitive)
     * @param section the name of the section to remove it from, or null for the main section
     * @throws IllegalArgumentException if the named section or attribute do not exist
     */
    public void removeAttribute(String name, String section) throws IllegalArgumentException {
        Section s = findSection(section);
        if (s == null) {
            throw new IllegalArgumentException(section);
        }
        s.removeAttribute(name);
    }
    
    /**
     * Gets a list of all attributes.
     * @param section the name of the section to examine, or null for the main section
     * @throws IllegalArgumentException if the named section does not exist
     */
    public Set<String> getAttributeNames(String section) throws IllegalArgumentException {
        Section s = findSection(section);
        if (s == null) {
            throw new IllegalArgumentException(section);
        }
        return s.getAttributeNames();
    }
    
    private static final class Line {
        
        private static final Pattern NAME_VALUE = Pattern.compile("([^: ]+) *: *(.*)"); // NOI18N
        
        public final String text;
        public final String name;
        public final String value;
        
        public Line(String text) throws IOException {
            this.text = text;
            assert text.length() > 0;
            Matcher m = NAME_VALUE.matcher(text);
            if (m.matches()) {
                name = m.group(1);
                value = m.group(2);
            } else {
                throw new IOException("Malformed line: " + text); // NOI18N
            }
        }
        
        public Line(String name, String value) {
            this(name, value, name + ": " + value); // NOI18N
        }
        
        public Line(String name, String value, String text) {
            this.name = name;
            this.value = value;
            this.text = text;
        }

        private static final Pattern NEWLINE = Pattern.compile("\r?\n");

        public void write(Writer w) throws IOException {
            // translating all newlines to correct format,
            // see SingleModulePropertiesTest#testThatManifestFormattingIsNotMessedUp_61248
            String output = NEWLINE.matcher(text).replaceAll(RET);
            w.write(output);
            newline(w);
        }
        
    }
    
    private static final String RET = System.getProperty("line.separator");
    
    private static void newline(Writer w) throws IOException {
        w.write(RET); // NOI18N
    }
    
    private static final class Section {
        
        private static final String NAME = "Name"; // NOI18N
        
        public final String name;
        private final List<Line> lines;
        private final int blankLinesAfter;
        
        public Section(List<Line> lines, boolean main, int blankLinesAfter) throws IOException {
            this.lines = new ArrayList<Line>(lines);
            this.blankLinesAfter = blankLinesAfter;
            if (main) {
                name = null;
                if (!lines.isEmpty()) {
                    Line first = lines.get(0);
                    if (first.name.equalsIgnoreCase(NAME)) {
                        throw new IOException("Cannot start with a named section"); // NOI18N
                    }
                }
            } else {
                assert !lines.isEmpty();
                Line first = lines.get(0);
                if (!first.name.equalsIgnoreCase(NAME)) {
                    throw new IOException("Section did not start with " + NAME); // NOI18N
                }
                name = first.value;
                if (name.length() == 0) {
                    throw new IOException("Cannot have a blank section name"); // NOI18N
                }
            }
            Set<String> attrNames = new HashSet<String>();
            Iterator<Line> it = lines.iterator();
            if (!main) {
                it.next();
            }
            while (it.hasNext()) {
                String name = it.next().name;
                if (name.equals(NAME)) {
                    throw new IOException("Sections not separated by blank lines"); // NOI18N
                } else if (!attrNames.add(name.toLowerCase(Locale.US))) {
                    throw new IOException("Duplicated attributes in a section: " + name); // NOI18N
                }
            }
        }
        
        public Section(String name) {
            this.name = name;
            lines = new ArrayList<Line>();
            lines.add(new Line(NAME, name)); // NOI18N
            blankLinesAfter = 1;
        }
        
        private Line findAttribute(String name) {
            Iterator<Line> it = lines.iterator();
            if (this.name != null) {
                it.next();
            }
            while (it.hasNext()) {
                Line line = it.next();
                if (line.name.equalsIgnoreCase(name)) {
                    return line;
                }
            }
            return null;
        }
        
        private int findAttributeIndex(String name) {
            for (int i = (this.name != null ? 1 : 0); i < lines.size(); i++) {
                Line line = lines.get(i);
                if (line.name.equalsIgnoreCase(name)) {
                    return i;
                }
            }
            return -1;
        }
        
        public String getAttribute(String name) {
            Line line = findAttribute(name);
            if (line != null) {
                return line.value;
            } else {
                return null;
            }
        }
        
        public void setAttribute(String name, String value) {
            for (int i = (this.name != null ? 1 : 0); i < lines.size(); i++) {
                Line line = lines.get(i);
                if (name.equalsIgnoreCase(line.name)) {
                    if (line.value.equals(value)) {
                        // No change, leave alone to preserve formatting.
                        return;
                    }
                    // Edit this line.
                    lines.remove(i);
                    int insertionPoint = name.equalsIgnoreCase(MANIFEST_VERSION) ? 0 : i;
                    lines.add(insertionPoint, new Line(name, value));
                    return;
                }
            }
            // Didn't find an existing line. Look for the right place to insert this one.
            int insertionPoint;
            if (name.equalsIgnoreCase(MANIFEST_VERSION)) {
                insertionPoint = 0;
            } else {
                insertionPoint = lines.size();
                for (int i = (this.name != null ? 1 : 0); i < lines.size(); i++) {
                    Line line = lines.get(i);
                    int comp = line.name.compareToIgnoreCase(name);
                    assert comp != 0;
                    if (comp > 0 && !line.name.equalsIgnoreCase(MANIFEST_VERSION)) {
                        insertionPoint = i;
                        break;
                    }
                }
            }
            lines.add(insertionPoint, new Line(name, value));
        }
        
        public void removeAttribute(String name) throws IllegalArgumentException {
            int i = findAttributeIndex(name);
            if (i != -1) {
                lines.remove(i);
            } else {
                throw new IllegalArgumentException(name);
            }            
        }
        
        public Set<String> getAttributeNames() {
            Set<String> attrNames = new HashSet<String>();
            Iterator<Line> it = lines.iterator();
            if (name != null) {
                it.next();
            }
            while (it.hasNext()) {
                attrNames.add(it.next().name);
            }
            return attrNames;
        }
        
        public void write(Writer w, boolean forceBlankLine) throws IOException {
            Iterator<Line> it = lines.iterator();
            while (it.hasNext()) {
                Line line = it.next();
                line.write(w);
            }
            for (int i = 0; i < blankLinesAfter; i++) {
                newline(w);
            }
            if (forceBlankLine && blankLinesAfter == 0) {
                newline(w);
            }
        }
        
    }
    
}
