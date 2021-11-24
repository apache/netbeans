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
import java.util.function.Consumer;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.modules.java.lsp.server.htmlui.Browser;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.spi.htmlui.HtmlViewer;
import org.openide.util.Exceptions;

public class AbstractLspHtmlViewer implements HtmlViewer<AbstractLspHtmlViewer.View> {
    protected AbstractLspHtmlViewer() {
    }

    @Override
    public View newView() {
        return new View();
    }

    @Override
    public void makeVisible(View view, Runnable whenReady) {
        whenReady.run();
    }

    @Override
    public void load(View view, ClassLoader loader, URL pageUrl, Callable<Object> initialize, String[] techIds) {
        UIContext ui = UIContext.find();

        Browser.Config c = new Browser.Config();
        c.browser((page) -> {
            try {
                ui.showHtmlPage(new HtmlPageParams(page.toASCIIString()));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        Fn.Presenter b = new Browser(c);
        b.displayPage(pageUrl, () -> {
            try {
                Object v = initialize.call();
                System.err.println("v: " + v);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public Object createButton(String id, Consumer<String> callback) {
        return createButton0(id, callback);
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
            + "  callback.@java.util.function.Consumer::accept(Ljava/lang/Object;)(id);\n"
            + "};\n"
            + "button.classList.add('regular-button');\n"
            + "button.classList.add('vscode-font');\n"
            + "if (first) {\n"
            + "  button.classList.add('align-right');\n"
            + "}\n"
            + "footer.appendChild(button);\n"
            + "return button;\n"
    )
    native static Object createButton0(String id, Consumer<?> callback);

    public static final class View {
        public View() {
        }
    }
}
