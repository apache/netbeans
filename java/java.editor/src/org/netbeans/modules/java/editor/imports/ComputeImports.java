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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.Symbols;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.completion.Utilities;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Exceptions;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
public final class ComputeImports {
    
    private static final String ERROR = "<error>";
    
    /** Creates a new instance of JavaFixAllImports */
    public ComputeImports(final CompilationInfo info) {
        this.info = info;
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, pcl, preferences));
    }
    
    private final CompilationInfo info;
    private CompilationInfo allInfo;
    
    private final PreferenceChangeListener pcl = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            info.putCachedValue(IMPORT_CANDIDATES_KEY, null, CacheClearPolicy.ON_CHANGE);
        }
    };
    private boolean cancelled;

    /**
     * Candidate Elements, filtered according to visibility rules
     */
    Map<String, List<Element>> candidates = new HashMap<>();
    
    /**
     * Candidate Elements, with no respect to visibility
     */
    Map<String, List<Element>> notFilteredCandidates = new HashMap<>();
    
    /**
     * For each name, a possible FQNs for methods. Computed for each round
     * of Hint processing
     */
    Map<String, Set<String>> possibleMethodFQNs = new HashMap<>();
    
    
    public synchronized void cancel() {
        cancelled = true;
        
        if (visitor != null)
            visitor.cancel();
    }
    
    public Set<String> getMethodFQNs(String simpleName) {
        return possibleMethodFQNs.get(simpleName);
    }
    
    private synchronized boolean isCancelled() {
        return cancelled;
    }
    
    public List<Element> getCandidates(String simpleName) {
        return candidates.get(simpleName);
    }
    
    public List<Element> getRawCandidates(String simpleName) {
        return notFilteredCandidates.get(simpleName);
    }
    
    private static final Object IMPORT_CANDIDATES_KEY = new Object();
    
    public ComputeImports computeCandidatesEx() {
        return computeCandidatesEx(Collections.emptySet());
    }

    private ComputeImports computeCandidatesEx(Set<String> forcedUnresolved) {
        ComputeImports cache = (ComputeImports)info.getCachedValue(IMPORT_CANDIDATES_KEY);
        if (cache != null) {
            return cache;
        }
        boolean modules = false;
        
        if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_9) <= 0) {
            if (info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource("module-info.java") != null) {
                modules = true;
            }
        }
        
        if (modules) {
            ClasspathInfo cpInfo = info.getClasspathInfo();
            ClasspathInfo extraInfo = ClasspathInfo.create(
                    ClassPathSupport.createProxyClassPath(
                            cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                            cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_BOOT)),
                    ClassPathSupport.createProxyClassPath(
                            cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE),
                            cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_COMPILE),
                            cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_CLASS)),
                    cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE));
            JavaSource src = JavaSource.create(extraInfo, info.getSnapshot().getSource().getFileObject());
            try {
                src.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController parameter) throws Exception {
                        allInfo = parameter;
                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                        doComputeCandidates(forcedUnresolved);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            allInfo = info;
            doComputeCandidates(forcedUnresolved);
        }
        info.putCachedValue(IMPORT_CANDIDATES_KEY, this, CacheClearPolicy.ON_CHANGE);
        return this;
    }
    
    public Pair<Map<String, List<Element>>, Map<String, List<Element>>> getSimpleCandidates() {
        return new Pair<Map<String, List<Element>>, Map<String, List<Element>>>(candidates, notFilteredCandidates);
    }
    
    public Pair<Map<String, List<Element>>, Map<String, List<Element>>> computeCandidates() {
        return computeCandidates(Collections.emptySet());
    }

    public Pair<Map<String, List<Element>>, Map<String, List<Element>>> computeCandidates(Set<String> forcedUnresolved) {
        return computeCandidatesEx(forcedUnresolved).getSimpleCandidates();
    }
    
    private TreeVisitorImpl visitor;
    
    private synchronized void setVisitor(TreeVisitorImpl visitor) {
        this.visitor = visitor;
    }
    
    private void doComputeCandidates(Set<String> forcedUnresolved) {
        final CompilationUnitTree cut = info.getCompilationUnit();
        ClasspathInfo cpInfo = allInfo.getClasspathInfo();
        final TreeVisitorImpl v = new TreeVisitorImpl(info);
        setVisitor(v);
        try {
            v.scan(cut, new HashMap<String, Object>());
        } finally {
            setVisitor(null);
        }
        
        Set<String> unresolvedNames = new HashSet<String>(v.unresolved);
        
        unresolvedNames.addAll(forcedUnresolved);
        
        unresolvedNames.addAll(JavadocImports.computeUnresolvedImports(info));
        
        Set<String> unresolvedNonTypes = new HashSet<String>(v.unresolvedNonTypes);

        unresolvedNonTypes.addAll(forcedUnresolved);

        for (String unresolved : unresolvedNames) {
            if (isCancelled())
                return;
            
            List<Element> classes = new ArrayList<Element>();
            Set<ElementHandle<TypeElement>> typeNames = cpInfo.getClassIndex().getDeclaredTypes(unresolved, NameKind.SIMPLE_NAME,EnumSet.allOf(ClassIndex.SearchScope.class));
            if (typeNames == null) {
                //Canceled
                return;
            }
            for (ElementHandle<TypeElement> typeName : typeNames) {
                if (isCancelled())
                    return;
                TypeElement te = typeName.resolve(allInfo);
                
                if (te == null) {
                    Logger.getLogger(ComputeImports.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeName + "\".");
                    continue;
                }
                
                //#122334: do not propose imports from the default package:
                if (info.getElements().getPackageOf(te).getQualifiedName().length() != 0 &&
                        !Utilities.isExcluded(te.getQualifiedName())) {
                    classes.add(te);
                }
            }
            
            if (unresolvedNonTypes.contains(unresolved)) {
                Iterable<Symbols> simpleNames = cpInfo.getClassIndex().getDeclaredSymbols(unresolved, NameKind.SIMPLE_NAME,EnumSet.allOf(ClassIndex.SearchScope.class));

                if (simpleNames == null) {
                    //Canceled:
                    return;
                }

                for (final Symbols p : simpleNames) {
                    if (isCancelled())
                        return;

                    final TypeElement te = p.getEnclosingType().resolve(allInfo);
                    final Set<String> idents = p.getSymbols();
                    if (te != null) {
                        for (Element ne : te.getEnclosedElements()) {
                            if (!ne.getModifiers().contains(Modifier.STATIC)) continue;
                            if (idents.contains(getSimpleName(ne, te))) {
                                classes.add(ne);
                            }
                        }
                    }
                }
            }
            
            candidates.put(unresolved, new ArrayList(classes));
            notFilteredCandidates.put(unresolved, classes);
        }
        
        boolean wasChanged = true;
        
        while (wasChanged) {
            if (isCancelled())
                return;
            
            wasChanged = false;
            // reset possible FQNs, since the set of acessible stuff may have changed -> 
            // collect again
            possibleMethodFQNs.clear();
            
            for (Hint hint: v.hints) {
                wasChanged |= hint.filter(allInfo, this);
            }
        }
        
        // post processing: if some method hint was involved for a SN, we must retain ONLY
        // such Elements, which correspond to at least 1 method reference in the text. 
        for (Map.Entry<String, Set<String>> entry : possibleMethodFQNs.entrySet()) {
            String sn = entry.getKey();
            Set<String> fqns = entry.getValue();
            
            List<Element> cands = candidates.get(sn);
            List<Element> rawCands = notFilteredCandidates.get(sn);
            
            if (cands != null) {
                for (Iterator<Element> itE = cands.iterator(); itE.hasNext(); ) {
                    Element x = itE.next();
                    if (x.getKind() != ElementKind.METHOD) {
                        continue;
                    }
                    String fq = info.getElementUtilities().getElementName(x, true).toString();
                    if (!fqns.contains(fq)) {
                        itE.remove();
                    }
                }
            }
            if (rawCands != null) {
                for (Iterator<Element> itE = rawCands.iterator(); itE.hasNext(); ) {
                    Element x = itE.next();
                    if (x.getKind() != ElementKind.METHOD) {
                        continue;
                    }
                    String fq = info.getElementUtilities().getElementName(x, true).toString();
                    if (!fqns.contains(fq)) {
                        itE.remove();
                    }
                }
            }
        }
    }
    
    public void addMethodFqn(Element el) {
        if (el.getKind() != ElementKind.METHOD) {
            return;
        }
        String fqn = info.getElementUtilities().getElementName(el, true).toString();
        String simpleName = ((ExecutableElement)el).getSimpleName().toString();
        Set<String> col = possibleMethodFQNs.get(simpleName);
        if (col == null) {
            col = new HashSet<>(3);
            possibleMethodFQNs.put(simpleName, col);
        }
        col.add(fqn);
    }
    
    public String displayNameForImport(@NonNull Element element) {
        if (element.getKind().isClass() || element.getKind().isInterface()) {
            return ((TypeElement) element).getQualifiedName().toString();
        }
        
        StringBuilder fqnSB = new StringBuilder();
        
        fqnSB.append(info.getElementUtilities().getElementName(element, true));

        if (element.getKind() == ElementKind.METHOD) {
            fqnSB.append("(...)"); // NOI18N
        }
        
        return fqnSB.toString();
    }

    private static final String INIT = "<init>"; //NOI18N
    private String getSimpleName (
            @NonNull final Element element,
            @NullAllowed final Element enclosingElement) {
        String result = element.getSimpleName().toString();
        if (enclosingElement != null && INIT.equals(result)) {
            result = enclosingElement.getSimpleName().toString();
        }
        return result;
    }
    
    private static boolean filter(Types types, List<Element> left, List<Element> right, boolean leftReadOnly, boolean rightReadOnly) {
        boolean changed = false;
        Map<TypeElement, List<TypeElement>> validPairs = new HashMap<TypeElement, List<TypeElement>>();
        
        for (TypeElement l : ElementFilter.typesIn(left)) {
            List<TypeElement> valid = new ArrayList<TypeElement>();
            
            for (TypeElement r : ElementFilter.typesIn(right)) {
                TypeMirror t1 = types.erasure(l.asType());
                TypeMirror t2 = types.erasure(r.asType());
                
//                System.err.println("t2 = " + t2);
//                System.err.println("t1 = " + t1);
//                System.err.println("types= " + types.getClass());
//                System.err.println("types.isAssignable(t2, t1) = " + types.isAssignable(t2, t1));
//                System.err.println("types.isSubtype(t2, t1) = " + types.isSubtype(t2, t1));
//                System.err.println("types.isAssignable(t1,t2) = " + types.isAssignable(t1,t2));
//                System.err.println("types.isSubtype(t1, t2) = " + types.isSubtype(t1, t2));
                if (types.isAssignable(t2, t1))
                    valid.add(r);
            }
            
//            System.err.println("l = " + l );
//            System.err.println("valid = " + valid );
            validPairs.put(l, valid);
        }
        
        Set<TypeElement> validRights = new HashSet<TypeElement>();
        
        for (Map.Entry<TypeElement, List<TypeElement>> entry : validPairs.entrySet()) {
            TypeElement l = entry.getKey();
            List<TypeElement> valid = entry.getValue();
            
            if (valid.isEmpty() && !leftReadOnly) {
                //invalid left:
                left.remove(l);
                changed = true;
            }
            
            validRights.addAll(valid);
        }
        
        if (!rightReadOnly)
            changed = right.retainAll(validRights) | changed;
        
        return changed;
    }
    
    private static EnumSet<TypeKind> INVALID_TYPES = EnumSet.of(TypeKind.NULL, TypeKind.NONE, TypeKind.OTHER, TypeKind.ERROR);
    
    private static class TreeVisitorImpl extends CancellableTreePathScanner<Void, Map<String, Object>> {
        
        private final CompilationInfo info;
        private boolean onlyTypes;
        private Set<String> unresolved;
        private Set<String> unresolvedNonTypes;
        
        private List<Hint> hints;
        
        public TreeVisitorImpl(CompilationInfo info) {
            this.info = info;
            unresolved = new HashSet<>();
            unresolvedNonTypes = new HashSet<>();
            hints = new ArrayList<>();
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, Map<String, Object> p) {
            if (tree.getExpression().getKind() == Kind.IDENTIFIER) {
                p.put("request", null);
            }
            
            scan(tree.getExpression(), p);
            
            Union2<String, DeclaredType> leftSide = (Union2<String, DeclaredType>) p.remove("result");
            
            p.remove("request");
            
            if (leftSide != null && leftSide.hasFirst()) {
                String rightSide = tree.getIdentifier().toString();
                
                if (ERROR.equals(rightSide))
                    rightSide = "";
                
                boolean isMethodInvocation = getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION;
                
                //Ignore .class (which will not help us much):
                if (!"class".equals(rightSide))
                    hints.add(new EnclosedHint(leftSide.first(), rightSide, !isMethodInvocation));
            }
            
            return null;
        }
        
        @Override
        public Void visitVariable(VariableTree tree, Map<String, Object> p) {
            scan(tree.getModifiers(), p);
            
            if (tree.getType() != null && tree.getType().getKind() == Kind.IDENTIFIER) {
                p.put("request", null);
            }
            
            scan(tree.getType(), p, true);
            
            Union2<String, DeclaredType> leftSide = (Union2<String, DeclaredType>) p.remove("result");
            
            p.remove("request");
            
            Union2<String, DeclaredType> rightSide = null;
            
            if (leftSide != null && tree.getInitializer() != null) {
                Element el = info.getTrees().getElement(new TreePath(getCurrentPath(),tree.getInitializer()));
                TypeMirror rightType = el != null ? el.asType() : null;
                
//                System.err.println("rightType = " + rightType );
//                System.err.println("tree.getInitializer()=" + tree.getInitializer());
//                System.err.println("rightType.getKind()=" + rightType.getKind());
//                System.err.println("INVALID_TYPES.contains(rightType.getKind())=" + INVALID_TYPES.contains(rightType.getKind()));
                if (rightType != null && rightType.getKind() == TypeKind.DECLARED) {
                    rightSide = Union2.<String, DeclaredType>createSecond((DeclaredType) rightType);
                } else {
                    if (tree.getInitializer().getKind() == Kind.NEW_CLASS || tree.getInitializer().getKind() == Kind.NEW_ARRAY) {
                        p.put("request", null);
                    }
                }
            }
            
            scan(tree.getInitializer(), p);
            
            rightSide = rightSide == null ? (Union2<String, DeclaredType>) p.remove("result") : rightSide;
            
            p.remove("result");
            
//            System.err.println("rightSide = " + rightSide );
            
            p.remove("request");
            
            if (leftSide != null && rightSide != null) {
                if (!(leftSide instanceof TypeMirror) || !(rightSide instanceof TypeMirror)) {
                    hints.add(new TypeHint(leftSide, rightSide));
                }
            }
            
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Map<String, Object> p) {
            super.visitIdentifier(tree, p);
            
            boolean methodInvocation = getCurrentPath().getParentPath() != null && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION;
            
            if (methodInvocation) {
                MethodInvocationTree mit = (MethodInvocationTree) getCurrentPath().getParentPath().getLeaf();
                
                if (mit.getMethodSelect() == tree) {
                    List<TypeMirror> params = new ArrayList<TypeMirror>();
                    for (ExpressionTree realParam : mit.getArguments()) {
                        TypeMirror tm = info.getTrees().getTypeMirror(new TreePath(getCurrentPath().getParentPath(), realParam));
                        if (tm != null && tm.getKind() == TypeKind.NONE && (realParam.getKind() == Tree.Kind.LAMBDA_EXPRESSION || realParam.getKind() == Kind.MEMBER_REFERENCE)) {
                            tm = info.getTypes().getNullType();
                        }
                        params.add(tm);
                    }
                    this.hints.add(new MethodParamsHint(tree.getName().toString(), params));
                }
            }
            
//            System.err.println("tree=" + tree);
            final Element el = info.getTrees().getElement(getCurrentPath());
            if (el != null && (el.getKind().isClass() || el.getKind().isInterface() || el.getKind() == ElementKind.PACKAGE)) {
                TypeMirror type = el.asType();
                String simpleName = null;
                
                if (type != null) {
                    if (type.getKind() == TypeKind.ERROR) {
                        boolean allowImport = true;

                        if (getCurrentPath().getParentPath() != null && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.ASSIGNMENT) {
                            AssignmentTree at = (AssignmentTree) getCurrentPath().getParentPath().getLeaf();

                            allowImport = at.getVariable() != tree;
                        }
                        
                        if (methodInvocation) {
                            Scope s = info.getTrees().getScope(getCurrentPath());

                            while (s != null) {
                                allowImport &= !info.getElementUtilities().getLocalMembersAndVars(s, new ElementAcceptor() {
                                    @Override public boolean accept(Element e, TypeMirror type) {
                                        return e.getSimpleName().contentEquals(el.getSimpleName()) &&
                                                (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR);
                                    }
                                }).iterator().hasNext();
                                s = s.getEnclosingScope();
                            }
                        }

                        if (allowImport) {
                            simpleName = el.getSimpleName().toString();
                        }
                    }

                    if (type != null && type.getKind() == TypeKind.PACKAGE) {
                        //does the package really exists?
                        String s = ((PackageElement) el).getQualifiedName().toString();
                        Element thisPack = info.getTrees().getElement(new TreePath(info.getCompilationUnit()));
                        ModuleElement module = thisPack != null ? info.getElements().getModuleOf(thisPack) : null;
                        PackageElement pack = module != null ? info.getElements().getPackageElement(module, s) : info.getElements().getPackageElement(s);
                        if (pack == null) {
                            //probably situation like:
                            //Map.Entry e;
                            //where Map is not imported
                            simpleName = el.getSimpleName().toString();
                        }
                    }

                    if (simpleName == null || !SourceVersion.isIdentifier(simpleName) || SourceVersion.isKeyword(simpleName)) {
                        simpleName = null;
                    }

                    if (simpleName != null) {
                        unresolved.add(simpleName);

                        if (!onlyTypes) {
                            unresolvedNonTypes.add(simpleName);
                        }

                        Scope currentScope = getScope();

                        hints.add(new AccessibleHint(simpleName, currentScope));

                        if (p.containsKey("request")) {
                            p.put("result", Union2.<String, DeclaredType>createFirst(simpleName));
                        }
                    } else {
                        if (p.containsKey("request") && type.getKind() == TypeKind.DECLARED) {
                            p.put("result", Union2.<String, DeclaredType>createSecond((DeclaredType) type));
                        }
                    }
                }
            }
            
            p.remove("request");
            
            return null;
        }

        @Override
        public Void visitNewClass(NewClassTree node, Map<String, Object> p) {
            filterByNotAcceptedKind(node.getIdentifier(), ElementKind.ENUM);
            scan(node.getEnclosingExpression(), new HashMap<String, Object>());
            scan(node.getIdentifier(), p, true);
            scan(node.getTypeArguments(), new HashMap<String, Object>(), true);
            scan(node.getArguments(), new HashMap<String, Object>());
            scan(node.getClassBody(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Map<String, Object> p) {
            scan(node.getTypeArguments(), new HashMap<String, Object>(), true);
            scan(node.getMethodSelect(), p);
            scan(node.getArguments(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitNewArray(NewArrayTree node, Map<String, Object> p) {
            scan(node.getType(), p, true);
            scan(node.getDimensions(), new HashMap<String, Object>());
            scan(node.getInitializers(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree node, Map<String, Object> p) {
            scan(node.getType(), p);
            scan(node.getTypeArguments(), new HashMap<String, Object>());
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Map<String, Object> p) {
            if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.NEW_CLASS) {
                filterByAcceptedKind(node.getExtendsClause(), ElementKind.CLASS);
                for (Tree intf : node.getImplementsClause()) {
                    filterByAcceptedKind(intf, ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE);
                }
            }

            scan(node.getModifiers(), p);
            scan(node.getTypeParameters(), p, true);
            scan(node.getExtendsClause(), p, true);
            scan(node.getImplementsClause(), p, true);
            scan(node.getMembers(), p);

            return null;
        }

        @Override
        public Void visitAnnotation(AnnotationTree node, Map<String, Object> p) {
            filterByAcceptedKind(node.getAnnotationType(), ElementKind.ANNOTATION_TYPE);
            scan(node.getAnnotationType(), p, true);
            scan(node.getArguments(), p, false);
            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Map<String, Object> p) {
            scan(node.getModifiers(), p);
            scan(node.getTypeParameters(), p, true);
            scan(node.getReturnType(), p, true);
            scan(node.getReceiverParameter(), p);
            scan(node.getParameters(), p);
            scan(node.getThrows(), p, true);
            scan(node.getDefaultValue(), p);
            scan(node.getBody(), p);
            return null;
        }

        private void scan(Iterable<? extends Tree> trees, Map<String, Object> p, boolean onlyTypes) {
            for (Tree tree : trees) {
                scan(tree, p, onlyTypes);
            }
        }

        private void scan(Tree tree, Map<String, Object> p, boolean onlyTypes) {
            boolean oldOnlyTypes = this.onlyTypes;

            try {
                this.onlyTypes = onlyTypes;
                scan(tree, p);
            } finally {
                this.onlyTypes = oldOnlyTypes;
            }
        }
        
        private Scope topLevelScope;
        
        private Scope getScope() {
            if (topLevelScope == null) {
                topLevelScope = info.getTrees().getScope(new TreePath(getCurrentPath().getCompilationUnit()));
            }
            return topLevelScope;
        }
        
        private void filterByAcceptedKind(Tree toFilter, ElementKind acceptedKind, ElementKind... otherAcceptedKinds) {
            filterByKind(toFilter, EnumSet.of(acceptedKind, otherAcceptedKinds), EnumSet.noneOf(ElementKind.class));
        }
        
        private void filterByNotAcceptedKind(Tree toFilter, ElementKind notAcceptedKind, ElementKind... otherNotAcceptedKinds) {
            filterByKind(toFilter, EnumSet.noneOf(ElementKind.class), EnumSet.of(notAcceptedKind, otherNotAcceptedKinds));
        }
        
        private void filterByKind(Tree toFilter, Set<ElementKind> acceptedKinds, Set<ElementKind> notAcceptedKinds) {
            if (toFilter == null) return;
            switch (toFilter.getKind()) {
                case IDENTIFIER:
                    hints.add(new KindHint(((IdentifierTree) toFilter).getName().toString(), acceptedKinds, notAcceptedKinds));
                    break;
                case PARAMETERIZED_TYPE:
                    filterByKind(((ParameterizedTypeTree) toFilter).getType(), acceptedKinds, notAcceptedKinds);
                    break;
            }
        }
    }
    
    public static interface Hint {
        
        public abstract boolean filter(CompilationInfo info, ComputeImports state);
        
    }
    
    public static final class TypeHint implements Hint {
        
        private Union2<String, DeclaredType> left;
        private Union2<String, DeclaredType> right;
        
        public TypeHint(Union2<String, DeclaredType> left, Union2<String, DeclaredType> right) {
            this.left = left;
            this.right = right;
        }
        
        public boolean filter(CompilationInfo info, ComputeImports state) {
            Map<String, List<Element>> candidates = state.candidates;
            
            List<Element> left = null;
            List<Element> right = null;
            boolean leftReadOnly = false;
            boolean rightReadOnly = false;
            
            if (this.left.hasSecond()) {
                Element el = this.left.second().asElement();
                
                //TODO do not use instanceof!
                if (el instanceof TypeElement) {
                    left = Collections.singletonList(el);
                    leftReadOnly = true;
                }
            } else {
                left = candidates.get(this.left.first());
            }
            
            if (this.right.hasSecond()) {
                Element el = this.right.second().asElement();
                
                //TODO do not use instanceof!
                if (el instanceof TypeElement) {
                    right = Collections.singletonList(el);
                    rightReadOnly = true;
                }
            } else {
                right = candidates.get(this.right.first());
            }
            
            if (left != null && right != null && !left.isEmpty() && !right.isEmpty()) {
                return ComputeImports.filter(info.getTypes(), left, right, leftReadOnly, rightReadOnly);
            }
            
            return false;
        }
        
    }
    
    public static final class EnclosedHint implements Hint {
        
        private String simpleName;
        private String methodName;
        private boolean allowPrefix;
        
        public EnclosedHint(String simpleName, String methodName, boolean allowPrefix) {
            this.simpleName = simpleName;
            this.methodName = methodName;
            this.allowPrefix = allowPrefix;
        }
        
        public boolean filter(CompilationInfo info, ComputeImports state) {
            Map<String, List<Element>> candidates = state.candidates;
            
            List<Element> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<TypeElement> toRemove = new ArrayList<TypeElement>();
            
            for (TypeElement te : ElementFilter.typesIn(cands)) {
                boolean found = false;
                
                for (Element e : te.getEnclosedElements()) {
                    String simpleName = e.getSimpleName().toString();
                    
                    if (methodName.contentEquals(simpleName)) {
                        found = true;
                        break;
                    }
                    
                    if (allowPrefix && simpleName.startsWith(methodName)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    toRemove.add(te);
                }
            }
            
            return cands.removeAll(toRemove);
        }
        
    }
    
    public static final class KindHint implements Hint {
        
        private String simpleName;
        private Set<ElementKind> acceptedKinds;
        private Set<ElementKind> notAcceptedKinds;

        public KindHint(String simpleName, Set<ElementKind> acceptedKinds, Set<ElementKind> notAcceptedKinds) {
            this.simpleName = simpleName;
            this.acceptedKinds = acceptedKinds;
            this.notAcceptedKinds = notAcceptedKinds;
        }
        
        public boolean filter(CompilationInfo info, ComputeImports state) {
            Map<String, List<Element>> rawCandidates = state.notFilteredCandidates;
            Map<String, List<Element>> candidates = state.candidates;
            
            // this is a hack, but if the kind does not match, we cannot offer the element even as 'not preferred'.
            // Better enable just for annotation types, otherwise we might stop offering e.g. interfaces in place where a class could be used (?)
            if (acceptedKinds.contains(ElementKind.ANNOTATION_TYPE) && acceptedKinds.size() == 1) {
                doFilter(info, rawCandidates);
            }
            return doFilter(info,candidates);
        }
        
        private boolean doFilter(CompilationInfo info, Map<String, List<Element>> candidates) {
            List<Element> cands = candidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<TypeElement> toRemove = new ArrayList<TypeElement>();
            
            for (TypeElement te : ElementFilter.typesIn(cands)) {
                if (!acceptedKinds.isEmpty() && !acceptedKinds.contains(te.getKind())) {
                    toRemove.add(te);
                    continue;
                }
                if (!notAcceptedKinds.isEmpty() && notAcceptedKinds.contains(te.getKind())) {
                    toRemove.add(te);
                    continue;
                }
            }
            
            return cands.removeAll(toRemove);
        }
        
    }
    
    public static final class AccessibleHint implements Hint {
        
        private String simpleName;
        private Scope scope;
        
        public AccessibleHint(String simpleName, Scope scope) {
            this.simpleName = simpleName;
            this.scope = scope;
        }
        
        public boolean filter(CompilationInfo info, ComputeImports state) {
            Map<String, List<Element>> rawCandidates = state.notFilteredCandidates;
            Map<String, List<Element>> candidates = state.candidates;
            
            List<Element> cands = rawCandidates.get(simpleName);
            
            if (cands == null || cands.isEmpty())
                return false;
            
            List<Element> toRemove = new ArrayList<Element>();
            
            for (Element te : cands) {
                if (te.getKind().isClass() || te.getKind().isInterface() ? !info.getTrees().isAccessible(scope, (TypeElement) te)
                                                                         : !info.getTrees().isAccessible(scope, te, (DeclaredType) te.getEnclosingElement().asType())) {
                    toRemove.add(te);
                }
            }
            
            //remove it from the candidates too:
            candidates.get(simpleName).removeAll(toRemove);
            
            return cands.removeAll(toRemove);
        }
        
    }
    
    public static final class MethodParamsHint implements Hint {
        
        private final String simpleName;
        private final List<TypeMirror> paramTypes;

        public MethodParamsHint(String simpleName, List<TypeMirror> paramTypes) {
            this.simpleName = simpleName;
            this.paramTypes = paramTypes;
        }

        public boolean filter(CompilationInfo info, ComputeImports state) {
            List<Element> rawCands = state.notFilteredCandidates.get(simpleName);
            List<Element> cands = state.candidates.get(simpleName);
            
            if (rawCands == null || cands == null) {
                return false;
            }

            boolean modified = false;
            boolean someMatch = false;
            for (Element c : new ArrayList<Element>(rawCands)) {
                if (c.getKind() != ElementKind.METHOD) {
                    rawCands.remove(c);
                    cands.remove(c);
                    modified |= true;
                } else {
                    //XXX: varargs
                    Iterator<? extends TypeMirror> real = paramTypes.iterator();
                    Iterator<? extends TypeMirror> formal = ((ExecutableType) c.asType()).getParameterTypes().iterator();
                    boolean matches = true;
                    boolean inVarArgs = false;
                    TypeMirror currentFormal = null;
                    
                    while (real.hasNext() && (formal.hasNext() || inVarArgs)) {
                        TypeMirror currentReal = real.next();
                        
                        if (!inVarArgs)
                            currentFormal = formal.next();
                        
                        if (!info.getTypes().isAssignable(info.getTypes().erasure(currentReal), info.getTypes().erasure(currentFormal))) {
                            if (((ExecutableElement) c).isVarArgs() && !formal.hasNext() && currentFormal.getKind() == TypeKind.ARRAY) {
                                currentFormal = ((ArrayType) currentFormal).getComponentType();
                                
                                if (!info.getTypes().isAssignable(info.getTypes().erasure(currentReal), info.getTypes().erasure(currentFormal))) {
                                    matches = false;
                                    break;
                                }
                                
                                inVarArgs = true;
                            } else {
                                matches = false;
                                break;
                            }
                        }
                    }
                    
                    matches &= real.hasNext() == formal.hasNext();
                    
                    if (matches) {
                        state.addMethodFqn(c);
                        someMatch = true;
                    }
                }
            }
            if (!someMatch) {
                // sorry, no candidate matched the simple name -> remove all candidates
                if (!rawCands.isEmpty()) {
                    cands.clear();
                    rawCands.clear();
                    modified = true;
                }
            }
            return modified;
        }
        
    }
    
    public static class Pair<A, B> {
        
        public A a;
        public B b;
        
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }

}
