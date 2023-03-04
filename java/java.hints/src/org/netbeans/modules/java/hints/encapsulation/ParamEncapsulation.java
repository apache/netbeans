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
