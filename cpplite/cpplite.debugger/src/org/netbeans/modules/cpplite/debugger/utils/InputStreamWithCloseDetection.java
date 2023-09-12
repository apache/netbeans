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
package org.netbeans.modules.cpplite.debugger.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * InputStream wrapper, which detects end of stream.
 * Use {@link InputStreamWithCloseDetection#waitForClose()} to wait for either an
 * explicit {@link InputStream#close()}, or EOF during read operations.
 *
 * @author Martin Entlicher
 */
public final class InputStreamWithCloseDetection extends FilterInputStream {

    private final CountDownLatch closed = new CountDownLatch(1);

    public InputStreamWithCloseDetection(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int r;
        try {
            r = super.read();
        } catch (IOException ex) {
            notifyClosed();
            throw ex;
        }
        if (r == -1) {
            notifyClosed();
        }
        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int l;
        try {
            l = super.read(b);
        } catch (IOException ex) {
            notifyClosed();
            throw ex;
        }
        if (l == -1) {
            notifyClosed();
        }
        return l;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int l;
        try {
            l = super.read(b, off, len);
        } catch (IOException ex) {
            notifyClosed();
            throw ex;
        }
        if (l == -1) {
            notifyClosed();
        }
        return l;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            notifyClosed();
        }
    }

    private void notifyClosed() {
        closed.countDown();
    }

    /**
     * Wait till this stream is closed, or at EOF.
     */
    public void waitForClose() throws InterruptedException {
        closed.await();
    }
}
