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

import java.util.List;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Abstract element.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractElement extends AXIContainer {
    
    /**
     * Creates a new instance of Element
     */
    public AbstractElement(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Element
     */
    public AbstractElement(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Element.
     */
    public AbstractElement(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Element.
     */
    public abstract void accept(AXIVisitor visitor);
            
    /**
     * Returns the MinOccurs.
     */
    public String getMinOccurs() {
        return minOccurs;
    }
    
    /**
     * Sets the MinOccurs.
     */
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
     * Returns the MaxOccurs.
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
    
    /**
     * Sets the MaxOccurs.
     */
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
     * true if #getMaxOccurs() and #getMinOccurs() allow multiciplity outside
     * [0,1], false otherwise. This method is only accurate after the element
     * has been inserted into the model.
     */
    public boolean allowsFullMultiplicity() {
		return !(getParent() instanceof Compositor && 
				((Compositor)getParent()).getType() == Compositor.CompositorType.ALL);
    }	
    
    protected String minOccurs = "1";
    protected String maxOccurs = "1";
    
    public static final String PROP_MINOCCURS     = "minOccurs"; // NOI18N
    public static final String PROP_MAXOCCURS     = "maxOccurs"; // NOI18N
    public static final String PROP_ELEMENT       = "element"; // NOI18N
}
