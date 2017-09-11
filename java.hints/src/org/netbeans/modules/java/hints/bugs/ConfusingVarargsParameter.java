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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_ConfusingNullPassedToVararg=Confusing null passed to vararg method",
    "TEXT_ConfusingPrimitveArrayToVararg=Confusing primitive array passed to vararg method"
})
public class ConfusingVarargsParameter {
    /*
    XXX -- handled by javac as a warning.
    @Hint(
            displayName = "#DN_ConfusingVarargsNull",
            description = "#DESCR_ConfusingVarargsNull",
            enabled = true,
            category = "bugs",
            suppressWarnings = { "ConfusingNullVararg", "NullArgumentToVariableArgMethod" }
    )
    @TriggerPatterns({
        @TriggerPattern("$method($vars$, null)"),
        @TriggerPattern("new $classname($vars, null)")
    })
    public static ErrorDescription nullParameter(HintContext ctx) {
        TreePath invPath = ctx.getPath();
        CompilationInfo ci = ctx.getInfo();
        if (!isVarargsParameter(ci, invPath)) {
            return null;
        }
        MethodInvocationTree mit = (MethodInvocationTree)invPath.getLeaf();
        return ErrorDescriptionFactory.forTree(ctx,
                mit.getArguments().get(mit.getArguments().size() - 1),
                Bundle.TEXT_ConfusingNullPassedToVararg());
    }
    */
    
    private static boolean isVarargsParameter(CompilationInfo ci, TreePath invPath) {
        if (invPath.getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            return false;
        }
        MethodInvocationTree mit = (MethodInvocationTree)invPath.getLeaf();
        Element e = ci.getTrees().getElement(invPath);
        if (e == null || e.getKind() != ElementKind.METHOD) {
            return false;
        }
        ExecutableElement ee = (ExecutableElement)e;
        return ee.isVarArgs() && mit.getArguments().size() == ee.getParameters().size();
    }
    
    private static final String[] EXCLUDE_CLASSES = {
        "java.text.MessageFormat",
        "java.io.PrintStream",
        "java.io.PrintWriter",
        "java.lang.String"
    };
    
    @TriggerPatterns({
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "int[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "boolean[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "byte[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "short[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "char[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "long[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "float[]")),
        @TriggerPattern(value="$methodName($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "double[]")),

        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "int[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "boolean[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "byte[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "short[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "char[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "long[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "float[]")),
        @TriggerPattern(value="new $className($vars$, $v)", constraints = @ConstraintVariableType(variable = "$v", type = "double[]"))
    })
    @Hint(
            displayName = "#DN_ConfusingVarargsArray",
            description = "#DESCR_ConfusingVarargsArray",
            enabled = true,
            category = "bugs",
            suppressWarnings = { "ConfusingArrayVararg", "PrimitiveArrayArgumentToVariableArgMethod" }
    )
    public static ErrorDescription primitiveArray(HintContext ctx) {
        TreePath invPath = ctx.getPath();
        CompilationInfo ci = ctx.getInfo();
        if (!isVarargsParameter(ci, invPath)) {
            return null;
        }
        MethodInvocationTree mit = (MethodInvocationTree)invPath.getLeaf();
        ExpressionTree arg = mit.getArguments().get(mit.getArguments().size() - 1);
        if (arg.getKind() == Tree.Kind.NULL_LITERAL) {
            return null;
        }
        // suppress on classes covered by ArrayStringConversions
        Element e = ci.getTrees().getElement(invPath);
        if (e != null) {
            if (e.getKind() == ElementKind.CONSTRUCTOR || e.getKind() == ElementKind.METHOD) {
                VariableElement var = ((ExecutableElement)e).getParameters().get(mit.getArguments().size() - 1);
                TypeKind tk = var.asType().getKind();
                if (tk == TypeKind.ARRAY) {
                    TypeMirror tm = ((ArrayType)var.asType()).getComponentType();
                    if (tm.getKind().isPrimitive()) {
                        return null;
                    }
                }
            }
            Element ecl = e.getEnclosingElement();
            if (ecl != null && (ecl.getKind().isClass() || ecl.getKind().isInterface())) {
                TypeElement te = (TypeElement)ecl;
                String qn = te.getQualifiedName().toString();
                for (String s : EXCLUDE_CLASSES) {
                    if (s.equals(qn)) {
                        return null;
                    }
                }
            }
        }
        return ErrorDescriptionFactory.forTree(ctx,
                mit.getArguments().get(mit.getArguments().size() - 1),
                Bundle.TEXT_ConfusingPrimitveArrayToVararg());
    }
    
}
