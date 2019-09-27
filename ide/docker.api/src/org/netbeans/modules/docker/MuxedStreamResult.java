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
package org.netbeans.modules.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public class MuxedStreamResult implements StreamResult {

    private static final Logger LOGGER = Logger.getLogger(MuxedStreamResult.class.getName());

    private final Endpoint s;

    private final Charset charset;

    private final OutputStream outputStream;

    private final Demuxer demultiplexer;

    private final InputStream stdOut;

    private final InputStream stdErr;

    private StreamItem last = StreamItem.EMPTY;

    public MuxedStreamResult(Endpoint s, Charset charset, InputStream is) throws IOException {
        this.s = s;
        this.charset = charset;
        this.outputStream = s.getOutputStream();
        this.demultiplexer = new Demuxer(is == null ? s.getInputStream() : is);
        this.stdOut = new ResultInputStream(false);
        this.stdErr = new ResultInputStream(true);
    }

    @Override
    public OutputStream getStdIn() {
        return outputStream;
    }

    @Override
    public InputStream getStdOut() {
        return stdOut;
    }

    @Override
    public InputStream getStdErr() {
        return stdErr;
    }

    @Override
    public boolean hasTty() {
        return false;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void close() {
        try {
            s.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }

    private class ResultInputStream extends InputStream {

        private final boolean error;

        public ResultInputStream(boolean error) {
            this.error = error;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            synchronized (MuxedStreamResult.this) {
                int size = fetchData();
                if (size <= 0) {
                    return size;
                }

                int limit = Math.min(len, last.getData().remaining());
                last.getData().get(b, off, limit);
                return limit;
            }
        }

        @Override
        public int read() {
            synchronized (MuxedStreamResult.this) {
                int size = fetchData();
                if (size <= 0) {
                    return size;
                }

                return last.getData().get();
            }
        }

        private int fetchData() {
            synchronized (MuxedStreamResult.this) {
                try {
                    if (last == null) {
                        return -1;
                    }
                    while (!last.getData().hasRemaining()) {
                        last = demultiplexer.fetch();
                        if (last == null) {
                            return -1;
                        }
                    }
                } finally {
                    MuxedStreamResult.this.notifyAll();
                }

                try {
                    while (last != null && last.isError() != error) {
                        MuxedStreamResult.this.wait();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                if (last == null) {
                    return -1;
                }
                return last.getData().remaining();
            }
        }
    }
}
