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

package org.netbeans.modules.openfile;

import java.io.File;
import org.netbeans.api.sendopts.CommandException;
import static org.netbeans.modules.openfile.Bundle.*;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 * Processor for command line options.
 * @author Jesse Glick, Jaroslav Tulach
 */
public final class Handler implements ArgsProcessor {
    @Arg(longName="open", implicit=true)
    @Description(
        displayName="#MSG_OpenOptionDisplayName", 
        shortDescription="#MSG_OpenOptionDescription"
    )
    @Messages({
        "MSG_OpenOptionDisplayName=--open file1[:line1]...",
        "MSG_OpenOptionDescription=open specified file(s), possibly at given location; can also pass project directories"
    })
    public String[] files;

    public Handler() {
    }

    @Messages("EXC_MissingArgOpen=Missing arguments to --open")
    @Override
    public void process(Env env) throws CommandException {
        String[] argv = files;
        if (argv == null || argv.length == 0) {
            throw new CommandException(2, EXC_MissingArgOpen());
        }
        
        File curDir = env.getCurrentDirectory ();

        StringBuffer failures = new StringBuffer();
        String sep = "";
        for (int i = 0; i < argv.length; i++) {
            String error = openFile (curDir, env, argv[i]);
            if (error != null) {
                failures.append(sep);
                failures.append(error);
                sep = "\n";
            }
        }
        if (failures.length() > 0) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(failures.toString()));
            throw new CommandException(1, failures.toString());
        }
    }

    private File findFile (File curDir, String name) {
        File f = new File(name);
        if (!f.isAbsolute()) {
            f = new File(curDir, name);
        }
        return f;
    }
    
    private String openFile (File curDir, Env args, String s) {
        int line = -1;
        File f = findFile (curDir, s);
        if (!f.exists()) {
            // Check if it is file:line syntax.
            int idx = s.lastIndexOf(':'); // NOI18N
            if (idx != -1) {
                try {
                    line = Integer.parseInt(s.substring(idx + 1)) - 1;
                    f = findFile (curDir, s.substring(0, idx));
                } catch (NumberFormatException e) {
                    // OK, leave as a filename
                }
            }
        }
        // Just make sure it was opened, then exit.
        return OpenFile.openFile(f, line);
    }
}
