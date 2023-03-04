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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;

/**
 *
 * @author girix
 */
public class CatalogEntryImpl implements CatalogEntry {
    private CatalogElement entryType;
    //mapping string
    private String source;
    //mapped string
    private String target;
    
    private CatalogModel thisCatModel = null;
    
    private HashMap<String,String> extraAttributeMap = null;
    
    /**
     * one example of source is: systemId attribute value for system tag of catalog
     * one example of mappingEntity is: uri attribute value for system tag of catalog
     *
     * @param entryType - Catalog entry type as in public, system, rewriteSystem, etc.
     * @param source - source URL/String
     * @param target - Target URL/String
     */
    public CatalogEntryImpl(CatalogElement entryType, String mappingEntity, String mappedEntity) {
        this.entryType = entryType;
        this.source = mappingEntity;
        this.target = mappedEntity;
    }
    
    public CatalogEntryImpl(CatalogElement entryType, String mappingEntity, String mappedEntity, HashMap<String,String> extraAttribMap) {
        this.entryType = entryType;
        this.source = mappingEntity;
        this.target = mappedEntity;
        this.extraAttributeMap = extraAttribMap;
    }
    
    public CatalogElement getEntryType(){
        return entryType;
    }
    
    public String getSource(){
        return this.source;
    }
    
    public String getTarget(){
        return this.target;
    }
    
    public HashMap<String,String> getExtraAttributeMap(){
        return extraAttributeMap;
    }
    
    public boolean isValid() {
        if(thisCatModel == null)
            return false;
        ModelSource ms = null;
        try {
            //TODO remove null
            ms = thisCatModel.getModelSource(new URI(source), null);
        } catch (URISyntaxException ex) {
            return false;
        } catch (CatalogModelException ex) {
            return false;
        }
        if(ms != null)
            return true;
        return false;
    }
    
    public void setCatalogModel(CatalogModel thisCatModel){
        this.thisCatModel = thisCatModel;
    }
}
