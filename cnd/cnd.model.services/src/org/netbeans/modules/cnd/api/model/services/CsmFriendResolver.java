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
