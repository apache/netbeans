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
package org.netbeans;

import java.io.File;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CLIHandlerNoServerTest extends NbTestCase {

    public CLIHandlerNoServerTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("org.netbeans.CLIHandler.server", "false");
        
        // setups a temporary file
        String p = getWorkDirPath();
        if (p == null) {
            p = System.getProperty("java.io.tmpdir");
        }
        String tmp = p;
        assertNotNull(tmp);
        System.getProperties().put("netbeans.user", tmp);

        File f = new File(tmp, "lock");
        if (f.exists()) {
            assertTrue("Clean up previous mess", f.delete());
            assertTrue(!f.exists());
        }
    }
    

    public void testCannotStartForTheSecondTime() {
        CLIHandler.Status res = CLIHandlerTest.cliInitialize(
            new String[0], 
            new CLIHandler[0], 
            CLIHandlerTest.nullInput, CLIHandlerTest.nullOutput, CLIHandlerTest.nullOutput
        );

        assertEquals("Started", 0, res.getExitCode());
        
        CLIHandler.Status snd = CLIHandlerTest.cliInitialize(
            new String[0], 
            new CLIHandler[0], 
            CLIHandlerTest.nullInput, CLIHandlerTest.nullOutput, CLIHandlerTest.nullOutput
        );
        
        assertEquals("Can't start for the second time", CLIHandler.Status.ALREADY_RUNNING, snd.getExitCode());
    }
}
