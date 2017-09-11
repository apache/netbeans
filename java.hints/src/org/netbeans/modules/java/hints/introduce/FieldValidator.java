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
