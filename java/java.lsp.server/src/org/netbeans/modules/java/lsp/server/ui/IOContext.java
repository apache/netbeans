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
package org.netbeans.modules.java.lsp.server.ui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.util.Lookup;
import org.openide.util.io.NullInputStream;

public abstract class IOContext {
    private static Reference<IOContext> lastCtx = new WeakReference<>(null);

    static IOContext find() {
        IOContext ctx = Lookup.getDefault().lookup(IOContext.class);
        if (ctx == null) {
            ctx = lastCtx.get();
            if (ctx != null && !ctx.isValid()) {
                lastCtx.clear();
                ctx = null;
            }
        }
        if (ctx == null) {
            ctx = StdErrContext.DEFAULT;
        } else {
            if (lastCtx.get() != ctx) {
                lastCtx = new WeakReference<>(ctx);
            }
        }
        return ctx;
    }

    protected void stdIn(String line) throws IOException {
        // no op
    }
    protected InputStream getStdIn() throws IOException {
        return new NullInputStream();
    }
    protected abstract void stdOut(String chunk);
    protected abstract void stdErr(String chunk);
    protected abstract boolean isValid();

    private static final class StdErrContext extends IOContext {
        static final StdErrContext DEFAULT = new StdErrContext();

        @Override
        protected void stdIn(String line) throws IOException {
            // no op
        }

        @Override
        protected InputStream getStdIn() throws IOException {
            return System.in;
        }

        @Override
        protected void stdOut(String chunk) {
            System.out.print(chunk);
        }

        @Override
        protected void stdErr(String chunk) {
            System.err.print(chunk);
        }

        @Override
        protected boolean isValid() {
            return true;
        }
    }

}
