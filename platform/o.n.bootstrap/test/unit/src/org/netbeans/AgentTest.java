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

package org.netbeans;

import java.io.File;
import java.util.concurrent.Callable;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AgentTest extends SetupHid {
    public AgentTest(String name) {
        super(name);
    }
    
    public void testAgentClassRedefinesHello() throws Exception {
        File jar = new File(jars, "agent.jar");
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m = mgr.create(jar, null, false, false, false);
        try {
            mgr.enable(m);
            Callable<?> c = (Callable<?>) m.getClassLoader().loadClass("org.agent.HelloWorld").getDeclaredConstructor().newInstance();
            assertEquals("Bytecode has been patched", "Ahoj World!", c.call());
        } finally {
            mgr.disable(m);
        }
    }
}
