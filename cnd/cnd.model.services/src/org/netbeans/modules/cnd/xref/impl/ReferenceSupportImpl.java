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

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;

/**
 *
 */
public class ReferenceSupportImpl {
    public CsmReference createObjectReference(CsmOffsetable obj) {
        return createObjectReference(obj, obj);
    }

    public CsmReference createObjectReference(CsmObject target, CsmOffsetable owner) {
        int start = getStartRefenceOffset(owner);
        int end = getEndReferenceOffset(owner);
        CsmUID<CsmObject> targetUID = target != null ? getUID(target) : null;
        CsmUID<CsmObject> ownerUID = getUID((CsmObject)owner);
        CsmUID<CsmFile> fileUID = getUID(owner.getContainingFile());
        CsmReferenceKind kind = getObjectKind(target, owner);
        CsmObject top = CsmBaseUtilities.findClosestTopLevelObject((CsmObject) owner);
        CsmUID<CsmObject> topUID = getUID(top);
        return new ObjectReferenceImpl(targetUID, ownerUID, topUID, fileUID, kind, start, end);
    }

    private CsmReferenceKind getObjectKind(CsmObject target, CsmObject owner) {
        CsmReferenceKind kind = CsmReferenceKind.UNKNOWN;
        if (target == null && CsmKindUtilities.isInclude(owner)) {
            kind = CsmReferenceKind.DIRECT_USAGE;
        } else if (CsmKindUtilities.isFile(target)) {
            kind = CsmReferenceKind.DIRECT_USAGE;
        } else if (target != null) {
            if (owner != null && !owner.equals(target)) {
                CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, true);
                CsmObject targetDecl = decDef[0];
                CsmObject targetDef = decDef[1];        
                kind = CsmReferenceKind.DIRECT_USAGE;
                if (CsmKindUtilities.isClassForwardDeclaration(owner) || 
                    CsmClassifierResolver.getDefault().isForwardClassifier(owner)) {
                    kind = CsmReferenceKind.DIRECT_USAGE;
                } else if (owner.equals(targetDef)) {
                    kind = CsmReferenceKind.DEFINITION;
                } else if (CsmReferenceSupport.sameDeclaration(owner, targetDecl)) {
                    kind = CsmReferenceKind.DECLARATION;
                }
            } else {
                kind = CsmReferenceKind.DECLARATION;
                if (CsmKindUtilities.isFunctionDefinition(target) ||
                        CsmKindUtilities.isVariableDefinition(target)) {
                    kind = CsmReferenceKind.DEFINITION;
                }
            }
        }
        return kind;
    }
    
    private int getStartRefenceOffset(CsmOffsetable obj) {
        return obj.getStartOffset();
    }
    
    private int getEndReferenceOffset(CsmOffsetable obj) {
        int end = obj.getEndOffset();
        if (CsmKindUtilities.isClass(obj)) {
            end = ((CsmClass) obj).getLeftBracketOffset();
        } else if (CsmKindUtilities.isFunctionDefinition(obj)) {
            if (((CsmFunctionDefinition) obj).getBody() != null) {
                end = ((CsmFunctionDefinition) obj).getBody().getStartOffset();
            }
        }
        return end;
    }
    
    public <T extends CsmObject> CsmUID<T> getUID(T element) {
        return UIDs.get(element);
    }
}
