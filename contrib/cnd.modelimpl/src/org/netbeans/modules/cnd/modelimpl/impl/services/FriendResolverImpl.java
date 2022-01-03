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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmFriendResolver.class)
public final class FriendResolverImpl extends CsmFriendResolver {
    
    /** Creates a new instance of FriendResolverImpl */
    public FriendResolverImpl() {
    }
    
    /**
     * checks if target has declared 'friendDecl' as friend declaration, i.e.
     *  class target {
     *      friend class friendDecl;
     *      friend void friendDecl();
     *  };
     *
     *  void friendDecl() {
     *  }
     *
     *  void friendDecl::foo() {
     *  }
     * @param friendDecl declaration to check (not null)
     * @param target class to check
     * @return true if 'friendDecl' is the declarated friend declaration of 'target'
     * @throws IllegalArgumentException if friendDecl is null
     */
    @Override
    public boolean isFriend(CsmOffsetableDeclaration friendDecl, CsmClass target) {
        if (friendDecl == null) {
            throw new IllegalArgumentException("friendDecl must not be null"); // NOI18N
        }
        CsmClass containingClass = null;
        if (CsmKindUtilities.isMethodDefinition(friendDecl)){
            CsmFunction decl = CsmBaseUtilities.getFunctionDeclaration((CsmFunction)friendDecl);
            containingClass = ((CsmMember)decl).getContainingClass();
        } else if (CsmKindUtilities.isMethodDeclaration(friendDecl)) {
            containingClass = ((CsmMember)friendDecl).getContainingClass();
        }
        for (CsmFriend friend : target.getFriends()){
            if (CsmKindUtilities.isFriendClass(friend)){
                CsmFriendClass cls = (CsmFriendClass) friend;
                CsmClass reference = cls.getReferencedClass();
                if (friendDecl.equals(reference)){
                    return true;
                }
                if (containingClass != null && containingClass.equals(reference)) {
                    return true;
                }
                if (containingClass != null && isNestedClass(containingClass, reference)) {
                    return true;
                }
            } else if (CsmKindUtilities.isFriendMethod(friend)){
                if (friendDecl.equals(friend)) {
                    return true;
                }
                CsmFriendFunction fun = (CsmFriendFunction) friend;
                CsmFunction ref = fun.getReferencedFunction();
                if (friendDecl.equals(ref)) {
                    return true;
                }
                if (ref != null && CsmKindUtilities.isFunctionDefinition(ref)){
                    if (friendDecl.equals(((CsmFunctionDefinition)ref).getDeclaration())){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean isNestedClass(CsmClass inner, CsmClass outer) {
        return inner != null && outer != null &&
               CharSequenceUtilities.startsWith(inner.getQualifiedName(),outer.getQualifiedName());
    }
    
    /**
     * return all friend declarations for declaration, i.e.
     *  class target {
     *      friend class friendClass;
     *      friend void friendMethod();
     *  };
     *  class friendClass{
     *  }
     *  void friendMethod(){
     *  }
     *
     * @return friend class declaration "friendClass" for class declaration "friendClass" or
     *         friend method declaration "friendMethod" for method definition "friendMethod"
     */
    @Override
    public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl) {
        if (decl.isValid()) {
            CsmProject prj = decl.getContainingFile().getProject();
            if (prj instanceof ProjectBase) {
                return ((ProjectBase)prj).findFriendDeclarations(decl);
            }
        }
        return Collections.<CsmFriend>emptyList();
    }
}
