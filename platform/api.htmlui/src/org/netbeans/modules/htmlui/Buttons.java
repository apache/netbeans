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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import net.java.html.js.JavaScriptBody;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Buttons {
    private final List<JButton> arr = new ArrayList<>();
    
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
        "var l = function(changes) { throw 'Here';\n" +
        "  for (var i = 0; i < changes.length; i++) {\n" +
        "    var b = changes[i].target;\n" +
        "  };\n" +
        "};\n" +
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
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (JButton b : arr) {
                    if (b.getName().equals(id)) {
                        b.setEnabled(!disabled);
                        b.setText(text);
                    }
                }
            }
        });
    }
    
    @NbBundle.Messages({
        "CTL_OK=OK",
        "CTL_Cancel=Cancel",
    })
    public static JButton[] buttons() {
        final Buttons btns = new Buttons();
        final Object[] all = btns.list();
        for (int i = 0; i < all.length; i += 3) {
            JButton b = new JButton();
            b.setName(all[i].toString());
            b.setText(all[i + 1].toString());
            if (Boolean.TRUE.equals(all[i + 2])) {
                b.setEnabled(false);
            }
            btns.arr.add(b);
        }
        if (btns.arr.isEmpty()) {
            JButton ok = new JButton(Bundle.CTL_OK());
            ok.setName("OK");
            btns.arr.add(ok);
            btns.arr.add(new JButton(Bundle.CTL_Cancel()));
        }
        return btns.arr.toArray(new JButton[0]);
    }
}
