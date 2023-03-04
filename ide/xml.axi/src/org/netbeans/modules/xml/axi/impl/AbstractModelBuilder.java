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
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 * An AXI model can be created with few schema componnets. This class must
 * define a set of visit methods for those components and all the builder
 * implementation must implement those.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractModelBuilder extends DeepSchemaVisitor {
    
    public AbstractModelBuilder(AXIModelImpl model) {
        this.model = model;
    }
    
    public AXIModelImpl getModel() {
        return model;
    }    
    
    public abstract void visit(Schema schema);
    
    public abstract void visit(AnyElement schemaComponent);
    
    public abstract void visit(AnyAttribute schemaComponent);
    
    public abstract void visit(GlobalElement schemaComponent);
    
    public abstract void visit(LocalElement component);
    
    public abstract void visit(ElementReference component);
    
    public abstract void visit(GlobalAttribute schemaComponent);
    
    public abstract void visit(LocalAttribute component);
    
    public abstract void visit(AttributeReference component);
    
    public abstract void visit(Sequence component);
    
    public abstract void visit(Choice component);
    
    public abstract void visit(All component);
    
    public abstract void visit(GlobalGroup schemaComponent);
    
    public abstract void visit(GroupReference component);
    
    public abstract void visit(GlobalAttributeGroup schemaComponent);
    
    public abstract void visit(AttributeGroupReference component);
    
    public abstract void visit(GlobalComplexType schemaComponent);
    
    public abstract void visit(LocalComplexType component);
    
    public abstract void visit(ComplexContent component);
    
    public abstract void visit(SimpleContent component);
    
    public abstract void visit(SimpleExtension component);
    
    public abstract void visit(ComplexExtension component);

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected AXIModelImpl model;    
}
