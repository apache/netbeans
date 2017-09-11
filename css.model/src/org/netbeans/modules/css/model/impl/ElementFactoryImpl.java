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
package org.netbeans.modules.css.model.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.model.api.AtRule;
import org.netbeans.modules.css.model.api.AtRuleId;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.BodyItem;
import org.netbeans.modules.css.model.api.CharSet;
import org.netbeans.modules.css.model.api.CharSetValue;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.FontFace;
import org.netbeans.modules.css.model.api.GenericAtRule;
import org.netbeans.modules.css.model.api.ImportItem;
import org.netbeans.modules.css.model.api.Imports;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaFeature;
import org.netbeans.modules.css.model.api.MediaFeatureValue;
import org.netbeans.modules.css.model.api.MediaQuery;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.MediaQueryOperator;
import org.netbeans.modules.css.model.api.MediaType;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.MozDocument;
import org.netbeans.modules.css.model.api.MozDocumentFunction;
import org.netbeans.modules.css.model.api.Namespace;
import org.netbeans.modules.css.model.api.NamespacePrefixName;
import org.netbeans.modules.css.model.api.Namespaces;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.PlainElement;
import org.netbeans.modules.css.model.api.Prio;
import org.netbeans.modules.css.model.api.Property;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.netbeans.modules.css.model.api.ResourceIdentifier;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.Selector;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.model.api.VendorAtRule;
import org.netbeans.modules.css.model.api.WebkitKeyframeSelectors;
import org.netbeans.modules.css.model.api.WebkitKeyframes;
import org.netbeans.modules.css.model.api.WebkitKeyframesBlock;
import org.openide.util.Parameters;

/**
 *
 * @author marekfukala
 */
public final class ElementFactoryImpl implements ElementFactory {

    private final Model model;
    
    public ElementFactoryImpl(Model model) {
        this.model = model;
    }
    
    public Element createElement(Model model, Node node) { //NOI18N for whole method
        NodeType type = node.type();
        String className = Utils.getInterfaceForNodeType(type.name());
        //TODO generate this ugly switch!!!
        switch (className) {
            case "AtRuleId":
                return new AtRuleIdI(model, node);
            case "AtRule":
                return new AtRuleI(model, node);
            case "GenericAtRule":
                return new GenericAtRuleI(model, node);
            case "MozDocument":
                return new MozDocumentI(model, node);
            case "MozDocumentFunction":
                return new MozDocumentFunctionI(model, node);
            case "VendorAtRule":
                return new VendorAtRuleI(model, node);
            case "WebkitKeyframes":
                return new WebkitKeyframesI(model, node);
            case "WebkitKeyframeSelectors":
                return new WebkitKeyframeSelectorsI(model, node);
            case "WebkitKeyframesBlock":
                return new WebkitKeyframesBlockI(model, node);
            case "StyleSheet":
                return new StyleSheetI(model, node);
            case "CharSet":
                return new CharSetI(model, node);
            case "CharSetValue":
                return new CharSetValueI(model, node);
            case "FontFace":
                return new FontFaceI(model, node);
            case "Imports":
                return new ImportsI(model, node);
            case "ImportItem":
                return new ImportItemI(model, node);
            case "ResourceIdentifier":
                return new ResourceIdentifierI(model, node);
            case "Media":
                return new MediaI(model, node);
            case "MediaBody":
                return new MediaBodyI(model, node);
            case "MediaBodyItem":
                return new MediaBodyItemI(model, node);
            case "MediaQueryList":
                return new MediaQueryListI(model, node);
            case "MediaQuery":
                return new MediaQueryI(model, node);
            case "MediaQueryOperator":
                return new MediaQueryOperatorI(model, node);
            case "MediaExpression":
                return new MediaExpressionI(model, node);
            case "MediaFeature":
                return new MediaFeatureI(model, node);
            case "MediaFeatureValue":
                return new MediaFeatureValueI(model, node);
            case "MediaType":
                return new MediaTypeI(model, node);
            case "Namespaces":
                return new NamespacesI(model, node);
            case "Namespace":
                return new NamespaceI(model, node);
            case "NamespacePrefixName":
                return new NamespacePrefixNameI(model, node);
            case "Body":
                return new BodyI(model, node);
            case "BodyItem":
                return new BodyItemI(model, node);
            case "Rule":
                return new RuleI(model, node);
            case "SelectorsGroup":
                return new SelectorsGroupI(model, node);
            case "Selector":
                return new SelectorI(model, node);
            case "Declarations":
                return new DeclarationsI(model, node);
            case "PropertyDeclaration":
                return new PropertyDeclarationI(model, node);
            case "Declaration":
                return new DeclarationI(model, node);
            case "Property":
                return new PropertyI(model, node);
            case "PropertyValue":
                return new PropertyValueI(model, node);
            case "Expression":
                return new ExpressionI(model, node);
            case "Prio":
                return new PrioI(model, node);
            case "PlainElement":
                return new PlainElementI(model, node);
            case "Page":
                return new PageI(model, node);
            case "Ws":
                return new WsI(model, node);
            case "Token":
                return new PlainElementI(model, node);
            case "Error":
                return new PlainElementI(model, node);
            default:
                //fallback for unknown types
                Logger.getLogger(ElementFactoryImpl.class.getName()).log( Level.WARNING, "created element by reflection for {0}, update the ElementFactoryImpl.createElement() methods ugly switch!", className);
                return createElementByReflection(model, node);
        }
    }

