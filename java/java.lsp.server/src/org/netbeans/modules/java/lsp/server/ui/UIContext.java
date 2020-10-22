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
package org.netbeans.modules.java.lsp.server.ui;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.eclipse.lsp4j.MessageParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Lookup;

public abstract class UIContext {
    private static Reference<UIContext> lastCtx = new WeakReference<>(null);

    public static synchronized UIContext find() {
        UIContext ctx = Lookup.getDefault().lookup(UIContext.class);
        if (ctx == null) {
            ctx = lastCtx.get();
            if (ctx != null && !ctx.isValid()) {
                lastCtx.clear();
                ctx = null;
            }
        }

        if (ctx == null) {
            ctx = LogImpl.DEFAULT;
        } else {
            if (lastCtx.get() != ctx) {
                lastCtx = new WeakReference<>(ctx);
            }
        }

        return ctx;
    }

    protected abstract boolean isValid();
    protected abstract void showMessage(MessageParams msg);
    protected abstract void logMessage(MessageParams msg);
    protected abstract Message showStatusMessage(ShowStatusMessageParams msg);


    private static final class LogImpl extends UIContext {
        static final LogImpl DEFAULT = new LogImpl();

        private LogImpl() {
        }

        @Override
        protected void showMessage(MessageParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
        }

        @Override
        protected void logMessage(MessageParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
        }

        @Override
        protected Message showStatusMessage(ShowStatusMessageParams msg) {
            System.out.println(msg.getType() + ": " + msg.getMessage());
            return (int timeInMillis) -> {};
        }

        @Override
        protected boolean isValid() {
            return true;
        }
    }
}
