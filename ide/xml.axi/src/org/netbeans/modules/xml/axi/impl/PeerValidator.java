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
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;

/**
 * PeerValidator validates the peer in an AXIComponent.
 * It is possible that the code generator, sets arbitrary peer values
 * for various AXIComponent. AXI sync should treat those components as
 * invalid. For example if there was an ElementImpl but the peer was found
 * as an ElementReference then that ElementImpl is bad.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class PeerValidator extends DefaultVisitor {

    private boolean result = true;
        
    /**
     * Creates a new instance of PeerValidator
     */
    public PeerValidator() {
    }
    
    public boolean validate(AXIComponent component) {
        result = true;
        component.accept(this);
        return result;
    }
    
    public void visit(AXIDocument root) {
        if(! (root.getPeer() instanceof Schema) )
            result = false;
    }
    
    public void visit(Element element) {
        SchemaComponent peer = element.getPeer();        
        if(element instanceof ElementImpl) {
            if( !(peer instanceof GlobalElement) &&
                !(peer instanceof LocalElement) )
                result = false;
        }
        if(element instanceof ElementRef) {
            if( !(peer instanceof ElementReference) )
                result = false;
        }        
    }
    
    public void visit(AnyElement element) {
        if(! (element.getPeer() instanceof org.netbeans.modules.xml.schema.model.AnyElement) )
            result = false;
    }
    
    public void visit(Attribute attribute) {        
        SchemaComponent peer = attribute.getPeer();        
        if(attribute instanceof AttributeImpl) {
            if( !(peer instanceof GlobalAttribute) &&
                !(peer instanceof LocalAttribute) )
                result = false;
        }
        if(attribute instanceof AttributeRef) {
            if( !(peer instanceof AttributeReference) )
                result = false;
        }        
    }
        
    public void visit(AnyAttribute attribute) {        
        if(! (attribute.getPeer() instanceof org.netbeans.modules.xml.schema.model.AnyAttribute) )
            result = false;
    }
    
    public void visit(Compositor compositor) {
        SchemaComponent peer = compositor.getPeer();
        if( !(peer instanceof Sequence) &&
            !(peer instanceof Choice) &&
            !(peer instanceof All) )
            result = false;
    }
    
    public void visit(ContentModel contentModel) {
        SchemaComponent peer = contentModel.getPeer();
        if( !(peer instanceof GlobalComplexType) &&
            !(peer instanceof GlobalGroup) &&
            !(peer instanceof GlobalAttributeGroup) )
            result = false;
    }    
}
