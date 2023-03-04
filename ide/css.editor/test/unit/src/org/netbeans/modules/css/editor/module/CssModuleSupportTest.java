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
package org.netbeans.modules.css.editor.module;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;

/**
 *
 * @author marekfukala
 */
public class CssModuleSupportTest extends NbTestCase {
    
    public CssModuleSupportTest(String name) {
        super(name);
    }

    
    public void testGetProperty() {
        PropertyDefinition p = Properties.getPropertyDefinition( "perspective");
        assertNotNull(p);
        assertEquals("perspective", p.getName());
        
        //get refered(invisible) property of the same name
        p = Properties.getPropertyDefinition( "perspective", true);
        assertNotNull(p);
        assertEquals("@perspective", p.getName());
    }
    
    public void testAllPropertiesHaveSomeGrammar() {
        for (PropertyDefinition property : Properties.getPropertyDefinitions(null)) {
            assertNotNull(property);
            assertNotNull(property.getName());
            assertNotNull(property.getGrammar());
            assertTrue(String.format("Property %s have empty grammar", property.getName()), !property.getGrammar().isEmpty());
            }
        }

    
}
