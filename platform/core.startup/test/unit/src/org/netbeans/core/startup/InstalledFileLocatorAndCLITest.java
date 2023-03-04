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
import java.io.PrintStream;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.CLIHandler;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstalledFileLocatorAndCLITest extends NbTestCase {
    public InstalledFileLocatorAndCLITest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(InstalledFileLocatorAndCLITest.class)
            .addStartupArgument("--create", "$UD/modules/t.jar", "--find", "modules/t.jar")
            .honorAutoloadEager(true).gui(false).suite();
    }
    
    public void testTJarWasFound() {
        assertEquals("InstalledFileLocator failed to find the modules/t.jar file",
            "true", System.getProperty("found.modules/t.jar"));
    }

    
    @ServiceProvider(service = CLIHandler.class, position = -3)
    public static final class CreateFile extends CLIHandler {

        public CreateFile() {
            super(WHEN_EXTRA);
        }

        @Override
        protected int cli(Args args) {
            final PrintStream err =
                args.getErrorStream() != null ?
                new PrintStream(args.getErrorStream()) : System.err;
            boolean create = false;
            boolean seek = false;
            final String[] arr = args.getArguments();
            for (int i = 0; i < arr.length; i++) {
                String s = arr[i];
                try {
                    if ("--create".equals(s)) {
                        create = true;
                        arr[i] = null;
                        continue;
                    }
                    if (create) {
                        s = s.replace("$UD", System.getProperty("netbeans.user"))
                            .replace('/', File.separatorChar);
                        File f = new File(s);
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                        arr[i] = null;
                    }
                    create = false;
                    
                    if ("--find".equals(s)) {
                        seek = true;
                        arr[i] = null;
                        continue;
                    }
                    if (seek) {
                        arr[i] = null;
                        File found = InstalledFileLocator.getDefault().locate(s, null, false);
                        if (found == null) {
                            err.println("Installed file locator cannot find " + s);
                            return 0;
                        }
                        System.setProperty("found." + s, "true");
                        return 0;
                    }
                    seek = false;
                } catch (IOException iOException) {
                    iOException.printStackTrace(err);
                }
            }
            return 0;
        }

        @Override
        protected void usage(PrintWriter w) {
        }
        
    }
}
