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

import java.io.*;
import java.util.logging.Level;
import org.netbeans.junit.*;
import java.util.*;
import java.util.logging.Logger;
import org.netbeans.CLIHandler.Status;
import org.openide.util.RequestProcessor;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerMemoryUserDirTest extends NbTestCase {

    private static ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    private static ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private Logger LOG;

    public CLIHandlerMemoryUserDirTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        System.getProperties().put("netbeans.user", "memory");
    }
    
    protected @Override Level logLevel() {
        return Level.FINEST;
    }

    protected @Override int timeOut() {
        return 50000;
    }
 
    public void testCLIHandlerCanStopEvaluation() throws Exception {
        class H extends CLIHandler {
            private int cnt;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                cnt++;
                return 1;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        CLIHandler.Args args = new CLIHandler.Args(new String[0], nullInput, nullOutput, nullOutput, ".");
        CLIHandler.Status res = CLIHandler.initialize(args, null, Collections.<CLIHandler>singletonList(h1), true, true, null);
        int result = CLIHandler.finishInitialization(false);

        assertEquals("Res is 0", 0, res.getExitCode());
        assertEquals("CLI evaluation failed with return code of h1", 1, result);
        assertEquals("First one executed", 1, h1.cnt);
        // all handlers shall be executed immediatelly
    }
}
