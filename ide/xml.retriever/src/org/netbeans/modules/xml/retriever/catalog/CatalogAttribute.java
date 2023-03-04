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

import org.netbeans.modules.xml.xam.locator.*;

/**
 *
 * @author girix
 */
public enum CatalogAttribute {
    /*
     * Attribute on the catalog node
     */
    xmlns,
    
    /**
     * Attribute on all elements
     */
    id,
    /**
     * Key for 
     * - group 
     * - catalog
     */
    prefer,
    /**
     * Key for 
     * - public
     */
    publicId,
    /** 
     * Key for 
     * - system
     */
    systemId,
    /**
     * Value for 
     * - public
     * - system 
     * - uri
     */
    uri,
    /**
     * Key for 
     * - rewriteSystem 
     * - delegateSystem
     */
    systemIdStartString,
    /**
     * Value for 
     * - rewriteSystem 
     * - rewriteURI
     */
    rewritePrefix,
    /**
     * Key for 
     * - delegatePublic
     */
    publicIdStartString,
    /**
     * Value for 
     * -delegatePublic
     * -delegateSystem
     * -nextCatalog
     */
    catalog,
    /**
     * Key for 
     * - uri
     */
    name,
    /**
     * Key for 
     * - rewriteURI
     * - delegateURI
     */
    uriStartString,
    
    /*
     * Key for storing the Original URI of the local/internet resource
     * - not in the catalog definition
     */
    originalResourcePointer,
    
    /* 
     *Attribute for cross project resource's catalog file location
     */
    xprojectCatalogFileLocation,
    
    /*
     *Attribute for storing reference back to the resource that created this cross
     *project catalog entry.
     */
    referencingFiles;
}
