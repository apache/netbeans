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
import org.openide.util.RequestProcessor;
import static org.netbeans.CLIHandlerTest.*;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerReadingOfInputTest extends NbTestCase {

    static final ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    static final ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private Logger LOG;

    public CLIHandlerReadingOfInputTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();

        // all handlers shall be executed immediatelly
        CLIHandler.finishInitialization (false);
        
        // setups a temporary file
        String p = getWorkDirPath ();
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

    @Override
    protected void tearDown() throws Exception {
        CLIHandler.stopServer();
    }
    
    protected @Override Level logLevel() {
        return Level.FINEST;
    }

    protected @Override int timeOut() {
        return 50000;
    }
    
    public void testReadingOfInputWorksInHandler() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[is.available() / 2];
                    if (arr.length > 0) {
                        assertEquals("Read amount is the same", arr.length, is.read(arr));
                    }
                    is.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, new ByteArrayInputStream(template), nullOutput, nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "Read two bytes", 2, h1.arr.length);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            
            assertNotNull("Attempt " + i + ": " + "Can read as well", h2.arr);
            assertEquals("Attempt " + i + ": " + "Just one char", 1, h2.arr.length);
            assertEquals("Attempt " + i + ": " + "And is the right one", template[2], h2.arr[0]);
            
            h1.arr = null;
            h2.arr = null;
        }
    }
}
