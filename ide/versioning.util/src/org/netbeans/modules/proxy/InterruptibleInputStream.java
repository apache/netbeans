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

package org.netbeans.modules.proxy;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Wrapper input stream that reacts to Thread.interrupt().
 *
 * @author Maros Sandor
 */
public class InterruptibleInputStream extends FilterInputStream {

    public InterruptibleInputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        waitAvailable();
        return in.read();
    }

    public int read(byte b[], int off, int len) throws IOException {
        waitAvailable();
        return super.read(b, off, len);
    }

    private void waitAvailable() throws IOException {
        while (in.available() == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
        }
    }
}
