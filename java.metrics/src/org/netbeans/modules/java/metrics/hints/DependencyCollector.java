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
import com.sun.source.util.TreePathScanner;
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
class DependencyCollector extends TreePathScanner<Object, Object> {
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
