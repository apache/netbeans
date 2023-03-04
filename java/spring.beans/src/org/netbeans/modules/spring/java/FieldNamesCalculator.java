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
