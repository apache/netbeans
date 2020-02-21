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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
