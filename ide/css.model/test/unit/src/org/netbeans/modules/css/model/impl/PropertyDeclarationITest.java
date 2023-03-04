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

import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class PropertyDeclarationITest extends ModelTestBase {

    public PropertyDeclarationITest(String name) {
        super(name);
    }

    public void testResolvedProperty() throws BadLocationException, ParseException {
        String code = "div { padding : 1px 2px }";
        
        StyleSheet styleSheet = createStyleSheet(code);
        Declaration d = styleSheet.getBody().getRules().get(0).getDeclarations().getDeclarations().get(0);
        assertNotNull(d);
        
        PropertyDeclaration pd = d.getPropertyDeclaration();
        assertNotNull(pd);
        
        ResolvedProperty rp = pd.getResolvedProperty();
        assertNotNull(rp);
        
        assertTrue(rp.isResolved());
        Node ptree = rp.getParseTree();
        
        assertNotNull(ptree);
    }
    
    
}
