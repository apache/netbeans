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
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

/**
 * This class provides the algorithm to determine what classes to create
 * based on the current node context.
 * @author Chris Webster
 */
class SchemaElementNodeVisitor implements SchemaVisitor {
    
    /**
     * This method determines the incoming component and creates the appropriate
     * subcomponent.
     * 
     * @return SchemaComponent for the specified element or null if the
     * element is not recognized as a type specified in schema.
     */
    public SchemaComponent createSubComponent(
        SchemaComponent parent,
        org.w3c.dom.Element e,
        SchemaModelImpl model) {
        assert model != null;
        this.e = e;
        setNewComponent(null);
        this.model = model;
        if (parent == null) {
            if (SchemaElements.SCHEMA.getName().equals(e.getLocalName())) {
                setNewComponent(new SchemaImpl(model,e));
            }
        } else {
            if (SchemaElements.ANNOTATION.getName().equals(e.getLocalName())) {
                setNewComponent(new AnnotationImpl(model,e));
            } else {
                parent.accept(this);
            }
        }
        return newCommonSchemaComponent;
    }
    
    public void visit(SimpleTypeRestriction str) {
        allSimpleRestriction();
    }
    
    public void visit(GlobalAttributeGroup gag) {
        recognizeAttributes();
    }
    
    public void visit(MinLength ml) {
        // only annotation
    }
    
    public void visit(AnyAttribute anyAttr) {
        // only child is annotation
    }
    
    public void visit(AttributeGroupReference agr) {
        // only child is annotation
    }
    
    public void visit(SimpleContentRestriction scr) {
        allSimpleRestriction();
        recognizeAttributes();
    }
    
    public void visit(LocalSimpleType type) {
        allSimpleType();
    }
    
    public void visit(MaxInclusive mi) {
        // only annotation
    }
    
    public void visit(Documentation d) {
        // any content
    }

    public void visit(AppInfo d) {
        // any content
    }
    
    public void visit(Notation d) {
        // only annotation
    }
    
    public void visit(ComplexExtension ce) {
        recognizeGroupAllChoiceSequence();
        recognizeAttributes();
    }
    
    public void visit(Field f) {
        // only annotation
    }
    
