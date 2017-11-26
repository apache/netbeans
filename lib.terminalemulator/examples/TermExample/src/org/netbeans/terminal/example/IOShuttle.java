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

package org.netbeans.terminal.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.openide.ErrorManager;
import org.openide.windows.OutputWriter;

/**
 * Shuttles data between the io of a {@link java.lang.Process}, or any
 * sort of derivative/mimic, and an NB-style IOProvider.
 * @author ivan
 */
final class IOShuttle {

    private final OutputMonitor outputMonitor;
    private final InputMonitor inputMonitor;

    public IOShuttle(OutputStream pin, InputStream pout,
                      OutputWriter toIO, Reader fromIO) {
        InputStreamReader fromProc = new InputStreamReader(pout);
        outputMonitor = new IOShuttle.OutputMonitor(fromProc, toIO);
        if (fromIO != null) {
            final OutputStreamWriter toProc = new OutputStreamWriter(pin);
            inputMonitor = new IOShuttle.InputMonitor(fromIO, toProc);
        } else {
            inputMonitor = null;
        }
    }

    public void run() {
        outputMonitor.start();
        if (inputMonitor != null)
            inputMonitor.start();
    }

    private static class OutputMonitor extends Thread {

        private final InputStreamReader reader;
        private final Writer writer;
        private static int nextSerial = 0;
        private int serial = nextSerial++;
        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        OutputMonitor(InputStreamReader reader, Writer writer) {
            super("ExecutorUnix.OutputMonitor");
            // NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                }
            } catch (IOException e) {
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }

    /**
     * Copies kestrokes in io window to external process.
     */
    private static class InputMonitor extends Thread {

        private final Reader reader;
        private final Writer writer;
        private static int nextSerial = 0;
        private int serial = nextSerial++;
        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        InputMonitor(Reader reader, Writer writer) {
            super("ExecutorUnix.InputMonitor");			// NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                    writer.flush();
                }
                writer.close();
            } catch (IOException e) {
		// no-op
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
