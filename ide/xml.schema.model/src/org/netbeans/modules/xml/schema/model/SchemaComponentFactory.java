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

package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.dom.ComponentFactory;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 * Factory for providing concrete implementations of the CommonSchemaElement
 * subclasses.
 * @author Chris Webster
 */
public interface SchemaComponentFactory extends ComponentFactory<SchemaComponent> {
    All createAll();
    Annotation createAnnotation();
    AnyElement createAny();
    AnyAttribute createAnyAttribute();
    AppInfo createAppInfo();
    AttributeGroupReference createAttributeGroupReference();
    Choice createChoice();
    ComplexContent createComplexContent();
    ComplexContentRestriction createComplexContentRestriction();
    ComplexExtension createComplexExtension();
    Documentation createDocumentation();
    Enumeration createEnumeration();
    Field createField();
    FractionDigits createFractionDigits();
    GlobalAttribute createGlobalAttribute();
    GlobalAttributeGroup createGlobalAttributeGroup();
    GlobalComplexType createGlobalComplexType();
    GlobalElement createGlobalElement();
    GlobalSimpleType createGlobalSimpleType();
    GlobalGroup createGroupDefinition();
    GroupReference createGroupReference();
    Import createImport();
    Include createInclude();
    Key createKey();
    KeyRef createKeyRef();
    Length createLength();
    List createList();
    LocalAttribute createLocalAttribute();
    AttributeReference createAttributeReference();
    LocalComplexType createLocalComplexType();
    LocalElement createLocalElement();
    ElementReference createElementReference();
    LocalSimpleType createLocalSimpleType();
    MaxExclusive createMaxExclusive();
    MaxInclusive createMaxInclusive();
    MaxLength createMaxLength();
    MinInclusive createMinInclusive();
    MinExclusive createMinExclusive();
    MinLength createMinLength();
    Notation createNotation();
    Pattern createPattern();
    Redefine createRedefine();
    Schema createSchema();
    Sequence createSequence();
    Selector createSelector();
    SimpleContent createSimpleContent();
    SimpleContentRestriction createSimpleContentRestriction();
    SimpleExtension createSimpleExtension();
    SimpleTypeRestriction createSimpleTypeRestriction();
    TotalDigits createTotalDigits();
    Union createUnion();
    Unique createUnique();
    Whitespace createWhitespace();
    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> 
        createGlobalReference(T referenced, Class<T> c, SchemaComponent referencing);
}
