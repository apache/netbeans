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

package org.netbeans.modules.netbinox;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.Callable;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AgentTest extends NetigsoHid {
    private File agent;
    private ModuleManager mgr;
    public AgentTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        agent = createTestJAR("agent", null);
    }
    
    public void testAgentClassRedefinesHello() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        mgr.mutexPrivileged().enterWriteAccess();
        Module m = mgr.create(agent, null, false, false, false);
        try {
            mgr.enable(m);
            Callable<?> c = (Callable<?>) m.getClassLoader().loadClass("org.agent.HelloWorld").getDeclaredConstructor().newInstance();
            assertEquals("Bytecode has been patched", "Ahoj World!", c.call());
        } finally {
            mgr.disable(m);
        }
    }
}
