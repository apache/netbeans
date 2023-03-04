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
 * AXIDocumentImpl.java
 *
 * Created on May 10, 2006, 1:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi.impl;

import java.util.HashMap;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * AXIDocument implementation.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public final class AXIDocumentImpl extends AXIDocument {
    
    /**
     * Creates a new instance of AXIDocumentImpl
     */
    public AXIDocumentImpl(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    public AXIComponent findChild(SchemaComponent child) {
        //first try from cache, if not found, lookup children
        AXIComponent axiChild = globalChildrenCache.get(child);
        if(axiChild != null)
            return axiChild;
        for(AXIComponent c : getChildren()) {
            if(c.getPeer() == child) {
                addToCache(c);
                return c;
            }
        }
        return null;        
    }

    public void addToCache(AXIComponent child) {
        if(child.getPeer() == null)
            return;
        globalChildrenCache.put(child.getPeer(), child);
    }
    
    public void removeFromCache(AXIComponent child) {
        globalChildrenCache.remove(child.getPeer());
    }
    
    private HashMap<SchemaComponent, AXIComponent> globalChildrenCache =
            new HashMap<SchemaComponent, AXIComponent>();
}
