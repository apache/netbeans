/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.eecommon.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

public class ExecSupport {

    /** Creates a new instance of ExecSupport */
    ExecSupport() {
    }

    /**
     * Redirect the standard output and error streams of the child
     * process to an output window.
     */
    void displayProcessOutputs(final Process child, String displayName, String initialMessage)
            throws IOException, InterruptedException {
        // Get a tab on the output window.  If this client has been
        // executed before, the same tab will be returned.
        InputOutput io = org.openide.windows.IOProvider.getDefault().getIO(displayName, false);
        OutputWriter ow = io.getOut();
        try {
            io.getOut().reset();
        } catch (IOException e) {
            // not a critical error, continue
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
//            io.select();
        ow.println(initialMessage);
        final Thread[] copyMakers = new Thread[3];
        (copyMakers[0] = new OutputCopier(new InputStreamReader(child.getInputStream()), ow, true)).start();
        (copyMakers[1] = new OutputCopier(new InputStreamReader(child.getErrorStream()), io.getErr(), true)).start();
        (copyMakers[2] = new OutputCopier(io.getIn(), new OutputStreamWriter(child.getOutputStream()), true)).start();
        new Thread() {

            @Override
            public void run() {
                try {
                    child.waitFor();
                    Thread.sleep(2000);  // time for copymakers

                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                        copyMakers[2].interrupt();
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }.start();
    }

    /** This thread simply reads from given Reader and writes read chars to given Writer. */
    public static class OutputCopier extends Thread {
        final Writer os;
        final Reader is;
        /** while set to false at streams that writes to the OutputWindow it must be
         * true for a stream that reads from the window.
         */
        final boolean autoflush;
        private boolean done = false;

        public OutputCopier(Reader is, Writer os, boolean b) {
            this.os = os;
            this.is = is;
            autoflush = b;
        }

        /* Makes copy. */
        @Override
        public void run() {
            int read;
            char[] buff = new char[256];
            try {
                while ((read = read(is, buff, 0, 256)) > 0x0) {
                    if (os != null) {
                        os.write(buff, 0, read);
                        if (autoflush) {
                            os.flush();
                        }
                    }
                }
            } catch (IOException | InterruptedException ex) {
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            done = true;
        }

        private int read(Reader is, char[] buff, int start, int count) throws InterruptedException, IOException {

            while (!is.ready() && !done) {
                sleep(100);
            }
            return is.read(buff, start, count);
        }
    }
}
