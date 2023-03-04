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
