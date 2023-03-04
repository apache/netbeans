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
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.Element;

/**
 * Proxy compositor, acts on behalf of an original Compositor.
 * Delegates all calls to the original Compositor.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompositorProxy extends Compositor implements AXIComponentProxy {
            
    
    /**
     * Creates a new instance of CompositorProxy
     */
    public CompositorProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
            
    private Compositor getShared() {
        return (Compositor)getSharedComponent();
    }
    
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    /**
     * Returns the type of this content model.
     */
    public CompositorType getType() {
        return getShared().getType();
    }
    
    /**
     * Returns the type of this content model.
     */
    public void setType(CompositorType value) {
        getShared().setType(value);
    }
    
    /**
     * Returns the min occurs.
     */
    public String getMinOccurs() {
        return getShared().getMinOccurs();            
    }
    
    public void setMinOccurs(String value) {
        getShared().setMinOccurs(value);
    }
    
    /**
     * Returns the max occurs.
     */
    public String getMaxOccurs() {
        return getShared().getMaxOccurs();
    }
    
    public void setMaxOccurs(String value) {
        getShared().setMaxOccurs(value);
    }
        
    /**
     * Adds a Compositor as its child.
     */
    public void addCompositor(CompositorProxy compositor) {
        getShared().addCompositor(compositor);
    }
    
    /**
     * Removes an Compositor.
     */
    public void removeCompositor(CompositorProxy compositor) {
        getShared().removeCompositor(compositor);
    }
    
    /**
     * Adds an Element as its child.
     */
    public void addElement(Element element) {
        getShared().addElement(element);
    }
        
    /**
     * Removes an Element.
     */
    public void removeElement(Element element) {
        getShared().removeElement(element);
    }
    
    public String toString() {        
        return getShared().toString();
    }
    
}
