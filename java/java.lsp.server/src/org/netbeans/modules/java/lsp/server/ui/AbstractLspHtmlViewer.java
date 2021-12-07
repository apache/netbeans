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

import java.net.URL;
import java.util.concurrent.Callable;
import net.java.html.js.JavaScriptBody;
import org.netbeans.modules.java.lsp.server.htmlui.Browser;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.openide.util.Exceptions;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

public class AbstractLspHtmlViewer implements HTMLViewerSpi<AbstractLspHtmlViewer.View, Object> {
    protected AbstractLspHtmlViewer() {
    }

    @Override
    public View newView(Context context) {
        UIContext ui = UIContext.find();
        View view = new View(ui, context);
        return view;
    }

    @JavaScriptBody(args = {}, body = "\n"
        + "const vscode = acquireVsCodeApi();\n" // this method can be called only once per WebView existance
        + "window.close = function() {\n"
        + "  vscode.postMessage({\n"
        + "    command: 'dispose',\n"
        + "  });\n"
        + "};\n"
    )
    private static native void registerCloseWindow();

    @Override
    public Object createButton(View view, String id) {
        return createButton0(id, view.ctx);
    }

    @JavaScriptBody(args = { "id", "callback" }, javacall = true, body = "\n"
            + "var first = false;\n"
            + "var footer = document.getElementById('dialog-buttons');\n"
            + "if (!footer) {\n"
            + "  first = true\n"
            + "  footer = document.createElement('div');\n"
            + "  footer.id = 'dialog-buttons';\n"
            + "  footer.classList.add('flex');\n"
            + "  footer.classList.add('section');\n"
            + "  document.body.appendChild(footer);\n"
            + "}\n"
            + "var button = document.createElement('button');\n"
            + "button.id = id;\n"
            + "button.onclick = function() {;\n"
            + "  @org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer::clickButton0(Ljava/lang/String;Ljava/lang/Object;)(id, callback);\n"
            + "};\n"
            + "button.classList.add('regular-button');\n"
            + "button.classList.add('vscode-font');\n"
            + "if (first) {\n"
            + "  button.classList.add('align-right');\n"
            + "}\n"
            + "footer.appendChild(button);\n"
            + "return button;\n"
    )
    native static Object createButton0(String id, Context callback);

    static void clickButton0(String id, Object callback) {
        Context ctx = (Context) callback;
        ctx.onSubmit(id);
    }

    @Override
    public <C> C component(View view, Class<C> type) {
        if (type == Void.class) {
            view.load();
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
        buttonDisabled0(b, !enabled);
    }

    @Override
    public void runLater(View view, Runnable r) {
        r.run();
    }

    @JavaScriptBody(args = { "b" }, body = "return b.id;")
    native static String buttonName0(Object b);

    @JavaScriptBody(args = { "b", "text" }, body = "b.innerHTML = text;")
    native static void buttonText0(Object b, String text);

    @JavaScriptBody(args = { "b", "disabled" }, body = "return b.disabled = disabled;")
    native static String buttonDisabled0(Object b, boolean disabled);

    static final class View {
        private final Context ctx;
        private final UIContext ui;
        private Browser presenter;

        private View(UIContext ui, Context ctx) {
            this.ui = ui;
            this.ctx = ctx;
        }

        private void notifyClose() {
            if (ctx != null) {
                ctx.onSubmit(null);
            }
        }

        private void load() {
            URL pageUrl = ctx.getPage();
            Browser.Config c = new Browser.Config();
            c.browser((page) -> {
                try {
                    ui.showHtmlPage(new HtmlPageParams(page.toASCIIString())).thenAccept((t) -> {
                        final Browser p = presenter;
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
                        } catch (Exception ex) {
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
            presenter = new Browser(c);
            presenter.displayPage(pageUrl, () -> {
                registerCloseWindow();
                try {
                    Object v = ctx.onPageLoad();
                    System.err.println("v: " + v);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

        }
    }
}
