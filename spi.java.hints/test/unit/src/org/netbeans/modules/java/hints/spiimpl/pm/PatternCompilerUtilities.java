/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
