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

import java.io.IOException;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class ModelTest extends ModelTestBase {

    public ModelTest(String name) {
        super(name);
    }

    public void testAddElements() throws BadLocationException, ParseException, IOException, InterruptedException {
        String code = "div {\n"
                + "\tcolor: red; /* comment */\n"
                + "\tpadding: 10px;\n"
                + "}";

        CssParserResult result = TestUtil.parse(code);

        Model model = new Model(result);
        ElementFactory factory = model.getElementFactory();
        StyleSheet styleSheet = model.getStyleSheet();

        Expression expression = factory.createExpression("20px");
        PropertyValue pv = factory.createPropertyValue(expression);
        Property property = factory.createProperty("margin");
        PropertyDeclaration declaration = factory.createPropertyDeclaration(property, pv, false);
        Declarations declarations = factory.createDeclarations(declaration);
        Selector selector = factory.createSelector("h1");
        SelectorsGroup sgroup = factory.createSelectorsGroup(selector);
        Rule rule = factory.createRule(sgroup, declarations);

        styleSheet.getBody().addRule(rule);

        Difference[] diffs = model.getModelSourceDiff();
        assertEquals(1, diffs.length);

        Difference diff = diffs[0];
        assertEquals(Difference.ADD, diff.getType());

        assertEquals("h1 {\n"
                + "    margin: 20px;\n\n"
                + "}\n", diff.getSecondText());

    }

    public void testModify() throws BadLocationException, ParseException, IOException, InterruptedException {
        String code = "/* comment */\n"
                + " div { \n"
                + "     color: red; /* my color */\n"
                + "     color: green; /* c2 */\n"
                + " } \n"
                + "a { padding: 2px }\n";

        Model model = createModel(code);
        ElementFactory factory = model.getElementFactory();
        StyleSheet styleSheet = model.getStyleSheet();
        assertNotNull(styleSheet);

        Rule rule = styleSheet.getBody().getRules().get(0);
        assertNotNull(rule);

        Declaration declaration = rule.getDeclarations().getDeclarations().get(1);
        assertNotNull(declaration);

        PropertyDeclaration propertyDeclaration = declaration.getPropertyDeclaration();
        assertNotNull(propertyDeclaration);
        
        //add new property declaration at the end of the rule 
        PropertyDeclaration newpd = factory.createPropertyDeclaration(
                factory.createProperty("margin"), 
                factory.createPropertyValue(factory.createExpression("20px")), false);
        
        Declaration newd = factory.createDeclaration();
        newd.setPropertyDeclaration(newpd);
        rule.getDeclarations().addDeclaration(newd);

        System.out.println(model.getModelSource().toString());
        
        Difference[] diffs = model.getModelSourceDiff();
        assertEquals(1, diffs.length);

        Difference diff = diffs[0];
        assertEquals("Difference(ADD, 4, 0, 5, 5)", diff.toString());

    }

    public void testBuildModel() throws BadLocationException, ParseException {
        String code = "/* comment */\n"
                + " div { \n"
                + "     color: red; /* my color */\n"
                + "     color: green;\n"
                + " } \n"
                + "a { padding: 2px }\n";

        Model model = createModel(code);
        StyleSheet styleSheet = model.getStyleSheet();
        assertNotNull(styleSheet);

        Collection<Rule> rules = styleSheet.getBody().getRules();
        assertNotNull(rules);
        assertEquals(2, rules.size());

        Rule rule = rules.iterator().next();
        Collection<Selector> selectors = rule.getSelectorsGroup().getSelectors();
        assertNotNull(selectors);
        assertEquals(1, selectors.size());

        Selector selector = selectors.iterator().next();
        assertEquals("div", selector.getContent().toString().trim());

        Collection<Declaration> declarations = rule.getDeclarations().getDeclarations();
        assertNotNull(declarations);
        assertEquals(2, declarations.size());

        Declaration declaration = declarations.iterator().next();
        assertNotNull(declaration);
        
        PropertyDeclaration propertyDeclaration = declaration.getPropertyDeclaration();
        assertNotNull(propertyDeclaration);
        
        assertEquals("color", propertyDeclaration.getProperty().getContent());

        PropertyValue pv = propertyDeclaration.getPropertyValue();
        assertNotNull(pv);
        
        Expression expression = pv.getExpression();
        assertNotNull(expression);
        assertEquals("red", expression.getContent());

    }

    public void testRunReadTask() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("div { color: red }");
        Model model = new Model(result);
        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                assertEquals(1, styleSheet.getBody().getRules().size());
            }
        });
        
    }
    
    public void testRunWriteTask() throws BadLocationException, ParseException {
        CssParserResult result = TestUtil.parse("div { color: red }");
        Model model = new Model(result);
        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.getBody().getRules().get(0).getDeclarations()
                        .getDeclarations().get(0).getPropertyDeclaration().getProperty().setContent("background-color");
            }
        });
        
    }
    
//    public void testModelCaching() {
//        CssParserResult result = TestUtil.parse("div { color: red; }");
//        Model model = Model.createModel(result);
//        
//        System.gc();
//        
//        Model model2 = Model.createModel(result);
//        
//        assertSame(model, model2);
//
//        //check the model is properly released when no one holds it
//        Reference<Model> ref = new WeakReference<Model>(model);
//        
//        model = null;
//        model2 = null;
//        
//        assertGC("model not properly released", ref);
//        
//    }
    
    
}
