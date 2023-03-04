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

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;

/**
 * Proxy for AnyAttribute, acts on behalf of an AnyAttribute.
 * Delegates all calls to the original attribute.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AnyAttributeProxy extends AnyAttribute implements AXIComponentProxy {
    
    /**
     * Creates a new instance of AnyAttributeProxy
     */
    public AnyAttributeProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }    
    
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    private AnyAttribute getShared() {
        return (AnyAttribute)getSharedComponent();
    }
	
    public String getTargetNamespace() {
        return getShared().getTargetNamespace();
    }

    public void setTargetNamespace(String value) {
        getShared().setTargetNamespace(value);
    }
    
    public ProcessContents getProcessContents() {
        return getShared().getProcessContents();
    }
    
    public void setProcessContents(ProcessContents value) {
        getShared().setProcessContents(value);
    }
    
    public String toString() {
        return getShared().toString();
    }
}
