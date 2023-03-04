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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class UtilityClass {
    
    @Hint(id="org.netbeans.modules.java.hints.UtilityClass_1", displayName="#MSG_UtilityClass", description="#HINT_UtilityClass", category="api", enabled=false, severity=Severity.VERIFIER, suppressWarnings="UtilityClassWithoutPrivateConstructor")
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription utilityClass(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        Element e = compilationInfo.getTrees().getElement(treePath);
        if (e == null) {
            return null;
        }
        
        if (!isUtilityClass(compilationInfo, e)) return null;
        
        for (ExecutableElement c : ElementFilter.constructorsIn(e.getEnclosedElements())) {
            if (!compilationInfo.getElementUtilities().isSynthetic(c)) {
                return null;
            }
        }

        return ErrorDescriptionFactory.forName(ctx,
                                               treePath,
                                               NbBundle.getMessage(UtilityClass.class, "MSG_UtilityClass"),
                                               new FixImpl(true,
                                                           TreePathHandle.create(e, compilationInfo)
                                               ).toEditorFix());
    }
    
    @Hint(id="org.netbeans.modules.java.hints.UtilityClass_2", displayName="#MSG_PublicConstructor", description="#HINT_PublicConstructor", category="api", enabled=false, severity=Severity.HINT, suppressWarnings="UtilityClassWithPublicConstructor")
    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription constructor(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        Element e = compilationInfo.getTrees().getElement(treePath);
        if (e == null) {
            return null;
        }
        if (   e.getKind() != ElementKind.CONSTRUCTOR
            || compilationInfo.getElementUtilities().isSynthetic(e)
            || (!e.getModifiers().contains(Modifier.PROTECTED) && !e.getModifiers().contains(Modifier.PUBLIC))) {
            return null;
        }
        
        if (!isUtilityClass(compilationInfo, e.getEnclosingElement())) return null;
        
        return ErrorDescriptionFactory.forName(ctx,
                                               treePath,
                                               NbBundle.getMessage(UtilityClass.class, "MSG_PublicConstructor"),
                                               new FixImpl(false,
                                                           TreePathHandle.create(e, compilationInfo)
                                               ).toEditorFix());
    }
    
    private static boolean isMainMethod(ExecutableElement m) {
        return m.getModifiers().contains(Modifier.STATIC) &&
               m.getSimpleName().contentEquals("main") &&
               (m.getReturnType().getKind() == TypeKind.VOID) &&
               (m.getParameters().size() == 1) &&
               (m.getParameters().get(0).asType().toString().equals("java.lang.String[]"));
    }

    private static boolean isUtilityClass(CompilationInfo compilationInfo, Element clazz) {
        if (clazz.getKind() != ElementKind.CLASS) {
            return false;
        }
        
        TypeMirror supr = ((TypeElement) clazz).getSuperclass();
        if (supr == null) {
            return false;
        }
        Element superElem = compilationInfo.getTypes().asElement(supr);
        if (superElem instanceof TypeElement) {
            Name superName = compilationInfo.getElements().getBinaryName((TypeElement) superElem);
            if (superName != null && !superName.contentEquals("java.lang.Object")) {
                return false;
            }
        }

        int cnt = 0;
        for (Element m : clazz.getEnclosedElements()) {
            if (m.getKind() == ElementKind.METHOD && isMainMethod(((ExecutableElement) m))) return false;
            if (m.getKind() == ElementKind.METHOD || m.getKind() == ElementKind.FIELD) {
                if (!m.getModifiers().contains(Modifier.STATIC)) return false;
                cnt++;
            }
        }

        return cnt > 0;
    }
    
    private static final class FixImpl extends JavaFix {
        private boolean clazz;

        public FixImpl(boolean clazz, TreePathHandle handle) {
            super(handle);
            this.clazz = clazz;
        }

        public String getText() {
            return NbBundle.getMessage(UtilityClass.class, clazz ? "MSG_PrivateConstructor" : "MSG_MakePrivate"); // NOI18N
        }

        @Override public String toString() {
            return "FixUtilityClass"; // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            Element e = wc.getTrees().getElement(tp);
            if (e == null) {
                return;
            }
            Tree outer = wc.getTrees().getTree(e);
            if (clazz) {
                if (outer == null || !TreeUtilities.CLASS_TREE_KINDS.contains(outer.getKind())) {
                    return;
                }
                ClassTree cls = (ClassTree)outer;

                ModifiersTree modifiers = wc.getTreeMaker().Modifiers(Collections.singleton(Modifier.PRIVATE));
                MethodTree m = wc.getTreeMaker().Constructor(
                    modifiers,
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    wc.getTreeMaker().Block(Collections.<StatementTree>emptyList(), false)
                );
                wc.rewrite(cls, wc.getTreeMaker().addClassMember(cls, m));
            } else {
                if (outer == null || outer.getKind() != Kind.METHOD) {
                    return;
                }
                MethodTree met = (MethodTree)outer;

                ModifiersTree modifiers = wc.getTreeMaker().Modifiers(Collections.singleton(Modifier.PRIVATE), met.getModifiers().getAnnotations());
                wc.rewrite(met.getModifiers(), modifiers);
            }
        }
    }

}
