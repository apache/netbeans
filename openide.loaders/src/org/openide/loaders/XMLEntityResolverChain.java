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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
     * @throw SAXException - Any SAX exception, possibly wrapping another exception. 
     * @throw IOException - A Java-specific IO exception, possibly the result of creating 
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
            Iterator it = resolverChain.iterator();
            while (it.hasNext()) {
                EntityResolver resolver = (EntityResolver) it.next();
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
