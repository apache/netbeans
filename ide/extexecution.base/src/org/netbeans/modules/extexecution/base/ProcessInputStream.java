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

package org.netbeans.modules.extexecution.base;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public final class ProcessInputStream extends FilterInputStream {

    private static final Logger LOGGER = Logger.getLogger(ProcessInputStream.class.getName());

    private final Process process;

    private byte[] buffer;

    private int position;

    private boolean closed;

    private boolean exhausted;

    public ProcessInputStream(Process process, InputStream in) {
        super(in);
        this.process = process;
    }

    @Override
    public synchronized int available() throws IOException {
        if (buffer != null && position < buffer.length) {
            return buffer.length - position;
        } else if (closed) {
            if (!exhausted) {
                exhausted = true;
                return 0;
            } else {
                throw new IOException("Already closed stream");
            }
        }
        return super.available();
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            close(false);
        }
    }

    @Override
    public void mark(int readlimit) {
        // noop
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized int read() throws IOException {
        if (buffer != null && position < buffer.length) {
            return buffer[position++];
        } else if (closed) {
            if (!exhausted) {
                exhausted = true;
                return -1;
            } else {
                throw new IOException("Already closed stream");
            }
        }
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (buffer != null) {
            int available = buffer.length - position;
            if (available == 0) {
                return -1;
            }
            int size = Math.min(len, available);
            System.arraycopy(buffer, position, b, off, size);
            position += size;
            return size;
        } else if (closed) {
            if (!exhausted) {
                exhausted = true;
                return -1;
            } else {
                throw new IOException("Already closed stream");
            }
        }
        return super.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        // noop
    }

    @Override
    public long skip(long n) throws IOException {
        return 0;
    }

    public synchronized void close(boolean drain) throws IOException {
        closed = true;

        if (drain) {
            LOGGER.log(Level.FINE, "Draining process stream");

            boolean running = process.isAlive();
            if (running) {
                LOGGER.log(Level.FINE, "Process is still running");
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                if (running) {
                    while (super.available() > 0) {
                        os.write(super.read());
                    }
                } else {
                    int read;
                    // FIXME this occasionaly block forever on Vista :(
                    while ((read = super.read()) >= 0) {
                        os.write(read);
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }

            buffer = os.toByteArray();
            LOGGER.log(Level.FINE, "Read {0} bytes from stream", buffer.length);
        }

        super.close();
    }
}
