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
