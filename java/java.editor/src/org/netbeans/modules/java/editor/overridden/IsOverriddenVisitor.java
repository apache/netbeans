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
    private final CompilationInfo info;
    
    Map<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>> type2Declaration;
    Map<ElementHandle<ExecutableElement>, MethodTree> declaration2Tree;
    Map<ElementHandle<TypeElement>, ClassTree> declaration2Class;
    
    private final Map<TypeElement, ElementHandle<TypeElement>> type2Handle;
    
    IsOverriddenVisitor(CompilationInfo info, AtomicBoolean cancel) {
        super(cancel);
        this.info = info;
        
        type2Declaration = new HashMap<>();
        declaration2Tree = new HashMap<>();
        declaration2Class = new HashMap<>();
        
        type2Handle = new HashMap<>();
    }
    
    private ElementHandle<TypeElement> getHandle(TypeElement type) {
        return type2Handle.computeIfAbsent(type, k -> ElementHandle.create(type));
    }
    
    @Override
    public Void visitMethod(MethodTree tree, Tree d) {
        if (currentClass != null) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind()  == ElementKind.METHOD) {
                if (!el.getModifiers().contains(Modifier.PRIVATE) && !el.getModifiers().contains(Modifier.STATIC)) {
                    ExecutableElement overridee = (ExecutableElement) el;
                    List<ElementHandle<ExecutableElement>> methods = type2Declaration.computeIfAbsent(currentClass, k -> new ArrayList<>());
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
