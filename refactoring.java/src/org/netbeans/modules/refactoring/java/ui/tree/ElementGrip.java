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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.refactoring.java.ui.tree;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public final class ElementGrip implements Openable {
    private TreePathHandle delegateElementHandle;
    /** used to speed up resolving, TreePathHandle runs lexer; see issue 171652 */
    private ElementHandle handle;
    private String toString;
    private FileObject fileObject;
    private Icon icon;
    
    /**
     * Creates a new instance of ElementGrip
     */
    public ElementGrip(TreePath treePath, CompilationInfo info) {
        this(TreePathHandle.create(treePath, info), info.getTrees().getElement(treePath), info);
    }

    public ElementGrip(Element elm, CompilationInfo info) {
        this(TreePathHandle.create(elm, info), elm, info);
    }

    private ElementGrip(TreePathHandle delegateElementHandle, Element elm, CompilationInfo info) {
        this.delegateElementHandle = delegateElementHandle;
        this.handle = elm == null ? null : ElementHandle.create(elm);
        if (elm != null) {
            if (elm.getKind() == ElementKind.CLASS && elm.getSimpleName().length() == 0) {
                this.toString = ((TypeElement) elm).asType().toString();
                this.icon = ElementIcons.getElementIcon(elm.getKind(), elm.getModifiers());
            } else if(elm.getKind() == ElementKind.ENUM 
                    && elm.getSimpleName().length() == 0
                    && elm.getEnclosingElement() != null) {
                final Element enclosingElement = elm.getEnclosingElement();
                this.toString = enclosingElement.getSimpleName().toString();
                this.icon = ElementIcons.getElementIcon(enclosingElement.getKind(), enclosingElement.getModifiers());
            } else {
                // workaround for issue 171692
                this.toString = elm.getKind() != ElementKind.CONSTRUCTOR
                        ? elm.getSimpleName().toString()
                        : elm.getEnclosingElement().getSimpleName().toString();
                this.icon = ElementIcons.getElementIcon(elm.getKind(), elm.getModifiers());
//            this.toString = ElementHeaders.getHeader(treePath, info, ElementHeaders.NAME);
            }
        }
        this.fileObject = info.getFileObject();
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        return toString;
    }

    public ElementGrip getParent() {
        return ElementGripFactory.getDefault().getParent(this);
    }

    public TreePath resolve(CompilationInfo info) {
        return delegateElementHandle.resolve(info);
    } 

    public Element resolveElement(CompilationInfo info) {
        return handle == null ? null : handle.resolve(info);
    } 

    public Tree.Kind getKind() {
        return delegateElementHandle.getKind();
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    public TreePathHandle getHandle() {
        return delegateElementHandle;
    }

    @Override
    public void open() {
        if(fileObject != null && handle != null && fileObject.isValid()) {
            ElementOpen.open(fileObject, handle);
        }
    }
    
}
