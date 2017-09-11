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
package org.netbeans.modules.java.hints.testing;

import com.sun.source.util.TreePath;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({"DN_assertEqualsForArrays=assertEquals for array parameters",
           "DESC_assertEqualsForArrays=Warns about assertEquals whose parameters are arrays",
           "ERR_assertEqualsForArrays=Invoking Assert.assertEquals on arrays",
           "FIX_assertEqualsForArrays=Use Assert.assertArrayEquals",
           "DN_assertEqualsMismatchedConstantVSReal=Incorrect order of parameters of Assert.assertEquals",
           "DESC_assertEqualsMismatchedConstantVSReal=Incorrect order of parameters of Assert.assertEquals",
           "ERR_assertEqualsMismatchedConstantVSReal=Order of parameters of Assert.assertEquals incorrect",
           "FIX_assertEqualsMismatchedConstantVSReal=Flip parameters of Assert.assertEquals",
           "DN_assertEqualsIncovertibleTypes=Inconvertible parameters of Assert.assertEquals",
           "DESC_assertEqualsIncovertibleTypes=Inconvertible parameters of Assert.assertEquals",
           "ERR_assertEqualsIncovertibleTypes=The parameters of Assert.assertEquals are of inconvertible types"
})
public class Tiny {

    @Hint(displayName="#DN_assertEqualsForArrays",
          description="#DESC_assertEqualsForArrays",
          category="testing",
          suppressWarnings="AssertEqualsCalledOnArray")
    @TriggerPatterns({
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="java.lang.Object[]"),
                                     @ConstraintVariableType(variable="$actual", type="java.lang.Object[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="java.lang.Object[]"),
                                     @ConstraintVariableType(variable="$actual", type="java.lang.Object[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="byte[]"),
                                     @ConstraintVariableType(variable="$actual", type="byte[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="byte[]"),
                                     @ConstraintVariableType(variable="$actual", type="byte[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="char[]"),
                                     @ConstraintVariableType(variable="$actual", type="char[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="char[]"),
                                     @ConstraintVariableType(variable="$actual", type="char[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="short[]"),
                                     @ConstraintVariableType(variable="$actual", type="short[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="short[]"),
                                     @ConstraintVariableType(variable="$actual", type="short[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="int[]"),
                                     @ConstraintVariableType(variable="$actual", type="int[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="int[]"),
                                     @ConstraintVariableType(variable="$actual", type="int[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="long[]"),
                                     @ConstraintVariableType(variable="$actual", type="long[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="long[]"),
                                     @ConstraintVariableType(variable="$actual", type="long[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="float[]"),
                                     @ConstraintVariableType(variable="$actual", type="float[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="float[]"),
                                     @ConstraintVariableType(variable="$actual", type="float[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="double[]"),
                                     @ConstraintVariableType(variable="$actual", type="double[]")
                                    }
                       ),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="double[]"),
                                     @ConstraintVariableType(variable="$actual", type="double[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="java.lang.Object[]"),
                                     @ConstraintVariableType(variable="$actual", type="java.lang.Object[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="java.lang.Object[]"),
                                     @ConstraintVariableType(variable="$actual", type="java.lang.Object[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="byte[]"),
                                     @ConstraintVariableType(variable="$actual", type="byte[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="byte[]"),
                                     @ConstraintVariableType(variable="$actual", type="byte[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="char[]"),
                                     @ConstraintVariableType(variable="$actual", type="char[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="char[]"),
                                     @ConstraintVariableType(variable="$actual", type="char[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="short[]"),
                                     @ConstraintVariableType(variable="$actual", type="short[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="short[]"),
                                     @ConstraintVariableType(variable="$actual", type="short[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="int[]"),
                                     @ConstraintVariableType(variable="$actual", type="int[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="int[]"),
                                     @ConstraintVariableType(variable="$actual", type="int[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="long[]"),
                                     @ConstraintVariableType(variable="$actual", type="long[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="long[]"),
                                     @ConstraintVariableType(variable="$actual", type="long[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="float[]"),
                                     @ConstraintVariableType(variable="$actual", type="float[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="float[]"),
                                     @ConstraintVariableType(variable="$actual", type="float[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$expected", type="double[]"),
                                     @ConstraintVariableType(variable="$actual", type="double[]")
                                    }
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints={@ConstraintVariableType(variable="$message", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$expected", type="double[]"),
                                     @ConstraintVariableType(variable="$actual", type="double[]")
                                    }
                       )
    })
    public static ErrorDescription assertEqualsForArrays(HintContext ctx) {
        TypeElement ojAssert = ctx.getInfo().getElements().getTypeElement("org.junit.Assert");
        String targetPattern = null;
        
        if (ojAssert != null) {
            for (ExecutableElement ee : ElementFilter.methodsIn(ojAssert.getEnclosedElements())) {
                if (ee.getSimpleName().contentEquals("assertArrayEquals")) {
                    if (ctx.getVariables().containsKey("$message")) {
                        targetPattern = "org.junit.Assert.assertArrayEquals($message, $expected, $actual)";
                    } else {
                        targetPattern = "org.junit.Assert.assertArrayEquals($expected, $actual)";
                    }
                }
            }
        }
        
        TypeMirror type = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$expected"));
        
        if (type != null && type.getKind() == TypeKind.ARRAY) {
            TypeKind kind = ((ArrayType) type).getComponentType().getKind();
            
            if (kind == TypeKind.DOUBLE || kind == TypeKind.FLOAT) {
                targetPattern = null;
            }
        }
        
        if (targetPattern != null) {
            Fix fix = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_assertEqualsForArrays(), ctx.getPath(), targetPattern);
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_assertEqualsForArrays(), fix);
        } else {
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_assertEqualsForArrays());
        }
    }
    
    @Hint(displayName="#DN_assertEqualsMismatchedConstantVSReal",
          description="#DESC_assertEqualsMismatchedConstantVSReal",
          category="testing",
          suppressWarnings="MisorderedAssertEqualsArguments")
    @TriggerPatterns({
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)"),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints=@ConstraintVariableType(variable="$message", type="java.lang.String")
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)"),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints=@ConstraintVariableType(variable="$message", type="java.lang.String")
                       )
    })
    public static ErrorDescription mismatchedConstantVSReal(HintContext ctx) {
        if (isConstant(ctx, "$expected") || !isConstant(ctx, "$actual")) return null;
        if (!MatcherUtilities.matches(ctx, ctx.getPath(), "$method($arguments$)", true)) return null;
        
        String targetPattern = null;
        
        if (ctx.getVariables().containsKey("$message")) {
            targetPattern = "$method($message, $actual, $expected)";
        } else {
            targetPattern = "$method($actual, $expected)";
        }
        
        Fix fix = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_assertEqualsMismatchedConstantVSReal(), ctx.getPath(), targetPattern);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_assertEqualsMismatchedConstantVSReal(), fix);
    }
    
    private static boolean isConstant(HintContext ctx, String variable) {
        TreePath variablePath = ctx.getVariables().get(variable);
        
        return Utilities.isConstantString(ctx.getInfo(), variablePath, true) || ArithmeticUtilities.compute(ctx.getInfo(), variablePath, true) != null;
    }

    @Hint(displayName="#DN_assertEqualsIncovertibleTypes",
          description="#DESC_assertEqualsIncovertibleTypes",
          category="testing",
          suppressWarnings="AssertEqualsBetweenInconvertibleTypes")
    @TriggerPatterns({
        @TriggerPattern(value="junit.framework.Assert.assertEquals($expected, $actual)"),
        @TriggerPattern(value="junit.framework.Assert.assertEquals($message, $expected, $actual)",
                        constraints=@ConstraintVariableType(variable="$message", type="java.lang.String")
                       ),
        @TriggerPattern(value="org.junit.Assert.assertEquals($expected, $actual)"),
        @TriggerPattern(value="org.junit.Assert.assertEquals($message, $expected, $actual)",
                        constraints=@ConstraintVariableType(variable="$message", type="java.lang.String")
                       )
    })
    public static ErrorDescription incovertibleTypes(HintContext ctx) {
        TypeMirror expected = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$expected"));
        TypeMirror actual = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$actual"));
        
        if (!isAcceptable(ctx.getInfo(), expected) || !isAcceptable(ctx.getInfo(), actual)) return null;
        
        if (ctx.getInfo().getTypes().isSubtype(expected, actual)) return null;
        if (ctx.getInfo().getTypes().isSubtype(actual, expected)) return null;
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_assertEqualsIncovertibleTypes());
    }
    
    private static boolean isAcceptable(CompilationInfo info, TypeMirror type) {
        if (!Utilities.isValidType(type)) return false;
        TypeKind typeKind = type.getKind();
        return typeKind != TypeKind.EXECUTABLE && typeKind != TypeKind.PACKAGE;
    }
}
