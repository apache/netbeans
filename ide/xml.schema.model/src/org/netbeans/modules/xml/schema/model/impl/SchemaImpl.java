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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Schema.Block;
import org.netbeans.modules.xml.schema.model.Schema.Final;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * @author Vidhya Narayanan
 * @author Nam Nguyen
 */

public class SchemaImpl extends SchemaComponentImpl implements Schema {
    
    public static final String TNS = "tns"; //NOI18N
    
    private Component foreignParent;

    private CachedTargetNamespace mCachedTargetNamespace =
            new CachedTargetNamespace(this);
    
    /** Creates a new instance of SchemaImpl */
    public SchemaImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SCHEMA, model));
    }
    
    public SchemaImpl(SchemaModelImpl model, Element e){
        super(model,e);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
        return Schema.class;
    }
    
    public Collection<SchemaModelReference> getSchemaReferences() {
        return getChildren(SchemaModelReference.class);
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public Collection<GlobalElement> getElements() {
        return getChildren(GlobalElement.class);
    }
    
    public void removeElement(GlobalElement element) {
        removeChild(ELEMENTS_PROPERTY, element);
    }
    
    public void addElement(GlobalElement element) {
        appendChild(ELEMENTS_PROPERTY, element);
    }
    
    public Collection<GlobalAttributeGroup> getAttributeGroups() {
        return getChildren(GlobalAttributeGroup.class);
    }
    
    public void removeAttributeGroup(GlobalAttributeGroup group) {
        removeChild(ATTRIBUTE_GROUPS_PROPERTY, group);
    }
    
    public void addAttributeGroup(GlobalAttributeGroup group) {
        appendChild(ATTRIBUTE_GROUPS_PROPERTY, group);
    }
    
    public void removeExternalReference(SchemaModelReference ref) {
        removeChild(SCHEMA_REFERENCES_PROPERTY, ref);
    }
    
    public void addExternalReference(SchemaModelReference ref) {
        List<Class<? extends SchemaComponent>> afterList = new ArrayList<Class<? extends SchemaComponent>>();
        afterList.add(Annotation.class);
        afterList.add(SchemaModelReference.class);
        addAfter(SCHEMA_REFERENCES_PROPERTY, ref, afterList);
    }
    
    public Collection<GlobalComplexType> getComplexTypes() {
        return getChildren(GlobalComplexType.class);
    }
    
    public void removeComplexType(GlobalComplexType type) {
        removeChild(COMPLEX_TYPES_PROPERTY, type);
    }
    
    public void addComplexType(GlobalComplexType type) {
        appendChild(COMPLEX_TYPES_PROPERTY, type);
    }
    
    public Collection<GlobalAttribute> getAttributes() {
        return getChildren(GlobalAttribute.class);
    }
    
    public void addAttribute(GlobalAttribute attr) {
        appendChild(ATTRIBUTES_PROPERTY, attr);
    }
    
    public void removeAttribute(GlobalAttribute attr) {
        removeChild(ATTRIBUTES_PROPERTY, attr);
    }
    
    public void setVersion(String ver) {
        setAttribute(VERSION_PROPERTY, SchemaAttributes.VERSION, ver);
    }
    
    public String getVersion() {
        return getAttribute(SchemaAttributes.VERSION);
    }
    
    public void setLanguage(String language) {
        setAttribute(LANGUAGE_PROPERTY, SchemaAttributes.LANGUAGE, language);
    }
    
    public String getLanguage() {
        return getAttribute(SchemaAttributes.LANGUAGE);
    }
    
    public void setFinalDefault(Set<Final> finalDefault) {
        setAttribute(FINAL_DEFAULT_PROPERTY, SchemaAttributes.FINAL_DEFAULT,
                finalDefault == null ? null :
                    Util.convertEnumSet(Final.class, finalDefault));
    }
    
    public Set<Final> getFinalDefault() {
        String s = getAttribute(SchemaAttributes.FINAL_DEFAULT);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
    
    public Set<Final> getFinalDefaultEffective() {
        Set<Final> v = getFinalDefault();
        return v == null ? getFinalDefaultDefault() : v;
    }
    
    public Set<Final> getFinalDefaultDefault() {
        return new DerivationsImpl.DerivationSet<Final>();
    }
    
    public void setTargetNamespace(String uri) {
        String currentTargetNamespace = getTargetNamespace();
        setAttribute(TARGET_NAMESPACE_PROPERTY, SchemaAttributes.TARGET_NS, uri);
        ensureValueNamespaceDeclared(uri, currentTargetNamespace, TNS);
    }
    
    public String getTargetNamespace() {
        return mCachedTargetNamespace.getTargetNamespace();
    }
    
    public void setElementFormDefault(Form form) {
        setAttribute(ELEMENT_FORM_DEFAULT_PROPERTY, SchemaAttributes.ELEM_FORM_DEFAULT, form);
    }
    
    public Form getElementFormDefault() {
        String s = getAttribute(SchemaAttributes.ELEM_FORM_DEFAULT);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public void setAttributeFormDefault(Form form) {
        setAttribute(ATTRIBUTE_FORM_DEFAULT_PROPERTY, SchemaAttributes.ATTR_FORM_DEFAULT, form);
    }
    
    public Form getAttributeFormDefault() {
        String s = getAttribute(SchemaAttributes.ATTR_FORM_DEFAULT);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public Collection<GlobalSimpleType> getSimpleTypes() {
        return getChildren(GlobalSimpleType.class);
    }
    
    public void removeSimpleType(GlobalSimpleType type) {
        removeChild(SIMPLE_TYPES_PROPERTY, type);
    }
    
    public void addSimpleType(GlobalSimpleType type) {
        appendChild(SIMPLE_TYPES_PROPERTY, type);
    }
    
    public Collection<GlobalGroup> getGroups() {
        return getChildren(GlobalGroup.class);
    }
    
    public void removeGroup(GlobalGroup group) {
        removeChild(GROUPS_PROPERTY, group);
    }
    
    public void addGroup(GlobalGroup group) {
        appendChild(GROUPS_PROPERTY, group);
    }
    
    public Collection<Notation> getNotations() {
        return getChildren(Notation.class);
    }
    
    public void removeNotation(Notation notation) {
        removeChild(NOTATIONS_PROPERTY, notation);
    }
    
    public void addNotation(Notation notation) {
        appendChild(NOTATIONS_PROPERTY, notation);
    }
    
    public void setBlockDefault(Set<Block> blockDefault) {
        setAttribute(BLOCK_DEFAULT_PROPERTY, SchemaAttributes.BLOCK_DEFAULT,
                blockDefault == null ? null :
                    Util.convertEnumSet(Block.class, blockDefault));
    }
    
    public Set<Block> getBlockDefault() {
        String s = getAttribute(SchemaAttributes.BLOCK_DEFAULT);
        return s == null ? null : Util.valuesOf(Block.class, s);
    }
    
    public Set<Block> getBlockDefaultEffective() {
        Set<Block> v = getBlockDefault();
        return v == null ? getBlockDefaultDefault() : v;
    }
    
    public Set<Block> getBlockDefaultDefault() {
        return new DerivationsImpl.DerivationSet<Block>();
    }
    
    public Form getElementFormDefaultEffective() {
        Form v = getElementFormDefault();
        return v == null ? getElementFormDefaultDefault() : v;
    }
    
    public Form getElementFormDefaultDefault() {
        return Form.UNQUALIFIED;
    }
    
    public Form getAttributeFormDefaultEffective() {
        Form v = getAttributeFormDefault();
        return v == null ? getAttributeFormDefaultDefault() : v;
    }
    
    public Form getAttributeFormDefaultDefault() {
        return Form.UNQUALIFIED;
    }
    
    public Collection<Redefine> getRedefines() {
        return getChildren(Redefine.class);
    }
    
    public Collection<Include> getIncludes() {
        return getChildren(Include.class);
    }
    
    public Collection<Import> getImports() {
        return getChildren(Import.class);
    }
    
    public Collection<GlobalElement> findAllGlobalElements() {
        Collection<GlobalElement> result = new ArrayList<GlobalElement>();
        Collection<GlobalElement> tempCollection = this.getElements();
        if(tempCollection != null) {
            result.addAll(tempCollection);
        }
	// TODO need to add redefined elements to search
        result.addAll(getExternalGlobalElements(getImports()));
        result.addAll(getExternalGlobalElements(getIncludes()));
        result.addAll(getExternalGlobalElements(getRedefines()));
        return result;
    }
    
    private Collection<GlobalElement> getExternalGlobalElements(
	Collection<? extends SchemaModelReference> externalRefs){
	
	Collection<GlobalElement> result = new ArrayList<GlobalElement>();
	for(SchemaModelReference smr : externalRefs) {
	    try {
		SchemaModel sm = smr.resolveReferencedModel();
		if (sm.getState().equals(SchemaModel.State.VALID)) {
		    result.addAll(sm.getSchema().findAllGlobalElements());
		}
	    } catch (CatalogModelException ex) {
		// we are swalling this exception as the model cannot be found
		// we still want to continue to try and find the reference though
	    }
	}
	return result;
    }
    
    public Collection<GlobalType> findAllGlobalTypes() {
        Collection<GlobalType> result = new ArrayList<GlobalType>();
        //add all SimpleTypes
        Collection<? extends GlobalType> tempCollection = this.getSimpleTypes();
        if(tempCollection != null)
            result.addAll(tempCollection);
        //add all complex types
        tempCollection = this.getComplexTypes();
        if(tempCollection != null)
            result.addAll(tempCollection);
        //add from all the referenced docs
        result.addAll(getExternalGlobalTypes(getImports()));
        result.addAll(getExternalGlobalTypes(getIncludes()));
        result.addAll(getExternalGlobalTypes(getRedefines()));
        return result;
    }
    
    private Collection<GlobalType> getExternalGlobalTypes(
	Collection<? extends SchemaModelReference> externalRefs){
	
        Collection<GlobalType> result = new ArrayList<GlobalType>();
        for(SchemaModelReference smr : externalRefs){
	    try {
		SchemaModel sm = smr.resolveReferencedModel();
		if (sm.getState().equals(SchemaModel.State.VALID)) {
		    result.addAll(sm.getSchema().findAllGlobalTypes());
		}
	    } catch (CatalogModelException ex) {
		// swallow this exception to allow some resolution to occur
	    }
	}
        return result;
    }

    public Component getForeignParent() {
        return foreignParent;
    }

    public void setForeignParent(Component component) {
        foreignParent = component;
    }

    /**
     * Helps to improve performance because the targetNamespace is asked too frequently.
     * See the issue #169435
     */
    private static class CachedTargetNamespace {
        
        // deliberately using new String, to prevent sharing
        private static final String NO_NAMESPACE_PLACEHOLDER = new String("<null>");

        private final Schema mSchema;
        
        private volatile String mTargetNamespace = null;

        public CachedTargetNamespace(Schema schema) {
            mSchema = schema;
            //
            SchemaModel sModel = mSchema.getModel();
            assert sModel != null;
            sModel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String propName = evt.getPropertyName();
                    if (Schema.TARGET_NAMESPACE_PROPERTY.equals(propName)) {
                        // The TNS has to be repopulated
                        discardCache();
                    }
                }
            });
        }

        public synchronized void discardCache() {
            mTargetNamespace = null;
        }
        
       private static String translateNamespace(String s) {
            // == comparison is deliberate
            return s == NO_NAMESPACE_PLACEHOLDER ? null : s;
        }

        public String getTargetNamespace() {
            String ns = mTargetNamespace;
            boolean inTransaction = mSchema.getModel().isIntransaction();
            if (ns != null && !inTransaction) {
                    return translateNamespace(ns);
            }

            //
            // Use ordinary way if the model in transaction because
            // the cached value remains unchanged before transaction is commited.
            
            // TODO sdedic: not sure about the above comment. The original code changed the cached
            // value if model was inTransaction(). I retain the behaviour, but it seems to contradict the comment.
            String tns = mSchema.getAttribute(SchemaAttributes.TARGET_NS);

            if (tns == null) {
                mTargetNamespace = NO_NAMESPACE_PLACEHOLDER;
            } else {
                mTargetNamespace = tns;
            }
            return tns;
        }

    }
}
