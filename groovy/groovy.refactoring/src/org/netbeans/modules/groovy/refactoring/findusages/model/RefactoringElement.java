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

package org.netbeans.modules.groovy.refactoring.findusages.model;

import groovyjarjarasm.asm.Opcodes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public abstract class RefactoringElement {

    protected final FileObject fileObject;
    protected final ASTNode node;
    protected Set<Modifier> modifiers;
    

    protected RefactoringElement(FileObject fileObject, ASTNode node) {
        this.fileObject = fileObject;
        this.node = node;
    }


    /**
     * Returns the name of the refactoring element. (e.g. for field declaration
     * "private GalacticMaster master" the method return "master")
     *
     * @return name of the refactoring element
     */
    public abstract String getName();

    public abstract ElementKind getKind();

    public abstract String getShowcase();

    /**
     * Returns the name of the class owning this refactoring element. That means
     * the fully qualified name of the class where the method/variable/field is
     * defined.
     *
     * @return the name of the owning class
     */
    public final String getOwnerName() {
        return ElementUtils.getDeclaringClassName(node);
    }

    /**
     * Returns the name of the class owning this refactoring element only 
     * without package name. That means the name of the class where the
     * method/variable/field is defined.
     *
     * @return
     */
    public final String getOwnerNameWithoutPackage() {
        return ElementUtils.getDeclaringClassNameWithoutPackage(node);
    }

    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            int flags = -1;
            if (node instanceof FieldNode) {
                flags = ((FieldNode) node).getModifiers();
            } else if (node instanceof MethodNode) {
                flags = ((MethodNode) node).getModifiers();
            } else if (node instanceof ClassNode) {
                flags = ((ClassNode) node).getModifiers();
            }
            if (flags != -1) {
                Set<Modifier> result = EnumSet.noneOf(Modifier.class);
                if ((flags & Opcodes.ACC_PUBLIC) != 0) {
                    result.add(Modifier.PUBLIC);
                }
                if ((flags & Opcodes.ACC_PROTECTED) != 0) {
                    result.add(Modifier.PROTECTED);
                }
                if ((flags & Opcodes.ACC_PRIVATE) != 0) {
                    result.add(Modifier.PRIVATE);
                }
                if ((flags & Opcodes.ACC_STATIC) != 0) {
                    result.add(Modifier.STATIC);
                }
                modifiers = result;
            } else {
                modifiers = Collections.<Modifier>emptySet();
            }
        }

        return modifiers;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public ASTNode getNode() {
        return node;
    }
}
