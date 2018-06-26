/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Jan Becicka
 */
public final class ProcessUtilities {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessUtilities.class.getName());
    
    private static final RequestProcessor RP = new RequestProcessor(ProcessUtilities.class.getName(), 20);

    private static InputOutput io;
    
    static {
        boolean logger = Boolean.parseBoolean(System.getProperty("mobile.platforms.logger", "false"));
        if (logger) {
            io = IOProvider.getDefault().getIO("Mobile Platforms Logger", false);
        }
    }
    
    private static void logOut(String s) {
        if (io!=null) {
            io.getOut().append(s);
            io.getOut().flush();
        }
    }

    private static void logErr(String s) {
        if (io!=null) {
            io.getErr().append(s);
            io.getErr().flush();
        }
    }
    
    private static class Redirector implements Runnable {

        private final InputStream stream;
        private final StringBuilder output;
        
        private Redirector(InputStream stream, StringBuilder output) {
            this.stream = stream;
            this.output = output;
        }

        @Override
        public void run() { 
            try (InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(stream))) {
                char[] ch = new char[1];
                int number = inputStreamReader.read(ch);
                while (number > 0) {
                    output.append(ch);
                    number = inputStreamReader.read(ch);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } 
        }
    }
    
    
    public static String callProcess(final String executable, boolean wait, int timeout, String... parameters) throws IOException {
        ProcessBuilder pb = ProcessBuilder.getLocal();
        pb.setExecutable(executable);
        pb.setArguments(Arrays.asList(parameters));
        final Process call = pb.call();
        logOut(">" + executable);
        for (String parameter:parameters) {
            logOut(" " + parameter);
        }
        logOut("\n");
        if (timeout > 0) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        call.exitValue();
                    } catch (IllegalThreadStateException e) {
                        call.destroy();
                        LOGGER.severe("process " + executable + " killed."); // NOI18N
                    }
                }
            }, timeout);
        }
        StringBuilder error = new StringBuilder();
        RequestProcessor.Task errTask = RP.post(new Redirector(call.getErrorStream(), error));
        
        StringBuilder output = new StringBuilder();
        RequestProcessor.Task outTask = RP.post(new Redirector(call.getInputStream(), output));
        
        if (!wait) {
            return null;
        }

        try {
            call.waitFor();
            errTask.waitFinished();
            outTask.waitFinished();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        
        if (executable.endsWith("ios-sim") && call.exitValue() > 0) {
            for (String p:parameters) {
                if (p.endsWith("MobileSafari.app")) {
                    throw new IllegalStateException();
                }
            }
        }

        logErr(error.toString());
        if (!error.toString().trim().isEmpty()) {
            LOGGER.warning(error.toString());
        }

        logOut(output.toString());
        if (output.toString().isEmpty()) {
            LOGGER.severe("No output when executing " + executable + " " + Arrays.toString(parameters)); // NOI18N
        }
        
        return output.toString();
    }
    
}
