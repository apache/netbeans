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

import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class BasicUsetInterfaceModuleTest extends CssModuleTestBase {

    public BasicUsetInterfaceModuleTest(String name) {
        super(name);
    }

    public void testProperties() throws ParseException {
//        assertPropertyValues("appearance", "window", "signature");
//        
//        assertPropertyValues("icon", "auto", "url('hello.png')", "url('hello1.png'), url(hello2.png)");
        
//        assertPropertyValues("cursor", "auto");
        assertPropertyValues("cursor", "url(icon.png) 10 10, auto");
//        assertPropertyValues("cursor", "url(icon.png), copy");
//        
//        assertPropertyValues("@system-font", "status-bar");
//        assertPropertyValues("font", "status-bar");
// 
//        assertPropertyValues("nav-right", "auto", "auto root");
        
    }
    
}
