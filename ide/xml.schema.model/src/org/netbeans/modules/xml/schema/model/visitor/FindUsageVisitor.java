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

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Samaresh
 */
public class FindUsageVisitor extends DeepSchemaVisitor {

    /**
     * The global component being modified.
     */
    private NamedReferenceable<SchemaComponent> globalSchemaComponent         = null;

    /**
     * Preview
     */
    private PreviewImpl preview                         = null;
    
    /**
     * Find usages for the specified type in the list of the schemas.
     */
    public Preview findUsages(Collection<Schema> roots, NamedReferenceable<SchemaComponent> component ) {
	preview = new PreviewImpl();
        globalSchemaComponent = component;
        return findUsages(roots);
    }

    /**
     * All the usage methods eventually call this to get the preview.
     */
    private Preview findUsages(Collection<Schema> roots) {
        for(Schema schema : roots) {
            schema.accept(this);
        }
        
        return preview;
    }
        
    public void visit(Union u) {
        if (u.getMemberTypes() != null) {
            for (NamedComponentReference<GlobalSimpleType> t : u.getMemberTypes()) {
                checkReference(t, u);
            }
        }
        super.visit(u);
    }

    /**
     * For CommonSimpleRestriction, GlobalReference will be:
     * getBase(), when a GlobalSimpleType is modified.
     */
    public void visit(SimpleTypeRestriction str) {
        checkReference(str.getBase(), str);
        super.visit(str);
    }
        
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(LocalElement element) {
        checkReference((NamedComponentReference<GlobalType>)element.getType(), element);
        super.visit(element);
    }
    
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(ElementReference element) {
        checkReference(element.getRef(), element);
        super.visit(element);
    }
    
    /**
     * For GlobalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getSubstitutionGroup(), when a GlobalElement is modified.
     */
    public void visit(GlobalElement element) {
        checkReference(element.getType(), element);
        checkReference(element.getSubstitutionGroup(), element);
        super.visit(element);
    }
        
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(LocalAttribute attribute) {
        checkReference(attribute.getType(), attribute);
        super.visit(attribute);
    }
    
     /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(AttributeReference attribute) {
        checkReference(attribute.getRef(), attribute);
        super.visit(attribute);
    }
        
    /**
     * For AttributeGroupReference, GlobalReference will be:
     * getGroup(), when a GlobalAttributeGroup is modified.
     */
    public void visit(AttributeGroupReference agr) {
        checkReference(agr.getGroup(), agr);
        super.visit(agr);
    }
        
    /**
     * For ComplexContentRestriction, GlobalReference will be:
     * getBase(), when a GlobalComplexType is modified.
     */
    public void visit(ComplexContentRestriction ccr) {
        checkReference(ccr.getBase(), ccr);
        super.visit(ccr);
    }
    
    /**
     * For SimpleExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(SimpleExtension extension) {
        checkReference(extension.getBase(), extension);
        super.visit(extension);
    }
    
    /**
     * For ComplexExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(ComplexExtension extension) {
        checkReference(extension.getBase(), extension);
        super.visit(extension);
    }
    
    /**
     * For GroupReference, GlobalReference will be:
     * getRef(), when a GlobalGroup is modified.
     */
    public void visit(GroupReference gr) {
        checkReference(gr.getRef(), gr);
        super.visit(gr);
    }
    
    /**
     * For List, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified.
     */
    public void visit(List list) {
        checkReference(list.getType(), list);
        super.visit(list);
    }
    
    private <T extends NamedReferenceable<SchemaComponent>> void checkReference(
            NamedComponentReference<T> ref, SchemaComponent component) {
        if (ref == null || ! ref.getType().isAssignableFrom(globalSchemaComponent.getClass())) return;
        if (ref.references(ref.getType().cast(globalSchemaComponent))) {
            preview.addToUsage(component);            
        }
    }
}
