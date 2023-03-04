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
package org.netbeans.modules.java.hints.spiimpl.pm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.spiimpl.Hacks;

/**
 *
 * @author lahvac
 */
public class PatternCompilerUtilities {

    public static Pattern compile(CompilationInfo info, String pattern) {
        Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();
        pattern = parseOutTypesFromPattern(info, pattern, constraints);

        return PatternCompiler.compile(info, pattern, constraints, Collections.<String>emptyList());
    }

    public static String parseOutTypesFromPattern(CompilationInfo info, String pattern, Map<String, TypeMirror> variablesToTypes) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\$[0-9])(\\{([^}]*)\\})?");
        StringBuilder filtered = new StringBuilder();
        Matcher m = p.matcher(pattern);
        int i = 0;

        while (m.find()) {
            filtered.append(pattern.substring(i, m.start()));
            i = m.end();

            String var  = m.group(1);
            String type = m.group(3);

            filtered.append(var);
            variablesToTypes.put(var, type != null ? Hacks.parseFQNType(info, type) : null);
        }

        filtered.append(pattern.substring(i));

        return filtered.toString();
    }
}
