/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.List;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.ResolvedToken;

/**
 *
 * @author mfukala@netbeans.org
 */
public class BasicBoxModelModuleTest extends CssModuleTestBase {

    public BasicBoxModelModuleTest(String testName) {
        super(testName);
    }
    
    public void testProperties() {
        assertPropertyValues("display", "table", "table-cell");
        assertPropertyValues("margin", "10px", "20px 30px 40px");
        assertPropertyValues("padding", "1px");
    }
    
    public void testMargin() {
        assertPropertyDeclaration("margin: 1px"); //tblr == 1
        assertPropertyDeclaration("margin: 1px 2px"); //tb=1, lr=2
        assertPropertyDeclaration("margin: 1px 2px 3px"); //t=1, lr=2, b=3
        assertPropertyDeclaration("margin: 1px 2px 3px 4px"); //t=1, r=2, b=3, l=4
    }
    
    public void testMargin_Model() {
        PropertyDefinition margin = Properties.getPropertyDefinition( "margin");
        ResolvedProperty eval = new ResolvedProperty(margin, "1px 2px 3px");
        List<ResolvedToken> resolved = eval.getResolvedTokens();
        for(ResolvedToken token : resolved) {
            System.out.println(token);
        }
        
    }
    
}
