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

package org.netbeans.modules.gradle.execute;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Laszlo Kishalmi
 */
class EscapeProcessingOutputStream extends OutputStream {

    boolean esc;
    boolean csi;
    final AtomicBoolean closed = new AtomicBoolean();
    final ByteBuffer buffer = new ByteBuffer();
    final EscapeProcessor processor;

    public EscapeProcessingOutputStream(EscapeProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void write(int b) throws IOException {
        // Simply ignore writing on a closed stream.
        if (closed.get()) return;
        if (b == 0x1B) {
            esc = true;                   //Entering EscapeProcessingMode
            processBulk();                //Process the Buffer collected so far
        } else if ((b == 0x5B) && esc) {
            csi = true;                   //Entering CSI mode we are going to
            esc = false;                  //read ANSI CSI commands from now on
        } else {
            esc = false;
            if (csi) {
                if ((b >= 0x40) && (b <= 0x7E)) { //Got a command byte
                    processCommand((char) b);     //process that.
                    csi = false;
                } else {
                    buffer.put(b);
                }
            } else {
                if (b == '\n') {
                    buffer.put(b);
                    processBulk();
                } else if ((b >= 0x20) || (b == 0x09) || (b < 0)) {
                    buffer.put(b);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        if (!csi) {
            processBulk();
        }
    }


    private void processCommand(char command) {
        String buf = buffer.read();
        String[] sargs = buf.split(";");
        int[] args = new int[sargs.length];
        for (int i = 0; i < sargs.length; i++) {
            try {
                args[i] = Integer.parseInt(sargs[i]);
            } catch (NumberFormatException ex) {
                //TODO: What could we do here
            }
        }
        String sequence = "\u001B[" + buf + command;
        processor.processCommand(sequence, command, args);
    }

    private void processBulk() {
        String out = buffer.read();
        if (out.length() > 0) {
            processor.processText(out);
        }
    }

    private static class ByteBuffer {
        private int pos = 0;
        private byte[] buf = new byte[1024];

        public void put(int b) {
            if (pos == buf.length) {
                buf = Arrays.copyOf(buf, buf.length + 1024);
            }
            buf[pos++] = (byte) b;
        }

        public String read() {
            String ret = new String(buf, 0, pos, StandardCharsets.UTF_8);
            pos = 0;
            return ret;
        }
    }
}
