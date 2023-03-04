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

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.GenericAtRule;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class GenericAtRuleITest extends ModelTestBase {

    public GenericAtRuleITest(String name) {
        super(name);
    }

    public void testBasic() throws BadLocationException, ParseException {
        String code = "@-some-rule { property: value; }";

        StyleSheet styleSheet = createStyleSheet(code);

//        TestUtil.dumpResult(TestUtil.parse(code));

        List<GenericAtRule> gars = styleSheet.getBody().getGenericAtRules();
        assertNotNull(gars);
        assertEquals(1, gars.size());

        GenericAtRule gar = gars.get(0);
        assertNotNull(gar);

        assertEquals(code, gar.getContent().toString());

    }

    public void testAddToEmptyStyleSheet() {
        Model model = createModel();
        StyleSheet styleSheet = getStyleSheet(model);
        ElementFactory f = model.getElementFactory();
        
        Body body = f.createBody();
        styleSheet.setBody(body);

        GenericAtRule gar = f.createGenericAtRule();
        
        String garContent = "@-a-rule { prop: val }";
        gar.setContent(garContent);
        
        styleSheet.getBody().addGenericAtRule(gar);
        
        List<GenericAtRule> gars = styleSheet.getBody().getGenericAtRules();
        assertNotNull(gars);
        assertEquals(1, gars.size());

        GenericAtRule g = gars.get(0);
        assertNotNull(g);

        assertEquals(garContent, g.getContent());
        assertEquals(garContent, model.getModelSource().toString());
        
    }
    
    public void testAddToExistingStyleSheet() {
        String code = "div { color: red; }";
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);

        ElementFactory f = model.getElementFactory();
        
        GenericAtRule gar = f.createGenericAtRule();
        
        String garContent = "@-a-rule { prop: val }";
        gar.setContent(garContent);
        
        styleSheet.getBody().addGenericAtRule(gar);
        
        List<GenericAtRule> gars = styleSheet.getBody().getGenericAtRules();
        assertNotNull(gars);
        assertEquals(1, gars.size());

        GenericAtRule g = gars.get(0);
        assertNotNull(g);

        assertEquals(garContent, g.getContent());
        assertEquals(code + garContent, model.getModelSource().toString());
        
    }
    
    public void testRemoveFromExistingStyleSheet() {
        String code1 = "div { color: red; }";
        String code2 = "@-a-rule { prop: val }";
        
        Model model = createModel(code1 + code2);
        StyleSheet styleSheet = getStyleSheet(model);

        List<GenericAtRule> gars = styleSheet.getBody().getGenericAtRules();
        assertNotNull(gars);
        assertEquals(1, gars.size());

        GenericAtRule g = gars.get(0);
        assertNotNull(g);

        styleSheet.getBody().removeGenericAtRule(g);
        
        assertEquals(0, styleSheet.getBody().getGenericAtRules().size());
        
        assertEquals(code1, model.getModelSource().toString());
        
        
    }
    
    
    
}
