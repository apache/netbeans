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
package org.netbeans.modules.websvc.jaxws.catalog;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

public enum CatalogQNames {
    CATALOG("catalog"),
    SYSTEM("system"),
    NEXTCATALOG("nextCatalog");//,
    
    /*SYSTEMID("systemId"),
    URI("uri"),
    XPROJECT_CATALOGFILE_LOCATION("xprojectCatalogFileLocation"),
    REFERENCING_FILE("referencingFile");*/
    
    
    public static final String CATALOG_NS = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
    public static final String CATALOG_PREFIX = "cat";
    
    private static Set<QName> mappedQNames = new HashSet<QName>();
    static {
        mappedQNames.add(CATALOG.getQName());
        mappedQNames.add(SYSTEM.getQName());
        mappedQNames.add(NEXTCATALOG.getQName());
        
        /*mappedQNames.add(SYSTEMID.getQName());
        mappedQNames.add(URI.getQName());
        mappedQNames.add(XPROJECT_CATALOGFILE_LOCATION.getQName());
        mappedQNames.add(REFERENCING_FILE.getQName());*/
    }

    private QName qname;
    
    CatalogQNames(String localName) {
        qname = new QName(CATALOG_NS, localName, CATALOG_PREFIX);
    }
    
    public QName getQName() { 
        return qname; 
    }

    public String getLocalName() { 
        return qname.getLocalPart();
    }
    
    public String getQualifiedName() {
        return qname.getPrefix() + ":" + qname.getLocalPart();
    }
    
    public static Set<QName> getMappedQNames() {
        return Collections.unmodifiableSet(mappedQNames);
    }
}
