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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;

/**
 * Represents a compositor in XML Schema. A compositor can be
 * one of Sequence, Choice or All.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class Compositor extends AXIComponent {
            
    /**
     * Represents the type of this compositor.
     * Can be one of sequence, choice and all.
     */
    public static enum CompositorType {
        SEQUENCE, CHOICE, ALL;
                
        public String getName() {
            return toString();
        }
        
        public String toString() {
            String retValue = super.toString();
            return retValue.substring(0,1) + retValue.substring(1).toLowerCase();
        }
    }
    
    /**
     * Creates a new instance of Compositor 
     */
    Compositor(AXIModel model, CompositorType type) {
        super(model);
        this.type = type;
    }
    
    /**
     * Creates a new instance of Compositor
     */
    Compositor(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
        if(schemaComponent instanceof Sequence)
            type = CompositorType.SEQUENCE;
        if(schemaComponent instanceof Choice)
            type = CompositorType.CHOICE;
        if(schemaComponent instanceof All)
            type = CompositorType.ALL;            
    }
    
    /**
     * Creates a proxy Compositor
     */
    public Compositor(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the type of this compositor.
     */
    public CompositorType getType() {
        return type;
    }
        
    /**
     * Sets the type of this compositor.
     */
    public void setType(CompositorType value) {
        if(getModel() == null)
            return;
        getModel().startTransaction();
        try{
            firePropertyChangeEvent(Compositor.PROP_TYPE, getType(), value);
        }finally{
            getModel().endTransaction();
            try {
                ((AXIModelImpl)getModel()).setForceSync(true);
                getModel().sync();
            } catch(IOException iox) {
            } finally {
                if(getModel() != null)
                    ((AXIModelImpl)getModel()).setForceSync(false);
            }            
        }
    }
        
    void setCompositorType(Compositor.CompositorType newType) {
        this.type = newType;
    }
    
    /**
     * Returns the min occurs.
     */
    public String getMinOccurs() {
        return minOccurs;
    }
    
    public void setMinOccurs(String value) {        
        String oldValue = getMinOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.minOccurs = value;
        firePropertyChangeEvent(PROP_MINOCCURS, oldValue, value);
    }
    
    /**
     * Returns the max occurs.
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
    
    public void setMaxOccurs(String value) {
        String oldValue = getMaxOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.maxOccurs = value;
        firePropertyChangeEvent(PROP_MAXOCCURS, oldValue, value);
    }
        
    /**
     * Adds a Compositor as its child.
     */
    public void addCompositor(Compositor compositor) {
        appendChild(Compositor.PROP_COMPOSITOR, compositor);
    }
    
    /**
     * Removes an Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        removeChild(Compositor.PROP_COMPOSITOR, compositor);
    }
    
    /**
     * Adds an Element as its child.
     */
    public void addElement(Element element) {
        appendChild(Element.PROP_ELEMENT, element);
    }
        
    /**
     * Removes an Element.
     */
    public void removeElement(Element element) {
        removeChild(Element.PROP_ELEMENT, element);
    }
	
    /**
     * true if #getMaxOccurs() and #getMinOccurs() allow multiciplity outside
     * [0,1], false otherwise. This method is only accurate after the element
     * has been inserted into the model.
     */
    public boolean allowsFullMultiplicity() {
		return !(getParent() instanceof Compositor && 
				((Compositor)getParent()).getType() == CompositorType.ALL);
    }	
    
    public String toString() {
        if(type == null)
            return null;
        
        return type.toString();
    }
    
    private CompositorType type;
    private String minOccurs;
    private String maxOccurs;
    
    public static final String PROP_COMPOSITOR  = "compositor"; // NOI18N
    public static final String PROP_MINOCCURS   = "minOccurs"; // NOI18N
    public static final String PROP_MAXOCCURS   = "maxOccurs"; // NOI18N
    public static final String PROP_TYPE        = "type";       // NOI18N
}
