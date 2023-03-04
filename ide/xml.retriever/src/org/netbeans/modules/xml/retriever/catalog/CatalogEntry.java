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

package org.netbeans.modules.xml.retriever.catalog;

import java.util.HashMap;
import org.netbeans.modules.xml.xam.locator.*;

/**
 *
 * @author girix
 */
public interface CatalogEntry {
    
    /**
     * entryType - Catalog entry type as in public, system, rewriteSystem, etc.
     **/
    public CatalogElement getEntryType();
    
    /**
     * one example of source is: systemId attribute value for system tag of catalog
     **/
    public String getSource();
    /**
     * one example of mappingEntity is: uri attribute value for system tag of catalog
     */
    public String getTarget();
    
    /**
     * If catalog is augmented with extra attributes, then the key of the HashMap will
     * be the attribute key and the value of the HashMap entry will be the value of the 
     * attribute
     */
    public HashMap<String,String> getExtraAttributeMap();
    
    
    /**
     * If this catalog entry is does not resolve to a file then valid=false
     */
    public boolean isValid();
    
}
