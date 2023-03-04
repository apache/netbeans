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
package org.netbeans.tax.io;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeEntityManager extends TreeEntityResolver {

    /** Creates new TreeEntityManager */
    public TreeEntityManager () {
    }

    /** Resolve entity.
     * @param publicId Public Identifier.
     * @param systemId System Identifier.
     * @param baseSystemId Base System Identifier
     * @return Resolved entity or <CODE>null</CODE>.
     */
    public TreeInputSource resolveEntity (String publicId, String systemId, String baseSystemId) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityManager.resolveEntity ( " + publicId + " , " + systemId + " , " + baseSystemId + " ) : null"); // NOI18N
        return null;
    }
    
    /** Add entity resolver to list of used resolvers.
     * @param entityResolver entity resolver to add
     */
    public void addEntityResolver (TreeEntityResolver entityResolver) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityManager.addEntityResolver ( " + entityResolver.getClass ().getName () + " )"); // NOI18N
    }
    
    /** Remove entity resolver from list of used.
     * @param entityResolver entity resolver to remove
     */
    public void removeEntityResolver (TreeEntityResolver entityResolver) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityManager.removeEntityResolver ( " + entityResolver.getClass ().getName () + " )"); // NOI18N
    }
    
    /** Resolve entity.
     * @param publicId Public Identifier.
     * @param systemId System Identifier.
     * @return Resolved entity or <CODE>null</CODE>.
     */
    public TreeInputSource resolveEntity (String publicId, String systemId) {
        TreeInputSource retValue;
        
        retValue = super.resolveEntity (publicId, systemId);
        return retValue;
    }
    
    /** Expand system identifier.
     * @param systemId System Identifier.
     * @param baseSystemId Base System Identifier.
     * @return Resolver system identifier.
     */
    public String expandSystemId (String systemId, String baseSystemId) {
        String retValue;
        
        retValue = super.expandSystemId (systemId, baseSystemId);
        return retValue;
    }
    
    /** Expand system identifier.
     * @param systemId System Identifier.
     * @return Expanded system identifier.
     */
    public String expandSystemId (String systemId) {
        String retValue;
        
        retValue = super.expandSystemId (systemId);
        return retValue;
    }
    
}
