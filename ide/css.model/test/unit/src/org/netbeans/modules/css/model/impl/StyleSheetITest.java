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
package org.netbeans.modules.css.model.impl;

import java.io.IOException;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.Property;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.Selector;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class StyleSheetITest extends ModelTestBase {

    public StyleSheetITest(String name) {
        super(name);
    }
    
    public void testCreateStyleSheet() throws IOException, InterruptedException {
        Model model = createModel();
        StyleSheet styleSheet = getStyleSheet(model);

        ElementFactory factory = model.getElementFactory();
        Body body = factory.createBody();
        styleSheet.setBody(body);

        Property p = factory.createProperty("border-color");
        Expression e = factory.createExpression("green");
        PropertyValue pv = factory.createPropertyValue(e);
        PropertyDeclaration d = factory.createPropertyDeclaration(p, pv, false);

        Declarations ds = factory.createDeclarations(d);

        Selector s = factory.createSelector(".myclass");
        Selector s2 = factory.createSelector("#myid");
        SelectorsGroup sg = factory.createSelectorsGroup(s, s2);

        Rule rule = factory.createRule(sg, ds);
        body.addRule(rule);

        CharSequence code = model.getModelSource();
        assertEquals("\n.myclass, #myid {\n"
                + "    border-color: green;\n\n"
                + "}\n", code.toString());
    }
    
    public void testParseSource() throws BadLocationException, ParseException {
        String code = "div { \n"
                + "     color: green;\n"
                + " } \n";

        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        assertNotNull(styleSheet);

        Collection<Rule> rules = styleSheet.getBody().getRules();
        assertNotNull(rules);
        assertEquals(1, rules.size());

        Rule rule = rules.iterator().next();
        Collection<Selector> selectors = rule.getSelectorsGroup().getSelectors();
        assertNotNull(selectors);
        assertEquals(1, selectors.size());

        Selector selector = selectors.iterator().next();
        assertEquals("div", selector.getContent().toString().trim());

        Collection<Declaration> declarations = rule.getDeclarations().getDeclarations();
        assertNotNull(declarations);
        assertEquals(1, declarations.size());

        Declaration declaration = declarations.iterator().next();
        assertNotNull(declaration);
        PropertyDeclaration pd = declaration.getPropertyDeclaration();
        assertEquals("color", pd.getProperty().getContent());
        
        PropertyValue pv = pd.getPropertyValue();
        assertNotNull(pv);
        
        Expression expression = pv.getExpression();
        assertNotNull(expression);
        assertEquals("green", expression.getContent());

    
    }
    
}
