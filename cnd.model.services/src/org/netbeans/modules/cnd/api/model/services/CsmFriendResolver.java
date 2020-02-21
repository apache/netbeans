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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.openide.util.Lookup;

/**
 * entry point to resolve friends of declarations
 */
public abstract class CsmFriendResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmFriendResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmFriendResolver defaultResolver;
    
    protected CsmFriendResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmFriendResolver getDefault() {
        /*no need for sync synchronized access*/
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmFriendResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
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
     *
     * @return true if 'friendDecl' is the declarated friend declaration of 'target'
     */
    public abstract boolean isFriend(CsmOffsetableDeclaration friendDecl, CsmClass target);
    
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
    public abstract Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl);

    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmFriendResolver {
        Empty() {
        }
        @Override
        public boolean isFriend(CsmOffsetableDeclaration friendDecl, CsmClass target) {
            return false;
        }

        @Override
        public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl) {
            return Collections.<CsmFriend>emptyList();
        }
    }    
}
