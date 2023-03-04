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

package org.netbeans.modules.maven.osgi.customizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;

/**
 * Utility class to compute instructions from package list and back.
 *
 * @author Dafe Simonek
 */
public final class InstructionsConverter {

    private static final String DELIMITER = ",";
    private static final String ALL_MARK = ".*";

    public static final Integer EXPORT_PACKAGE = 1;
    public static final Integer PRIVATE_PACKAGE = 2;

    public static Map<Integer, String> computeExportInstructions (Map<String, Boolean> items, Project project) {
        Map<Integer, String> instructionsMap = new HashMap<Integer, String>(2);
        StringBuilder exportIns = new StringBuilder();
        boolean isFirst = true;
        for (Entry<String, Boolean> entry : items.entrySet()) {
            if (entry.getValue()) {
                if (!isFirst) {
                    exportIns.append(DELIMITER);
                }
                exportIns.append(entry.getKey());
                isFirst = false;
            }
        }
        instructionsMap.put(EXPORT_PACKAGE, exportIns.toString());

        Iterator<String> baseNames = FileUtilities.getBasePackageNames(project).iterator();
        StringBuilder privateIns = new StringBuilder();
        while (baseNames.hasNext()) {
            String baseName = baseNames.next();
            privateIns.append(baseName);
            if (baseNames.hasNext()) {
                privateIns.append(ALL_MARK + DELIMITER);
            } else {
                privateIns.append(ALL_MARK);
            }
        }
        String privateText = privateIns.toString();
        if (!privateText.equals("..*")) {
            instructionsMap.put(PRIVATE_PACKAGE, privateText);
        }

        return instructionsMap;
    }

    public static SortedMap<String, Boolean> computeExportList (Map<Integer, String> exportInstructions, Project project) {
        SortedMap<String, Boolean> pkgMap = new TreeMap<String, Boolean>();
        SortedSet<String> pkgNames = FileUtilities.getPackageNames(project);
        for (String name : pkgNames) {
            pkgMap.put(name, Boolean.FALSE);
        }
        String exportIns = exportInstructions.get(EXPORT_PACKAGE);
        if (exportIns != null) {
            StringTokenizer strTok = new StringTokenizer(exportIns, DELIMITER);

            while(strTok.hasMoreTokens()) {
                String cur = strTok.nextToken();
                pkgMap.remove(cur);
                pkgMap.put(cur, Boolean.TRUE);
            }
        }

        return pkgMap;
    }

    public static String computeEmbedInstruction (Map<String, Boolean> items) {
        // TBD
        return null;
    }

    public static Map<String, Boolean> computeEmbedList (String embedInstruction) {
        // TBD
        return null;
    }


}
