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
package org.netbeans.modules.java.metrics.hints;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * Collect dependencies on external classes.
 * Depending on 'ignoreJavaLibraries' it ignores classes whose QN begins with 'java.' or 'javax.'.
 */
class DependencyCollector extends ErrorAwareTreePathScanner<Object, Object> {
    private boolean ignoreJavaLibraries;
    private final CompilationInfo info;
    private TypeElement outermostClass;
    private final Set<Name> seenQNames = new HashSet<>();
    private final Set<Name> ignoreQNames = new HashSet<>();
    private boolean inClass;

    public DependencyCollector(CompilationInfo info) {
        this.info = info;
    }

    public void setIgnoreJavaLibraries(boolean ignoreJavaLibraries) {
        this.ignoreJavaLibraries = ignoreJavaLibraries;
    }

    public void setOutermostClass(TypeElement outermostClass) {
        this.outermostClass = outermostClass;
    }
    
    public Set<Name> getSeenQNames() {
        return seenQNames;
    }
    
    public void addIgnoredQName(Name n) {
        ignoreQNames.add(n);
    }

    private void addDependency(Tree t) {
        if (t == null) {
            return;
        }
        addDependency(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), t)));
    }

    private void addDependency(TypeMirror t) {
        Collection<DeclaredType> declaredTypes = new ArrayList<DeclaredType>(7);
        DeclaredTypeCollector.INSTANCE.visit(t, declaredTypes);
        for (DeclaredType dt : declaredTypes) {
            TypeElement te = (TypeElement)dt.asElement();
            
            if (outermostClass != null ) {
                TypeElement outermost = info.getElementUtilities().outermostTypeElement(te);
                if (outermost == outermostClass) {
                    return;
                }
            }
            
            Name qn = ((TypeElement) dt.asElement()).getQualifiedName();
            if (qn.length() > 5) {
                if (qn.subSequence(0, 4).equals("java")) { // NOI18N
                    // java. or javax.
                    if (qn.charAt(4) == '.' || (qn.length() > 6 && qn.charAt(4) == 'x' && qn.charAt(5) == '.')) {
                        if (ignoreJavaLibraries) {
                            return;
                        }
                    }
                }
            }
            if (!ignoreQNames.contains(qn)) {
                seenQNames.add(qn);
            }
        }
    }

    /**
     * Allow to visit 1st level local classes in methods. If started on the class node,
     * it prevents from visiting inner classes - they will be reported separately.
     * @param node
     * @param p
     * @return 
     */
    @Override
    public Object visitClass(ClassTree node, Object p) {
        if (!inClass) {
            inClass = true;
            Object r = super.visitClass(node, p);
            inClass = false;
            return r;
        } else {
            return null;
        }
    }

    @Override
    public Object visitArrayType(ArrayTypeTree node, Object p) {
        addDependency(node.getType());
        return super.visitArrayType(node, p);
    }

    /**
     * Catch introduces a dependency on the exception type
     */
    @Override
    public Object visitCatch(CatchTree node, Object p) {
        addDependency(node.getParameter());
        return super.visitCatch(node, p);
    }

    /**
     * Method adds dependencies on its return type, and parameter types
     */
    @Override
    public Object visitMethod(MethodTree node, Object p) {
        ExecutableElement e = (ExecutableElement) info.getTrees().getElement(getCurrentPath());
        addDependency(e.getReturnType());
        // parameters will be captured by visit(VariableTree).
        for (TypeMirror tm : e.getThrownTypes()) {
            addDependency(tm);
        }
        return super.visitMethod(node, p);
    }

    /**Method
     * Method invocation adds a dependency on the method's declaring class
    @Override
    public Object visitMethodInvocation(MethodInvocationTree node, Object p) {
    // possibly redundant, member select could handle the same
    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getMethodSelect()));
    if (e.getKind() == ElementKind.METHOD) {
    Element encl = e.getEnclosingElement();
    if (encl.getKind() == ElementKind.CLASS || encl.getKind() == ElementKind.INTERFACE || encl.getKind() == ElementKind.ENUM) {
    TypeElement te = (TypeElement) encl;
    addDependency(te.asType());
    }
    }
    return super.visitMethodInvocation(node, p);
    }
     */
    @Override
    public Object visitCase(CaseTree node, Object p) {
        if (node.getExpression() != null && node.getExpression().getKind() == Tree.Kind.IDENTIFIER) {
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));
            
            addDependency(node.getExpression());
        }
        return super.visitCase(node, p); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * new class operator introduces a dependency on the created type
     */
    @Override
    public Object visitNewClass(NewClassTree node, Object p) {
        addDependency(node.getIdentifier());
        return super.visitNewClass(node, p);
    }

    /**
     * type cast adds dependency on the casted-to type
     */
    @Override
    public Object visitTypeCast(TypeCastTree node, Object p) {
        addDependency(node.getType());
        return super.visitTypeCast(node, p);
    }

    /**
     * instanceof adds dependency on the literal type
     */
    @Override
    public Object visitInstanceOf(InstanceOfTree node, Object p) {
        addDependency(node.getType());
        return super.visitInstanceOf(node, p);
    }
    
    

    @Override
    public Object visitMemberSelect(MemberSelectTree node, Object p) {
        addDependency(node.getExpression());
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Object visitVariable(VariableTree node, Object p) {
        addDependency(node.getType());
        return super.visitVariable(node, p);
    }

    @Override
    public Object visitNewArray(NewArrayTree node, Object p) {
        addDependency(node.getType());
        return super.visitNewArray(node, p);
    }

    @Override
    public Object visitUnionType(UnionTypeTree node, Object p) {
        for (Tree t : node.getTypeAlternatives()) {
            addDependency(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), t)));
        }
        return super.visitUnionType(node, p);
    }

    @Override
    public Object visitIntersectionType(IntersectionTypeTree node, Object p) {
        for (Tree t : node.getBounds()) {
            addDependency(info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), t)));
        }
        return super.visitIntersectionType(node, p);
    }

    @Override
    public Object visitMemberReference(MemberReferenceTree node, Object p) {
        // PENDING
        return super.visitMemberReference(node, p);
    }
    
}
