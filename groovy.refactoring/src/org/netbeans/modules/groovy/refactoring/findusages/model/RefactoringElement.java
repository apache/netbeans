/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
