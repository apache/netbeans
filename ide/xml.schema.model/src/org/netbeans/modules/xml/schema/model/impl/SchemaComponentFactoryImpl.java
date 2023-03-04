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

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author rico
 * @author Chris Webster
 */
public class SchemaComponentFactoryImpl implements SchemaComponentFactory{

    private SchemaModelImpl model;

    /**
     * Creates a new instance of SchemaComponentFactoryImpl
     */
    public SchemaComponentFactoryImpl(SchemaModelImpl model) {
	this.model = model;
    }
    
    /**
     *
     *
     * @param currentElement to create model object from
     * @param parent containing this element
     * @return instance of model object for the given element.
     */
    public SchemaComponent create(org.w3c.dom.Element currentElement, SchemaComponent parent) {
	SchemaElementNodeVisitor v = new SchemaElementNodeVisitor();
	return v.createSubComponent(parent,currentElement,model);
    }
    
    public Whitespace createWhitespace() {
	return new WhitespaceImpl(model);
    }
    
    public Unique createUnique() {
	return new UniqueImpl(model);
    }
    
    public Union createUnion() {
	return new UnionImpl(model);
    }
    
    public TotalDigits createTotalDigits() {
	return new TotalDigitsImpl(model);
    }
    
    public GroupReference createGroupReference() {
	return new GroupReferenceImpl(model);
    }
    
    public GlobalGroup createGroupDefinition() {
	return new GlobalGroupImpl(model);
    }
    
    public GlobalSimpleType createGlobalSimpleType() {
	return new GlobalSimpleTypeImpl(model);
    }
    
    public GlobalElement createGlobalElement() {
	return new GlobalElementImpl(model);
    }
    
    public GlobalComplexType createGlobalComplexType() {
	return new GlobalComplexTypeImpl(model);
    }
    
    public GlobalAttributeGroup createGlobalAttributeGroup() {
	return new GlobalAttributeGroupImpl(model);
    }
    
    public GlobalAttribute createGlobalAttribute() {
	return new GlobalAttributeImpl(model);
    }
    
    public FractionDigits createFractionDigits() {
	return new FractionDigitsImpl(model);
    }
    
    public Field createField() {
	return new FieldImpl(model);
    }
    
    public All createAll() {
	return new AllImpl(model);
    }
    
    public Annotation createAnnotation() {
	return new AnnotationImpl(model);
    }
    
    public AnyElement createAny() {
	return new AnyImpl(model);
    }
    
    public AnyAttribute createAnyAttribute() {
	return new AnyAttributeImpl(model);
    }
    
    public AttributeGroupReference createAttributeGroupReference() {
	return new AttributeGroupReferenceImpl(model);
    }
    
    public Choice createChoice() {
	return new ChoiceImpl(model);
    }
    
    public ComplexContent createComplexContent() {
	return new ComplexContentImpl(model);
    }
    
    public ComplexContentRestriction createComplexContentRestriction() {
	return new ComplexContentRestrictionImpl(model);
    }
    
    public ComplexExtension createComplexExtension() {
	return new ComplexExtensionImpl(model);
    }
    
    public Documentation createDocumentation() {
	return new DocumentationImpl(model);
    }
    
    public Enumeration createEnumeration() {
	return new EnumerationImpl(model);
    }
    
    public Import createImport() {
	return new ImportImpl(model);
    }
    
    public Include createInclude() {
	return new IncludeImpl(model);
    }
    
    public Key createKey() {
	return new KeyImpl(model);
    }
    
    public KeyRef createKeyRef() {
	return new KeyRefImpl(model);
    }
    
    public Length createLength() {
	return new LengthImpl(model);
    }
    
    public List createList() {
	return new ListImpl(model);
    }
    
    public LocalAttribute createLocalAttribute() {
	return new LocalAttributeImpl(model);
    }
    
    public LocalComplexType createLocalComplexType() {
	return new LocalComplexTypeImpl(model);
    }
    
    public LocalElement createLocalElement() {
	return new LocalElementImpl(model);
    }
    
    public LocalSimpleType createLocalSimpleType() {
	return new LocalSimpleTypeImpl(model);
    }
    
    public MaxExclusive createMaxExclusive() {
	return new MaxExclusiveImpl(model);
    }
    
    public MaxInclusive createMaxInclusive() {
	return new MaxInclusiveImpl(model);
    }
    
    public MaxLength createMaxLength() {
	return new MaxLengthImpl(model);
    }
    
    public MinExclusive createMinExclusive() {
	return new MinExclusiveImpl(model);
    }
    
    public MinInclusive createMinInclusive() {
	return new MinInclusiveImpl(model);
    }
    
    public MinLength createMinLength() {
	return new MinLengthImpl(model);
    }
    
    public Pattern createPattern() {
	return new PatternImpl(model);
    }
    
    public Redefine createRedefine() {
	return new RedefineImpl(model);
    }
    
    public Schema createSchema() {
	return new SchemaImpl(model);
    }
    
    public Selector createSelector() {
	return new SelectorImpl(model);
    }
    
    public Sequence createSequence() {
	return new SequenceImpl(model);
    }
    
    public SimpleContent createSimpleContent() {
	return new SimpleContentImpl(model);
    }
    
    public SimpleContentRestriction createSimpleContentRestriction() {
	return new SimpleContentRestrictionImpl(model);
    }
    
    public SimpleExtension createSimpleExtension() {
	return new SimpleExtensionImpl(model);
    }
    
    public SimpleTypeRestriction createSimpleTypeRestriction() {
	return new SimpleTypeRestrictionImpl(model);
    }
    
    public <T extends ReferenceableSchemaComponent> NamedComponentReference<T> createGlobalReference(
	    T type, Class<T> c, SchemaComponent parent){
	return new GlobalReferenceImpl<T>(type, c, (SchemaComponentImpl) parent);
    }
    
    public Notation createNotation() {
	return new NotationImpl(model);
    }
    
    public AppInfo createAppInfo() {
	return new AppInfoImpl(model);
    }
    
    public ElementReference createElementReference() {
	return new ElementReferenceImpl(model);
    }
    
    public AttributeReference createAttributeReference() {
	return new AttributeReferenceImpl(model);
    }
    
}
