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

package org.netbeans.modules.java.hints.encapsulation;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class ParamEncapsulation {

    private static final String COLLECTION = "java.util.Collection";    //NOI18N
    private static final String MAP = "java.util.Map";                  //NOI18N
    private static final String DATE = "java.util.Date";                //NOI18N
    private static final String CALENDAR="java.util.Calendar";          //NOI18N
    private static final String A_OBJ = "java.lang.Object[]";           //NOI18N
    private static final String A_BOOL = "boolean[]";                   //NOI18N
    private static final String A_BYTE = "byte[]";                      //NOI18N
    private static final String A_CHAR = "char[]";                      //NOI18N
    private static final String A_SHORT = "short[]";                    //NOI18N
    private static final String A_INT = "int[]";                        //NOI18N
    private static final String A_LONG = "long[]";                      //NOI18N
    private static final String A_FLOAT = "float[]";                    //NOI18N
    private static final String A_DOUBLE = "double[]";                  //NOI18N

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.ParamEncapsulation.collection", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.ParamEncapsulation.collection", category="encapsulation",                                     //NOI18N
        suppressWarnings={"AssignmentToCollectionOrArrayFieldFromParameter"},   //NOI18N
        enabled=false,
        options=Options.QUERY)
    @TriggerPatterns ({
        @TriggerPattern(value="$var=$expr",                             //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=COLLECTION)           //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",                             //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=MAP)                  //NOI18N
        })
    })
    public static ErrorDescription collection(final HintContext ctx) {
        assert ctx != null;
        return create (ctx,
            NbBundle.getMessage(ParamEncapsulation.class, "TXT_AssignmentToCollection"),
            "AssignmentToCollectionOrArrayFieldFromParameter"); //NOI18N
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.ParamEncapsulation.array", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.ParamEncapsulation.array", category="encapsulation",                                             //NOI18N
        suppressWarnings={"AssignmentToCollectionOrArrayFieldFromParameter"},   //NOI18N
        enabled=false,
        options=Options.QUERY)
    @TriggerPatterns ({
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_OBJ)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_BOOL)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_BYTE)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_CHAR)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_SHORT)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_INT)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_LONG)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_FLOAT)   //NOI18N
            }
        ),
        @TriggerPattern(value="$var=$expr",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=A_DOUBLE)   //NOI18N
            }
        )
    })
    public static ErrorDescription array(final HintContext ctx) {
        assert ctx != null;
        return create (ctx,
            NbBundle.getMessage(ParamEncapsulation.class, "TXT_AssignmentToArray"),
            "AssignmentToCollectionOrArrayFieldFromParameter"); //NOI18N
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.encapsulation.ParamEncapsulation.date", description = "#DESC_org.netbeans.modules.java.hints.encapsulation.ParamEncapsulation.date", category="encapsulation",
        suppressWarnings={"AssignmentToDateFieldFromParameter"},
        enabled=false,
        options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="$var=$expr",   //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=DATE)   //NOI18N
        }),
        @TriggerPattern(value="$var=$expr",   //NOI18N
            constraints={
                @ConstraintVariableType(variable="$expr",type=CALENDAR)   //NOI18N
        })
    })
    public static ErrorDescription date(final HintContext ctx) {
        assert ctx != null;
        return create (ctx,
            NbBundle.getMessage(ParamEncapsulation.class, "TXT_AssignmentToDate"),
            "AssignmentToDateFieldFromParameter"); //NOI18N
    }

    private static ErrorDescription create (final HintContext ctx,
        final String description, final String suppressWarnings) {
        assert ctx != null;
        final TreePath varPath = ctx.getVariables().get("$var");    //NOI18N
        assert varPath != null;
        final Trees trees = ctx.getInfo().getTrees();
        final Element varElm = trees.getElement(varPath);
        if (varElm == null || varElm.getKind() != ElementKind.FIELD) {
            return null;
        }
        final Element varOwner = varElm.getEnclosingElement();
        if (varOwner == null || !varOwner.getKind().isClass()) {
            return null;
        }
        final TreePath exprPath = ctx.getVariables().get("$expr");  //NOI18N
        final Element exprElm = trees.getElement(exprPath);
        if (exprElm == null || exprElm.getKind() != ElementKind.PARAMETER) {
            return null;
        }
        final Element exprOwner = exprElm.getEnclosingElement();
        if (exprOwner == null ||
            exprOwner.getKind() != ElementKind.METHOD ||
            !((ExecutableElement)exprOwner).getParameters().contains(exprElm) ||
            varOwner != exprOwner.getEnclosingElement()) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, varPath,
            description);
    }
}
