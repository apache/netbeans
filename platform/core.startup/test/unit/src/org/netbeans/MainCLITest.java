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
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class MainCLITest extends NbTestCase {
    public MainCLITest (String name) {
        super(name);
    }

    public void testHandlersCanBeInUserDir () throws Exception {
        clearWorkDir ();

        class H extends CLIHandler {
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                String[] arr = args.getArguments ();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].equals("--userdir")) {
                        System.setProperty ("netbeans.user", arr[i + 1]);
                        return 0;
                    }
                }
                fail ("One of the arguments should be --userdir: " + Arrays.asList (arr));
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        
        File dir = super.getWorkDir ();
        File lib = new File (dir, "core"); 
        lib.mkdirs ();
        File jar = new File (lib, "sample.jar");
        JarOutputStream os = new JarOutputStream (new FileOutputStream (jar));
        os.putNextEntry(new ZipEntry("META-INF/services/org.netbeans.CLIHandler"));
        os.write (TestHandler.class.getName ().getBytes ());
        String res = "/" + TestHandler.class.getName ().replace ('.', '/') + ".class";
        os.putNextEntry(new ZipEntry(res));
        FileUtil.copy(getClass().getResourceAsStream(res), os);
        os.close();
        
        TestHandler.called = false;

        String[] args = {"--userdir", dir.toString()};
        assertFalse ("User dir is not correct. Will be set by org.netbeans.core.CLIOptions", dir.toString ().equals (System.getProperty ("netbeans.user")));
        MainImpl.execute (args, null, null, null, null);
        Main.finishInitialization ();
        assertEquals ("User set", dir.toString (), System.getProperty ("netbeans.user"));
        assertTrue ("CLI Handler from user dir was called", TestHandler.called);
    }

    /** Sample handler
     */
    public static final class TestHandler extends CLIHandler {
        public static boolean called;
        
        public TestHandler () {
            super (CLIHandler.WHEN_INIT);
        }
        
        protected int cli(CLIHandler.Args args) {
            called = true;
            return 0;
        }
        
        protected void usage (PrintWriter w) {
        }
        
    }
}
