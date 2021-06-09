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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.modules.java.lsp.server.ui.IOContext;

abstract class WorkspaceIOContext extends IOContext {
    private final InputStream inputSink = new EmptyBlockingInputStream();
    
    WorkspaceIOContext() {
    }

    @Override
    protected void stdIn(String line) throws IOException {
        // no op
    }

    @Override
    protected InputStream getStdIn() throws IOException {
        return inputSink;
    }

    @Override
    protected void stdOut(String line) {
        if ("\n".equals(line)) {
            return;
        }
        if (client() != null) {
            client().logMessage(new MessageParams(MessageType.Info, line));
        }
    }

    @Override
    protected void stdErr(String line) {
        if ("\n".equals(line)) {
            return;
        }
        if (client() != null) {
            client().logMessage(new MessageParams(MessageType.Error, line));
        }
    }

    @Override
    protected boolean isValid() {
        return client() != null;
    }

    protected abstract LanguageClient client();
    
    private static class EmptyBlockingInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new IOException(ex);
                }
            }
            return -1;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int available() throws IOException {
            return 0;
        }
    }
}
