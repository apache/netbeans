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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Element implementation.
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public final class ElementImpl extends Element {
    
    /**
     * Creates a new instance of ElementImpl
     */
    public ElementImpl(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of ElementImpl
     */
    public ElementImpl(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Returns true if it is a reference, false otherwise.
     */
    public boolean isReference() {
        return false;
    }
    
    /**
     * Returns the referent if isReference() is true.
     */
    public Element getReferent() {
        return null;
    }
    
    /**
     * Returns abstract property.
     */
    public boolean getAbstract() {
        return isAbstract;
    }
    
    /**
     * Sets the abstract property.
     */
    public void setAbstract(boolean value) {
        boolean oldValue = getAbstract();
        if(oldValue != value) {
            this.isAbstract = value;
            firePropertyChangeEvent(PROP_ABSTRACT, oldValue, value);
        }
    }
    
    /**
     * Returns the block.
     */
    public String getBlock() {
        return block;
    }
    
    /**
     * Sets the block property.
     */
    public void setBlock(String value) {
        String oldValue = getBlock();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.block = value;
        firePropertyChangeEvent(PROP_BLOCK, oldValue, value);
    }
    
    /**
     * Returns the final property.
     */
    public String getFinal() {
        return finalValue;
    }
    
    /**
     * Sets the final property.
     */
    public void setFinal(String value) {
        String oldValue = getFinal();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.finalValue = value;
        firePropertyChangeEvent(PROP_FINAL, oldValue, value);
    }
    
    /**
     * Returns the fixed value.
     */
    public String getFixed() {
        return fixedValue;
    }
    
    /**
     * Sets the fixed value.
     */
    public void setFixed(String value) {
        String oldValue = getFixed();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.fixedValue = value;
        firePropertyChangeEvent(PROP_FIXED, oldValue, value);
    }
    
    /**
     * Returns the default value.
     */
    public String getDefault() {
        return defaultValue;
    }
    
    /**
     * Sets the default value.
     */
    public void setDefault(String value) {
        String oldValue = getDefault();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.defaultValue = value;
        firePropertyChangeEvent(PROP_DEFAULT, oldValue, value);
    }
    
    /**
     * Returns the form.
     */
    public Form getForm() {
        return form;
    }
    
    /**
     * Sets the form.
     */
    public void setForm(Form value) {
        Form oldValue = getForm();
        if(oldValue != value) {
            this.form = value;
            firePropertyChangeEvent(PROP_FORM, oldValue, value);
        }
    }
    
    /**
     * Returns the nillable.
     */
    public boolean getNillable() {
        return isNillable;
    }
    
    /**
     * Sets the nillable property.
     */
    public void setNillable(boolean value) {
        boolean oldValue = getNillable();
        if(oldValue != value) {
            this.isNillable = value;
            firePropertyChangeEvent(PROP_NILLABLE, oldValue, value);
        }
    }
    
    /**
     * Returns the AXIType.
     */
    public AXIType getType() {
        if(axiType != null)
            return axiType;
        if(getTypeSchemaComponent() == null) {
            SchemaComponent type = Util.getSchemaType((AXIModelImpl)getModel(), getPeer());
            setTypeSchemaComponent(type);
        }
        
        this.axiType = Util.getAXIType(this, getTypeSchemaComponent());
        return axiType;
    }
    
    /**
     * Sets the AXIType.
     */
    public void setType(AXIType newValue) {
        if( (this == newValue) ||
                (this.isGlobal() && (newValue instanceof Element)) )
            return;
        
        if(newValue instanceof Element) {
            setElementAsType(newValue);
            return;
        }
        
        AXIType oldValue = getType();
        if(!Util.canSetType(oldValue, newValue))
            return;
        
        updateChildren(oldValue, newValue);
        this.axiType = newValue;
        firePropertyChangeEvent(PROP_TYPE, oldValue, newValue);
        setTypeSchemaComponent(getSchemaType(newValue));
    }
    
    private void setElementAsType(final AXIType newValue) {
        if(newValue == this)
            return;
        int index = this.getIndex();
        AXIComponent parent = getParent();
        Element ref = getModel().getComponentFactory().createElementReference((Element)newValue);
        parent.removeChild(this);
        parent.insertAtIndex(Element.PROP_ELEMENT_REF, ref, index);
    }
    
    /**
     * Overwrites populateChildren of AXIComponent.
     *
     * An AXI element can keep LocalElement, GlobalElement or
     * ElementReference as its peer. For the local and global,
     * element, the element type's children becomes this AXI
     * element's children. For ElementReference, the global element's
     * type becomes this AXI element's children.
     */
    public void populateChildren(List<AXIComponent> children) {
        if(getPeer() == null) {
            return;
        }
        
        //populate children from the element's type
        SchemaComponent type = Util.getSchemaType((AXIModelImpl)getModel(), getPeer());
        setTypeSchemaComponent(type);
        
        if(type == null || type instanceof SimpleType) {
            if(this.isGlobal()) {
                handleSubstitutionGroup(getPeer(), children);
            }
            return;
        }
        
        AXIModelBuilder builder = new AXIModelBuilder(this);
        builder.populateChildren(type, false, children);
    }
    
    private void handleSubstitutionGroup(SchemaComponent sc, List<AXIComponent> children) {
        if(!(sc instanceof GlobalElement))
            return;
        GlobalElement ge = (GlobalElement)sc;
        NamedComponentReference<GlobalElement> ncr = ge.getSubstitutionGroup();
        if(ncr == null || ncr.get() ==null)
            return;
        GlobalElement head = ncr.get();
        Element axiHead = (Element)((AXIModelImpl)getModel()).findChild(head);
        Util.addProxyChildren(this, axiHead, children);        
    }
    
    /**
     * Returns the last value for the element's type.
     */
    SchemaComponent getTypeSchemaComponent() {
        return typeSchemaComponent;
    }
    
    /**
     * Sets the new value for the element's type.
     */
    void setTypeSchemaComponent(SchemaComponent type) {
        this.typeSchemaComponent = type;
    }
    
    private SchemaComponent getSchemaType(AXIType axiType) {
        if(axiType instanceof Datatype)
            return null;
        if(axiType instanceof ContentModel)
            return ((ContentModel)axiType).getPeer();
        if(axiType instanceof AnonymousType)
            return ((AnonymousType)axiType).getPeer();
        
        return null;
    }
    
    /**
     * OLD VALUE       NEW VALUE         RESULT
     * SType           LCT               return
     * SType           GCT               add proxy children
     * LCT             SType             remove all and return
     * LCT             GCT               remove all and add proxy children
     * GCT             SType             remove all and return
     * GCT             LCT               not allowed
     */
    private void updateChildren(AXIType oldValue, AXIType newValue) {
        //do not remove children if the old type was SimpleType and
        //user added children on top of it, that makes the type as anonymous.
        if(oldValue instanceof Datatype && newValue instanceof AnonymousType) {
            return;
        }
                
        //remove all children anyway
        removeAllChildren();
        if( (newValue == null) || (newValue instanceof Datatype) ) {
            return;
        }
        
        //remove listener from old content model
        if(oldValue instanceof ContentModel) {
            ContentModel cm = (ContentModel)oldValue;
            cm.removeListener(this);
        }
        
        //add proxy children for the new content model
        if(newValue instanceof ContentModel)
            Util.addProxyChildren(this, (ContentModel)newValue, null);
        
        if(newValue instanceof AnonymousType) {
            List<AXIComponent> children = new ArrayList<AXIComponent>();
            AXIModelBuilder builder = new AXIModelBuilder(this);
            builder.populateChildren( ((AnonymousType)newValue).getPeer(), false, children);
            for(AXIComponent child : children) {
                this.appendChild(child);
            }
        }
    }
    
    /**
     * Represents a local complex type.
     */
    public static class AnonymousType implements AXIType {
        private SchemaComponent schemaComponent;
        public AnonymousType(SchemaComponent schemaComponent) {
            this.schemaComponent = schemaComponent;
        }
        public String getName() {
            return null;
        }
        public void accept(AXIVisitor visitor) {
        }
        public SchemaComponent getPeer() {
            return schemaComponent;
        }
    }
    
    /**
     * Overwrites addCompositor of AXIContainer.
     */
    public void addCompositor(Compositor compositor) {
        if( getType() instanceof ContentModel &&
                getModel() != ((ContentModel)getType()).getModel() ) {
            //no drops allowed when the element's type
            //belongs to some other model
            return;
        }
        
        if(getType() instanceof Datatype) {
            setType(new AnonymousType(null));
        }
        super.addCompositor(compositor);
    }
    
    /**
     * Overwrites addAttribute of AXIContainer.
     */
    public void addAttribute(AbstractAttribute attribute) {
        AXIType type = getType();
        if(type instanceof ContentModel) {
            ((ContentModel)type).addAttribute(attribute);
            return;
        }
        
        if(type instanceof Datatype) {
            setType(new AnonymousType(null));
        }
    
        super.addAttribute(attribute);
    }
    
    
    private AXIType axiType;
    private SchemaComponent typeSchemaComponent;
}
