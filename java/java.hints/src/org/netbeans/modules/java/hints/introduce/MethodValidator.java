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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 * Validates that method is not in conflict with other one, or that it does not shadow
 * another one.
 * 
 * @author sdedic
 */
final class MethodValidator implements MemberValidator {
    private TreePathHandle          target;
    private String                              name;
    private final Source  theSource;
    private final List<TreePathHandle> parameters;
    private final TypeMirrorHandle     returnType;
    private MemberSearchResult result;
    
    public MethodValidator(Source theSource, List<TreePathHandle> parameters, TypeMirrorHandle returnType) {
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
                ParserManager.parse(Collections.singleton(theSource), wrk);
            } catch (ParseException ex) {
            }
            this.result = wrk.result;
            this.name = n;
        }
        return result;
    }
    
    private class SearchWorker extends UserTask implements ElementAcceptor {
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
        public void run(ResultIterator resultIterator) throws Exception {
            CompilationController parameter = CompilationController.get(resultIterator.getParserResult());
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
