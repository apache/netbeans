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
@RandomlyFails
public class CLIHandlerServerWaitsTest extends NbTestCase {

    static final ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    static final ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private Logger LOG;

    public CLIHandlerServerWaitsTest(String name) {
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
    
    public void testServerWaitsBeforeFinishInitializationIsCalledOn () throws Exception {
        // this tests will not execute handlers immediatelly
        CLIHandler.finishInitialization (true);
        
        class H extends CLIHandler implements Runnable {
            public volatile int cnt;
            public volatile int afterFinish = -1;
            
            public H () {
                super (CLIHandler.WHEN_INIT);
            }
            
            protected int cli (Args args) {
                cnt++;
                LOG.info("Increased cnt to: " + cnt + " by thread " + Thread.currentThread());
                return afterFinish;
            }
            
            public void run () {
                // cnt will be two as once the first cliInitialize will
                // invoke the handler and once the second cliInitialize 
                // using the Server            
                afterFinish = 2;
                CLIHandler.finishInitialization (false);
            }
            
            protected void usage (PrintWriter w) {}
        }
        H h = new H ();
        
        CLIHandler.Status res = cliInitialize (new String[0], h, nullInput, nullOutput, nullOutput, null);
        assertEquals ("Returns 0 as no finishInitialization is called", 0, res.getExitCode ());
        // after two seconds it calls finishInitialization
        RequestProcessor.Task task = RequestProcessor.getDefault ().post (h, 7000); // 7s is higher than socket timeout
        res = cliInitialize (new String[0], h, nullInput, nullOutput, nullOutput, null);
        
        assertEquals ("Returns 2 as afterFinish needed to be set to 2 before" +
        " calling finishInitialization", 2, res.getExitCode ());

        long time = System.currentTimeMillis ();
        task.waitFinished ();
        time = System.currentTimeMillis () - time;
        
        if (time > 1000) {
            fail ("The waitFinished should return almost immediatelly. But was: " + time);
        }
        
        if (h.afterFinish != h.cnt) {
            // in order to find out whether the failures in issue #44833
            // are not caused just by threading issues, let's wait another
            // few seconds and print the results of h.afterFinish and h.cnt
            // if they will be the same then just replace the initial condition
            // by say that h.afterFinish == 2 and h.cnt > 1 is ok
            Thread.sleep (5000);
            fail ("H is not executed before finishInitialization is called :" + h.afterFinish + " cnt: " + h.cnt);
        }
        
    }

    @RandomlyFails // core-main: #10025
    public void testReadingMoreThanAvailableIsOk () throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            Exception error;
            
            public H() {
                super(WHEN_INIT);
            }
            
            @Override
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[8];
                    assertEquals("Read amount is the maximum of template", template.length, is.read(arr));
                    assertEquals("EOF", -1, is.read());
                    is.close();
                } catch (Exception ex) {
                    error = ex;
                }
                return 0;
            }
            
            @Override
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1 }, new ByteArrayInputStream(template), nullOutput, nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            assertEquals("Attempt " + i + ": " + "3rd is same", template[2], h1.arr[2]);
            assertEquals("Attempt " + i + ": " + "4th is same", template[3], h1.arr[3]);
            
            h1.arr = null;
        }
        if (h1.error != null) {
            throw h1.error;
        }
    }
    
    
    public void testWritingToOutputIsFine() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    OutputStream os = args.getOutputStream();
                    os.write(template);
                    os.close();
                    
                    os = args.getErrorStream();
                    os.write(0); 
                    os.write(1);
                    os.write(2);
                    os.write(3);
                    os.write(4);
                    os.close();
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
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, nullInput, os, err);
            
            byte[] arr = os.toByteArray();
            assertEquals("Double size of template", template.length * 2, arr.length);
            
            for (int pos = 0; pos < arr.length; pos++) {
                assertEquals(pos + ". is the same", template[pos % template.length], arr[pos]);
            }
            
            byte[] errArr = err.toByteArray();
            assertEquals("5 bytes", 10, errArr.length);
            for (int x = 0; x < errArr.length; x++) {
                assertEquals(errArr[x], x % 5);
            }
        }
    }

    public void testGetInetAddressDoesNotBlock () throws Exception {
        CLIHandler.Status res = cliInitialize(new String[0], Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, Integer.valueOf(27));
        assertEquals("CLIHandler init finished" ,0, res.getExitCode());
    }
    
    public void testServerCanBeStopped () throws Exception {
        class H extends CLIHandler {
            private int cnt = -1;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        h.toReturn = 7;
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, nullOutput, null);
        assertEquals("Called once, increased -1 to 0", 0, h.cnt);
        assertEquals("Result is provided by H", 7, res.getExitCode());
        
        CLIHandler.stopServer ();
        
        h.toReturn = 5;
        h.cnt = -1;
        res = cliInitialize(new String[0], Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, null);
        assertEquals("Not called -1", -1, h.cnt);
        // right now the handler will not be called, if there is anything else
        // to do, let's wait for such requirements
    }

    public void testHostNotFound64004 () throws Exception {
        class H extends CLIHandler {
            private int cnt;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        // handlers shall not be executed immediatelly
        CLIHandler.finishInitialization(true);
        
        h.toReturn = 7;
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, nullOutput, new Integer(667));
        // finish all calls
        int newRes = CLIHandler.finishInitialization(false);
        
        assertEquals("Called once, increased", 1, h.cnt);
        assertEquals("Result is provided by H", 7, newRes);
    }
}
