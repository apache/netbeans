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
package org.netbeans.modules.java.lsp.server.htmlui;

import net.java.html.js.JavaScriptBody;
import org.netbeans.spi.htmlui.HTMLViewerSpi;

/**
 * Helper utilities to deal with HTML buttons.
 */
public final class Buttons {
    private Buttons() {
    }

    @JavaScriptBody(args = {}, body = "\n"
        + "window.close = function() {\n"
        + "  vscode.postMessage({\n"
        + "    command: 'dispose'\n"
        + "  });\n"
        + "};\n"
    )
    public static native void registerCloseWindow();

    @JavaScriptBody(args = { "id", "callback" }, javacall = true, wait4java=false, body = "\n"
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
            + "  @org.netbeans.modules.java.lsp.server.htmlui.Buttons::clickButton0(Ljava/lang/String;Ljava/lang/Object;)(id, callback);\n"
            + "};\n"
            + "button.classList.add('regular-button');\n"
            + "button.classList.add('vscode-font');\n"
            + "if (first) {\n"
            + "  button.classList.add('align-right');\n"
            + "}\n"
            + "footer.appendChild(button);\n"
            + "return button;\n"
    )
    public static native Object createButton0(String id, HTMLViewerSpi.Context callback);

    static void clickButton0(String id, Object callback) {
        HTMLViewerSpi.Context ctx = (HTMLViewerSpi.Context) callback;
        ctx.onSubmit(id);
    }

    @JavaScriptBody(args = { "b" }, body = "return b.id;")
    public static native String buttonName0(Object b);

    @JavaScriptBody(args = { "b", "text" }, body = "b.innerHTML = text;")
    public static native void buttonText0(Object b, String text);

    @JavaScriptBody(args = { "b", "disabled" }, body = "return b.disabled = disabled;")
    public static native String buttonDisabled0(Object b, boolean disabled);
    
}
