/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
