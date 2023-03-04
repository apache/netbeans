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
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstallTmpModuleTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(InstallTmpModuleTest.class.getName());

    public InstallTmpModuleTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        Configuration c = NbModuleSuite.createConfiguration(InstallTmpModuleTest.class);
        c = c.addTest("testInstallJARFromTmp").failOnException(Level.INFO).gui(false);
        return c.suite();
    }
    
    public void testInstallJARFromTmp() throws Exception {
        final ModuleManager mgr = Main.getModuleSystem().getManager();
        File dir = File.createTempFile("dir", ".dir");
        dir.delete();
        dir.mkdirs();
        assertTrue("Directory created", dir.isDirectory());
        assertEquals("No files in it", 0, dir.list().length);
        File f = CLILookupHelpTest.createJAR(dir, "org.tmp.test", null);
        final Module m = mgr.create(f, null, false, false, false);

        mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                mgr.enable(m);
                return null;
            }
        });
        
        assertTrue("Module is enabled", m.isEnabled());
        
    }
}
