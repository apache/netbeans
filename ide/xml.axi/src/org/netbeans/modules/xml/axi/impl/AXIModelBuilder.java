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

import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.SchemaReference;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Creates and populates children for a parent AXIComponent.
 * Parent AXIComponent must first construct a AXIModelBuilder and then
 * call its populateChildren method to populate the list of children.
 *
 * This is a visitor, which visits each node in the schema model tree
 * to construct the AXI model.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelBuilder extends AbstractModelBuilder {

    /**
     * Creates a new instance of AXIModelBuilder
     */
    public AXIModelBuilder(AXIComponent parent) {
        super((AXIModelImpl)parent.getModel());
        this.parent = parent;
    }
            
    /**
     * Populates the children list for the specified schema component.
     * @param schemaComponent the component, for which, children and attribute
     * lists are to be populated.
     * @param visitChildren in general, this should be true, except when this is
     * being called for an Element's type. Purely to keep the type tree in the sharedPool.
     * @children children to be populated.
     * @attributes attributes to be populated.
     */
    public void populateChildren(SchemaComponent schemaComponent,
            boolean visitChildren, List<AXIComponent> children) {
        this.children = children;
                
        //For an Element's type (if global), we want to visit the
        //type so that it gets stored in the shared pool.
        if(!visitChildren) {
            schemaComponent.accept(this);
            return;
        }
        
        //visit all children
        for(SchemaComponent child : schemaComponent.getChildren()) {
            child.accept(this);
        }
    }

    /**
     * Visit Schema.
     */
    public void visit(Schema schema) {        
    }
    
    /**
     * Visit AnyElement.
     */
    public void visit(org.netbeans.modules.xml.schema.model.AnyElement schemaComponent) {
        AXIComponent child = getAXIComponent(schemaComponent, false);
        addChild(child);
    }
    
    /**
     * Visit AnyAttribute.
     */
    public void visit(AnyAttribute schemaComponent) {
        AXIComponent child = getAXIComponent(schemaComponent, false);
        addChild(child);
    }
    
    /**
     * Visit GlobalElement.
     */
    public void visit(GlobalElement component) {
        AXIComponent child = getAXIComponent(component, true);
        addChild(child);
    }
    
    /**
     * Visit LocalElement.
     */
    public void visit(LocalElement component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit ElementReference.
     */
    public void visit(ElementReference component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit GlobalAttribute.
     */
    public void visit(GlobalAttribute component) {
        AXIComponent child = getAXIComponent(component, true);
        addChild(child);
    }
    
    /**
     * Visit LocalAttribute.
     */
    public void visit(LocalAttribute component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit AttributeReference.
     */
    public void visit(AttributeReference component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit Sequence.
     */
    public void visit(Sequence component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit Choice.
     */
    public void visit(Choice component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit All.
     */
    public void visit(All component) {
        AXIComponent child = getAXIComponent(component, false);
        addChild(child);
    }
    
    /**
     * Visit GlobalGroup.
     */
    public void visit(GlobalGroup component) {
        AXIComponent child = getAXIComponent(component, true);
        addChild(child);
    }
    
    /**
     * Visit GroupReference.
     */
    public void visit(GroupReference component) {
        NamedComponentReference ref = component.getRef();
        if(ref == null)
            return;
        SchemaComponent sc = model.getReferenceableSchemaComponent(ref);
        if(sc == null)
            return;
        AXIComponent child = getAXIComponent(sc, true);
        addChild(child);
    }
    
    /**
     * Visit GlobalAttributeGroup.
     */
    public void visit(GlobalAttributeGroup component) {
        AXIComponent child = getAXIComponent(component, true);
        addChild(child);
    }
    
    /**
     * Visit AttributeGroupReference.
     */
    public void visit(AttributeGroupReference component) {
        NamedComponentReference ref = component.getGroup();
        if(ref == null)
            return;
        SchemaComponent sc = model.getReferenceableSchemaComponent(ref);
        if(sc == null)
            return;        
        AXIComponent child = getAXIComponent(sc, true);
        addChild(child);
    }
        
    /**
     * Visit GlobalComplexType.
     */
    public void visit(GlobalComplexType component) {
        AXIComponent child = getAXIComponent(component, true);
        addChild(child);
    }
        
    /**
     * Visit LocalComplexType.
     */
    public void visit(LocalComplexType component) {
        visitChildren(component);
    }
    
    /**
     * Visit ComplexContent.
     */
    public void visit(ComplexContent component) {
        visitChildren(component);
    }
    
    /**
     * Visit SimpleContent.
     */
    public void visit(SimpleContent component) {
        visitChildren(component);
    }
    
    /**
     * Visit ComplexExtension.
     */
    public void visit(SimpleExtension component) {
        NamedComponentReference ref = component.getBase();        
        if(ref != null) {
            SchemaComponent type = model.getReferenceableSchemaComponent(ref);
            if(type != null) {
                AXIComponent child = getAXIComponent(type, true);
                addChild(child);
            }
        }
        visitChildren(component);
    }
    
    /**
     * Visit ComplexExtension.
     */
    public void visit(ComplexExtension component) {
        NamedComponentReference ref = component.getBase();        
        if(ref != null) {
            SchemaComponent type = model.getReferenceableSchemaComponent(ref);
            if(type != null) {
                AXIComponent child = getAXIComponent(type, true);
                addChild(child);
            }
        }
        visitChildren(component);
    }

    @Override
    public void visit(Include include) {
        SchemaReference ref = model.getComponentFactory().createSchemaReference(include);
        addChild(ref);
        visitChildren(include);
    }

    @Override
    public void visit(Import im) {
        SchemaReference ref = model.getComponentFactory().createSchemaReference(im);
        addChild(ref);
        visitChildren(im);
    }
    
    /**
     * Returns an AXIComponent if one exists or creates one.
     * 
     * When this method gets called with a global component, it looks up the model
     * and returns the global AXI component, unless the parent is AXIDocument.
     * 
     * For local components, it always creates one.
     */
    private AXIComponent getAXIComponent(final SchemaComponent schemaComponent, boolean isGlobal) {
        //when parent is schema or for non-global components,
        //always create one.
        if(parent instanceof AXIDocument || !isGlobal) {
            return new AXIComponentCreator(getModel()).
                    createNew(schemaComponent);
        }

        if(!getModel().fromSameSchemaModel(schemaComponent)) {
            return getModel().lookupFromOtherModel(schemaComponent);
        }
        
        return getModel().lookup(schemaComponent);
    }
        
    /**
     * Adds the new component to the children list.
     */
    private void addChild(AXIComponent child) {
        if(child == null)
            return;
        
        if(parent instanceof AXIDocument) {
            children.add(child);
            ((AXIDocumentImpl)parent).addToCache(child);
            return;
        }
                
        if(child instanceof ContentModel) {
            Util.addProxyChildren(parent, child, children);
            return;
        }
        children.add(child);
    }
        
    // member variables
    /**
     * Parent AXIComponent for whom, children are being populated.
     */
    private AXIComponent parent;
    
    /**
     * Ref to the original children list being passed by the caller.
     */
    private List<AXIComponent> children;
}


