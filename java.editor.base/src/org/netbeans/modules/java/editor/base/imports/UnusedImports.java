/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.base.imports;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;

/**
 *
 * @author lahvac
 */
public class UnusedImports {
    private static final Object KEY_CACHE = new Object();
    
    public static List<TreePathHandle> computeUnusedImports(CompilationInfo info) {
        final List<TreePathHandle> result = new ArrayList<>();
        
        for (TreePath unused : process(info, new AtomicBoolean())) {
            result.add(TreePathHandle.create(unused, info));
        }
        
        return result;
    }
    
    public static Collection<TreePath> process(CompilationInfo info, AtomicBoolean cancel) {
        Collection<TreePath> result = (Collection<TreePath>) info.getCachedValue(KEY_CACHE);
        
        if (result != null) return result;
        
        DetectorVisitor v = new DetectorVisitor(info, cancel);
        
        CompilationUnitTree cu = info.getCompilationUnit();
        
        v.scan(cu, null);
        
        if (cancel.get())
            return null;
        
        List<TreePath> allUnusedImports = new ArrayList<TreePath>();

        for (TreePath tree : v.getUnusedImports().values()) {
            if (cancel.get()) {
                return null;
            }

            allUnusedImports.add(tree);
        }
        
        allUnusedImports = Collections.unmodifiableList(allUnusedImports);
        
        info.putCachedValue(KEY_CACHE, allUnusedImports, CacheClearPolicy.ON_CHANGE);
        
        return allUnusedImports;
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, Void> {
        
        private final CompilationInfo info;
        
        private final Map<Element, ImportTree> element2Import = new HashMap<>();
        private final Set<Element> importedBySingleImport = new HashSet<>();
        private final Map<String, Collection<ImportTree>> simpleName2UnresolvableImports = new HashMap<>();
        private final Set<ImportTree> unresolvablePackageImports = new HashSet<>();
        private final Map<ImportTree, TreePath/*ImportTree*/> import2Highlight = new HashMap<>();
        private final Map<ImportTree, Integer> usageCounts = new HashMap<>();
        
        private DetectorVisitor(CompilationInfo info, AtomicBoolean cancel) {
            super(cancel);
            
            this.info = info;
        }
        
        private void handleJavadoc(TreePath classMember) {
            if (classMember == null) {
                return;
            }
            for (Element el : JavadocImports.computeReferencedElements(info, classMember)) {
                typeUsed(el, null, false);
            }
        }
        
        public Map<ImportTree, TreePath/*ImportTree*/> getUnusedImports() {
            Map<ImportTree, TreePath> ret = new HashMap<>(import2Highlight);
            ret.keySet().removeAll(usageCounts.keySet());
            return ret;
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            handleJavadoc(getCurrentPath());
            return super.visitClass(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            handleJavadoc(getCurrentPath());
            return super.visitMethod(node, p);
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e != null && e.getKind().isField()) {
                handleJavadoc(getCurrentPath());
            }
            
            return super.visitVariable(node, p);
        }
        
        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, Void d) {
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
	    scan(tree.getImports(), d);
	    scan(tree.getPackageAnnotations(), d);
	    scan(tree.getTypeDecls(), d);
	    return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void d) {
            if (info.getTrees().getSourcePositions().getStartPosition(getCurrentPath().getCompilationUnit(), tree) < 0)
                return null;
            
            typeUsed(info.getTrees().getElement(getCurrentPath()), getCurrentPath(), getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION);
            
            return super.visitIdentifier(tree, null);
        }
        
        private boolean isStar(ImportTree tree) {
            Tree qualIdent = tree.getQualifiedIdentifier();
            
            if (qualIdent == null || qualIdent.getKind() == Kind.IDENTIFIER) {
                return false;
            }
            
            return ((MemberSelectTree) qualIdent).getIdentifier().contentEquals("*");
        }

        private boolean parseErrorInImport(ImportTree imp) {
            if (isStar(imp)) return false;
            final StringBuilder fqn = new StringBuilder();
            new TreeScanner<Void, Void>() {
                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void p) {
                    super.visitMemberSelect(node, p);
                    fqn.append('.');
                    fqn.append(node.getIdentifier());
                    return null;
                }
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    fqn.append(node.getName());
                    return null;
                }
            }.scan(imp.getQualifiedIdentifier(), null);

