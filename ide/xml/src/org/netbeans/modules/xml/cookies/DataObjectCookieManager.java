/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.cookies;

import org.netbeans.modules.xml.util.Util;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.cookies.*;

/**
 * This implementation of CookieManagerCookie updates modified property of
 * passed DataObject according of presence of SaveCookie.
 *
 * @author  Petr Kuzel
 * @version
 */
public class DataObjectCookieManager extends DefaultCookieManager {

    private final DataObject dobj;

    /** Creates new DataObjectCookieManager */
    public DataObjectCookieManager(DataObject dobj, CookieSet set) {
        super(set);
        this.dobj = dobj;
    }

    /**
     * Remove given cookie from cookies managed by owner.
     * The owner is free to update its internal state according it.
     */
    public void removeCookie(Node.Cookie cake) {
        
        //??? place it into RP? it fires. It should do callee, it knows about locks!

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("DataObjectCookieManager::removeCookie " + cake.getClass()); // NOI18N
        
        super.removeCookie(cake);
        
        // any save cookie subclass means the we are not modified any more
        if (SaveCookie.class.isAssignableFrom (cake.getClass())) {
            dobj.setModified (false);
        }
    }
    
    /**
     * Add given cookie into set of cookies ot this cookie owner.
     * Cookie owner is free to update its internal state according it.
     */
    public void addCookie(Node.Cookie cake) {
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("DataObjectCookieManager::addCookie " + cake.getClass()); // NOI18N
        
        super.addCookie(cake);
        
        // any save cookie subclass means the we are modified
        if (SaveCookie.class.isAssignableFrom (cake.getClass())) {
            dobj.setModified (true);
        }        
    }
    
}
