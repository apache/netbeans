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

package org.openide.loaders;

import java.io.*;
import java.util.*;

import org.xml.sax.*;

/**
 * A utility class allowing to chain multiple EntityResolvers into one delegating
 * resolution process on them.
 * <p>
 * If all registered resolves are thread-safe then the implementation is thread-safe too.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
final class XMLEntityResolverChain implements EntityResolver {

    /**
     * Chain of resolvers contaning all EntityResolvers registred by a user.
     */
    private final List<EntityResolver> resolverChain = new ArrayList<EntityResolver>(3);
    
    
    /** Creates new EntityResolverChain instance */
    public XMLEntityResolverChain() {
    }
    
    /**
     * Add a given entity resolver to the resolver chain.
     * The resolver chain is then searched by resolveEntity() method
     * until some registered resolver succed.
     *
     * @param resolver - a non null resolver to be added
     *
     * @return true if successfully added
     */
    public boolean addEntityResolver(EntityResolver resolver) {
        if (resolver == null) throw new IllegalArgumentException();
        synchronized (resolverChain) {
            if (resolverChain.contains(resolver)) return false;
            return resolverChain.add(resolver);
        }
    }

    /**
     * Remove a given entity resolver from the entity resolver chain.
     *
     * @param resolver - a non null resolver to be removed
     *
     * @return removed resolver instance or null if not present
     */    
    public EntityResolver removeEntityResolver(EntityResolver resolver) {
        if (resolver == null) throw new IllegalArgumentException();
        synchronized (resolverChain) {
            int index = resolverChain.indexOf(resolver);
            if ( index < 0 ) return null;
            return resolverChain.remove(index);
        }
    }

    /**
     * Chaining resolveEntity() implementation iterating over registered resolvers.
     *
     * @param publicID - The public identifier of the external entity being referenced, 
     *        or null if none was supplied.
     * @param systemID - The system identifier of the external entity being referenced.
     *
     * @throws SAXException - Any SAX exception, possibly wrapping another exception.
     * @throws IOException - A Java-specific IO exception, possibly the result of creating
     *        a new InputStream or Reader for the InputSource.
     *
     * @return An InputSource object describing the new input source, 
     *         or null to request that the parser open a regular URI connection to the system identifier.
     */
    public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
        
        // user's resolver chain
        SAXException lsex = null;
        IOException lioex = null;
        
        synchronized (resolverChain) {
            Iterator<EntityResolver> it = resolverChain.iterator();
            while (it.hasNext()) {
                EntityResolver resolver = it.next();
                try {
                    InputSource test = resolver.resolveEntity(publicID, systemID);
                    if (test == null) continue;
                    return test;
                } catch (SAXException sex) {
                    lsex = sex;
                    continue;
                } catch (IOException ioex) {
                    lioex = ioex;
                    continue;
                }
            }
            
            //retain the last exception for diagnostics purposes
            
            if (lioex != null) throw lioex;  
            if (lsex != null) throw lsex;
            
            //just can not resolve it            
            return null;  
        }
    }        
}
