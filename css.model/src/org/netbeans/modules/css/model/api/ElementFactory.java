/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
