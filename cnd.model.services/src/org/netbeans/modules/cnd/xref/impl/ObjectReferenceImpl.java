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
