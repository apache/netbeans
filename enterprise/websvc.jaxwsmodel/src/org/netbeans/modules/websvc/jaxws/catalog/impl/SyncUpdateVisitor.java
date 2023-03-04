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
package org.netbeans.modules.websvc.jaxws.catalog.impl;

import org.netbeans.modules.websvc.jaxws.catalog.NextCatalog;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogComponent;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogVisitor;
import org.netbeans.modules.websvc.jaxws.catalog.Catalog;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

public class SyncUpdateVisitor extends CatalogVisitor.Default implements ComponentUpdater<CatalogComponent> {
    private CatalogComponent target;
    private Operation operation;
    private int index;
    
    public SyncUpdateVisitor() {
    }
    
    public void update(CatalogComponent target, CatalogComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }
    
    public void update(CatalogComponent target, CatalogComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }
    
    private void insert(String propertyName, CatalogComponent component) {
        ((CatalogComponentImpl)target).insertAtIndex(propertyName, component, index);
    }
    
    private void remove(String propertyName, CatalogComponent component) {
        ((CatalogComponentImpl)target).removeChild(propertyName, component);
    }
    
    public void visit(org.netbeans.modules.websvc.jaxws.catalog.System system) {
        if (target instanceof Catalog) {
            if (operation == Operation.ADD) {
                insert(Catalog.SYSTEM_PROP, system);
            } else {
                remove(Catalog.SYSTEM_PROP, system);
            }
        }
    }
    
    public void visit(NextCatalog nextCatalog) {
        if (target instanceof Catalog) {
            if (operation == Operation.ADD) {
                insert(Catalog.NEXTCATALOG_PROP, nextCatalog);
            } else {
                remove(Catalog.NEXTCATALOG_PROP, nextCatalog);
            }
        }
    }
    
}
