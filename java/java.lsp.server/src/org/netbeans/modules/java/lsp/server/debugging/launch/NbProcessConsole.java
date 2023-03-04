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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.netbeans.modules.java.lsp.server.ui.IOContext;
import org.openide.util.Exceptions;

public final class NbProcessConsole extends IOContext {

    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    private final Consumer<ConsoleMessage> messageConsumer;
    private boolean stopped;
    
    private final PipedOutputStream inputSource = new PipedOutputStream();
    private BufferedWriter inputBuffer;
    private PipedInputStream inputSink;
    
    /**
     * Constructor.
     * @param process
     *              the process
     * @param name
     *              the process name
     * @param encoding
     *              the process encoding format
     */
    NbProcessConsole(Consumer<ConsoleMessage> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    /**
     * Stop monitoring the process console.
     */
    public void stop() {
        stopped = true;
        synchronized (this) {
            if (inputSink != null) {
                try {
                    inputSink.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    protected InputStream getStdIn() throws IOException {
        synchronized (this) {
            if (inputSink == null) {
                inputSink = new PipedInputStream(inputSource) {
                    @Override
                    public void close() throws IOException {
                        synchronized(this) {
                            super.close();
                            // Bug in Piped*Stream: in.close() closes, but does
                            // not unblock waiters, nor returns -1 from writer to
                            // stop waiting. Close the writer - will also notifyAll().
                            inputSource.close();
                        }
                    }
                };
            }
        }
        return inputSink;
    }
    
    public void stdIn(String line) throws IOException {
        synchronized (this) {
            if (inputBuffer == null) {
                inputBuffer = new BufferedWriter(new OutputStreamWriter(inputSource, StandardCharsets.UTF_8));
            }
        }
        inputBuffer.write(line);
        inputBuffer.newLine();
        inputBuffer.flush();
    }

    @Override
    protected void stdOut(String line) {
        ConsoleMessage msg = new ConsoleMessage(line, STDOUT);
        messageConsumer.accept(msg);
    }

    @Override
    public void stdErr(String line) {
        ConsoleMessage msg = new ConsoleMessage(line, STDERR);
        messageConsumer.accept(msg);
    }

    @Override
    protected boolean isValid() {
        return !stopped;
    }

    public static final class ConsoleMessage {
        public String output;
        public String category;

        public ConsoleMessage(String message, String category) {
            this.output = message;
            this.category = category;
        }
    }

}
