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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
