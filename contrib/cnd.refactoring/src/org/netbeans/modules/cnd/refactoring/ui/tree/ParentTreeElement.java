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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

/**
 * 
 */
public class ParentTreeElement implements TreeElement {
    
    private final CsmUID<CsmObject> element;
    private final Icon icon;
    private final String text;
    /** Creates a new instance of ParentTreeElement */
    public ParentTreeElement(CsmObject element) {
        this.element = CsmRefactoringUtils.getHandler(element);
        this.icon = CsmImageLoader.getIcon(element);
        this.text = CsmRefactoringUtils.getHtml(element);
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        CsmObject enclosing = getParent();
        if (enclosing != null) {
            return TreeElementFactory.getTreeElement(enclosing);
        } else {
            System.err.println("element without parent " + getUserObject());
            return null;
        }
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return text;
    }

    @Override
    public Object getUserObject() {
        return element.getObject();
    }
    
    private CsmObject getParent() {
        CsmObject obj = (CsmObject) getUserObject();
        CsmObject enclosing = null;
        if (obj != null) {
            enclosing = CsmRefactoringUtils.getEnclosingElement(obj);
        }
        return enclosing;
    }

}
