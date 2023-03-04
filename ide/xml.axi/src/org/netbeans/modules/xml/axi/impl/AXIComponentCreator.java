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
import org.netbeans.modules.xml.axi.AXIComponentFactory;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaReference;
import org.netbeans.modules.xml.schema.model.*;

/**
 * This is a visitor, which visits a specified schema component
 * and creates an AXI component. Not every schema component will
 * have a corresponding AXI component. The things we care in AXI
 * are, element, attribute, compositor, references and schema constructs
 * that will yield these as children.
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIComponentCreator extends AbstractModelBuilder {

    /**
     * Creates a new instance of AXIComponentCreator
     */
    public AXIComponentCreator(AXIModelImpl model) {
        super(model);
        factory = model.getComponentFactory();
    }

    /**
     * Create an AXI component from a schema component.
     */
    AXIComponent createNew(SchemaComponent schemaComponent) {
        schemaComponent.accept(this);
        
        return newAXIComponent;
    }
    
    /**
     * Visit Schema.
     */
    public void visit(Schema schema) {
        newAXIComponent = new AXIDocumentImpl(model, schema);
    }
    
    /**
     * Visit AnyElement.
     */
    public void visit(AnyElement schemaComponent) {
        org.netbeans.modules.xml.axi.AnyElement element = factory.
                createAnyElement(schemaComponent);
        Util.updateAnyElement(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit AnyAttribute.
     */
    public void visit(AnyAttribute schemaComponent) {
        org.netbeans.modules.xml.axi.AnyAttribute attribute = factory.
                createAnyAttribute(schemaComponent);
        Util.updateAnyAttribute(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit GlobalElement.
     */
    public void visit(GlobalElement schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        
        Element element = factory.createElement(schemaComponent);
        Util.updateGlobalElement(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit LocalElement.
     */
    public void visit(LocalElement component) {
        Element element = factory.createElement(component);
        Util.updateLocalElement(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit ElementReference.
     */
    public void visit(ElementReference component) {
        if(component == null || component.getRef() == null)
            return;
        SchemaComponent originalElement = component.getRef().get();
        if(originalElement == null)
            return;
        AXIComponent referent = null;
        if(!model.fromSameSchemaModel(originalElement)) {
            referent = model.lookupFromOtherModel(originalElement);
        } else {
            referent = model.lookup(originalElement);
        }
        assert (referent != null);
        Element element = factory.createElementReference(component, (Element)referent);
        Util.updateElementReference(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit GlobalAttribute.
     */
    public void visit(GlobalAttribute schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        Attribute attribute = factory.createAttribute(schemaComponent);
        Util.updateGlobalAttribute(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit LocalAttribute.
     */
    public void visit(LocalAttribute component) {
        Attribute attribute = factory.createAttribute(component);
        Util.updateLocalAttribute(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit AttributeReference.
     */
    public void visit(AttributeReference component) {
        if(component == null || component.getRef() == null)
            return;        
        SchemaComponent originalElement = component.getRef().get();
        if(originalElement == null)
            return;
        AXIComponent referent = null;
        if(!model.fromSameSchemaModel(originalElement)) {
            referent = model.lookupFromOtherModel(originalElement);
        } else {
            referent = model.lookup(originalElement);
        }
        assert(referent != null);
        Attribute attribute = factory.createAttributeReference(component,
                (Attribute)referent);
        Util.updateAttributeReference(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit Sequence.
     */
    public void visit(Sequence component) {
        Compositor compositor = factory.createSequence(component);
        Util.updateCompositor(compositor);
        newAXIComponent = compositor;
    }
    
    /**
     * Visit Choice.
     */
    public void visit(Choice component) {
        Compositor compositor = factory.createChoice(component);
        Util.updateCompositor(compositor);
        newAXIComponent = compositor;
    }
    
    /**
     * Visit All.
     */
    public void visit(All component) {
        Compositor compositor = factory.createAll(component);
        Util.updateCompositor(compositor);
        newAXIComponent = compositor;
    }
    
    /**
     * Visit GlobalGroup.
     */
    public void visit(GlobalGroup schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        ContentModel cm = factory.createContentModel(schemaComponent);
        Util.updateContentModel(cm);
        newAXIComponent = cm;
    }
    
    /**
     * Visit GroupReference.
     */
    public void visit(GroupReference component) {
        SchemaComponent sc = component.getRef().get();
        if(sc == null)
            return;
        AXIComponent referent = new AXIComponentCreator(model).
                createNew(sc);
        newAXIComponent = referent;
    }
    
    /**
     * Visit AttributeGroup.
     */
    public void visit(GlobalAttributeGroup schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        ContentModel cm = factory.createContentModel(schemaComponent);
        Util.updateContentModel(cm);
        newAXIComponent = cm;
    }
    
    /**
     * Visit AttributeGroupReference.
     */
    public void visit(AttributeGroupReference component) {
        SchemaComponent sc = component.getGroup().get();
        if(sc == null)
            return;
        AXIComponent referent = new AXIComponentCreator(model).
                createNew(sc);
        newAXIComponent = referent;        
    }
        
    /**
     * Visit GlobalComplexType.
     */
    public void visit(GlobalComplexType schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        ContentModel cm = factory.createContentModel(schemaComponent);
        Util.updateContentModel(cm);
        newAXIComponent = cm;
    }

    @Override
    public void visit(Import im) {
        if(!model.fromSameSchemaModel(im)) {
            newAXIComponent = model.lookupFromOtherModel(im);
            return;
        }
        SchemaReference ref = factory.createSchemaReference(im);
        Util.updateSchemaReference(ref);
        newAXIComponent = ref;
    }

    @Override
    public void visit(Include include) {
        if(!model.fromSameSchemaModel(include)) {
            newAXIComponent = model.lookupFromOtherModel(include);
            return;
        }
        SchemaReference ref = factory.createSchemaReference(include);
        Util.updateSchemaReference(ref);
        newAXIComponent = ref;
    }
            
    public void visit(LocalComplexType component) {        
    }
    public void visit(ComplexContent component) {        
    }    
    public void visit(SimpleContent component) {        
    }
    public void visit(SimpleExtension component) {        
    }    
    public void visit(ComplexExtension component) {        
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    /**
     * Newly created component.
     */
    private AXIComponent newAXIComponent;
    private AXIComponentFactory factory;
}

