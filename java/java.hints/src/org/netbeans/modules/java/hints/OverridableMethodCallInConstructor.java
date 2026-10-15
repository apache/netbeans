/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author David Strupl
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.OverridableMethodCallInConstructor", description = "#DESC_org.netbeans.modules.java.hints.OverridableMethodCallInConstructor", category="initialization", suppressWarnings={"OverridableMethodCallInConstructor", "", "OverridableMethodCallDuringObjectConstruction"})
public class OverridableMethodCallInConstructor {

    public OverridableMethodCallInConstructor() {
    }

    @TriggerTreeKind(Tree.Kind.METHOD_INVOCATION)
    public static ErrorDescription hint(HintContext ctx) {
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        CompilationInfo info = ctx.getInfo();
        TreePath enclosingMethod = Utilities.findOwningExecutable(ctx, ctx.getPath(), false);

        if (enclosingMethod == null) {
            return null;
        }

        Element enclosingMethodElement = ctx.getInfo().getTrees().getElement(enclosingMethod);
        
        if (enclosingMethodElement == null || enclosingMethodElement.getKind() != ElementKind.CONSTRUCTOR) {
            return null;
        }

        Element methodInvocationElement = info.getTrees().getElement(new TreePath(ctx.getPath(), mit.getMethodSelect()));
        if (methodInvocationElement == null || methodInvocationElement.getKind() != ElementKind.METHOD) {
            return null;
        }
        Element classElement = methodInvocationElement.getEnclosingElement();
        if (classElement == null || classElement.getKind() != ElementKind.CLASS) {
            return null;
        }
        Element classEl = enclosingMethodElement.getEnclosingElement();
        if (classEl == null || classEl.getKind() != ElementKind.CLASS) {
            return null;
        }
        boolean sameClass = classElement.equals(enclosingMethodElement.getEnclosingElement());
        if (!info.getTypes().isSubtype(classEl.asType(), classElement.asType())) {
            return null;
        }
        // classEl exts classElemenet - either classElement == classEl, or classElement cannot be final anyway
        if (classEl.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }

        Set<Modifier> modifiers = methodInvocationElement.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || 
            modifiers.contains(Modifier.FINAL) || 
            modifiers.contains(Modifier.STATIC)) {
            return null;
        }

        if (!invocationOnThis(mit)) {
            return null;
        }

        TreePath methodDeclaration = ctx.getInfo().getTrees().getPath(methodInvocationElement);

        if (methodDeclaration == null || ctx.getInfo().getTreeUtilities().isSynthetic(methodDeclaration)) return null;

        return ErrorDescriptionFactory.forName(ctx, mit,
                NbBundle.getMessage(
                    OverridableMethodCallInConstructor.class,
                    "MSG_org.netbeans.modules.java.hints.OverridableMethodCallInConstructor"),
                    sameClass ? computeFixes((MethodTree) methodDeclaration.getLeaf(),
                        classElement, ctx) : null);
    }

    private static Fix[] computeFixes(MethodTree mt, Element classElement, HintContext ctx) {
        List<Fix> result = new ArrayList<Fix>();
        // #238048: none of the following fixes are compatible with abstract methods; we could check if the method is
        // really overriden in some subclass, but it takes a lot of time; 
        Set<Modifier> flags = mt.getModifiers().getFlags();
        if (flags.contains(Modifier.ABSTRACT)) {
            return result.toArray(new Fix[0]);
        }
        ClassTree ct = ctx.getInfo().getTrees().getTree((TypeElement)classElement);
        result.add(FixFactory.addModifiersFix(
            ctx.getInfo(),
            TreePath.getPath(
                ctx.getInfo().getCompilationUnit(),
                ct.getModifiers()
            ),
            Collections.singleton(Modifier.FINAL),
            NbBundle.getMessage(OverridableMethodCallInConstructor.class,
                "FIX_MakeClass", "final", ct.getSimpleName())));
        result.add(FixFactory.addModifiersFix(
            ctx.getInfo(),
            TreePath.getPath(
                ctx.getInfo().getCompilationUnit(),
                mt.getModifiers()
            ),
            Collections.singleton(Modifier.FINAL),
            NbBundle.getMessage(OverridableMethodCallInConstructor.class,
                "FIX_MakeMethod", "final", mt.getName())));
        result.add(FixFactory.addModifiersFix(
            ctx.getInfo(),
            TreePath.getPath(
                ctx.getInfo().getCompilationUnit(),
                mt.getModifiers()
            ),
            Collections.singleton(Modifier.STATIC),
            NbBundle.getMessage(OverridableMethodCallInConstructor.class,
                "FIX_MakeMethod", "static", mt.getName())));
        result.add(FixFactory.changeModifiersFix(
            ctx.getInfo(),
            TreePath.getPath(
                ctx.getInfo().getCompilationUnit(),
                mt.getModifiers()
            ),
            Collections.singleton(Modifier.PRIVATE),
            flags,
            NbBundle.getMessage(OverridableMethodCallInConstructor.class,
                "FIX_MakeMethod", "private", mt.getName())));
        return result.toArray(new Fix[0]);
    }

    private static boolean invocationOnThis(MethodInvocationTree mit) {
        Tree select = mit.getMethodSelect();
        
        switch (select.getKind()) {
            case IDENTIFIER:
                return true;
            case MEMBER_SELECT:
                if (((MemberSelectTree) select).getExpression().getKind() == Kind.IDENTIFIER) {
                    IdentifierTree ident = (IdentifierTree) ((MemberSelectTree) select).getExpression();

                    return ident.getName().contentEquals("this");
                }
        }

        return false;
    }

}
