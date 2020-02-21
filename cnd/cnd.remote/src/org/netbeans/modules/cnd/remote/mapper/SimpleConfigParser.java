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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;

/**
 * Simple parser for *.ini style configuration files
 *
 */
public class SimpleConfigParser {

    private final Map<String, Map<String, String>> sections2attributes = new HashMap<>();
    private final Map<String, String> orphanAttributes = new HashMap<>();
    private final Pattern patternSection = Pattern.compile("\\[.+\\]"); //NOI18N
    private final Pattern patternAttribute = Pattern.compile("[^=]+=[^=]+"); //NOI18N
    private boolean parsed = false;

    public SimpleConfigParser() {
    }

    public boolean parse(Reader text) {
        if (parsed) {
            return false;
        }
        try {
            BufferedReader reader = new BufferedReader(text);

            String currentSection = null;
            for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
                line = line.trim();
                if (line.length() > 0) {
                    if (patternAttribute.matcher(line).matches()) {
                        int idxEq = line.indexOf('=');
                        String name = line.substring(0, idxEq).trim();
                        String value = line.substring(idxEq+1).trim();
                        if (currentSection == null) {
                            orphanAttributes.put(name, value);
                        } else {
                            sections2attributes.get(currentSection).put(name, value);
                        }
                    } else if (patternSection.matcher(line).matches()) {
                        // parse section name
                        currentSection = line.substring(1, line.length() - 1).trim();
                        if (!sections2attributes.containsKey(currentSection)) {
                            sections2attributes.put(currentSection, new HashMap<String, String>());
                        }
                    }
                }
            }
            parsed = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return parsed;
    }

    public Set<String> getSections() {
        return parsed ? sections2attributes.keySet() : Collections.<String>emptySet();
    }

    public Map<String, String> getAttributes(String section) {
        return parsed ? sections2attributes.get(section) : Collections.<String, String>emptyMap();
    }

    public Map<String, String> getOrphanAttributes() {
        return parsed ? orphanAttributes : Collections.<String, String>emptyMap();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        writeAttributes(sb, getOrphanAttributes());
        for (String section : getSections()) {
            Map<String, String> attributes = getAttributes(section);
            sb.append("[" + section + "]\n"); // empty sections are ok? //NOI18N
            writeAttributes(sb, attributes);
        }
        return sb.toString();
    }

    private static void writeAttributes(StringBuilder sb, Map<String, String> attributes) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append("\t" + entry.getKey() + "=" + entry.getValue() + "\n"); //NOI18N
        }
    }
}