            return !SourceVersion.isName(fqn);
        }
        
        public boolean isErroneous(@NullAllowed Element e) {
            if (e == null) {
                return true;
            }
            if (e.getKind() == ElementKind.MODULE) {
                return false;
            }
            final TypeMirror type = e.asType();
            if (type == null) {
                return false;
            }
            return type.getKind() == TypeKind.ERROR || type.getKind() == TypeKind.OTHER;
        }

        @Override
        public Void visitImport(ImportTree tree, Void d) {
            if (parseErrorInImport(tree)) {
                return super.visitImport(tree, null);
            }
            if (tree.getQualifiedIdentifier() == null ||
                tree.getQualifiedIdentifier().getKind() != Tree.Kind.MEMBER_SELECT) {
                return super.visitImport(tree, null);
            }
            MemberSelectTree qualIdent = (MemberSelectTree) tree.getQualifiedIdentifier();
            boolean assign = false;
            
            // static imports and star imports only use the qualifier part
            boolean star = isStar(tree);
            TreePath tp = tree.isStatic() || star ?
                    new TreePath(new TreePath(getCurrentPath(), qualIdent), qualIdent.getExpression()) :
                    new TreePath(getCurrentPath(), tree.getQualifiedIdentifier());
            Element decl = info.getTrees().getElement(tp);
            
            import2Highlight.put(tree, getCurrentPath());
            if (decl != null && !isErroneous(decl)) {
                if (!tree.isStatic()) {
                    if (star) {
                        List<TypeElement> types = ElementFilter.typesIn(decl.getEnclosedElements());
                        for (TypeElement te : types) {
                            assign = true;
                            if (!element2Import.containsKey(te)) {
                                element2Import.put(te, tree);
                            }
                        }
                    } else {
                        element2Import.put(decl, tree);
                        importedBySingleImport.add(decl);
                    }
                } else if (decl.getKind().isClass() || decl.getKind().isInterface()) {
                    Name simpleName = star ? null : qualIdent.getIdentifier();

                    for (Element e : info.getElements().getAllMembers((TypeElement) decl)) {
                        if (!e.getModifiers().contains(Modifier.STATIC)) continue;
                        if (simpleName != null && !e.getSimpleName().equals(simpleName)) {
                            continue;
                        }
                        if (!star || !element2Import.containsKey(e)) {
                            element2Import.put(e, tree);
                        }
                        assign = true;
                    }
                }
            }
            if (!assign) {
                if (!tree.isStatic() && star) {
                    unresolvablePackageImports.add(tree);
                } else {
                    addUnresolvableImport(qualIdent.getIdentifier(), tree);
                }
            }
            super.visitImport(tree, null);
            return null;
        }

        private void addUnresolvableImport(Name name, ImportTree imp) {
            String key = name.toString();

            Collection<ImportTree> l = simpleName2UnresolvableImports.get(key);

            if (l == null) {
                simpleName2UnresolvableImports.put(key, l = new LinkedList<ImportTree>());
            }

            l.add(imp);
        }
        
        private void addUsage(ImportTree imp) {
            Integer i = usageCounts.get(imp);
            if (i == null) {
                i = 0;
            }
            usageCounts.put(imp, i++);
        }
        
        private void typeUsed(Element decl, TreePath expr, boolean methodInvocation) {
            if (decl != null && (expr == null || expr.getLeaf().getKind() == Kind.IDENTIFIER || expr.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE)) {
                if (!isErroneous(decl)) {
                    ImportTree imp = element2Import.get(decl);

                    if (imp != null) {
                        addUsage(imp);
                        if (isStar(imp)) {
                            //TODO: explain
                            handleUnresolvableImports(decl, methodInvocation, false);
                        }
                    }
                } else {
                    handleUnresolvableImports(decl, methodInvocation, true);
                    
                    for (Entry<Element, ImportTree> e : element2Import.entrySet()) {
                        if (importedBySingleImport.contains(e.getKey())) continue;
                        
                        if (e.getKey().getSimpleName().equals(decl.getSimpleName())) {
                            import2Highlight.remove(e.getValue());
                        }
                    }
                }
            }
        }

        private void handleUnresolvableImports(Element decl,
                boolean methodInvocation, boolean removeStarImports) {
            Name simpleName = decl.getSimpleName();
            if (simpleName != null) {
                Collection<ImportTree> imps = simpleName2UnresolvableImports.get(simpleName.toString());

                if (imps != null) {
                    for (ImportTree imp : imps) {
                        if (!methodInvocation || imp.isStatic()) {
                            import2Highlight.remove(imp);
                        }
                    }
                } else {
                    if (removeStarImports) {
                        //TODO: explain
                        for (ImportTree unresolvable : unresolvablePackageImports) {
                            if (!methodInvocation || unresolvable.isStatic()) {
                                import2Highlight.remove(unresolvable);
                            }
                        }
                    }
                }
            }
        }
    }
    
}
