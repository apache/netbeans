/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.twig.editor;

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
public final class TwigSyntax {
    private static final int END_PREFIX_LENGTH = "end".length(); //NOI18N
    public static final Set<String> BLOCK_MACROS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "if", //NOI18N
            "for", //NOI18N
            "block", //NOI18N
            "set", //NOI18N
            "macro", //NOI18N
            "filter", //NOI18N
            "autoescape", //NOI18N
            "spaceless", //NOI18N
            "embed", //NOI18N
            "raw", //NOI18N
            "verbatim", //NOI18N
            "sandbox", //NOI18N
            "trans" //NOI18N
    )));

    public static final Set<String> ELSE_MACROS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "elseif", //NOI18N
            "else" //NOI18N
    )));

    public static final Map<String, Set<String>> RELATED_MACROS = Collections.unmodifiableMap(new HashMap<String, Set<String>>() {
        {
            put("if", new HashSet<>(Arrays.asList("else", "elseif"))); //NOI18N
            put("for", new HashSet<>(Arrays.asList("else"))); //NOI18N
        }
    });

    private TwigSyntax() {
    }

    public static boolean isBlockMacro(String macro) {
        assert macro != null;
        String macroName = macro.toLowerCase();
        return !macroName.isEmpty()
                && (BLOCK_MACROS.contains(macroName)
                    || (macroName.length() > END_PREFIX_LENGTH && BLOCK_MACROS.contains(macroName.substring(END_PREFIX_LENGTH)))
                    || ELSE_MACROS.contains(macroName));
    }

    public static boolean isElseMacro(String macro) {
        assert macro != null;
        String macroName = macro.toLowerCase();
        return ELSE_MACROS.contains(macroName);
    }

    public static boolean isRelatedMacro(String actualMacro, String relatedToMacro) {
        assert actualMacro != null && actualMacro.length() > 0;
        assert relatedToMacro != null;
        return (actualMacro.length() > END_PREFIX_LENGTH && actualMacro.substring(END_PREFIX_LENGTH).equals(relatedToMacro))
                || (RELATED_MACROS.get(relatedToMacro) != null && RELATED_MACROS.get(relatedToMacro).contains(actualMacro));
    }

}
