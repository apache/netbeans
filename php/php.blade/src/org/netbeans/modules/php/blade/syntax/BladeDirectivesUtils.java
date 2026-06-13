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
package org.netbeans.modules.php.blade.syntax;

import org.netbeans.modules.php.blade.editor.directives.DirectivesList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.blade.syntax.annotation.Directive;

/**
 * 
 *
 * @author bhaidu
 */
public final class BladeDirectivesUtils {

    public static final String AT = "@"; // NOI18N
    public static final String END_DIRECTIVE_PREFIX = "@end"; // NOI18N
    public static final String DIRECTIVE_SECTION = "@section"; // NOI18N
    public static final String DIRECTIVE_HAS_SECTION = "@hasSection"; // NOI18N
    public static final String DIRECTIVE_SECTION_MISSING = "@sectionMissing"; // NOI18N
    public static final String DIRECTIVE_ENDSECTION = "@endsection"; // NOI18N
    public static final String DIRECTIVE_SHOW = "@show"; // NOI18N
    public static final String DIRECTIVE_STOP = "@stop"; // NOI18N
    public static final String DIRECTIVE_APPEND = "@append"; // NOI18N
    public static final String DIRECTIVE_OVERWRITE = "@overwrite"; // NOI18N
    public static final String DIRECTIVE_IF = "@if"; // NOI18N
    public static final String DIRECTIVE_ELSEIF = "@elseif"; // NOI18N
    public static final String DIRECTIVE_ELSE = "@else"; // NOI18N
    public static final String DIRECTIVE_ENDIF = "@endif"; // NOI18N
    public static final String DIRECTIVE_FOREACH = "@foreach"; // NOI18N
    public static final String DIRECTIVE_INCLUDE = "@include"; // NOI18N
    public static final String DIRECTIVE_EXTENDS = "@extends"; // NOI18N
    public static final String DIRECTIVE_SESSION = "@session"; // NOI18N
    public static final String DIRECTIVE_CAN = "@can"; // NOI18N

    public static String[] blockDirectiveEndings(String directive) {

        if (directive.equals(DIRECTIVE_SECTION)) {
            return new String[]{DIRECTIVE_ENDSECTION, DIRECTIVE_SHOW, DIRECTIVE_STOP, DIRECTIVE_APPEND, DIRECTIVE_OVERWRITE};
        }
        
        DirectivesList listClass = new DirectivesList();
        for (Directive directiveEl : listClass.getDirectives()) {

            if (!directiveEl.name().equals(directive)) {
                continue;
            }
            if (directiveEl.endtag().isEmpty()) {
                return null;
            }
            return new String[]{directiveEl.endtag()};
        }
        return null;
    }

    @CheckForNull
    public static String[] blockDirectiveOpenings(String directive) {
        switch (directive) {
            case DIRECTIVE_ENDIF -> {
                return new String[]{DIRECTIVE_IF, DIRECTIVE_HAS_SECTION, DIRECTIVE_SECTION_MISSING};
            }
            case DIRECTIVE_ELSEIF -> {
                return new String[]{DIRECTIVE_IF, DIRECTIVE_ELSEIF};
            }
            case DIRECTIVE_ELSE -> {
                return new String[]{DIRECTIVE_IF, DIRECTIVE_ELSEIF, DIRECTIVE_CAN};
            }
            case DIRECTIVE_ENDSECTION, DIRECTIVE_APPEND, DIRECTIVE_STOP, DIRECTIVE_SHOW, DIRECTIVE_OVERWRITE -> {
                return new String[]{DIRECTIVE_SECTION};
            }
        }
        DirectivesList listClass = new DirectivesList();
        for (Directive directiveEl : listClass.getDirectives()) {
            if (directiveEl.endtag().isEmpty()) {
                continue;
            }
            if (directiveEl.endtag().equals(directive)) {
                return new String[]{directiveEl.name()};
            }
        }

        return null;
    }

    public static Set<String> blockDirectiveOpeningsSet(String[] endings) {
        Set<String> result = new HashSet<>();

        for (String endDirective : endings) {
            String[] startDirectives = blockDirectiveOpenings(endDirective);

            if (startDirectives != null) {
                result.addAll(Arrays.asList(startDirectives));
            }
        }
        
        return result;
    }

    public static Set<String> blockDirectiveEndingsSet(String[] openings) {
        Set<String> result = new HashSet<>();

        for (String startDirective : openings) {
            String[] endDirectives = blockDirectiveEndings(startDirective);

            if (endDirectives != null) {
                result.addAll((Arrays.asList(endDirectives)));
            }
        }
        
        return result;
    }
}
