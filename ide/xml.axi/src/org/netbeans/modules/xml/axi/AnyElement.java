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

import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;

/**
 * Represents 'any' element in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AnyElement extends AbstractElement {
    
    /**
     * Creates a new instance of Element
     */
    public AnyElement(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Element
     */
    public AnyElement(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this AXIComponent.
     */
    public AnyElement(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Element.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        return "any"; //NOI18N
    }
    
    /**
     * Returns the processContents.
     */
    public ProcessContents getProcessContents() {
        return processContents;
    }
    
    /**
     * Sets the processContents.
     */
    public void setProcessContents(ProcessContents value) {
        ProcessContents oldValue = getProcessContents();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue == value) ) {
            return;
        }
        this.processContents = value;
        firePropertyChangeEvent(PROP_PROCESSCONTENTS, oldValue, value);
    }
    
    /**
     * Returns the target namespace.
     */
    public String getTargetNamespace() {
        return namespace;
    }

    /**
     * Sets the target namespace.
     */
    public void setTargetNamespace(String value) {
        String oldValue = getTargetNamespace();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.namespace = value;
        firePropertyChangeEvent(PROP_NAMESPACE, oldValue, value);
    }
    
    /**
     * String representation of this Element.
     */
    public String toString() {        
        return getName();
    }	
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    private String namespace;
    private ProcessContents processContents;
    
    ////////////////////////////////////////////////////////////////////
    ////////////////// Properties for firing events ////////////////////
    ////////////////////////////////////////////////////////////////////
    public static final String PROP_NAMESPACE         = "namespace"; // NOI18N
    public static final String PROP_PROCESSCONTENTS   = "processContents"; // NOI18N
}
