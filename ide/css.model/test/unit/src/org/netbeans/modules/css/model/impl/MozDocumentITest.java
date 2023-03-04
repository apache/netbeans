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
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.MozDocument;
import org.netbeans.modules.css.model.api.MozDocumentFunction;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class MozDocumentITest extends ModelTestBase {

    public MozDocumentITest(String name) {
        super(name);
    }

    public void testBasic() throws BadLocationException, ParseException {
        String code = "@-moz-document domain(mozilla.org),  regexp(\"https:.*\")  { body { color: purple } }";

        StyleSheet styleSheet = createStyleSheet(code);

//        TestUtil.dumpResult(TestUtil.parse(code));

        List<MozDocument> mozdocs = styleSheet.getBody().getMozDocuments();
        assertNotNull(mozdocs);
        assertEquals(1, mozdocs.size());

        MozDocument mozdoc = mozdocs.get(0);
        assertNotNull(mozdoc);

        Body body = mozdoc.getBody();
        assertNotNull(body);
        
        Declarations ds = body.getRules().get(0).getDeclarations();
        assertNotNull(ds);
        
        List<MozDocumentFunction> restrictions = mozdoc.getRestrictions();
        assertEquals(2, restrictions.size());
        
        MozDocumentFunction r1 = restrictions.get(0);
        assertNotNull(r1);
        
        assertEquals("domain(mozilla.org)", r1.getContent().toString());
        
        MozDocumentFunction r2 = restrictions.get(1);
        assertNotNull(r2);
        
        assertEquals("regexp(\"https:.*\")", r2.getContent().toString());

    }

    //XXX: MozDocument is not mutable
//    public void testAddToEmptyStyleSheet() {
//        Model model = createModel();
//        StyleSheet styleSheet = getStyleSheet(model);
//        ElementFactory f = model.getElementFactory();
//        
//        Body body = f.createBody();
//        styleSheet.setBody(body);
//
//        MozDocument mozd = f.createMozDocument();
//        MozDocumentFunction mozdf1 = f.createMozDocumentFunction();
//        mozdf1.setContent("domain(mozilla.org)");
//        MozDocumentFunction mozdf2 = f.createMozDocumentFunction();
//        mozdf2.setContent("regexp(\"https:.*\")");
//    }
    
}
