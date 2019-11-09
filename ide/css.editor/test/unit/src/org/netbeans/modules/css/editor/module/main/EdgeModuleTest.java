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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;

/**
 *
 * @author peedeeboy
 */
public class EdgeModuleTest extends CssModuleTestBase {

    public EdgeModuleTest(String name) {
        super(name);
    }
    
    public void testBrowser() {
        CssEditorModule edgeModule = getCssModuleByClass(EdgeModule.class);
        assertNotNull(edgeModule);
        
        Browser edge = edgeModule.getExtraBrowsers(null).iterator().next();
        
        assertEquals("Edge", edge.getName());
        assertEquals("Microsoft", edge.getVendor());
        assertNotNull(edge.getDescription());
        
        assertNotNull(edge.getActiveIcon());
        assertNotNull(edge.getInactiveIcon());
    }
}
