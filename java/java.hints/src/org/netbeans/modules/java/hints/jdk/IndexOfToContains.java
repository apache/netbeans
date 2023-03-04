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
