/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;

/**
 * Validates that method is not in conflict with other one, or that it does not shadow
 * another one.
 * 
 * @author sdedic
 */
final class MethodValidator implements MemberValidator {
    private TreePathHandle          target;
    private String                              name;
    private final JavaSource  theSource;
    private final List<TreePathHandle> parameters;
    private final TypeMirrorHandle     returnType;
    private MemberSearchResult result;
    
    public MethodValidator(JavaSource theSource, List<TreePathHandle> parameters, TypeMirrorHandle returnType) {
        this.theSource = theSource;
        this.parameters = parameters;
        this.returnType = returnType;
    }
    
    public MemberSearchResult getResult() {
        return result;
    }
    
    @Override
    public synchronized MemberSearchResult validateName(TreePathHandle type, String n) {
        if (!(type.equals(target) && n.equals(name))) {
            SearchWorker wrk = new SearchWorker(type, n);
            try {
                theSource.runUserActionTask(wrk, true);
            } catch (IOException ex) {
            }
            this.result = wrk.result;
            this.name = n;
        }
        return result;
    }
    
    private class SearchWorker implements Task<CompilationController>, ElementAcceptor {
        private final TreePathHandle  targetHandle;
        private final String name;
        private MemberSearchResult result;
        private CompilationInfo cinfo;
        private Scope initialScope;
        
        private TypeElement target;
        private DeclaredType targetType;
        private List<TypeMirror> paramTypes;
        private Collection<Element> erasures = new HashSet<>();
        private TypeMirror returnType;
        
        public SearchWorker(TreePathHandle type, String name) {
            this.targetHandle = type;
            this.name = name;
        }
        
        @Override
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.RESOLVED);
            this.cinfo = parameter;
            TreePath targetPath = targetHandle.resolve(cinfo);
            if (targetPath == null) {
                return;
            }
            Element te = cinfo.getTrees().getElement(targetPath);
            if (te == null || !(te.getKind().isClass() || te.getKind().isInterface())) {
                return;
            }
            target = (TypeElement)te;
            TypeMirror m = target.asType();
            if (m.getKind() != TypeKind.DECLARED) {
                return;
            }
            targetType = (DeclaredType)m;
            this.returnType = MethodValidator.this.returnType.resolve(cinfo);
            if (returnType == null) {
                return;
            }
             paramTypes = new ArrayList<>(parameters.size());
            for (TreePathHandle tph : parameters) {
                Element el = tph.resolveElement(cinfo);
                if (el == null
                        || !(el.getKind() == ElementKind.PARAMETER || el.getKind() == ElementKind.LOCAL_VARIABLE
                        || el.getKind() == ElementKind.RESOURCE_VARIABLE || el.getKind() == ElementKind.EXCEPTION_PARAMETER)) {
                    return;
                }
                paramTypes.add(el.asType());
            }

            final DeclaredType targetType = (DeclaredType) target.asType();
            final Scope scope = cinfo.getTrees().getScope(targetPath);
            this.initialScope = scope;
            
            Map<? extends Element, Scope> visibleMethods = cinfo.getElementUtilities().findElementsAndOrigins(scope, this);
            for (Element e : visibleMethods.keySet()) {
                // only methods will pass the ElementAcceptor.
                ExecutableElement ee = (ExecutableElement)e;
                Element owner = visibleMethods.get(e).getEnclosingClass();
                // note - owner MAY be null, indicating that the method was static-imported 
                Element clazz = e.getEnclosingElement();
                if (clazz == target ||
                     // null owner (= no enclosing class for scope) means static import
                     owner == null ||
                    // possible clash on erasure on inherited
                    (owner == target && erasures.contains(e)) ||
                    // possible clash on return type
                    (!cinfo.getTypes().isSubtype(returnType, ee.getReturnType()))) {
                    // either same signature, or same erasure.
                    result =  new MemberSearchResult(ElementHandle.create(ee));
                    return;
                } else {
                    Modifier mod = null;
                    if (!(owner.getKind().isClass() || owner.getKind().isInterface())) {
                        // FIXME !!
                        continue;
                    }
                    ElementHandle<? extends TypeElement> ownerHandle = ElementHandle.create((TypeElement)owner);
                    if (!e.getModifiers().contains(Modifier.STATIC) && owner == target) {
                        Set<Modifier> mods = ee.getModifiers();
                        if (mods.contains(Modifier.PUBLIC)) {
                            mod = Modifier.PUBLIC;
                        } else if (mods.contains(Modifier.PROTECTED)) {
                            mod = Modifier.PROTECTED;
                        } else if (!mods.contains(Modifier.PRIVATE)) {
                            mod = Modifier.DEFAULT;
                        }
                        result = new MemberSearchResult(ElementHandle.create(ee), ownerHandle, mod);
                        return;
                    } else {
                        result = new MemberSearchResult(ElementHandle.create(ee), ownerHandle);
                    }
                }
            }
        }

        @Override
        public boolean accept(Element e, TypeMirror type) {
            if (e.getKind() != ElementKind.METHOD) {
                return false;
            }
            ExecutableElement ee = (ExecutableElement) e;
            // the same number of parameters
            if (ee.getParameters().size() != parameters.size()) {
                return false;
            }
            if (!ee.getSimpleName().contentEquals(name)) {
                return false;
            }
            // visibility
            if (type != null && !cinfo.getTrees().isAccessible(initialScope, ee, (DeclaredType)type)) {
                return false;
            }
            ExecutableType eeType = (ExecutableType) cinfo.getTypes().asMemberOf((DeclaredType)type, ee);
            boolean checkErasure = false;
            DeclaredType parentType = (DeclaredType)ee.getEnclosingElement().asType();// DeclaredType) cinfo.getTypes().asMemberOf(targetType, ee.getEnclosingElement());
            if (parentType != null && cinfo.getTypes().isSubtype(targetType, parentType)) {
                checkErasure = true;
            }
            boolean erasureUsed = false;
            for (int i = 0; i < paramTypes.size(); i++) {
                TypeMirror existing = eeType.getParameterTypes().get(i);
                TypeMirror pt = paramTypes.get(i);
                if (!cinfo.getTypes().isSubtype(pt, existing)) {
                    if (!checkErasure) {
                        return false;
                    }
                    if (checkErasure) {
                        TypeMirror e1 = cinfo.getTypes().erasure(pt);
                        TypeMirror e2 = cinfo.getTypes().erasure(existing);
                        if (!cinfo.getTypes().isSameType(e1, e2)) {
                            return false;
                        }
                        erasureUsed = true;
                    }
                }
            }
            if (erasureUsed) {
                erasures.add(ee);
            }
            return true;
        }
    }
}