    private Element createElementByReflection(Model model, Node node) {
        Parameters.notNull("model", model);
        Parameters.notNull("node", node);
        try {
            Class<?> clazz = Class.forName(Utils.getImplementingClassNameForNodeType(node.type()));
            Constructor<?> constructor = clazz.getConstructor(Model.class, Node.class);
            return (Element) constructor.newInstance(model, node);
        } catch (ClassNotFoundException cnfe ) {
            //no implementation found - use default
            return new PlainElementI(model, node);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException 
                | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public StyleSheet createStyleSheet() {
        return new StyleSheetI(model);
    }
 
    @Override
    public CharSet createCharSet() {
        return new CharSetI(model);
    }

    @Override
    public CharSetValue createCharSetValue() {
        return new CharSetValueI(model);
    }

    @Override
    public FontFace createFontFace() {
        return new FontFaceI(model);
    }

    @Override
    public FontFace createFontFace(Declarations declarations) {
        FontFace fontFace = createFontFace();
        fontFace.setDeclarations(declarations);
        return fontFace;
    }

    @Override
    public Imports createImports() {
        return new ImportsI(model);
    }

    @Override
    public ImportItem createImportItem() {
        return new ImportItemI(model);
    }

    @Override
    public ResourceIdentifier createResourceIdentifier() {
        return new ResourceIdentifierI(model);
    }

    @Override
    public MediaQueryList createMediaQueryList() {
        return new MediaQueryListI(model);
    }

    @Override
    public MediaQuery createMediaQuery() {
        return new MediaQueryI(model);
    }

    @Override
    public Namespaces createNamespaces() {
        return new NamespacesI(model);
    }

    @Override
    public Namespace createNamespace() {
        return new NamespaceI(model);
    }

    @Override
    public NamespacePrefixName createNamespacePrefixName() {
        return new NamespacePrefixNameI(model);
    }

    @Override
    public Body createBody() {
        return new BodyI(model);
    }

    @Override
    public BodyItem createBodyItem() {
        return new BodyItemI(model);
    }

    @Override
    public Rule createRule() {
        return new RuleI(model);
    }

    @Override
    public Rule createRule(SelectorsGroup selectorsGroup, Declarations declarations) {
        Rule rule = createRule();
        rule.setSelectorsGroup(selectorsGroup);
        rule.setDeclarations(declarations);
        return rule;
    }

    @Override
    public SelectorsGroup createSelectorsGroup() {
        return new SelectorsGroupI(model);
    }

    @Override
    public SelectorsGroup createSelectorsGroup(Selector... selectors) {
        SelectorsGroup sg = createSelectorsGroup();
        for (Selector s : selectors) {
            sg.addSelector(s);
        }
        return sg;
    }

    @Override
    public Selector createSelector() {
        return new SelectorI(model);
    }

    @Override
    public Selector createSelector(CharSequence code) {
        return new SelectorI(model, code);
    }

    @Override
    public Declarations createDeclarations() {
        return new DeclarationsI(model);
    }

    @Override
    public Declarations createDeclarations(PropertyDeclaration... declarations) {
        Declarations ds = createDeclarations();
        for (PropertyDeclaration pd : declarations) {
            Declaration declaration = createDeclaration();
            declaration.setPropertyDeclaration(pd);
            ds.addDeclaration(declaration);
        }
        return ds;
    }

     @Override
    public Declaration createDeclaration() {
        return new DeclarationI(model);
    }
    
    @Override
    public PropertyDeclaration createPropertyDeclaration() {
        return new PropertyDeclarationI(model);
    }

    @Override
    public PropertyDeclaration createPropertyDeclaration(Property property, PropertyValue propertyValue, boolean isImportant) {
        PropertyDeclaration d = createPropertyDeclaration();
        d.setProperty(property);
        d.setPropertyValue(propertyValue);

        Prio prio = createPrio();
        prio.setContent(isImportant ? "!" : "");

        d.setPrio(prio);
        return d;
    }

    @Override
    public Property createProperty() {
        return new PropertyI(model);
    }

    @Override
    public Property createProperty(CharSequence propertyName) {
        Property p = createProperty();
        p.setContent(propertyName);
        return p;
    }

    @Override
    public PropertyValue createPropertyValue() {
        return new PropertyValueI(model);
    }

    @Override
    public PropertyValue createPropertyValue(Expression expression) {
        PropertyValue pv = createPropertyValue();
        pv.setExpression(expression);
        return pv;
    }

    @Override
    public Expression createExpression() {
        return new ExpressionI(model);
    }

    @Override
    public Expression createExpression(CharSequence expression) {
        Expression e = createExpression();
        e.setContent(expression);
        return e;
    }

    @Override
    public Prio createPrio() {
        return new PrioI(model);
    }

    @Override
    public PlainElement createPlainElement() {
        return new PlainElementI(model);
    }

    @Override
    public PlainElement createPlainElement(CharSequence text) {
        return new PlainElementI(model, text);
    }

    @Override
    public MediaQueryOperator createMediaQueryOperator() {
        return new MediaQueryOperatorI(model);
    }

    @Override
    public MediaExpression createMediaExpression() {
        return new MediaExpressionI(model);
    }

    @Override
    public MediaExpression createMediaExpression(MediaFeature mediaFeature, MediaFeatureValue mediaFeatureValue) {
        MediaExpression me = createMediaExpression();
        me.setMediaFeature(mediaFeature);
        me.setMediaFeatureValue(mediaFeatureValue);
        return me;
    }

    @Override
    public MediaFeature createMediaFeature() {
        return new MediaFeatureI(model);
    }
    
    @Override
    public MediaFeatureValue createMediaFeatureValue() {
        return new MediaFeatureValueI(model);
    }

    @Override
    public MediaType createMediaType() {
        return new MediaTypeI(model);
    }

    @Override
    public Media createMedia() {
        return new MediaI(model);
    }

    @Override
    public Page createPage() {
        return new PageI(model);
    }

    @Override
    public Page createPage(CharSequence source) {
        return new PageI(model, source);
    }

    @Override
    public MediaQueryOperator createMediaQueryOperator(CharSequence text) {
        return new MediaQueryOperatorI(model, text);
    }

    @Override
    public MediaFeature createMediaFeature(CharSequence text) {
        return new MediaFeatureI(model, text);
    }

    @Override
    public MediaType createMediaType(CharSequence text) {
        return new MediaTypeI(model, text);
    }

    @Override
    public MediaQuery createMediaQuery(MediaQueryOperator mediaQueryOperator, MediaType mediaType, MediaExpression... mediaExpression) {
        MediaQuery mq = createMediaQuery();
        mq.setMediaQueryOperator(mediaQueryOperator);
        mq.setMediaType(mediaType);

        for (MediaExpression me : mediaExpression) {
            mq.addMediaExpression(me);
        }
        return mq;
    }

    @Override
    public MediaQueryList createMediaQueryList(MediaQuery... mediaQuery) {
        MediaQueryList mql = createMediaQueryList();
        for (MediaQuery mq : mediaQuery) {
            mql.addMediaQuery(mq);
        }
        return mql;
    }

    @Override
    public Media createMedia(MediaQueryList mediaQueryList, MediaBody mediaBody) {
        Media media = createMedia();
        media.setMediaQueryList(mediaQueryList);
        media.setMediaBody(mediaBody);
        return media;
    }
    
    @Override
    public MediaBody createMediaBody() {
        return new MediaBodyI(model);
    }
    
    public MediaBodyItem createMediaBodyItem() {
        return new MediaBodyItemI(model);
    }
    
    @Override
    public MediaBody createMediaBody(Rule... rules) {
        MediaBody mediaBody = createMediaBody();
        for (Rule r : rules) {
            mediaBody.addRule(r);
        }
        return mediaBody;
    }

    @Override
    public MediaBody createMediaBody(Page... pages) {
        MediaBody mediaBody = createMediaBody();
        for (Page page : pages) {
            mediaBody.addPage(page);
        }
        return mediaBody;
    }

    @Override
    public VendorAtRule createVendorAtRule() {
        return new VendorAtRuleI(model);
    }

    @Override
    public AtRuleId createAtRuleId() {
        return new AtRuleIdI(model);
    }

    @Override
    public AtRuleId createAtRuleId(CharSequence text) {
        AtRuleId atRuleId = createAtRuleId();
        atRuleId.setContent(text);
        return atRuleId;
    }

    @Override
    public MozDocument createMozDocument() {
        return new MozDocumentI(model);
    }

    @Override
    public MozDocumentFunction createMozDocumentFunction() {
        return new MozDocumentFunctionI(model);
    }

    @Override
    public GenericAtRule createGenericAtRule() {
        return new GenericAtRuleI(model);
    }

    @Override
    public WebkitKeyframes createWebkitKeyFrames() {
        return new WebkitKeyframesI(model);
    }

    @Override
    public WebkitKeyframesBlock createWebkitKeyFramesBlock() {
        return new WebkitKeyframesBlockI(model);
    }

    @Override
    public WebkitKeyframeSelectors createWebkitKeyframeSelectors() {
        return new WebkitKeyframeSelectorsI(model);
    }

    @Override
    public AtRule createAtRule() {
        return new AtRuleI(model);
    }

    @Override
    public MediaFeatureValue createMediaFeatureValue(Expression expression) {
        MediaFeatureValue mediaFeatureValue = createMediaFeatureValue();
        mediaFeatureValue.setExpression(expression);
        return mediaFeatureValue;
    }

}
