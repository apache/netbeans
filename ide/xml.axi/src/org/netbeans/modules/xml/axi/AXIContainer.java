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

import java.util.List;
import org.netbeans.modules.xml.axi.ContentModel.ContentModelType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.NbBundle;

/**
 * Represents a named component that can contain attributes,
 * for example an Element or a ContentModel. 
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIContainer extends AXIComponent {
    
    /**
     * Creates a new instance of AXIContainer.
     */
    public AXIContainer(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of AXIContainer.
     */
    public AXIContainer(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this AXIContainer.
     */
    public AXIContainer(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Returns the name.
     */
    public String getName() {
        if(name != null)
            return name;
        
        if(this instanceof Element)
            return NbBundle.getMessage(AXIContainer.class, "Unnamed-Element");
        
        if(this instanceof ContentModel) {
            ContentModelType type = ((ContentModel)this).getType();            
            switch (type) {
                case COMPLEX_TYPE:
                    return NbBundle.getMessage(AXIContainer.class, "Unnamed-ComplexType");
                case GROUP:
                    return NbBundle.getMessage(AXIContainer.class, "Unnamed-Group");
                case ATTRIBUTE_GROUP:
                    return NbBundle.getMessage(AXIContainer.class, "Unnamed-AttributeGroup");
            }
        }
        
        return NbBundle.getMessage(AXIContainer.class, "Unnamed-Component");
    }
    
    /**
     * Sets the name.
     */
    public void setName(String name) {
        String oldName = getName();
        if( (oldName == null && name == null) ||
                (oldName != null && oldName.equals(name)) ) {
            return;
        }
        
        this.name = name;
        firePropertyChangeEvent(PROP_NAME, oldName, name);
    }
    
    /**
     * Adds a Compositor as its child.
     * Compositor must always be at the 0th index.
     */
    public void addCompositor(Compositor compositor) {
        insertAtIndex(Compositor.PROP_COMPOSITOR, compositor, 0);
    }
    
    /**
     * Removes a Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        removeChild(Compositor.PROP_COMPOSITOR, compositor);
    }
    
    /**
     * Adds an Element as its child.
     * If attributes exist, add the new child before all attributes.
     * Attributes must always be added at the end of the list.
     */
    public void addElement(AbstractElement child) {
        if(this instanceof Element) {
            AXIType type = ((Element)this).getType();
            if(type instanceof ContentModel) {
                ((ContentModel)type).addElement(child);
                return;
            }
        }
        
        //if compositor does not exist, add one.
        Compositor c = getCompositor();
        if(c == null) {
            c = getModel().getComponentFactory().createSequence();
            addCompositor(c);
        }
        //add element to the compositor
        c.appendChild(AbstractElement.PROP_ELEMENT, child);
    }
    
    /**
     * Removes an Element.
     */
    public void removeElement(AbstractElement element) {
        removeChild(AbstractElement.PROP_ELEMENT, element);
    }
    
    /**
     * Adds an attribute.
     */
    public void addAttribute(AbstractAttribute attribute) {
        appendChild(AbstractAttribute.PROP_ATTRIBUTE, attribute);
    }
    
    /**
     * Removes an attribute.
     */
    public void removeAttribute(AbstractAttribute attribute) {
        removeChild(AbstractAttribute.PROP_ATTRIBUTE, attribute);
    }
    
    /**
     * Returns the compositor.
     */
    public Compositor getCompositor() {
        for(AXIComponent child: getChildren()) {
            if(Compositor.class.isAssignableFrom(child.getClass()))
                return (Compositor)child;
        }        
        return null;
    }
    
    /**
     * Returns the list of attributes.
     */
    public final List<AbstractAttribute> getAttributes() {
        return getChildren(AbstractAttribute.class);
    }
    
    protected String name;
	
    public static final String PROP_NAME          = "name"; // NOI18N
}
