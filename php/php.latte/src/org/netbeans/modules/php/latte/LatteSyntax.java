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
package org.netbeans.modules.php.latte;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class LatteSyntax {

    public static final Set<String> BLOCK_MACROS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "if", //NOI18N
            "ifset", //NOI18N
            "ifcurrent", //NOI18N
            "foreach", //NOI18N
            "for", //NOI18N
            "while", //NOI18N
            "first", //NOI18N
            "last", //NOI18N
            "sep", //NOI18N
            "capture", //NOI18N
            "cache", //NOI18N
            "syntax", //NOI18N
            "form", //NOI18N
            "label", //NOI18N
            "snippet" //NOI18N
    )));

    public static final Set<String> ELSE_MACROS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "elseif", //NOI18N
            "else", //NOI18N
            "elseifset" //NOI18N
    )));

    public static final Map<String, Set<String>> RELATED_MACROS = Collections.unmodifiableMap(new HashMap<String, Set<String>>() {
        {
            put("if", new HashSet<>(Arrays.asList("else", "elseif"))); //NOI18N
            put("ifset", new HashSet<>(Arrays.asList("else", "elseifset"))); //NOI18N
        }
    });

    private LatteSyntax() {
    }

    public static boolean isBlockMacro(String macro) {
        assert macro != null;
        String macroName = macro.toLowerCase();
        return !macroName.isEmpty()
                && (BLOCK_MACROS.contains(macroName) || BLOCK_MACROS.contains(macroName.substring(1)) || ELSE_MACROS.contains(macroName));
    }

    public static boolean isElseMacro(String macro) {
        assert macro != null;
        String macroName = macro.toLowerCase();
        return ELSE_MACROS.contains(macroName);
    }

    public static boolean isRelatedMacro(String actualMacro, String relatedToMacro) {
        assert actualMacro != null && actualMacro.length() > 0;
        assert relatedToMacro != null;
        return actualMacro.substring(1).equals(relatedToMacro)
                || (RELATED_MACROS.get(relatedToMacro) != null && RELATED_MACROS.get(relatedToMacro).contains(actualMacro));
    }

}
