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
package org.netbeans.modules.css.model.api;

/**
 *
 * @author marekfukala
 */
public interface ElementFactory {
    
    public StyleSheet createStyleSheet();

    public CharSet createCharSet();

    public CharSetValue createCharSetValue();

    public FontFace createFontFace();
    
    public FontFace createFontFace(Declarations declarations);
    
    public Imports createImports();

    public ImportItem createImportItem();

    public ResourceIdentifier createResourceIdentifier();

    public MediaQueryList createMediaQueryList();
    
    public MediaQueryList createMediaQueryList(MediaQuery... mediaQuery);

    public MediaQuery createMediaQuery();
    
    public MediaQuery createMediaQuery(MediaQueryOperator mediaQueryOperator, MediaType mediaType, MediaExpression... mediaExpression);

    public Namespaces createNamespaces();

    public Namespace createNamespace();

    public NamespacePrefixName createNamespacePrefixName();

    public Body createBody();

    public BodyItem createBodyItem();
    
    public AtRule createAtRule();

    public Rule createRule();

    public Rule createRule(SelectorsGroup selectorsGroup, Declarations declarations);

    public SelectorsGroup createSelectorsGroup();

    public SelectorsGroup createSelectorsGroup(Selector... selectors);

    public Selector createSelector();

    public Selector createSelector(CharSequence code);

    public Declarations createDeclarations();

    public Declarations createDeclarations(PropertyDeclaration... declarations);

    public Declaration createDeclaration();
    
    public PropertyDeclaration createPropertyDeclaration();

    public PropertyDeclaration createPropertyDeclaration(Property property, PropertyValue propertyValue, boolean isImportant);

    public Property createProperty();

    public Property createProperty(CharSequence propertyName);
    
    public PropertyValue createPropertyValue();

    public PropertyValue createPropertyValue(Expression expression);

    public Expression createExpression();

    public Expression createExpression(CharSequence expression);

    public Prio createPrio();

    public PlainElement createPlainElement();

    public PlainElement createPlainElement(CharSequence text);
    
    public MediaQueryOperator createMediaQueryOperator();
    
    public MediaQueryOperator createMediaQueryOperator(CharSequence text);
    
    public MediaExpression createMediaExpression();
    
    public MediaExpression createMediaExpression(MediaFeature mediaFeature, MediaFeatureValue mediaFeatureValue);
    
    public MediaFeature createMediaFeature();
    
    public MediaFeature createMediaFeature(CharSequence text);
    
    public MediaFeatureValue createMediaFeatureValue();
    
    public MediaFeatureValue createMediaFeatureValue(Expression expression);
    
    public MediaType createMediaType();
    
    public MediaType createMediaType(CharSequence text);
    
    public Media createMedia();
    
    public Media createMedia(MediaQueryList mediaQueryList, MediaBody body);
    
    public MediaBody createMediaBody();
    
    public MediaBody createMediaBody(Rule... rule);
    
    public MediaBody createMediaBody(Page... page);
    
    public Page createPage();
    
    public Page createPage(CharSequence source);
    
    public VendorAtRule createVendorAtRule();
    
    public AtRuleId createAtRuleId();
    
    public AtRuleId createAtRuleId(CharSequence text);
    
    public MozDocument createMozDocument();
    
    public MozDocumentFunction createMozDocumentFunction();
    
    public GenericAtRule createGenericAtRule();
    
    public WebkitKeyframes createWebkitKeyFrames();
    
    public WebkitKeyframesBlock createWebkitKeyFramesBlock();
    
    public WebkitKeyframeSelectors createWebkitKeyframeSelectors();
    
    
}
