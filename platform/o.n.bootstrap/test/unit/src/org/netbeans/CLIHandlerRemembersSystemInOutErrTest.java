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
import java.util.logging.Logger;
import org.netbeans.junit.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerRemembersSystemInOutErrTest extends NbTestCase {

    private static ByteArrayInputStream in = new ByteArrayInputStream("Ahoj".getBytes());
    private static PrintStream out = new PrintStream(new ByteArrayOutputStream());
    private static PrintStream err = new PrintStream(new ByteArrayOutputStream());
    private static String[] args = { "AnArg" };
    private static String curDir = "curDir";
    
    private Logger LOG;

    static {
        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }
    
    public CLIHandlerRemembersSystemInOutErrTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        System.setProperty ("netbeans.user", getWorkDirPath());
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected @Override Level logLevel() {
        return Level.ALL;
    }
    
    public void testFileExistsButItCannotBeRead() throws Exception {
        // just initialize the CLIHandler
        CLIHandler.Args a = new CLIHandler.Args(args, in, out, err, curDir);

        ArrayList<CLIHandler> arr = new ArrayList<CLIHandler>();
        arr.add(new H(H.WHEN_BOOT));
        arr.add(new H(H.WHEN_INIT));

        // now change the System values
        ByteArrayInputStream in2 = new ByteArrayInputStream("NeverBeSeen".getBytes());
        PrintStream out2 = new PrintStream(new ByteArrayOutputStream());
        PrintStream err2 = new PrintStream(new ByteArrayOutputStream());

        System.setIn(in2);
        System.setErr(err2);
        System.setOut(out2);

        LOG.info("before initialized");
        CLIHandler.initialize(a, null, arr, false, true, null);
        LOG.info("after initialize");
        assertEquals("One H called", 1, H.cnt);
        LOG.info("before finishInitialization");
        CLIHandler.finishInitialization(false);
        LOG.info("after finishInitialization");
        assertEquals("Both Hs called", 2, H.cnt);
    }

    private static final class H extends CLIHandler {
        static int cnt;

        public H(int w) {
            super(w);
        }

        protected int cli(CLIHandler.Args a) {
            cnt++;

            assertEquals("Same arg", Arrays.asList(args), Arrays.asList(a.getArguments()));
            assertEquals("same dir", curDir, a.getCurrentDirectory().toString());
            assertEquals("same in", in, a.getInputStream());
            assertEquals("same out", out, a.getOutputStream());
            assertEquals("same err", err, a.getErrorStream());

            return 0;
        }

        protected void usage(PrintWriter w) {
        }
    }
}
