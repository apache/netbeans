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
package org.netbeans.modules.docker.api;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.docker.Endpoint;
import org.netbeans.modules.docker.StreamItem;

/**
 *
 * @author Petr Hejl
 */
public final class ActionChunkedResult implements Closeable {

    private final Endpoint s;

    private final StreamItem.Fetcher fetcher;

    private final Charset charset;

    ActionChunkedResult(Endpoint s, StreamItem.Fetcher fetcher, Charset charset) {
        this.s = s;
        this.fetcher = fetcher;
        this.charset = charset;
    }

    @CheckForNull
    public Chunk fetchChunk() {
        StreamItem r = fetcher.fetch();
        if (r == null) {
            return null;
        }
        ByteBuffer buffer = r.getData();
        return new Chunk(new String(buffer.array(), buffer.position(), buffer.limit(), charset), r.isError());
    }

    @Override
    public void close() throws IOException {
        s.close();
    }

    public static class Chunk {

        private final String data;

        private final boolean error;

        private Chunk(String data, boolean error) {
            this.data = data;
            this.error = error;
        }

        public String getData() {
            return data;
        }

        public boolean isError() {
            return error;
        }
    }
}
