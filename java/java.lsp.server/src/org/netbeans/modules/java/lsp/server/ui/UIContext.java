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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.modules.java.lsp.server.protocol.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Lookup;

public abstract class UIContext {
    private static Reference<UIContext> lastCtx = new WeakReference<>(null);
    
    /**
     * Allows to pass Lookup as a context to locate UIContext implementation; can be useful for tests. If not found
     * in the context `lkp', will be searched in the default Lookup (if lkp is not the default one).
     * @param lkp context lookup
     * @return UIContext.
     */
    @NonNull
    public static synchronized UIContext find(Lookup lkp) {
        UIContext ctx = lkp.lookup(UIContext.class);
        if (ctx != null) {
            return ctx;
        }
        Lookup def = Lookup.getDefault();
        if (lkp != def) {
            ctx = def.lookup(UIContext.class);
        }
        if (ctx == null) {
            // PENDING: better context transfer between threads is needed; this way the UIContext can remote to a bad
            // LSP client window
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

    @NonNull
    public static synchronized UIContext find() {
        return find(Lookup.getDefault());
    }

    protected abstract boolean isValid();
    protected abstract void showMessage(MessageParams msg);
    protected CompletableFuture<String> showHtmlPage(HtmlPageParams msg) {
        showMessage(new MessageParams(MessageType.Log, msg.getUri()));
        return CompletableFuture.completedFuture(null);
    }
    protected abstract CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg);
    protected abstract void logMessage(MessageParams msg);
    protected abstract Message showStatusMessage(ShowStatusMessageParams msg);
    
    /**
     * Shows an input box to ask the user for a text input.
     *
     * @param params properties of input to display
     * @return future that yields the entered value
     * @since 1.18
     */
    protected CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
        throw new AbstractMethodError();
    }

    protected CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
        throw new AbstractMethodError();
    }

    private static final class LogImpl extends UIContext {
        static final LogImpl DEFAULT = new LogImpl();

        private LogImpl() {
        }

        @Override
        protected CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
            CompletableFuture<MessageActionItem> ai = CompletableFuture.completedFuture(null);
            return ai;
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

        @Override
        protected CompletableFuture<String> showHtmlPage(HtmlPageParams msg) {
            System.out.println("Open in browser: " + msg.getUri());
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
            System.err.println("input: " + params.getPrompt());
            CompletableFuture<String> ai = CompletableFuture.completedFuture(null);
            return ai;
        }

        @Override
        protected CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
            System.err.println("quickPick: " + params.getPlaceHolder());
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
