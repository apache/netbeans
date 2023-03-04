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
package org.netbeans.modules.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.openide.util.NbBundle;

/**
 * Bridge between the CLIHandler that can be unit tested
 * @author Jaroslav Tulach
 */
final class HandlerImpl extends Object {
    static int execute(String[] arr, InputStream is, OutputStream os, OutputStream err, File pwd) {
        try {
            CommandLine.getDefault().process(
                arr, is, os, err, pwd
                );
            for (int i = 0; i < arr.length; i++) {
                arr[i] = null;
            }
            return 0;
        } catch (CommandException ex) {
            PrintStream ps = new PrintStream(err);
            ps.println(ex.getLocalizedMessage());
            // XXX pst is not useful, only in verbose mode
            // the question is how to turn that mode on
            // ex.printStackTrace(ps);
            int ret = ex.getExitCode();
            if (ret == 0) {
                ret = Integer.MIN_VALUE;
            }
            return ret;
        }
    }
    
    static void usage(PrintWriter w) {
        w.print(NbBundle.getMessage(HandlerImpl.class, "MSG_OptionsHeader")); // NOI18N
        CommandLine.getDefault().usage(w);
        w.println();
    }
}
