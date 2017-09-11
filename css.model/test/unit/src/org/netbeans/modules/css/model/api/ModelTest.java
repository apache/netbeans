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
