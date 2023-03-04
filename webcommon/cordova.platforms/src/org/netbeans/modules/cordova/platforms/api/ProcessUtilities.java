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
