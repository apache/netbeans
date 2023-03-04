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

/*
 * NextCatalogImpl.java
 *
 * Created on December 6, 2006, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.net.URI;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogAttributes;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author girix
 */
public class NextCatalogImpl extends CatalogComponentImpl implements
        org.netbeans.modules.xml.retriever.catalog.model.NextCatalog{
    
    public NextCatalogImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public NextCatalogImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.NEXTCATALOG));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getCatalogAttr() {
        return getAttribute(CatalogAttributes.catalog);
    }

    public void setCatalogAttr(URI uri) {
        super.setAttribute(CATALOG_ATTR_PROP, CatalogAttributes.catalog, uri.toString());
    }
    
}
