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

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 * Helper class that exposes query-like APIs. Various queries can be made
 * on schema components such as whether or not the a component has any affect
 * on the AXI model OR whether or not a component can be viewed in the editor.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelBuilderQuery extends AbstractModelBuilder {
    
    public AXIModelBuilderQuery(AXIModelImpl model) {
        super(model);
    }
    
    /**
     * Returns true for all schema components that are viewable,
     * false otherwise. Not all schema components have corresponding AXI
     * components and not all AXI components are viewable.
     */
    public boolean canView(SchemaComponent schemaComponent) {
        canView = false;
        schemaComponent.accept(this);
        return canView;
    }
    
    /**
     * Returns true if the schema component has an impact on AXI model,
     * false otherwise. Not all schema components affects AXI model.
     */
    public boolean affectsModel(SchemaComponent schemaComponent) {
        affectsModel = false;
        schemaComponent.accept(this);
        return affectsModel;
    }
    
    public void visit(Schema schema) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(AnyElement schemaComponent) {
        affectsModel = true;
        canView = checkComponent(schemaComponent);
    }
    
    public void visit(AnyAttribute schemaComponent) {
        affectsModel = true;
        canView = checkComponent(schemaComponent);
    }
    
    public void visit(GlobalElement schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalElement component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(ElementReference component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(GlobalAttribute schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(LocalAttribute component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(AttributeReference component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(Sequence component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(Choice component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(All component) {
        affectsModel = true;
        canView = checkComponent(component);
    }
    
    public void visit(GlobalGroup schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GroupReference component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GlobalAttributeGroup schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(AttributeGroupReference component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GlobalComplexType schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalComplexType component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(ComplexContent component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(SimpleContent component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(SimpleExtension component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(ComplexExtension component) {
        affectsModel = true;
        canView = false;
    }
    
    /**
     * If the component's top level parent is a complex type or an element
     * it'll be visible in design view, else no.
     * 
     * @param component
     * @return
     */
    private boolean checkComponent(SchemaComponent component) {
        if(component == null)
            return false;
        SchemaComponent parent = component;
        while(parent.getParent() != null && !(parent.getParent() instanceof Schema)) {
            parent = parent.getParent();
        }
        if((parent instanceof GlobalComplexType) || (parent instanceof GlobalElement))
            return true;
        
        return false;
    }
    

    // member variables
    private boolean affectsModel;
    private boolean canView;
}
