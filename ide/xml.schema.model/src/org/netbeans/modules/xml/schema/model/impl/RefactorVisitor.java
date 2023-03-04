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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Samaresh
 */
class RefactorVisitor extends DefaultSchemaVisitor {

    /**
     * The global component being modified.
     */
    private ReferenceableSchemaComponent global_component = null;
    private String oldName = null;

    /**
     * Creates a new instance of RefactorVisitor
     */
    public RefactorVisitor() {
    }
    
    public void rename(ReferenceableSchemaComponent component, String newName) {
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview = usage.findUsages(
                Collections.singletonList(component.getModel().getSchema()),
                component);
        String originalName = component.getName();
        component.setName(newName);
        setRenamedElement(component, originalName);
        rename(preview);
    }
    
    /**
     * Sets the global component that has been renamed.
     */
    public void setRenamedElement(ReferenceableSchemaComponent component, String originalName) {
        global_component = component;
        oldName = originalName;
    }
    
    /**
     * Fixes the referneces for all schema components in this preview
     * to the new global component that has been renamed.
     *
     * User must call setRenamedElement() prior to this call.
     */
    public void rename(Preview preview) {
        if (global_component == null || oldName == null)
            return;
        
        for(SchemaComponent component : preview.getUsages().keySet()) {
            component.accept(this);
        }
    }

    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> 
            createReference(Class<T> type, SchemaComponent referencing) {
        if (type.isAssignableFrom(global_component.getClass())) {
            return referencing.createReferenceTo(type.cast(global_component), type);
        }
        return null;
    }
    
    /**
     * For CommonSimpleRestriction, GlobalReference will be:
     * getBase(), when a GlobalSimpleType is modified.
     */
    public void visit(SimpleTypeRestriction str) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, str);
        if (ref != null) {
            str.setBase(ref);
        }
    }
        
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(LocalElement element) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, element);
        if (ref != null) {
            element.setType(ref);
        }
    }
    
     public void visit(ElementReference element) {
        NamedComponentReference<GlobalElement> ref = createReference(GlobalElement.class, element);
        if (ref != null) {
            element.setRef(ref);            
        }
    }
    
    /**
     * For GlobalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getSubstitutionGroup(), when a GlobalElement is modified.
     */
    public void visit(GlobalElement element) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, element);
        if (ref != null) {
            element.setType(ref);
        } else {
            NamedComponentReference<GlobalElement> ref2 = createReference(GlobalElement.class, element);
            if (ref2 != null) {
                element.setSubstitutionGroup(ref2);
            }            
        }
    }
        
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(LocalAttribute attribute) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, attribute);
        if (ref != null) {
            attribute.setType(ref);
        }
    }
    
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(AttributeReference attribute) {
        NamedComponentReference<GlobalAttribute> ref = createReference(GlobalAttribute.class, attribute);
        if (ref != null) {
            attribute.setRef(ref);
        }
    }
        
    /**
     * For AttributeGroupReference, GlobalReference will be:
     * getGroup(), when a GlobalAttributeGroup is modified.
     */
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> ref = createReference(GlobalAttributeGroup.class, agr);
        if (ref != null) {
            agr.setGroup(ref);
        }        
    }
        
    /**
     * For ComplexContentRestriction, GlobalReference will be:
     * getBase(), when a GlobalComplexType is modified.
     */
    public void visit(ComplexContentRestriction ccr) {
        NamedComponentReference<GlobalComplexType> ref = createReference(GlobalComplexType.class, ccr);
        if (ref != null) {
            ccr.setBase(ref);
        }
    }
    
    /**
     * For SimpleExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(SimpleExtension extension) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, extension);
        if (ref != null) {
            extension.setBase(ref);
        }
    }
    
    /**
     * For ComplexExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(ComplexExtension extension) {
        NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, extension);
        if (ref != null) {
            extension.setBase(ref);
        }
    }
    
    /**
     * For GroupReference, GlobalReference will be:
     * getRef(), when a GlobalGroup is modified.
     */
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> ref = createReference(GlobalGroup.class, gr);
        if (ref != null) {
            gr.setRef(ref);
        }
    }
    
    /**
     * For List, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified.
     */
    public void visit(List list) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, list);
        if (ref != null) {
            list.setType(ref);
        }
    }

    public void visit(Union u) {
        NamedComponentReference<GlobalSimpleType> ref = createReference(GlobalSimpleType.class, u);
        if (ref != null) {
            ArrayList<NamedComponentReference<GlobalSimpleType>> members = 
                    new ArrayList<>(u.getMemberTypes());
            for (int i=0; i<members.size(); i++) {
                if (members.get(i).getRefString().indexOf(oldName) > -1) {
                    members.remove(i);
                    members.add(i, ref);
                    break;
                }
            }
            u.setMemberTypes(members);
        }
    }
}
