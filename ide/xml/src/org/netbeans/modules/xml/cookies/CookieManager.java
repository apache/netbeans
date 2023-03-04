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

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import org.openide.nodes.CookieSet;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.util.LookupManager;

/**
 * @author Libor Kramolis
 * @version 0.1
 */
public final class CookieManager extends LookupManager {
    /** */
    private final DataObject dataObject;
    /** */
    private final CookieSet cookieSet;
    /** */
    private final Map<CookieFactoryCreator, CookieFactory> factoryMap;


    //
    // init
    //

    /**
     */
    public CookieManager (DataObject dataObject, CookieSet cookieSet, Class clazz) {        
        if ( CookieFactoryCreator.class.isAssignableFrom (clazz) == false ) {
            throw new IllegalArgumentException ("Parameter class must extend CookieFactoryCreator class.");
        }

        this.dataObject = dataObject;
        this.cookieSet  = cookieSet;
        this.factoryMap = new HashMap<CookieFactoryCreator, CookieFactory>();

        register (clazz);

        addedToResult (getResult());
    }


    //
    // itself
    //

    /**
     */
    protected void removedFromResult (Collection removed) {
        Iterator it = removed.iterator();
        while ( it.hasNext() ) {
            CookieFactoryCreator creator = (CookieFactoryCreator) it.next();
            CookieFactory factory = this.factoryMap.remove (creator);
            if ( factory != null ) {
                factory.unregisterCookies (this.cookieSet);
            }
        }
    }

    /**
     */
    protected void addedToResult (Collection added) {
        //??? is getResult() meant here (rather than added)?

        Iterator it = getResult().iterator();
        while ( it.hasNext() ) {
            CookieFactoryCreator creator = (CookieFactoryCreator) it.next();
            CookieFactory factory = creator.createCookieFactory (this.dataObject);
            if ( factory != null ) {
                this.factoryMap.put (creator, factory);
                factory.registerCookies (this.cookieSet);
            }
        }
    }

}
