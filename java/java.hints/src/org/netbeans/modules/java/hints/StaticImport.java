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
/*
 * Contributor(s): markiewb
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;

/**
 * Hint offering to convert code to use static imports.
 * <p>
 * Supported are
 * <ul>
 * <li>a qualified static method is tranformed into a static import. e.g.
 * <code>Math.abs(-1)</code> -> <code>abs(-1)</code>.
 * </li>
 * <li>a qualified static field is tranformed into a static import. e.g.
 * <code>java.util.Calendar.JANUARY</code> -> <code>JANUARY</code>.
 * </li>
 * <li>a qualified static enum field is tranformed into a static import. e.g.
 * <code>java.util.concurrent.TimeUnit.DAYS</code> -> <code>DAYS</code>.
 * </li>
 * </ul>
 * </p>
 * Future versions might support other member types.
 *
 * @author Sam Halliday
 * @author markiewb
 * @see <a href="https://docs.oracle.com/javase/1.5.0/docs/guide/language/static-import.html>Static Imports</a>
 */
@Hint(category="rules15", displayName="#DN_StaticImport", description="#DSC_StaticImport", severity=Severity.HINT, enabled=false, suppressWarnings={"", "StaticImport"},
        minSourceVersion = "5")
public class StaticImport {
    
    private static final Set<ElementKind> SUPPORTED_TYPES = EnumSet.of(ElementKind.METHOD, ElementKind.ENUM_CONSTANT, ElementKind.FIELD);

    @TriggerTreeKind(Kind.MEMBER_SELECT)
    public static List<ErrorDescription> run(HintContext ctx) {
        CompilationInfo info = ctx.getInfo();
        TreePath treePath = ctx.getPath();

        Element e = info.getTrees().getElement(treePath);
        if (e == null || !e.getModifiers().contains(Modifier.STATIC) || !SUPPORTED_TYPES.contains(e.getKind())) {
            return null;
        }

        if (ElementKind.METHOD == e.getKind()) {
            TreePath mitp = treePath.getParentPath();
            if (mitp == null || mitp.getLeaf().getKind() != Kind.METHOD_INVOCATION) {
                return null;
            }
            if (((MethodInvocationTree) mitp.getLeaf()).getMethodSelect() != treePath.getLeaf()) {
                return null;
            }
            List<? extends Tree> typeArgs = ((MethodInvocationTree) mitp.getLeaf()).getTypeArguments();
            if (typeArgs != null && !typeArgs.isEmpty()) {
                return null;
            }
        }
        Element enclosingEl = e.getEnclosingElement();
        if (enclosingEl == null) {
            return null;
        }
        String sn = e.getSimpleName().toString();
        // rules out .class, but who knows what keywords will be abused in the future.
        if (SourceVersion.isKeyword(sn)) {
            return null;
        }
        TreePath cc = getContainingClass(treePath);
        if (cc == null){
            return null;
        }
        Element klass = info.getTrees().getElement(cc);
        if (klass == null || !klass.getKind().isDeclaredType()) {
            return null;
        }
        String fqn = null;
        String fqn1 = getFqn(info, e);
        if (!isSubTypeOrInnerOfSubType(info, klass, enclosingEl) && !isStaticallyImported(info, fqn1)) {
            if (hasMethodNameClash(info, klass, sn) || hasStaticImportSimpleNameClash(info, sn)) {
                return null;
            }
            fqn = fqn1;
        }
        Scope currentScope = info.getTrees().getScope(treePath);
        TypeMirror enclosingType = e.getEnclosingElement().asType();
        if (enclosingType == null || enclosingType.getKind() != TypeKind.DECLARED || !info.getTrees().isAccessible(currentScope, e, (DeclaredType) enclosingType)) {
            return null;
        }
        String desc = NbBundle.getMessage(StaticImport.class, "ERR_StaticImport");
        ErrorDescription ed = ErrorDescriptionFactory.forTree(ctx, treePath, desc, new FixImpl(TreePathHandle.create(treePath, info), fqn, sn).toEditorFix());
        if (ctx.isCanceled()) {
            return null;
        }
        return List.of(ed);
    }

    public static final class FixImpl extends JavaFix {

        private final String fqn;
        private final String sn;

        /**
         * @param handle to the MEMBER_SELECT
         * @param fqn to static import, or null to not perform any imports
         * @param sn simple name
         */
        public FixImpl(TreePathHandle handle, String fqn, String sn) {
            super(handle, "\uFFFFa");
            this.fqn = fqn;
            this.sn = sn;
        }

