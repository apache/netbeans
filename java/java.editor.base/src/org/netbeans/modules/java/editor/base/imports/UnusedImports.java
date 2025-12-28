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
package org.netbeans.modules.java.editor.base.imports;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
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
import org.openide.util.Pair;

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
        private final Map<PackageElement, Pair<ModuleElement, ModuleElement>> packagesFromModuleImports2PrimaryModuleAndRealModule = new HashMap<>();
        private final Map<ModuleElement, ImportTree> primaryModule2Import = new HashMap<>();
        private final Set<ModuleElement> usedModules = new HashSet<>();
        private final Map<ModuleElement, ModuleElement> usedTransitiveModule2PrimaryModule = new HashMap<>();
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
            HashSet<ModuleElement> pendingUsedModules = new HashSet<>(usedModules);
            for (Iterator<ModuleElement> it = pendingUsedModules.iterator(); it.hasNext(); ) {
                ModuleElement primary = it.next();
                ImportTree imp = primaryModule2Import.get(primary);

                if (imp != null) {
                    it.remove();
                    ret.remove(imp);
                }
            }
            for (ModuleElement remaining : pendingUsedModules) {
                ModuleElement primary = usedTransitiveModule2PrimaryModule.get(remaining);
                ImportTree imp = primary != null ? primaryModule2Import.get(primary) : null;

                if (imp != null) {
                    ret.remove(imp);
                }
            }
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
            PackageElement javaLang = info.getElements().getPackageElement("java.lang");
            if (javaLang != null) {
                List<TypeElement> types = ElementFilter.typesIn(javaLang.getEnclosedElements());
                for (TypeElement te : types) {
                    if (!element2Import.containsKey(te)) {
                        element2Import.put(te, FAKE_IMPORT);
                    }
                }
            }

	    scan(tree.getImports(), d);
	    scan(tree.getPackageAnnotations(), d);
	    scan(tree.getTypeDecls(), d);
	    scan(tree.getModule(), d);
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
            new ErrorAwareTreeScanner<Void, Void>() {
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
            if (tree.getQualifiedIdentifier() == null) {
                return super.visitImport(tree, null);
            }
            boolean assign = false;
            if (tree.isModule()) {
                String moduleName = tree.getQualifiedIdentifier().toString();
                ModuleElement primary = info.getElements().getModuleElement(moduleName);
                if (primary != null) {
                    primaryModule2Import.put(primary, tree);

                    for (PackageElement pack : info.getElementUtilities().transitivelyExportedPackages(primary)) {
                        usedTransitiveModule2PrimaryModule.putIfAbsent((ModuleElement) pack.getEnclosingElement(), primary);
                        packagesFromModuleImports2PrimaryModuleAndRealModule.put(pack, Pair.of(primary, (ModuleElement) pack.getEnclosingElement()));
                    }

                    import2Highlight.put(tree, getCurrentPath());
                } //do not mark unresolvable module imports as unused
            } else {
                if (tree.getQualifiedIdentifier().getKind() != Tree.Kind.MEMBER_SELECT) {
                    return super.visitImport(tree, null);
                }

                MemberSelectTree qualIdent = (MemberSelectTree) tree.getQualifiedIdentifier();

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

                    if (imp == null &&
                        (decl.getKind().isClass() || decl.getKind().isInterface()) &&
                        decl.getEnclosingElement().getKind() == ElementKind.PACKAGE &&
                        (info.getCompilationUnit().getPackage() == null || !decl.getEnclosingElement().equals(info.getTrees().getElement(new TreePath(new TreePath(info.getCompilationUnit()), info.getCompilationUnit().getPackage()))))) {
                        //TODO: also in a different package?
                        Pair<ModuleElement, ModuleElement> modules = packagesFromModuleImports2PrimaryModuleAndRealModule.get(decl.getEnclosingElement());

                        if (modules != null) {
                            usedModules.add(modules.second());
                        }
                    }

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
    
    private static final ImportTree FAKE_IMPORT = new ImportTree() {
        @Override
        public boolean isStatic() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Tree getQualifiedIdentifier() {
            return null;
        }

        @Override
        public Kind getKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
}
