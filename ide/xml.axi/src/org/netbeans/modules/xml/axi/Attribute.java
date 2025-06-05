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
package org.netbeans.modules.xml.axi;

import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.datatype.StringType;
import org.netbeans.modules.xml.axi.impl.DatatypeBuilder;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

/**
 * Represents an attribute in XML Schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class Attribute extends AbstractAttribute implements AXIType {
	
    /**
     * Creates a new instance of Attribute
     */
    public Attribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Attribute
     */
    public Attribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }

    /**
     * Creates a proxy for this Attribute.
     */
    public Attribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Attribute.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public abstract boolean isReference();
    
    /**
     * Returns the referent if isReference() is true.
     */
    public abstract Attribute getReferent();
    
    /**
     * Sets the name.
     */
    public abstract void setName(String name);
        
    /**
     * Returns the type. This is expensive, since it uses a visitor
     * to traverse to obtain the type information.
     */    
    public abstract AXIType getType();
    
    /**
     * Sets the type.
     */
    public abstract void setType(AXIType type);
    	
    /**
     * Returns the form.
     */
    public abstract Form getForm();
    
    /**
     * Sets the form.
     */
    public abstract void setForm(Form form);
    
    /**
     * Returns the fixed value.
     */
    public abstract String getFixed();
    
    /**
     * Sets the fixed value.
     */
    public abstract void setFixed(String value);
    
    /**
     * Returns the default value.
     */
    public abstract String getDefault();
    
    /**
     * Sets the default value.
     */
    public abstract void setDefault(String value);
    
    /**
     * Returns the use.
     */
    public abstract Use getUse();
    
    /**
     * Sets the use.
     */
    public abstract void setUse(Use use);
        
    /**
     * Returns the string representation of this Attribute.
     */
    public String toString() {        
        return getName();              //NOI18N
    }
    
    // member variables
    protected String name;
    protected Form form;
    protected Use use;
    protected String defaultValue;
    protected String fixedValue;
    protected AXIType datatype;

    // Properties for firing events
    public static final String PROP_NAME            = "name"; //NOI18N
    public static final String PROP_FORM            = "form"; //NOI18N
    public static final String PROP_USE             = "use"; //NOI18N
    public static final String PROP_DEFAULT         = "default"; //NOI18N
    public static final String PROP_FIXED           = "fixed"; //NOI18N
    public static final String PROP_TYPE            = "type"; //NOI18N    
    public static final String PROP_ATTRIBUTE_REF   = "attributeRef"; // NOI18N
}
