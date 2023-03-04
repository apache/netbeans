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

package org.netbeans.modules.java.hints.perf;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.NoBooleanConstructor", description = "#DESC_org.netbeans.modules.java.hints.perf.NoBooleanConstructor", category="performance", suppressWarnings="BooleanConstructorCall")
public class NoBooleanConstructor {

    @TriggerPattern(value="new java.lang.Boolean($b)", constraints=@ConstraintVariableType(variable="$b", type="boolean"))
    public static ErrorDescription hintBoolean(HintContext ctx) {
        switch (ctx.getInfo().getSourceVersion()) {
            case RELEASE_0:
            case RELEASE_1:
            case RELEASE_2:
            case RELEASE_3:
                return hint(ctx, "($b ? Boolean.TRUE : Boolean.FALSE)", "FIX_NoBooleanConstructorBoolean");
            case RELEASE_4:
                return hint(ctx, "java.lang.Boolean.valueOf($b)", "FIX_NoBooleanConstructorBoolean");
            default:
                return hint(ctx, "$b", "FIX_NoBooleanConstructorBoolean");
        }
    }

    @TriggerPattern(value="new java.lang.Boolean($str)", constraints=@ConstraintVariableType(variable="$str", type="java.lang.String"))
    public static ErrorDescription hintString(HintContext ctx) {
        return hint(ctx, "java.lang.Boolean.valueOf($str)", "FIX_NoBooleanConstructorString");
    }

    private static ErrorDescription hint(HintContext ctx, String fix, String fixKey) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, fixKey);
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), fix);
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_NoBooleanConstructor");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }
}
