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
package org.netbeans.modules.spring.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.spring.beans.utils.StringUtils;

/**
 *
 * @author Rohan Ranade
 */
public final class FieldNamesCalculator {

    private final String typeName;
    private final Set<String> forbidden;
    private static Pattern pattern = Pattern.compile("(\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\d|\\-)*)"); // NOI18N

    public FieldNamesCalculator(String typeName, Set<String> forbidden) {
        this.typeName = typeName;
        this.forbidden = forbidden;
    }

    public List<String> calculate() {
        List<String> nameBlocks = new ArrayList<String>();
        LinkedHashSet<String> names = new LinkedHashSet<String>();
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.find()) {
            int idx = matcher.start();
            if (idx > 0) {
                String prefix = typeName.substring(0, idx);
                nameBlocks.add(prefix);
            }

            String group = matcher.group();
            nameBlocks.add(group);

            while (matcher.find()) {
                group = matcher.group();
                nameBlocks.add(group);
            }

            String[] blocks = nameBlocks.toArray(new String[0]);
            for (int i = 0; i < blocks.length; i++) {
                StringBuilder sb = new StringBuilder(StringUtils.toLowerCamelCase(blocks[i]));
                for (int j = i + 1; j < blocks.length; j++) {
                    names.add(findFreeFieldName(sb.toString()));
                    sb.append(blocks[j]);
                }
                names.add(findFreeFieldName(sb.toString()));
            }
        } else {
            names.add(findFreeFieldName(typeName));
        }

        return Collections.unmodifiableList(new ArrayList<String>(names));
    }

    private String findFreeFieldName(String origName) {
        if (!forbidden.contains(origName)) {
            return origName;
        }

        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            String newName = origName + Integer.toString(i);
            if (!forbidden.contains(newName)) {
                return newName;
            }
        }

        return null;
    }
}
