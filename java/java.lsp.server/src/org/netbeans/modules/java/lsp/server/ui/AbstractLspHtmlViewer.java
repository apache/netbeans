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
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.modules.java.lsp.server.htmlui.Buttons;
import static org.netbeans.modules.java.lsp.server.htmlui.Buttons.buttonName0;
import static org.netbeans.modules.java.lsp.server.htmlui.Buttons.buttonText0;
import org.netbeans.modules.java.lsp.server.htmlui.WebView;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.netbeans.modules.java.lsp.server.protocol.UIContext;
import org.openide.util.Exceptions;
import org.netbeans.spi.htmlui.HTMLViewerSpi;
import org.openide.util.NbBundle;

/**
 * Implementation of {@link HTMLViewerSpi} that uses <code>window/showHtmlPage</code>
 * extended message of language server protocol to display a page.
 * 
 * @since 1.14
 */
public class AbstractLspHtmlViewer implements HTMLViewerSpi<AbstractLspHtmlViewer.View, Object> {

    protected AbstractLspHtmlViewer() {
    }

    @Override
    public View newView(Context context) {
        UIContext ui = UIContext.find();
        View view = new View(ui, context);
        return view;
    }


    @Override
    public Object createButton(View view, String id) {
        return Buttons.createButton0(id, view.ctx);
    }


    @Override
    public <C> C component(View view, Class<C> type) {
        if (type == Void.class) {
            view.load();
            return null;
        }
        throw new ClassCastException(view + " cannot be cast to " + type);
    }

    @Override
    public String getId(View view, Object b) {
        return buttonName0(b);
    }

    @Override
    public void setText(View view, Object b, String text) {
        buttonText0(b, text);
    }

    @Override
    public void setEnabled(View view, Object b, boolean enabled) {
        Buttons.buttonDisabled0(b, !enabled);
    }

    @Override
    public void runLater(View view, Runnable r) {
        r.run();
    }

    /** View element used by {@link AbstractLspHtmlViewer}.
     * @since 1.14
     */
    protected final class View {
        final Context ctx;
        private final UIContext ui;
        private WebView presenter;

        private View(UIContext ui, Context ctx) {
            this.ui = ui;
            this.ctx = ctx;
        }

        private void notifyClose() {
            if (ctx != null) {
                ctx.onSubmit(null);
            }
        }

        @NbBundle.Messages({
            "MSG_NoHtmlUI=HTML UI isn't supported by the client!"
        })
        private void load() {
            NbCodeClientCapabilities caps = NbCodeClientCapabilities.find(ui);
            if (caps == null || !caps.hasShowHtmlPageSupport()) {
                MessageParams msg = new MessageParams();
                msg.setMessage(Bundle.MSG_NoHtmlUI());
                msg.setType(MessageType.Warning);
                ui.showMessage(msg);
                notifyClose();
                return;
            }
            presenter = new WebView((page) -> {
                try {
                    HtmlPageParams params = new HtmlPageParams(page.getId(), page.getText());
                    if (!page.getResources().isEmpty()) {
                        params = params.setResources(page.getResources());
                    }
                    ui.showHtmlPage(params).thenAccept((t) -> {
                        final WebView p = presenter;
                        if (p == null) {
                            return;
                        }
                        try {
                            notifyClose();
                        } catch (Throwable ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        try {
                            p.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            presenter = null;
                        }
                    }).exceptionally((t) -> {
                        notifyClose();
                        presenter = null;
                        return null;
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            presenter.displayPage(ctx);
        }
    }
}