        @Override
        public String getText() {
            if (fqn == null) {
                return NbBundle.getMessage(StaticImport.class, "HINT_StaticImport", sn);
            } else {
                return NbBundle.getMessage(StaticImport.class, "HINT_StaticImport2", fqn);
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath treePath = ctx.getPath();
            TreePath mitp = treePath.getParentPath();
            if (mitp == null) {
                return;
            }
            Element e = copy.getTrees().getElement(treePath);
            if (e == null || !e.getModifiers().contains(Modifier.STATIC)) {
                return;
            }
            TreeMaker make = copy.getTreeMaker();
            copy.rewrite(treePath.getLeaf(), make.Identifier(sn));
            if (fqn == null) {
                return;
            }
            CompilationUnitTree cut = (CompilationUnitTree) copy.resolveRewriteTarget(copy.getCompilationUnit());
            CompilationUnitTree nue = GeneratorUtilities.get(copy).addImports(cut, Set.of(e));
            copy.rewrite(cut, nue);
        }

    }

    // returns true if a METHOD is enclosed in element with simple name sn
    private static boolean hasMethodWithSimpleName(CompilationInfo info, Element element, final String sn) {
        return info.getElementUtilities().getMembers(
                element.asType(),
                (elem, type) -> elem.getKind() == ElementKind.METHOD && elem.getSimpleName().toString().equals(sn)
        ).iterator().hasNext();
    }

    /**
     * @param info
     * @param simpleName of static method.
     * @return true if a static import exists with the same simple name.
     * Caveat, expect false positives on protected and default visibility methods from wildcard static imports.
     */
    private static boolean hasStaticImportSimpleNameClash(CompilationInfo info, String simpleName) {
        for (ImportTree i : info.getCompilationUnit().getImports()) {
            if (!i.isStatic()) {
                continue;
            }
            String q = i.getQualifiedIdentifier().toString();
            if (q.endsWith(".*")) { //NOI18N
                TypeElement ie = info.getElements().getTypeElement(q.substring(0, q.length() - 2));
                if (ie == null) {
                    continue;
                }
                for (Element enclosed : ie.getEnclosedElements()) {
                    Set<Modifier> modifiers = enclosed.getModifiers();
                    if (enclosed.getKind() != ElementKind.METHOD || !modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)) {
                        continue;
                    }
                    String sn1 = enclosed.getSimpleName().toString();
                    if (simpleName.equals(sn1)) {
                        return true;
                    }
                }
            } else {
                int endIndex = q.lastIndexOf("."); //NOI18N
                if (endIndex == -1 || endIndex >= q.length() - 1) {
                    continue;
                }
                if (q.substring(endIndex).equals(simpleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param info
     * @param t1
     * @param t3
     * @return true iff the first type (or its containing class in the case of inner classes)
     * is a subtype of the second.
     * @see Types#isSubtype(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)
     */
    private static boolean isSubTypeOrInnerOfSubType(CompilationInfo info, Element t1, Element t2) {
        boolean isSubtype = info.getTypes().isSubtype(t1.asType(), t2.asType());
        boolean isInnerClass = t1.getEnclosingElement().getKind().isDeclaredType();
        return isSubtype || (isInnerClass && info.getTypes().isSubtype(t1.getEnclosingElement().asType(), t2.asType()));
    }

    /**
     * @param info
     * @param klass the element for a CLASS
     * @param member the STATIC, MEMBER_SELECT Element for a MethodInvocationTree
     * @return true if member has a simple name which would clash with local or inherited
     * methods in klass (which may be an inner or static class).
     */
    private static boolean hasMethodNameClash(CompilationInfo info, Element klass, String simpleName) {
        // check the members and inherited members of the klass
        if (hasMethodWithSimpleName(info, klass, simpleName)) {
            return true;
        }
        Element klassEnclosing = klass.getEnclosingElement();
        return klassEnclosing != null && klassEnclosing.getKind().isDeclaredType() && hasMethodWithSimpleName(info, klassEnclosing, simpleName);
    }

    /**
     * @param e
     * @return the FQN for an Element
     */
    private static String getFqn(CompilationInfo info, Element e) {
        return info.getElementUtilities().getElementName(e.getEnclosingElement(), true) + "." + e.getSimpleName();
    }

    /**
     * @param tp
     * @return the first path which is a CLASS or null if none found
     */
    private static TreePath getContainingClass(TreePath tp) {
        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            tp = tp.getParentPath();
        }
        return tp;
    }

    // return true if the fqn already has a static import
    private static boolean isStaticallyImported(CompilationInfo info, String fqn) {
        for (ImportTree i : info.getCompilationUnit().getImports()) {
            if (!i.isStatic()) {
                continue;
            }
            String q = i.getQualifiedIdentifier().toString();
            if (q.endsWith(".*") && fqn.startsWith(q.substring(0, q.length() - 1))) { //NOI18N
                return true;
            }
            if (q.equals(fqn)) {
                return true;
            }
        }
        return false;
    }
}
