/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;

/**
 *
 * @author Jan Lahoda
 */
class IsOverriddenVisitor extends CancellableTreePathScanner<Void, Tree> {

    private static final Logger LOG = Logger.getLogger(IsOverriddenVisitor.class.getName());
    private CompilationInfo info;
    
    Map<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>> type2Declaration;
    Map<ElementHandle<ExecutableElement>, MethodTree> declaration2Tree;
    Map<ElementHandle<TypeElement>, ClassTree> declaration2Class;
    
    private Map<TypeElement, ElementHandle<TypeElement>> type2Handle;
    
    IsOverriddenVisitor(CompilationInfo info, AtomicBoolean cancel) {
        super(cancel);
        this.info = info;
        
        type2Declaration = new HashMap<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>>();
        declaration2Tree = new HashMap<ElementHandle<ExecutableElement>, MethodTree>();
        declaration2Class = new HashMap<ElementHandle<TypeElement>, ClassTree>();
        
        type2Handle = new HashMap<TypeElement, ElementHandle<TypeElement>>();
    }
    
    private ElementHandle<TypeElement> getHandle(TypeElement type) {
        ElementHandle<TypeElement> result = type2Handle.get(type);
        
        if (result == null) {
            type2Handle.put(type, result = ElementHandle.create(type));
        }
        
        return result;
    }
    
    @Override
    public Void visitMethod(MethodTree tree, Tree d) {
        if (currentClass != null) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind()  == ElementKind.METHOD) {
                if (!el.getModifiers().contains(Modifier.PRIVATE) && !el.getModifiers().contains(Modifier.STATIC)) {
                    ExecutableElement overridee = (ExecutableElement) el;
                    List<ElementHandle<ExecutableElement>> methods = type2Declaration.get(currentClass);
                    
                    if (methods == null) {
                        type2Declaration.put(currentClass, methods = new ArrayList<ElementHandle<ExecutableElement>>());
                    }

                    try {
                        ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(overridee);
                        methods.add(methodHandle);
                        declaration2Tree.put(methodHandle, tree);
                    } catch (IllegalArgumentException iae) {
                        LOG.log(
                            Level.INFO,
                            "Unresolvable method: {0}, reason: {1}",    //NOI18N
                            new Object[]{
                                overridee,
                                iae.getMessage()
                            });
                    }
                }
            }
        }
        
        super.visitMethod(tree, tree);
        return null;
    }
    
    @Override
    public Void visitClass(ClassTree tree, Tree d) {
        Element decl = info.getTrees().getElement(getCurrentPath());
        
        if (decl != null && (decl.getKind().isClass() || decl.getKind().isInterface())) {
            ElementHandle<TypeElement> oldCurrentClass = currentClass;
            
            currentClass = getHandle((TypeElement) decl);
            declaration2Class.put(currentClass, tree);
            super.visitClass(tree, d);
            currentClass = oldCurrentClass;
        } else {
            super.visitClass(tree, d);
        }
        
        return null;
    }
    
    private ElementHandle<TypeElement> currentClass;
    
}
