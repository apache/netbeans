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

import org.openide.nodes.*;


/**
 * This cookie manager operates upon given CookieSet.
 *
 * @author  Petr Kuzel
 * @version
 */
public class DefaultCookieManager implements CookieManagerCookie {

    private final CookieSet set;

    /** Creates new DefaultCookieManager */
    public DefaultCookieManager(CookieSet set) {
        this.set = set;
    }

    /**
     * Remove given cookie from cookies managed by owner.
     * The owner is free to update its internal state according it.
     */
    public void removeCookie(Node.Cookie cake) {
        set.remove(cake);
    }    
    
    /**
     * Add given cookie into set of cookies ot this cookie owner.
     * Cookie owner is free to update its internal state according it.
     */
    public void addCookie(Node.Cookie cake) {
        set.add(cake);
    }
    
}
