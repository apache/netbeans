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

package org.netbeans.core.execution;

import java.io.IOException;
import java.io.InputStream;

/** demutiplexes in-requests to task specific window
*
* @author Ales Novak
* @version 0.10 Dec 04, 1997
*/
final class SysIn extends InputStream {

    public SysIn() {
    }

    /** reads one char */
    public int read() throws IOException {
        return ExecutionEngine.getTaskIOs().getIn().read ();
    }

    /** reads an array of bytes */
    @Override
    public int read(byte[] b, int off, final int len) throws IOException {
        char[] b2 = new char[len];
        int ret = ExecutionEngine.getTaskIOs().getIn().read(b2, 0, len);
        for (int i = 0; i < len; i++) {
            b[off + i] = (byte) b2[i];
        }
        return ret;
    }

    /** closes the stream */
    @Override
    public void close() throws IOException {
        ExecutionEngine.getTaskIOs().getIn().close();
    }

    /** marks position at position <code>x</code> */
    @Override
    public void mark(int x) {
        try {
            ExecutionEngine.getTaskIOs().getIn().mark(x);
        } catch (IOException e) {
            // [TODO]
        }
    }

    /** resets the stream */
    @Override
    public void reset() throws IOException {
        ExecutionEngine.getTaskIOs().getIn().reset();
    }

    /**
    * @return true iff mark is supported false otherwise
    */
    @Override
    public boolean markSupported() {
        return ExecutionEngine.getTaskIOs().getIn().markSupported();
    }

    /** skips <code>l</code> bytes
    * @return number of skipped bytes
    */
    @Override
    public long skip(long l) throws IOException {
        return ExecutionEngine.getTaskIOs().getIn().skip(l);
    }
}
