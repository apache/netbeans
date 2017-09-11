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
