/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
