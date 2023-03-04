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

package org.netbeans.modules.glassfish.common.wizards;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class RetrieverTest implements Retriever.Updater {

    public RetrieverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void deleteJunk(File d) {
        if (!d.exists()) {
            return;
        }
        if (d.isFile()) {
            d.delete();
        } else { // directory
            for (File cf : d.listFiles()) {
                deleteJunk(cf);
            }
            d.delete();
        }
    }

    @Test
    public void testRun() throws IOException {
        TestServer server = TestServer.runSimpleServer(4444, 5);
        try {
            String tmp = System.getProperty("java.io.tmpdir");
            File file;
            if (tmp != null) {
                file = new File(tmp, "retrieverTest");
            } else {
                file = new File("retrieverTest");
            }
            file.deleteOnExit();

            Retriever r;

            try {
                message = "";
                status = "";
                // Test 301 redirect
                r = new Retriever(file, "http://localhost:" + server.getPort() + "/glassfishv3/moved.zip",
                        "http://localhost:" + server.getPort() + "/",
                        "http://localhost:" + server.getPort() + "/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip", this,
                        "glassfishv3");
                r.run();
                System.out.println("message="+message);
                System.out.println("status="+status);
                assert message.startsWith("Download & Install completed in") : message;
                assert status.equals("") : status;
            } finally {
                deleteJunk(file);
            }
            
            try {
                message = "";
                status = "";
                // Test indirect download location correct
                r = new Retriever(file, "http://localhost:" + server.getPort() + "/glassfishv3/preludezipfilename.txt",
                        "http://localhost:" + server.getPort() + "/",
                        "http://localhost:" + server.getPort() + "/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip", this,
                        "glassfishv3");
                r.run();
                System.out.println("message="+message);
                System.out.println("status="+status);
                assert message.startsWith("Download & Install completed in") : message;
                assert status.equals("") : status;
            } finally {
                deleteJunk(file);
            }
            
            try {
                message = "";
                status = "";
                // bad location source url -- falls back to the default url
                r = new Retriever(file, "http://localhost:" + server.getPort() + "/glassfishv3/preludezipfilename.tx",
                        "http://localhost:" + server.getPort() + "/",
                        "http://localhost:" + server.getPort() + "/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip", this,
                        "glassfishv3");
                r.run();
                System.out.println("message="+message);
                System.out.println("status="+status);
                assert message.startsWith("Download & Install completed in") : message;
                assert status.equals("") : status;
            } finally {
                deleteJunk(file);
            }
        } finally {
            server.cancel();
        }
    }

    /**
     * Test of getDurationString method, of class Retriever.
     */
    @Test
    public void testGetDurationString() {
        System.out.println("getDurationString");
        int time = 0;
        String expResult = "no time at all";
        String result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = -50;
        expResult = "an eternity";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 7;
        expResult = "7 ms";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 77;
        expResult = "77 ms";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 777;
        expResult = "777 ms";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 1100;
        expResult = "1 second";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 1600;
        expResult = "2 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 7777;
        expResult = "8 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 77777;
        expResult = "1 minute, 18 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 60000;
        expResult = "1 minute";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 60100;
        expResult = "1 minute";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 61001;
        expResult = "1 minute, 1 second";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 777777;
        expResult = "12 minutes, 58 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 7777777;
        expResult = "2 hours, 9 minutes, 38 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 77777777;
        expResult = "21 hours, 36 minutes, 18 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
        time = 777777777;
        expResult = "216 hours, 2 minutes, 58 seconds";
        result = Retriever.getDurationString(time);
        assertEquals(expResult, result);
    }

    private String message = "";
    public void updateMessageText(String msg) {
        message = msg;
    }

    private String status = "";
    public void updateStatusText(String status) {
        this.status = status;
    }

    public void clearCancelState() {
    }

}