    public void visit(Schema s) {
        if (SchemaElements.INCLUDE.getName().equals(e.getLocalName())) {
            setNewComponent(new IncludeImpl(model,e));
            
        } else if (SchemaElements.IMPORT.getName().equals(e.getLocalName())) {
            setNewComponent(new ImportImpl(model,e));
            
        } else if (SchemaElements.REDEFINE.getName().equals(e.getLocalName())) {
            setNewComponent(new RedefineImpl(model,e));
            
        } else if (SchemaElements.SIMPLE_TYPE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new GlobalSimpleTypeImpl(model,e));
            
        } else if (SchemaElements.COMPLEX_TYPE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new GlobalComplexTypeImpl(model,e));
            
        } else if (SchemaElements.GROUP.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalGroupImpl(model,e));
            
        } else if (SchemaElements.ATTRIBUTE_GROUP.getName().equals(
            e.getLocalName())) {
            setNewComponent(new GlobalAttributeGroupImpl(model,e));
            
        } else if (SchemaElements.ELEMENT.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalElementImpl(model,e));
            
        } else if (SchemaElements.ATTRIBUTE.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalAttributeImpl(model,e));
            
        } else if (SchemaElements.NOTATION.getName().equals(e.getLocalName())) {
            setNewComponent(new NotationImpl(model,e));
            
        }
    }
    
    public void visit(Union u) {
        if (SchemaElements.SIMPLE_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new LocalSimpleTypeImpl(model,e));
        }
    }
    
    public void visit(MinExclusive me) {
        // only annotation
    }
    
    public void visit(Whitespace ws) {
        // only annotation
    }
    
    public void visit(MinInclusive mi) {
        // only annotation
    }
    
    public void visit(Redefine rd) {
        if (SchemaElements.SIMPLE_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalSimpleTypeImpl(model,e));
            
        } else if (SchemaElements.COMPLEX_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalComplexTypeImpl(model,e));
            
        } else if (SchemaElements.GROUP.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalGroupImpl(model,e));
            
        } else if (SchemaElements.ATTRIBUTE_GROUP.getName().equals(e.getLocalName())) {
            setNewComponent(new GlobalAttributeGroupImpl(model,e));
        }
    }
    
    public void visit(Choice choice) {
        allChoice();
    }
    
    public void visit(GroupReference gr) {
        // only annotation
    }
    
    public void visit(FractionDigits fd) {
        // only annotation
    }
    
    public void visit(GlobalGroup gd) {
        if (SchemaElements.ALL.getName().equals(e.getLocalName())) {
            setNewComponent(new AllImpl(model,e));
            
        } else if (SchemaElements.CHOICE.getName().equals(e.getLocalName())) {
            setNewComponent(new ChoiceImpl(model,e));
            
        } else if (SchemaElements.SEQUENCE.getName().equals(e.getLocalName())) {
            setNewComponent(new SequenceImpl(model,e));
        }
    }
    
    public void visit(GlobalElement ge) {
        allElement();
    }
    
    public void visit(All all) {
        allCreation();
    }
    
    public void visit(ComplexContent cc) {
        if (SchemaElements.RESTRICTION.getName().equals(e.getLocalName())) {
            setNewComponent(new ComplexContentRestrictionImpl(model,e));
            
        } else if (SchemaElements.EXTENSION.getName().equals(e.getLocalName())) {
            setNewComponent(new ComplexExtensionImpl(model,e));
        }
        
    }
    
    public void visit(LocalComplexType type) {
        allComplexType();
    }
    
    public void visit(MaxExclusive me) {
        // annotation only
    }
    
    public void visit(Include include) {
        // only annotation
    }
    
    public void visit(ComplexContentRestriction ccr) {
        recognizeGroupAllChoiceSequence();
        recognizeAttributes();
    }
    
    public void visit(GlobalComplexType gct) {
        allComplexType();
    }
    
    public void visit(GlobalAttribute ga) {
        recognizeInlineSimpleType();
    }
    
    public void visit(Annotation ann) {
        // TODO add appinfo
        if (SchemaElements.DOCUMENTATION.getName().equals(e.getLocalName())) {
            setNewComponent(new DocumentationImpl(model,e));
        } else if (SchemaElements.APPINFO.getName().equals(e.getLocalName())) {
            setNewComponent(new AppInfoImpl(model, e));
        }
    }
    
    public void visit(SimpleContent sc) {
        if (SchemaElements.RESTRICTION.getName().equals(e.getLocalName())) {
            setNewComponent(new SimpleContentRestrictionImpl(model,e));
            
        } else if (SchemaElements.EXTENSION.getName().equals(e.getLocalName())) {
            setNewComponent(new SimpleExtensionImpl(model,e));
        }
    }
    
    public void visit(Key key) {
        recognizeSelectorAndField();
    }
    
    public void visit(KeyRef kr) {
        recognizeSelectorAndField();
    }
    
    public void visit(LocalElement le) {
        allElement();
    }
    
    public void visit(MaxLength ml) {
        // annotation only
    }
    
    public void visit(Sequence s) {
        recognizeGroupAnyChoiceSequenceElement();
    }
    
    public void visit(Pattern p) {
        // only annotation
    }
    
    public void visit(List l) {
        if (SchemaElements.SIMPLE_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new LocalSimpleTypeImpl(model,e));
        }
    }
    
    public void visit(Import im) {
        // only annotation
    }
    
    public void visit(Enumeration e) {
        // only child is annotation
    }
    
    public void visit(AnyElement any) {
        // only child is annotation
    }
    
    public void visit(LocalAttribute la) {
        recognizeInlineSimpleType();
    }
    
    public void visit(GlobalSimpleType gst) {
        allSimpleType();
    }
    
    public void visit(TotalDigits td) {
        // annotation only
    }
    
    public void visit(Unique u) {
        recognizeSelectorAndField();
    }
    
    public void visit(Length length) {
        // annotation only
    }
    
    public void visit(Selector selector) {
        // annotation only 
    }
    
    public void visit(SimpleExtension se) {
        recognizeAttributes();
    }
    
    public void visit(ElementReference er) {
	// annotation only
    }
    
    public void visit(AttributeReference reference) {
	// annotation only
    }
    
    private void allSimpleRestriction() {
        if (SchemaElements.SIMPLE_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new LocalSimpleTypeImpl(model,e));
            
        } else if (SchemaElements.MIN_EXCLUSIVE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new MinExclusiveImpl(model,e));
            
        } else if (SchemaElements.MIN_INCLUSIVE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new MinInclusiveImpl(model,e));
            
        } else if (SchemaElements.MAX_EXCLUSIVE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new MaxExclusiveImpl(model,e));
            
        } else if (SchemaElements.MAX_INCLUSIVE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new MaxInclusiveImpl(model,e));
            
        } else if (SchemaElements.TOTAL_DIGITS.getName().equals(
            e.getLocalName())) {
            setNewComponent(new TotalDigitsImpl(model,e));
            
        } else if (SchemaElements.FRACTION_DIGITS.getName().equals(
            e.getLocalName())) {
            setNewComponent(new FractionDigitsImpl(model,e));
            
        } else if (SchemaElements.LENGTH.getName().equals(
            e.getLocalName())) {
            setNewComponent(new LengthImpl(model,e));
            
        } else if (SchemaElements.MIN_LENGTH.getName().equals(
            e.getLocalName())) {
            setNewComponent(new MinLengthImpl(model,e));
            
        } else if (SchemaElements.MAX_LENGTH.getName().equals(
            e.getLocalName())) {
            setNewComponent(new MaxLengthImpl(model,e));
            
        } else if (SchemaElements.ENUMERATION.getName().equals(
            e.getLocalName())) {
            setNewComponent(new EnumerationImpl(model,e));
            
        } else if (SchemaElements.WHITESPACE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new WhitespaceImpl(model,e));
            
        } else if (SchemaElements.PATTERN.getName().equals(e.getLocalName())) {
            setNewComponent(new PatternImpl(model,e));
        }
        
    }
    
    private void recognizeSelectorAndField() {
        if (SchemaElements.SELECTOR.getName().equals(e.getLocalName())) {
            setNewComponent(new SelectorImpl(model,e));
            
        } else if (SchemaElements.FIELD.getName().equals(e.getLocalName())) {
            setNewComponent(new FieldImpl(model,e));
        }
    }
    
    private void recognizeAttributes() {
        if (SchemaElements.ATTRIBUTE.getName().equals(e.getLocalName())) {
	    if (isAttributeDefined(SchemaAttributes.REF)) {
		setNewComponent(new AttributeReferenceImpl(model,e));
	    } else {
		setNewComponent(new LocalAttributeImpl(model,e));
	    }
        } else if (SchemaElements.ATTRIBUTE_GROUP.getName().equals(
            e.getLocalName())) {
            setNewComponent(new AttributeGroupReferenceImpl(model,e));
        } else if (SchemaElements.ANY_ATTRIBUTE.getName().equals(
            e.getLocalName())) {
            setNewComponent(new AnyAttributeImpl(model,e));
        }
    }
    
    private void allComplexType() {
        recognizeAttributes();
        recognizeGroupAllChoiceSequence();
        if (SchemaElements.SIMPLE_CONTENT.getName().equals(e.getLocalName())) {
            setNewComponent(new SimpleContentImpl(model,e));
        } else if (SchemaElements.COMPLEX_CONTENT.getName().equals(e.getLocalName())) {
            setNewComponent(new ComplexContentImpl(model,e));
        }
    }
    
    private boolean isAttributeDefined(SchemaAttributes attribute) {
        return e.getAttributeNode(attribute.getName()) != null;
    }
    
    private void createLocalElement() {
	if (isAttributeDefined(SchemaAttributes.REF)) {
	    setNewComponent(new ElementReferenceImpl(model,e));
	} else {
	    setNewComponent(new LocalElementImpl(model,e));
	}
    }
    
    private void recognizeGroupAnyChoiceSequenceElement() {
        if (SchemaElements.ELEMENT.getName().equals(e.getLocalName())) {
            createLocalElement();
        } else {
            recognizeGroupChoiceSequenceAny();
        }
    }
    
    private void recognizeGroupAllChoiceSequence() {
        if (SchemaElements.ALL.getName().equals(e.getLocalName())) {
            setNewComponent(new AllImpl(model,e));
        } else {
            recognizeGroupChoiceSequence();
        }
    }
    
    private void allElement() {
        if (SchemaElements.SIMPLE_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new LocalSimpleTypeImpl(model,e));
            
        } else if (SchemaElements.COMPLEX_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new LocalComplexTypeImpl(model,e));
            
        } else if (SchemaElements.UNIQUE.getName().equals(e.getLocalName())) {
            setNewComponent(new UniqueImpl(model,e));
            
        } else if (SchemaElements.KEY.getName().equals(e.getLocalName())) {
            setNewComponent(new KeyImpl(model,e));
            
        } else if (SchemaElements.KEYREF.getName().equals(e.getLocalName())) {
            setNewComponent(new KeyRefImpl(model,e));
        }
    }
    
    private void allChoice() {
        if (SchemaElements.ELEMENT.getName().equals(e.getLocalName())) {
            createLocalElement();
        } else {
            recognizeGroupChoiceSequenceAny();
        }
    }
    
    
    
    private void recognizeGroupChoiceSequenceAny() {
        if (SchemaElements.ANY.getName().equals(e.getLocalName())) {
            setNewComponent(new AnyImpl(model,e));
        } else {
            recognizeGroupChoiceSequence();
        }
    }
    
    private void recognizeGroupChoiceSequence() {
        if (SchemaElements.GROUP.getName().equals(e.getLocalName())) {
            setNewComponent(new GroupReferenceImpl(model,e));
            
        } else if (SchemaElements.CHOICE.getName().equals(e.getLocalName())) {
            setNewComponent(new ChoiceImpl(model,e));
            
        } else if (SchemaElements.SEQUENCE.getName().equals(e.getLocalName())) {
            setNewComponent(new SequenceImpl(model,e));
            
        }
    }
    
    private void recognizeInlineSimpleType() {
        if (SchemaElements.SIMPLE_TYPE.getName().equals(e.getLocalName())) {
            setNewComponent(new LocalSimpleTypeImpl(model,e));
        }
    }
    
    private void allSimpleType() {
        if (SchemaElements.RESTRICTION.getName().equals(e.getLocalName())) {
            setNewComponent(new SimpleTypeRestrictionImpl(model,e));
            
        } else if (SchemaElements.LIST.getName().equals(e.getLocalName())) {
            setNewComponent(new ListImpl(model,e));
            
        } else  if (SchemaElements.UNION.getName().equals(e.getLocalName())) {
            setNewComponent(new UnionImpl(model,e));
            
        }
    }
    
    private void allCreation() {
        if (SchemaElements.ELEMENT.getName().equals(e.getLocalName())) {
            if (isAttributeDefined(SchemaAttributes.REF)) {
		setNewComponent(new ElementReferenceImpl(model,e));
	    } else {
		setNewComponent(new LocalElementImpl(model,e));
	    }
        }
    }
    
    private void setNewComponent(SchemaComponent c) {
        newCommonSchemaComponent = c;
    }
    
    private SchemaComponent newCommonSchemaComponent;
    private org.w3c.dom.Element e;
    private SchemaModelImpl model;
}
