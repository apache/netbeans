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
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class BodyITest extends ModelTestBase {

    public BodyITest(String name) {
        super(name);
    }

    public void testPage() throws BadLocationException, ParseException {
        String code = "@page:left { margin-left: 2cm }";
        
        StyleSheet styleSheet = createStyleSheet(code);
        
//        TestUtil.dumpResult(TestUtil.parse(code));
        
        List<Page> pages = styleSheet.getBody().getPages();
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        Page page = pages.get(0);
        assertNotNull(page);
        
        assertEquals(code, page.getContent().toString());
        
    }
    
    public void testAddRemovePage() {
        String code = "@page:left { margin-left: 2cm }";
        CssParserResult result = TestUtil.parse(code);
//        TestUtil.dumpResult(result);
        assertEquals(0, result.getDiagnostics().size());
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        
        List<Page> pages = styleSheet.getBody().getPages();
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        Page page = pages.get(0);
        assertNotNull(page);
        
        assertEquals(code, page.getContent().toString());
        assertEquals(code, model.getModelSource().toString());
        
        styleSheet.getBody().removePage(page);
        
        pages = styleSheet.getBody().getPages();
        assertNotNull(pages);
        assertEquals(0, pages.size());
        
        assertEquals("", model.getModelSource().toString());

        ElementFactory ef = model.getElementFactory();
        Page newPage = ef.createPage("@page { margin: 3cm }");
        
        styleSheet.getBody().addPage(newPage);
        
        assertEquals("@page { margin: 3cm }", model.getModelSource().toString());
        
    }
    
}
