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
package org.netbeans.modules.tomcat5;

import org.netbeans.modules.tomcat5.deploy.TomcatTarget;
import org.netbeans.modules.tomcat5.deploy.TomcatModule;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class TomcatModuleTest extends NbTestCase {

    public TomcatModuleTest(String name) {
        super(name);
    }

    public void testUrlAndId() {
        TomcatTarget target = new TomcatTarget("test", "test", "http://localhost:8080");
        TomcatModule module = new TomcatModule(target, "");
        assertEquals("/", module.getPath());
        assertEquals("http://localhost:8080/", module.getWebURL());
        assertEquals(module.getWebURL(), module.getModuleID());
        assertEquals(module.getWebURL(), module.toString());

        module = new TomcatModule(target, "/app");
        assertEquals("/app", module.getPath());
        assertEquals("http://localhost:8080/app", module.getWebURL());
        assertEquals(module.getWebURL(), module.getModuleID());
        assertEquals(module.getWebURL(), module.toString());
    }
}
