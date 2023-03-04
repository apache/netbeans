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

import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.ElementHandle;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;

/**
 *
 * @author marekfukala
 */
public class ElementHandleImplTest extends ModelTestBase {
    
    public ElementHandleImplTest(String name) {
        super(name);
    }

//    public void testGetElementId() {
//        Model model = createModel("div { color: red }\n .clz { color:blue } ");
//        StyleSheet styleSheet = getStyleSheet(model);
//        
//        Rule rule = styleSheet.getBody().getRules().get(0);
//        String path = ElementHandleImpl.createPath((ModelElement)rule);
//        assertEquals("StyleSheet/Body/BodyItem/Rule", path);
//
//        rule = styleSheet.getBody().getRules().get(1);
//        path = ElementHandleImpl.createPath((ModelElement)rule);
//        assertEquals("StyleSheet/Body/BodyItem|2/Rule", path);
//        
//    }
    
    public void testResolveElementHandle_to_same_model() {
        String code = "div { color: red }\n .clz { color:blue }";
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        
        Rule rule = styleSheet.getBody().getRules().get(1);
        ElementHandle handle = rule.getElementHandle();
        assertNotNull(handle);
        
        //resolve to the same model
        Element resolved = handle.resolve(model);
        assertNotNull(resolved);
        assertSame(rule, resolved);
        
    }
    
    public void testResolveElementHandle_to_different_model() {
        String code = "div { color: red }\n .clz { color:blue }";
        
        Model model = createModel(code);
        StyleSheet styleSheet = getStyleSheet(model);
        
        Rule rule = styleSheet.getBody().getRules().get(1);
        ElementHandle handle = rule.getElementHandle();
        assertNotNull(handle);
        
        code = "xxx { color: fuchsia }\n .clz { color:green }";
        model = createModel(code);
        
        //resolve to the new model
        Element resolved = handle.resolve(model);
        assertNotNull(resolved);
        assertTrue(resolved instanceof Rule);
        Rule resolvedRule = (Rule)resolved;
        SelectorsGroup selectorsGroup = resolvedRule.getSelectorsGroup();
        assertNotNull(selectorsGroup);
        assertEquals(".clz", model.getElementSource(selectorsGroup).toString());
        
    }
    
}