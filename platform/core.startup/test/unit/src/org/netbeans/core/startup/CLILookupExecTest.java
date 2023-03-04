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
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.CLIHandler;
import org.netbeans.junit.NbTestCase;

/** Make sure the CLIHandler can be in modules and really work.
 * @author Jaroslav Tulach
 */
public class CLILookupExecTest extends NbTestCase {
    File home, cluster2, user;
    static Logger LOG;

    public CLILookupExecTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger("test." + getName());

        home = new File(getWorkDir(), "nb/cluster1");
        cluster2 = new File(getWorkDir(), "nb/cluster2");
        user = new File(getWorkDir(), "testuserdir");
        
        home.mkdirs();
        cluster2.mkdirs();
        user.mkdirs();
        
        System.setProperty("netbeans.home", home.toString());
        System.setProperty("netbeans.dirs", cluster2.toString());
    }
    

    protected void tearDown() throws Exception {
    }
    
    public void testModuleInAClusterCanBeFound() throws Exception {
        createJAR(home, "test-module-one", One.class);
        createJAR(cluster2, "test-module-two", Two.class);
        createJAR(user, "test-module-user", User.class);

        LOG.info("Calling main");
        org.netbeans.Main.main(new String[] { "--userdir", user.toString(), "--nosplash", "--one", "--two", "--three"});
        LOG.info("finishInitialization");
        org.netbeans.Main.finishInitialization();
        LOG.info("testing");
        
        assertEquals("Usage one", 0, One.usageCnt); assertEquals("CLI one", 1, One.cliCnt);
        assertEquals("Usage two", 0, Two.usageCnt); assertEquals("CLI two ", 1, Two.cliCnt);
        assertEquals("Usage user", 0, User.usageCnt); assertEquals("CLI user", 1, User.cliCnt);
    }

    private static void createJAR(File cluster, String moduleName, Class metaInfHandler) 
    throws IOException {
        CLILookupHelpTest.createJAR(cluster, moduleName, metaInfHandler);
    }
    
    private static void assertArg(String[] arr, String expected) {
        for (int i = 0; i < arr.length; i++) {
            if (expected.equals(arr[i])) {
                arr[i] = null;
                return;
            }
        }
        
        fail("There should be: " + expected + " but was only: " + java.util.Arrays.asList(arr));
    }

    public static final class One extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public One() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            assertArg(args.getArguments(), "--one");
            LOG.info("one cli");
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            LOG.info("one usage");
            usageCnt++;
        }
    }
    public static final class Two extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public Two() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            assertArg(args.getArguments(), "--two");
            LOG.info("two cli");
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            LOG.info("two usage");
            usageCnt++;
        }
    }
    public static final class User extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public User() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            assertArg(args.getArguments(), "--three");
            LOG.info("user cli");
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
            LOG.info("user usage");
        }
    }
}
