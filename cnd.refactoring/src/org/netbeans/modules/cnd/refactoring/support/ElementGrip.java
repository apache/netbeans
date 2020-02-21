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

package org.netbeans.modules.cnd.refactoring.support;

import javax.swing.Icon;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;

/**
 * This wrapper is used to create composition of scopes which is 
 * remembered once and is not affected by the following changes in code
 * based on ElementGrip from java refactoring
 */
public final class ElementGrip implements Openable {
    private static final boolean LAZY = false;
    private CsmUID<CsmOffsetable> thisObject;
    private String toString;
    private FileObject fileObject;
    private Icon icon;
    private ElementGrip parent;
    private boolean parentInited;
    
    /**
     * Creates a new instance of ElementGrip
     */
    public ElementGrip(CsmOffsetable object) {
        this.thisObject = CsmRefactoringUtils.getHandler(object);
        this.toString = LAZY ? null : CsmRefactoringUtils.getHtml(object);
        this.fileObject = CsmRefactoringUtils.getFileObject(object);
        this.icon = CsmImageLoader.getIcon(object);
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        if (toString == null && LAZY) {
            toString = CsmRefactoringUtils.getHtml(getResolved());
        }
        return toString;
    }

    /*package*/ void initParent() {
        if (!parentInited) {
            parent = ElementGripFactory.getDefault().getParent(this);
            parentInited = true;
        }
    }
    
    public ElementGrip getParent() {
        initParent();
        return parent;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    public CsmOffsetable getResolved() {
        return thisObject.getObject();
    }

    @Override
    public void open() {
        CsmOffsetable resolved = getResolved();
        if (resolved != null) {
            CsmUtilities.openSource(resolved);
        }
    }
    
}
