/**
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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
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
 *
 * @author sdedic
 */
final class FieldValidator implements MemberValidator {
    private final JavaSource            theSource;
    private final TypeMirrorHandle      fieldTypeHandle;
    
    private String             name;
    private ElementHandle<Element>  target;
    private MemberSearchResult lastResult;

    public FieldValidator(JavaSource theSource, TypeMirrorHandle fieldTypeHandle) {
        this.theSource = theSource;
        this.fieldTypeHandle = fieldTypeHandle;
    }
    
    MemberSearchResult getLastResult() {
        return lastResult;
    }
    
    @Override
    public synchronized MemberSearchResult validateName(TreePathHandle target, String n) {
        if ((target == target || target.equals(target)) && n.equals(name)) {
            return lastResult;
        }
        SearchImpl impl = new SearchImpl(target, n);
        try {
            theSource.runUserActionTask(impl, true);
        } catch (IOException ex) {
           return null;
        }
        
        return lastResult;
    }
    
    private class SearchImpl implements Task<CompilationController>, ElementAcceptor {
        private final TreePathHandle targetHandle;
        private final String name;
        
        private CompilationInfo cinfo;
        private Element target;
        private Scope initialScope;

        public SearchImpl(TreePathHandle targetHandle, String name) {
            this.targetHandle = targetHandle;
            this.name = name;
        }
        
        @Override
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.RESOLVED);
            this.cinfo = parameter;
            if (targetHandle == null) {
                return;
            }
            TreePath targetPath = targetHandle.resolve(cinfo);
            if (target == null) {
                return;
            }
            initialScope = cinfo.getTrees().getScope(targetPath);

            Map<? extends Element, Scope> visibleVariables = 
                    cinfo.getElementUtilities().findElementsAndOrigins(initialScope, this);

            for (Element e : visibleVariables.keySet()) {
                if (e.getKind() == ElementKind.FIELD ||
                    e.getKind() == ElementKind.ENUM_CONSTANT) {
                    Scope def = visibleVariables.get(e);
                    TypeElement owner = def.getEnclosingClass();
                    if (owner == null) {
                        // static import
                        lastResult = new MemberSearchResult(ElementHandle.create(e),
                            ElementHandle.create((TypeElement)e.getEnclosingElement()));
                    } else if (owner == e.getEnclosingElement()) {
                        lastResult = new MemberSearchResult(ElementHandle.create(e),
                            ElementHandle.create(owner));
                    } else {
                        // special case - hiding superclass field
                        lastResult = new MemberSearchResult(ElementHandle.create(e),
                            ElementHandle.create(owner), null);
                    }
                } else {
                    // some locals, report a conflict since the hidden local
                    // cannot be dereferenced 
                    lastResult = new MemberSearchResult(ElementHandle.create(e));
                    return;
                }
            }
        }

        @Override
        public boolean accept(Element e, TypeMirror type) {
            boolean checkAccessible = false;
            switch (e.getKind()) {
                case ENUM_CONSTANT:
                case FIELD:
                    checkAccessible = true;
                    break;
                case PARAMETER:
                case LOCAL_VARIABLE:
                case EXCEPTION_PARAMETER:
                case TYPE_PARAMETER:
                case RESOURCE_VARIABLE:
                    break;
                default:
                    return false;
            }
            if (!e.getSimpleName().contentEquals(name)) {
                return false;
            }
            return !checkAccessible ||
                   type == null ||
                   cinfo.getTrees().isAccessible(initialScope, e, (DeclaredType)type);
        }
    }
    
}
