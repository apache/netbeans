/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "DN_containsForIndexOf=String.indexOf can be replaced with String.contains",
    "DESC_containsForIndexOf=Finds usages of String.indexOf that can be replaced with String.contains",
    "ERR_containsForIndexOf=String.indexOf can be replaced with String.contains",
    "FIX_containsForIndexOf=String.indexOf can be replaced with String.contains"
})
@Hint(displayName="#DN_containsForIndexOf", description="#DESC_containsForIndexOf", category="rules15", suppressWarnings="IndexOfReplaceableByContains")
public class IndexOfToContains {
    
    @TriggerPatterns({
        @TriggerPattern(value="$site.indexOf($substring) != (-1)", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) >= (0)", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) > (-1)", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) != -1", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) >= 0", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) > -1", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        })
    })
    public static ErrorDescription containsForIndexOf(HintContext ctx) {
        String target = "$site.contains($substring)";
        
        Fix fix = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_containsForIndexOf(), ctx.getPath(), target);
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_containsForIndexOf(), fix);
    }

    @TriggerPatterns({
        @TriggerPattern(value="$site.indexOf($substring) == (-1)", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) <= (-1)", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) < (0)", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) == -1", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) <= -1", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        }),
        @TriggerPattern(value="$site.indexOf($substring) < 0", constraints = {
            @ConstraintVariableType(variable = "$substring", type = "java.lang.String"),
            @ConstraintVariableType(variable = "$site", type = "java.lang.String"),
        })
    })
    public static ErrorDescription notContainsForIndexOf(HintContext ctx) {
        String target = "!$site.contains($substring)";
        
        Fix fix = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_containsForIndexOf(), ctx.getPath(), target);
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_containsForIndexOf(), fix);
    }
}
