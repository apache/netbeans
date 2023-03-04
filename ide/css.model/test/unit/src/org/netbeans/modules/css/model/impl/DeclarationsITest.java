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

import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class DeclarationsITest extends ModelTestBase {

    public DeclarationsITest(String name) {
        super(name);
    }

    public void testResolvedProperty() throws BadLocationException, ParseException {
        String code = "div { padding : 1px 2px }";
        
        StyleSheet styleSheet = createStyleSheet(code);
        Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
    }
    
    public void testAddRemoveDeclaration() {
        String code = "div {\n"
                + "    font-size: 222px;\n"
                + "    margin: 2px 1px 2px 2px; \n"
                + "}\n";
        
//        CssParserResult result = TestUtil.parse(code);
//        TestUtil.dumpResult(result);
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        Declaration marginDecl = ds.getDeclarations().get(1);
        assertNotNull(marginDecl);
        
        ds.removeDeclaration(marginDecl);
        
        ElementFactory ef = model.getElementFactory();
        PropertyDeclaration newMargin = ef.createPropertyDeclaration(
                ef.createProperty("margin"),
                ef.createPropertyValue(ef.createExpression("3px")),
                false);
        Declaration newMarginDecl = ef.createDeclaration();
        newMarginDecl.setPropertyDeclaration(newMargin);
        
        ds.addDeclaration(newMarginDecl);
        
//        System.out.println(model.getModelSource());
        
        assertEquals("div {\n"
                + "    font-size: 222px;\n"
                + "    margin: 3px;\n"
                + "}\n", model.getModelSource().toString());
        
    }
    
    public void testRemoveNewDeclaration() {
        String code = "div {}\n";
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        Rule rule = styleSheet.getBody().getRules().get(0);
        assertNotNull(rule);
        Declarations ds = rule.getDeclarations();
        assertNull(ds);
        
        ElementFactory ef = model.getElementFactory();
        ds = ef.createDeclarations();
        rule.setDeclarations(ds);
        
        assertNotNull(rule.getDeclarations());
        
        PropertyDeclaration newMargin = ef.createPropertyDeclaration(
                ef.createProperty("margin"),
                ef.createPropertyValue(ef.createExpression("3px")),
                false);
        Declaration newMarginDecl = ef.createDeclaration();
        newMarginDecl.setPropertyDeclaration(newMargin);
        
        ds.addDeclaration(newMarginDecl);
        assertEquals(1, ds.getDeclarations().size());
        
        ds.removeDeclaration(newMarginDecl);
        assertEquals(0, ds.getDeclarations().size());
        
    }
    
    public void testAddRemoveDeclarationWithComments() {
        String code = "div {\n"
                + "    font-size: 222px;\n"
                + "    margin: 2px 1px 2px 2px; /* comment */ \n"
                + "}\n";
        
//        CssParserResult result = TestUtil.parse(code);
//        TestUtil.dumpResult(result);
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        Declaration marginDecl = ds.getDeclarations().get(1);
        assertNotNull(marginDecl);
        
        PropertyDeclaration margin = marginDecl.getPropertyDeclaration();
        assertNotNull(margin);
        
        ds.removeDeclaration(marginDecl);
        
        ElementFactory ef = model.getElementFactory();
        PropertyDeclaration newMargin = ef.createPropertyDeclaration(
                ef.createProperty("margin"),
                ef.createPropertyValue(ef.createExpression("3px")),
                false);
        Declaration newMarginDecl = ef.createDeclaration();
        newMarginDecl.setPropertyDeclaration(newMargin);
        
        ds.addDeclaration(newMarginDecl);
        
//        System.out.println(model.getModelSource());
        
        assertEquals("div {\n"
                + "    font-size: 222px;\n"
                + " /* comment */ \n"
                + "    margin: 3px;\n"
                + "}\n", model.getModelSource().toString());
        
    }
    
    public void testRemovePropertyWithStarHack() {
        String code = "div {\n"
                + "    font-size: 222px;\n"
                + "    *margin: 2px 1px 2px 2px; \n"
                + "}\n";
        
//        CssParserResult result = TestUtil.parse(code);
//        TestUtil.dumpResult(result);
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        Declaration marginDecl = ds.getDeclarations().get(1);
        assertNotNull(marginDecl);
                
        ds.removeDeclaration(marginDecl);
        
//        System.out.println(model.getModelSource());
        
        assertEquals("div {\n"
                + "    font-size: 222px;\n"
                + "}\n", model.getModelSource().toString());
        
    }
    
     public void testAddSecondDeclarationWithSemi() {
         //first declaration not followed by semicolon
        String code = "div {\n"
                + "    font-size: 222px\n"
                + "}\n";
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        ElementFactory ef = model.getElementFactory();
        PropertyDeclaration newMargin = ef.createPropertyDeclaration(
                ef.createProperty("margin"),
                ef.createPropertyValue(ef.createExpression("3px")),
                false);
        
        Declaration newMarginDecl = ef.createDeclaration();
        newMarginDecl.setPropertyDeclaration(newMargin);
        
        ds.addDeclaration(newMarginDecl);
        
        assertEquals("div {\n"
                + "    font-size: 222px;\n"
                + "    margin: 3px;\n"
                + "}\n", model.getModelSource().toString());
        
    }
    
     public void testAddSecondDeclaration() {
         //first declaration not followed by semicolon
        String code = "div {\n"
                + "    font-size: 222px;\n"
                + "}\n";
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        ElementFactory ef = model.getElementFactory();
        PropertyDeclaration newMargin = ef.createPropertyDeclaration(
                ef.createProperty("margin"),
                ef.createPropertyValue(ef.createExpression("3px")),
                false);
        Declaration newMarginDecl = ef.createDeclaration();
        newMarginDecl.setPropertyDeclaration(newMargin);
        
        ds.addDeclaration(newMarginDecl);
        
        assertEquals("div {\n"
                + "    font-size: 222px;\n"
                + "    margin: 3px;\n"
                + "}\n", model.getModelSource().toString());
        
    }
    
    
}
