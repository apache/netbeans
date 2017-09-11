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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
