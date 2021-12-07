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
package org.netbeans.modules.htmlui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.java.html.js.JavaScriptBody;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.spi.htmlui.HtmlViewer;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class Buttons<View, Button> {
    private static final String PREFIX = "dialog-buttons-";

    private final HtmlViewer<View, Button> viewer;
    private final View view;

    private final List<Button> arr = new ArrayList<>();
    private boolean hasResult;
    private String result;
    private HTMLDialog.OnSubmit onSubmit;

    static <V, B> Buttons<V, B> create(HtmlPair<V, B> view) {
        HtmlViewer<V, B> viewer = view.viewer();
        return new Buttons<>(viewer, view.view());
    }

    protected Buttons(HtmlViewer<View, Button> viewer, View view) {
        this.viewer = viewer;
        this.view = view;
    }

    void onSubmit(HTMLDialog.OnSubmit onSubmit) {
        this.onSubmit = onSubmit;
    }

    @JavaScriptBody(args = {}, javacall = true, body =
        "var self = this;\n" +
        "var list = window.document.getElementsByTagName('button');\n" +
        "var arr = [];\n" +
        "function add(target) {\n" +
        "  var l = function(changes) {\n" +
        "    var b = target;\n" +
        "    self.@org.netbeans.modules.htmlui.Buttons::changeState(Ljava/lang/String;ZLjava/lang/String;)(b.id, b.disabled, b.innerHTML);\n" +
        "  }\n" +
        "  target.addEventListener('DOMSubtreeModified', l, false);\n" +
        "}\n" +
        "for (var i = 0; i < list.length; i++) {\n" +
        "  var b = list[i];\n" +
        "  if (b.hidden === true) {\n" +
        "    arr.push(b.id);\n" +
        "    arr.push(b.innerHTML);\n" +
        "    arr.push(b.disabled);\n" +
        "    add(b);\n" +
        "  }\n" +
        "}\n" +
        "return arr;\n"
    )
    private native Object[] list();

    final void changeState(final String id, final boolean disabled, final String text) {
        viewer.runLater(view, () -> {
            for (Button b : arr) {
                if (Objects.equals(getName(b), id)) {
                    viewer.setEnabled(view, b, !disabled);
                    viewer.setText(view, b, text);
                }
            }
        });
    }

    @NbBundle.Messages({
        "CTL_OK=OK",
        "CTL_Cancel=Cancel",
    })
    public List<Button> buttons() {
        if (arr.isEmpty()) {
            final Object[] all = list();
            for (int i = 0; i < all.length; i += 3) {
                final String id = all[i].toString();
                Button b = createButton(id);
                viewer.setText(view, b, all[i + 1].toString());
                if (Boolean.TRUE.equals(all[i + 2])) {
                    viewer.setEnabled(view, b, false);
                }
                arr.add(b);
            }
            if (arr.isEmpty()) {
                Button ok = createButton("OK"); // NOI18N
                viewer.setText(view, ok, Bundle.CTL_OK());
                arr.add(ok);
                Button cancel = createButton(null);
                viewer.setText(view, cancel, Bundle.CTL_Cancel());
                arr.add(cancel);
            }
        }
        return Collections.unmodifiableList(arr);
    }

    public synchronized String obtainResult() {
        while (!hasResult) {
            try {
                wait();
            } catch (InterruptedException ex) {
                // ignore
            }
        }
        return result;
    }

    public synchronized void accept(String t) {
        if (hasResult) {
            return;
        }
        if (t == null) {
            result = null;
        } else if (t.startsWith(PREFIX)) {
            String r = t.substring(PREFIX.length());
            if (onSubmit != null && !onSubmit.onSubmit(r)) {
                return;
            }
            result = r;
        }


        hasResult = true;
        notifyAll();
        closeWindow0();
    }

    private Button createButton(String name) {
        return viewer.createButton(view, name == null ? null : PREFIX + name);
    }

    private String getName(Button b) {
        String id = viewer.getName(view, b);
        if (id.startsWith(PREFIX)) {
            return id.substring(PREFIX.length());
        }
        return null;
    }

    @JavaScriptBody(args = {}, body = "window.close();")
    private native static void closeWindow0();
}
