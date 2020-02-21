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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
