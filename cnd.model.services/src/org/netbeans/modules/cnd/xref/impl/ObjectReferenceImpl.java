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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.xref.impl;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 * reference on object (has owner and target as passed object)
 */
/*package*/ class ObjectReferenceImpl implements CsmReference {

    private final CsmUID<CsmObject> targetDelegate;
    private final CsmUID<CsmObject> ownerDelegate;
    private final CsmUID<CsmObject> topDelegate;
    private final CsmUID<CsmFile> fileUID;
    
    private final int startPosition;
    private final int endPosition;   
    private final CsmReferenceKind kind;
    /*package*/ ObjectReferenceImpl(CsmUID<CsmObject> target, 
            CsmUID<CsmObject> owner, CsmUID<CsmObject> topUID, CsmUID<CsmFile> file,
            CsmReferenceKind kind, int startRef, int endRef) {
        this.targetDelegate = target;
        this.ownerDelegate = owner;
        topDelegate = topUID;
        this.fileUID = file;
        this.startPosition = startRef;
        this.endPosition = endRef;    
        this.kind = kind;
    }

    @Override
    public CsmObject getReferencedObject() {
        return targetDelegate.getObject();
    }

    @Override
    public CsmObject getOwner() {
        return ownerDelegate.getObject();
    }

    @Override
    public CsmObject getClosestTopLevelObject() {
        return topDelegate.getObject();
    }

    @Override
    public CsmReferenceKind getKind() {
        return this.kind;
    }
        
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObjectReferenceImpl other = (ObjectReferenceImpl) obj;
        if (this.startPosition != other.startPosition) {
            return false;
        }
        if (this.endPosition != other.endPosition) {
            return false;
        }
        if (this.targetDelegate != other.targetDelegate && (this.targetDelegate == null || !this.targetDelegate.equals(other.targetDelegate))) {
            return false;
        }
        if (this.ownerDelegate != other.ownerDelegate && (this.ownerDelegate == null || !this.ownerDelegate.equals(other.ownerDelegate))) {
            return false;
        }
        if (this.fileUID != other.fileUID && (this.fileUID == null || !this.fileUID.equals(other.fileUID))) {
            return false;
        }
        return true;
    }

    @Override
    public int getStartOffset() {
        return startPosition;
    }
    
    @Override
    public int getEndOffset() {
        return endPosition;
    }

    @Override
    public Position getStartPosition() {
        throw new UnsupportedOperationException("use getStartOffset instead");//NOI18N
    }
    
    @Override
    public Position getEndPosition() {
        throw new UnsupportedOperationException("use getEndOffset instead");//NOI18N
    }  
    
    @Override
    public CsmFile getContainingFile() {
        return _getFile();
    }

    @Override
    public CharSequence getText() {
        CsmFile file = getContainingFile();
        if (file != null) {
            return TextCache.getManager().getString(file.getText(getStartOffset(), getEndOffset()));
        }
        return "";
    }
    
    private CsmFile _getFile() {
        CsmFile file = fileUID.getObject();
        return file;
    }
    
    // test trace method
    protected String getOffsetString() {
        return "[" + getStartOffset() + "-" + getEndOffset() + "]"; // NOI18N
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + startPosition;
        hash = 97 * hash + endPosition;
        hash = 97 * hash + (this.targetDelegate != null ? this.targetDelegate.hashCode() : 0);
        hash = 97 * hash + (this.ownerDelegate != null ? this.ownerDelegate.hashCode() : 0);
        hash = 97 * hash + (this.fileUID != null ? this.fileUID.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Object Reference: " + (this.targetDelegate != null ? targetDelegate.toString() : getOffsetString()); // NOI18N
    }
}
