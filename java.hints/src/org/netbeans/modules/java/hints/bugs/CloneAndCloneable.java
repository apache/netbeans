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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * Hints / fixes related to j.l.Object.clone() contract.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_CloneWithoutSuperClone=clone() does not call super.clone()",
    "TEXT_CloneWithoutCloneNotSupported=clone() does not throw CloneNotSupportedException",
    "TEXT_CloneWithoutCloneable=clone() in non-Cloneable class",
    "TEXT_CloneableWithoutClone=Cloneable class does not implement clone()",
    "FIX_ImplementCloneableInterface=Implement java.lang.Cloneable",
    "FIX_AddCloneNotSupportedException=Declare CloneNotSupportedException",
    "FIX_AddCloneMethod=Override clone() method"
})
public class CloneAndCloneable {
    
    @TriggerPattern("$mods$ $type clone() throws $exc$ { $stmts$; }") // NOI18N
    @Hint(category = "bugs",
          displayName = "#DN_CloneAndCloneable_cloneWithoutSuperClone", // NOI18N
          description = "#DESC_CloneAndCloneable_cloneWithoutSuperClone", // NOI18N
          suppressWarnings={"CloneDoesntCallSuperClone"},  // NOI18N
          options= Hint.Options.QUERY
    )
    public static ErrorDescription cloneWithoutSuperClone(HintContext ctx) {
        ExecutableElement me = (ExecutableElement)ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (me == null) {
            return null;
        }
        ExecutableElement sup = ctx.getInfo().getElementUtilities().getOverriddenMethod(me);
        if (sup == null) {
            return null;
        }
        SuperCloneFinder f = new SuperCloneFinder(ctx.getInfo(), sup);
        if (f.scan(ctx.getPath(), null) == Boolean.TRUE) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), 
                TEXT_CloneWithoutSuperClone());
    }
    
    /**
     * Finder provides true, if it finds a call to 'superClone' method element passed
     * in the constructor.
     */
    private static final class SuperCloneFinder extends TreePathScanner<Boolean, Void> {
        private final CompilationInfo info;
        private final ExecutableElement superClone;

        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return (r1 == Boolean.TRUE || r2 == Boolean.TRUE) ? Boolean.TRUE : null;
        }

        public SuperCloneFinder(CompilationInfo info, ExecutableElement superClone) {
            this.info = info;
            this.superClone = superClone;
        }
        
        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            // might be used in paramters ?
            if (super.visitMethodInvocation(node, p) == Boolean.TRUE) {
                return Boolean.TRUE;
            }
            Element invoked = info.getTrees().getElement(getCurrentPath());
            if (invoked == superClone) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        
    }
    
    @TriggerPattern("$mods$ $type clone() throws $exc$ { $stmts$; }") // NOI18N
    @Hint(category = "bugs",
          displayName = "#DN_CloneAndCloneable_cloneWithoutThrows", // NOI18N
          description = "#DESC_CloneAndCloneable_cloneWithoutThrows", // NOI18N
          suppressWarnings={"CloneDeclaresCloneNotSupported"} // NOI18N
    )
    public static ErrorDescription cloneWithoutThrows(HintContext ctx) {
        final CompilationInfo info = ctx.getInfo();
        ExecutableElement cloneMethod = (ExecutableElement)info.getTrees().getElement(ctx.getPath());
        
        // check if the method is not final. Final = not overridable = no problem
        if (cloneMethod == null || cloneMethod.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }
        // check if the enclosing subclass is not final
        TypeElement declaringClass = info.getElementUtilities().enclosingTypeElement(cloneMethod);
        if (declaringClass == null || declaringClass.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }
        Element e = info.getElements().getTypeElement("java.lang.CloneNotSupportedException"); // NOI18N
        if (e == null) {
            return null;
        }
        TypeMirror cnse = e.asType();
        for (TypeMirror m : cloneMethod.getThrownTypes()) {
            if (info.getTypes().isSameType(cnse, m)) {
                return null;
            }
        }
        // check if the overriden clone method declares CNSE - if not, the body code may not throw
        // the exception and adding throws clause would break the code:
        ExecutableElement ee = info.getElementUtilities().getOverriddenMethod(cloneMethod);
        boolean superThrows = ee == null;
        if (ee != null) {
            for (TypeMirror m : ee.getThrownTypes()) {
                if (info.getTypes().isSameType(cnse, m)) {
                    superThrows = true;
                    break;
                }
            }
        }
        if (!superThrows) {
            return null;
        }
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), 
                TEXT_CloneWithoutCloneNotSupported(), 
                new AddCNSExceptionFix(info, ctx.getPath()).toEditorFix());
    }
    
    private static class AddCNSExceptionFix extends JavaFix {
        public AddCNSExceptionFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return FIX_AddCloneNotSupportedException();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath methodPath = ctx.getPath();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            MethodTree orig = (MethodTree)methodPath.getLeaf();
            
            MethodTree changed = make.addMethodThrows(orig,
                    (ExpressionTree)make.Type("java.lang.CloneNotSupportedException")); // NOI18N
            ctx.getWorkingCopy().rewrite(orig, changed);
        }
    }
    
    @TriggerPattern("$mods$ $type clone() throws $exc$ { $stmts$; }") // NOI18N
    @Hint(category = "bugs",
          displayName = "#DN_CloneAndCloneable_cloneInNonCloneableClass", // NOI18N
          description = "#DESC_CloneAndCloneable_cloneInNonCloneableClass", // NOI18N
          suppressWarnings={"CloneInNonCloneableClass"},
          enabled = false
    )
    public static ErrorDescription cloneInNonCloneableClass(HintContext ctx) {
        final CompilationInfo info = ctx.getInfo();
        ExecutableElement cloneMethod = (ExecutableElement)info.getTrees().getElement(ctx.getPath());
        if (cloneMethod == null) {
            return null;
        }
        if (!cloneMethod.getModifiers().contains(Modifier.PUBLIC)) {
            return null;
        }
        // check if the enclosing subclass is not final
        TypeElement declaringClass = info.getElementUtilities().enclosingTypeElement(cloneMethod);
        if (declaringClass == null) {
            return null;
        }
        TypeElement cloneableIface = info.getElements().getTypeElement("java.lang.Cloneable"); // NOI18N
        if (cloneableIface == null) {
            return null;
        }
        if (info.getTypes().isSubtype(declaringClass.asType(), cloneableIface.asType())) {
            return null;
        }
        Fix fix = new CloneableInsertFix(info, info.getTrees().getPath(declaringClass)).toEditorFix();
        return ErrorDescriptionFactory.forName(
                ctx,
                info.getTrees().getTree(declaringClass), 
                TEXT_CloneWithoutCloneable(), fix);
    }
    
    private static class CloneableInsertFix extends JavaFix {

        public CloneableInsertFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return FIX_ImplementCloneableInterface();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath clazz = ctx.getPath();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            ClassTree original = (ClassTree) clazz.getLeaf();
            ClassTree changed = make.addClassImplementsClause(original, 
                    make.Type("java.lang.Cloneable")); // NOI18N
            ctx.getWorkingCopy().rewrite(original, changed);
        }
        
    }
    
    private static boolean isCloneMethod(ExecutableElement ee) {
        return ee.getParameters().isEmpty() && ee.getSimpleName().contentEquals("clone"); // NOI18N
    }
    
    @Hint(category = "bugs",
          displayName = "#DN_CloneAndCloneable_cloneableWithoutClone", // NOI18N
          description = "#DESC_CloneAndCloneable_cloneableWithoutClone", // NOI18N
          suppressWarnings={"CloneableImplementsClone"},  // NOI18N
          enabled = false
    )
    @TriggerTreeKind(Tree.Kind.CLASS)
    public static ErrorDescription cloneableWithoutClone(HintContext ctx) {
        CompilationInfo
        info = ctx.getInfo();
        TypeElement cloneableIface = info.getElements().getTypeElement("java.lang.Cloneable"); // NOI18N
        if (cloneableIface == null) {
            return null;
        }
        TypeElement clazz = (TypeElement)info.getTrees().getElement(ctx.getPath());
        if (clazz == null || !info.getTypes().isSubtype(clazz.asType(), cloneableIface.asType())) {
            return null;
        }
        for (ExecutableElement exec : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (isCloneMethod(exec)) {
                return null;
            }
        }
        return ErrorDescriptionFactory.forName(
                ctx,
                info.getTrees().getTree(clazz), 
                TEXT_CloneableWithoutClone(), new AddCloneFix(info, ctx.getPath()).toEditorFix());
    }
    
    private static class AddCloneFix extends JavaFix {
        public AddCloneFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return FIX_AddCloneMethod();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath path = ctx.getPath();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            ClassTree clazz = (ClassTree)path.getLeaf();
            
            TypeElement clazzType = (TypeElement)ctx.getWorkingCopy().getTrees().getElement(path);
            if (clazzType == null) {
                return;
            }
            ExecutableElement superClone = null;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(ctx.getWorkingCopy().getElements().getAllMembers(clazzType))) {
                if (isCloneMethod(ee)) {
                    superClone = ee;
                    break;
                }
            }
            
            GeneratorUtilities gen = GeneratorUtilities.get(ctx.getWorkingCopy());
            MethodTree mt;
            if (superClone != null) {
                mt = gen.createOverridingMethod(clazzType, superClone);
                if (!mt.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                    ctx.getWorkingCopy().rewrite(mt.getModifiers(), 
                            make.Modifiers(
                                Collections.singleton(Modifier.PUBLIC),
                                Collections.singletonList(
                                    make.Annotation(
                                        make.Type("java.lang.Override"), // NOI18N
                                        Collections.<ExpressionTree>emptyList()
                                    )
                                )
                            )
                    );
                }
            } else {
                // an error ?
                mt = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                        "clone", 
                        clazz, 
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.singletonList(make.QualIdent("java.lang.CloneNotSupportedException")), // NOI18N
                        "", null);
            }
            ClassTree changedClazz = gen.insertClassMember(clazz, mt);
            ctx.getWorkingCopy().rewrite(clazz, changedClazz);
        }
    }
}
