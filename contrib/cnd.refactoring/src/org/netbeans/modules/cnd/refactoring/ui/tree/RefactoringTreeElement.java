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
import org.netbeans.modules.cnd.refactoring.support.ElementGrip;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * presentation of a leaf for refactoring element
 */
public class RefactoringTreeElement implements TreeElement { 
    
    private final RefactoringElement refactoringElement;
    private final Object parent;
    private final Icon icon;
    
    RefactoringTreeElement(RefactoringElement element) {
        this.refactoringElement = element;
        final Lookup lookup = element.getLookup(); 
        Object curParent = lookup.lookup(ElementGrip.class); 
        if (curParent == null) {
            curParent = lookup.lookup(FileObject.class);
        }
        this.parent = curParent;
        this.icon = lookup.lookup(Icon.class);
    }
    
    @Override
    public TreeElement getParent(boolean isLogical) {
        Object curParent = null;
        if (isLogical) {
            curParent = this.parent;
        } else {
            curParent = this.parent instanceof ElementGrip ? ((ElementGrip)this.parent).getFileObject() : this.parent;
        }
        return curParent != null ? TreeElementFactory.getTreeElement(curParent) : null;
    }
    
    @Override
    public Icon getIcon() {
        return icon;   
    }

    @Override
    public String getText(boolean isLogical) {
        return refactoringElement.getDisplayText();
    }

    @Override
    public Object getUserObject() {
        return refactoringElement;
    }
    
//    private CsmObject getCsmParent() {
//        CsmOffsetable obj = null;// thisObject.getObject();
//        CsmObject enclosing = null;
//        if (obj != null) {
//            enclosing = CsmRefactoringUtils.getEnclosingElement((CsmObject)obj);
//        }
//        return enclosing;
//    }
}
