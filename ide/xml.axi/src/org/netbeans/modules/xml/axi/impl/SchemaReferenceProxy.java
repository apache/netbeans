/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.SchemaReference;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;

/**
 *
 * @author sdedic
 */
public class SchemaReferenceProxy extends SchemaReference implements AXIComponentProxy {
    public SchemaReferenceProxy(AXIModel model, SchemaReference sharedComponent) {
        super(model, sharedComponent);
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    private SchemaReference getShared() {
        return (SchemaReference) getSharedComponent();
    }

    @Override
    public boolean isInclude() {
        return getShared().isInclude();
    }

    @Override
    public boolean isImport() {
        return getShared().isImport();
    }

    @Override
    public void accept(AXIVisitor visitor) {
        getShared().accept(visitor);
    }

    @Override
    public String getTargetNamespace() {
        return getShared().getTargetNamespace();
    }

    @Override
    public String getSchemaLocation() {
        return getShared().getSchemaLocation();
    }

    @Override
    public void setTargetNamespace(String targetNamespace) {
        getShared().setTargetNamespace(targetNamespace);
    }

    @Override
    public void setSchemaLocation(String schemaLocation) {
        getShared().setSchemaLocation(schemaLocation);
    }
}
