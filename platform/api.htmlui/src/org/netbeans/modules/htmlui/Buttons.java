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
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class Buttons<Button> {
    protected abstract Button createButton(String name);
    protected abstract String getName(Button b);
    protected abstract void setText(Button b, String text);
    protected abstract void setEnabled(Button b, boolean enabled);
    protected abstract void runLater(Runnable r);
    
    private final List<Button> arr = new ArrayList<>();
    
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
        runLater(() -> {
            for (Button b : arr) {
                if (Objects.equals(getName(b), id)) {
                    setEnabled(b, !disabled);
                    setText(b, text);
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
                setText(b, all[i + 1].toString());
                if (Boolean.TRUE.equals(all[i + 2])) {
                    setEnabled(b, false);
                }
                arr.add(b);
            }
            if (arr.isEmpty()) {
                Button ok = createButton("OK"); // NOI18N
                setText(ok, Bundle.CTL_OK());
                arr.add(ok);
                Button cancel = createButton(null);
                setText(cancel, Bundle.CTL_Cancel());
                arr.add(cancel);
            }
        }
        return Collections.unmodifiableList(arr);
    }
}
