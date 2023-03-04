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

import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;

/**
 *
 * @author mfukala@netbeans.org
 */
public class FirefoxModuleTest extends CssModuleTestBase {

    public FirefoxModuleTest(String name) {
        super(name);
    }
    
    public void testBrowser() {
        CssEditorModule firefoxModule = getCssModuleByClass(FirefoxModule.class);
        assertNotNull(firefoxModule);
        
        Browser firefox = firefoxModule.getExtraBrowsers(null).iterator().next();
        
        assertEquals("Firefox", firefox.getName());
        assertEquals("Mozilla", firefox.getVendor());
        assertNotNull(firefox.getDescription());
        
        assertNotNull(firefox.getActiveIcon());
        assertNotNull(firefox.getInactiveIcon());
    }
}
