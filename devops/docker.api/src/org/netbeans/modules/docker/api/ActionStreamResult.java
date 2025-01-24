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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.netbeans.modules.docker.StreamResult;

/**
 *
 * @author Petr Hejl
 */
public final class ActionStreamResult implements Closeable {

    private final StreamResult result;

    ActionStreamResult(StreamResult result) {
        this.result = result;
    }

    public OutputStream getStdIn() {
        return result.getStdIn();
    }

    public InputStream getStdOut() {
        return result.getStdOut();
    }

    public InputStream getStdErr() {
        return result.getStdErr();
    }

    public boolean hasTty() {
        return result.hasTty();
    }

    public Charset getCharset() {
        return result.getCharset();
    }

    @Override
    public void close() throws IOException {
        result.close();
    }
}
