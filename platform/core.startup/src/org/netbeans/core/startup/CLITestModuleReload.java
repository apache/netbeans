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
import org.netbeans.CLIHandler;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handles the --reload command-line option.
 * @author Jesse Glick
 */
@ServiceProvider(service=CLIHandler.class, /* XXX hot fix for #152992 */ position=0)
public final class CLITestModuleReload extends CLIHandler {

    public CLITestModuleReload() {
        super(CLIHandler.WHEN_INIT);
    }

    protected int cli(CLIHandler.Args args) {
        String[] argv = args.getArguments();
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] == null) {
                continue;
            }
            if (argv[i].equals("--reload")) { // NOI18N
                argv[i++] = null;
                if (i == argv.length || argv[i].startsWith("--")) { // NOI18N
                    log("Argument --reload must be followed by a file name", args); // NOI18N
                    return 2;
                }
                File module = new File(argv[i]);
                argv[i] = null;
                try {
                    TestModuleDeployer.deployTestModule(module);
                } catch (IOException e) {
                    e.printStackTrace(new PrintStream(args.getOutputStream()));
                    return 2;
                }
            }
        }
        // OK.
        return 0;
    }
    
    private static void log(String msg, CLIHandler.Args args) {
        PrintWriter w = new PrintWriter(args.getOutputStream());
        w.println(msg);
        w.flush();
    }
    
    protected void usage(PrintWriter w) {
        w.println("Module reload options:"); // NOI18N
        w.println("  --reload /path/to/module.jar  install or reinstall a module JAR file"); // NOI18N
    }

}
