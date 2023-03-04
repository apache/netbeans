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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;

/**
 * Actually there's no such class as RubyModule, the property descriptors
 * are supplied by the default css module
 *
 * @author mfukala@netbeans.org
 */
public class RubyModuleTest extends CslTestBase {

    public RubyModuleTest(String testName) {
        super(testName);
    }
    
    public void testProperties() {
        PropertyDefinition p = Properties.getPropertyDefinition( "ruby-align");
        
        assertNotNull(p);
        
        assertTrue(new ResolvedProperty(p, "start").isResolved());
        assertTrue(new ResolvedProperty(p, "center").isResolved());
    }
    
   
    
}
