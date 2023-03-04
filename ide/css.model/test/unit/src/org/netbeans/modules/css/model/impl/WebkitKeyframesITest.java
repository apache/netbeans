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
import org.netbeans.modules.css.model.api.AtRuleId;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.model.api.WebkitKeyframeSelectors;
import org.netbeans.modules.css.model.api.WebkitKeyframes;
import org.netbeans.modules.css.model.api.WebkitKeyframesBlock;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class WebkitKeyframesITest extends ModelTestBase {

    public WebkitKeyframesITest(String name) {
        super(name);
    }

    public void testBasic() throws BadLocationException, ParseException {
        String code = "@-webkit-keyframes spin { 40% {  left: 150px;  } from { left: 2px } }";

        StyleSheet styleSheet = createStyleSheet(code);

//        TestUtil.dumpResult(TestUtil.parse(code));

        List<WebkitKeyframes> wkfs = styleSheet.getBody().getWebkitKeyFrames();
        assertNotNull(wkfs);
        assertEquals(1, wkfs.size());

        WebkitKeyframes wkf = wkfs.get(0);
        assertNotNull(wkf);

        AtRuleId id = wkf.getAtRuleId();
        assertNotNull(id);
        assertEquals("spin", id.getContent().toString());
        
        List<WebkitKeyframesBlock> blocks = wkf.getKeyFramesBlocks();
        assertNotNull(blocks);
        assertEquals(2, blocks.size());
        
        WebkitKeyframesBlock block = blocks.get(0);
        assertNotNull(block);
        WebkitKeyframeSelectors selectors = block.getSelectors();
        assertNotNull(selectors);
        assertEquals("40%", selectors.getContent().toString());
        
        Declarations ds = block.getDeclarations();
        assertNotNull(ds);
        assertEquals(1, ds.getDeclarations().size());
        Declaration d = ds.getDeclarations().get(0);
        assertNotNull(d);
        PropertyDeclaration pd = d.getPropertyDeclaration();
        assertNotNull(pd);
        
        block = blocks.get(1);
        assertNotNull(block);
        selectors = block.getSelectors();
        assertNotNull(selectors);
        assertEquals("from", selectors.getContent().toString());
        
        ds = block.getDeclarations();
        assertNotNull(ds);
        assertEquals(1, ds.getDeclarations().size());
        d = ds.getDeclarations().get(0);
        assertNotNull(d);
        
    }

    //XXX: WebkitKeyframes is not mutable
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
