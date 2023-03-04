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
package org.netbeans.modules.docker;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Petr Hejl
 */
// @NotThreadSafe
public class ChunkedInputStream extends FilterInputStream {

    private boolean started;

    private boolean finished;

    private int remaining;

    public ChunkedInputStream(InputStream is) {
        super(is);
    }

    @Override
    public int read() throws IOException {
        int current = fetchData();
        if (current < 0) {
            return -1;
        }
        remaining--;
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int current = fetchData();
        if (current < 0) {
            return -1;
        }

        int count = 0;
        int limit = off + Math.min(len, remaining);
        for (int i = off; i < limit; i++) {
            int value = in.read();
            if (value < 0) {
                return count;
            }
            count++;
            b[i] = (byte) value;
        }
        remaining -= count;
        return count;
    }

    @Override
    public int available() throws IOException {
        // FIXME this is not really true as theoretically it might block anyway
        return remaining;
    }

    private int fetchData() throws IOException {
        if (finished) {
            return -1;
        }
        if (remaining == 0) {
            if (started) {
                // read end of previous chunk
                String line = HttpUtils.readResponseLine(in);
                if (!line.isEmpty()) {
                    throw new IOException("Chunk content has additional data: " + line);
                }
            } else {
                started = true;
            }
            String line = HttpUtils.readResponseLine(in);
            if (line == null) {
                finished = true;
                return -1;
            }
            int semicolon = line.indexOf(';');
            if (semicolon > 0) {
                line = line.substring(0, semicolon);
            }
            try {
                remaining = Integer.parseInt(line, 16);
                if (remaining == 0) {
                    // end of chunk stream
                    line = HttpUtils.readResponseLine(in);
                    if (!line.isEmpty()) {
                        throw new IOException("End of chunk stream contains additional data: " + line);
                    }
                    finished = true;
                    return -1;
                }
            } catch (NumberFormatException ex) {
                throw new IOException("Wrong chunk size");
            }
        }
        return remaining;
    }
}
