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
package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.retriever.catalog.model.System;
import org.w3c.dom.Element;

public class CatalogImpl extends CatalogComponentImpl implements Catalog {
    
    public CatalogImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public CatalogImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.CATALOG));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }

    public List<System> getSystems() {
       return super.getChildren(org.netbeans.modules.xml.retriever.catalog.model.System.class);
    }

    public void addSystem(System sid) {
        appendChild(Catalog.SYSTEM_PROP, sid);
    }

    public void removeSystem(System sid) {
        removeChild(Catalog.SYSTEM_PROP, sid);
    }

    public List<NextCatalog> getNextCatalogs() {
        return super.getChildren(NextCatalog.class);
    }

    public void addNextCatalog(NextCatalog ncat) {
        appendChild(Catalog.NEXTCATALOG_PROP, ncat);
    }

    public void removeNextCatalog(NextCatalog ncat) {
        removeChild(Catalog.NEXTCATALOG_PROP, ncat);
    }
    
    
}
