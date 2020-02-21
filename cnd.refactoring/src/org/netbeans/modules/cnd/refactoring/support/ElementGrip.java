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
