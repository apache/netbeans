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
package org.netbeans.modules.css.model.api;

import java.io.IOException;

/**
 *
 * @author marekfukala
 */
public class StyleSheetTest extends ModelTestBase {

    public StyleSheetTest(String name) {
        super(name);
    }
    
    public void testCreateStyleSheet() throws IOException, InterruptedException {
        Model model = createModel();
        StyleSheet styleSheet = model.getStyleSheet();

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
    
}
