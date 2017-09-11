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
