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

import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.Form;

/**
 * Proxy element, acts on behalf of an Element.
 * Delegates all calls to the original element.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ElementProxy extends Element implements AXIComponentProxy {
                
    /**
     * Creates a new instance of ElementProxy
     */
    public ElementProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Returns the type of this component,
     * may be local, shared, proxy or reference.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
        
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public boolean isReference() {
        return getShared().isReference();
    }
    
    /**
     * Returns the referent if isReference() is true.
     */
    public Element getReferent() {
        return getShared().getReferent();
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return getShared().getName();
    }
    
    /**
     * Sets the name.
     */
    public void setName(String name) {
        getShared().setName(name);
    }
    
    /**
     * Returns the MinOccurs.
     */
    public String getMinOccurs() {
        return getShared().getMinOccurs();
    }
    
    /**
     * Sets the MinOccurs.
     */
    public void setMinOccurs(String value) {        
        getShared().setMinOccurs(value);
    }
	
    /**
     * Returns the MaxOccurs.
     */
    public String getMaxOccurs() {
        return getShared().getMaxOccurs();
    }
    
    /**
     * Sets the MaxOccurs.
     */
    public void setMaxOccurs(String value) {        
        getShared().setMaxOccurs(value);
    }
    
    /**
     * Returns abstract property.
     */
    public boolean getAbstract() {
        return getShared().getAbstract();
    }
    
    /**
     * Sets the abstract property.
     */
    public void setAbstract(boolean value) {
        getShared().setAbstract(value);
    }
    
    /**
     * Returns the block.
     */
    public String getBlock() {
        return getShared().getBlock();
    }
        
    /**
     * Sets the block property.
     */
    public void setBlock(String value) {
        getShared().setBlock(value);
    }
    
    /**
     * Returns the final property.
     */
    public String getFinal() {
        return getShared().getFinal();
    }
    
    /**
     * Sets the final property.
     */
    public void setFinal(String value) {
        getShared().setFinal(value);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return getShared().getFixed();
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {
        getShared().setFixed(value);
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return getShared().getDefault();
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {
        getShared().setDefault(value);
    }
    
    /**
     * Returns the form.
     */
    public Form getForm() {
        return getShared().getForm();
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form value) {
        getShared().setForm(value);
    }
        
    /**
     * Returns the nillable.
     */
    public boolean getNillable() {
        return getShared().getNillable();
    }
    
    /**
     * Sets the nillable property.
     */
    public void setNillable(boolean value) {
        getShared().setNillable(value);
    }
    
    /**
     * Adds a Compositor as its child.
     * Compositor must always be at the 0th index.
     */
    public void addCompositor(Compositor compositor) {
        getShared().addCompositor(compositor);
    }
    
    /**
     * Removes a Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        getShared().removeCompositor(compositor);
    }
    
    /**
     * Adds an Element as its child.
     * If attributes exist, add the new child before all attributes.
     * Attributes must always be added at the end of the list.
     */
    public void addElement(AbstractElement child) {
        getShared().addElement(child);
    }
    
    /**
     * Removes an Element.
     */
    public void removeElement(AbstractElement element) {
        getShared().removeElement(element);
    }
    
    /**
     * Adds an attribute.
     */
    public void addAttribute(AbstractAttribute attribute) {
        getShared().addAttribute(attribute);
    }
    
    /**
     * Removes an attribute.
     */
    public void removeAttribute(AbstractAttribute attribute) {
        getShared().removeAttribute(attribute);
    }
    
    /**
     * gets the type of this element.
     */	
	public AXIType getType() {
		return getShared().getType();
	}
	
    /**
     * sets the type of this element.
     */
    public void setType(AXIType type) {
        getShared().setType(type);
    }
	
    /**
     * Returns the compositor.
     */
    public Compositor getCompositor() {
        return getShared().getCompositor();
    }    
    
    Element getShared() {
        return (Element)getSharedComponent();
    }
    
    /**
     * Proxy doesn't get refreshed in the UI. We must notify.
     */
    void forceFireEvent() {
        firePropertyChangeEvent(Element.PROP_NAME, null, getName());
    }
}
