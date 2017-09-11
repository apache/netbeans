/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.awt;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.ActionPresenterProvider;

public class DefaultAWTBridgeTest extends NbTestCase {

    public DefaultAWTBridgeTest(String n) {
        super(n);
    }

    public void testHideWhenDisabled() throws Exception {
        class NoOpAction extends AbstractAction {
            NoOpAction(String n) {
                super(n);
            }
            public @Override void actionPerformed(ActionEvent e) {}
        }
        Action a = new NoOpAction("a1");
        assertEquals(Collections.singletonList("a1"), popupMenu(a));
        a = new NoOpAction("a2");
        a.setEnabled(false);
        assertEquals(Collections.singletonList("a2[disabled]"), popupMenu(a));
        a = new NoOpAction("a3");
        a.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        assertEquals(Collections.singletonList("a3"), popupMenu(a));
        a = new NoOpAction("a4");
        a.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        a.setEnabled(false);
        assertEquals(Collections.emptyList(), popupMenu(a));
    }
    private static List<String> popupMenu(Action a) {
        ActionPresenterProvider app = new DefaultAWTBridge();
        Component[] comps = app.convertComponents(app.createPopupPresenter(a));
        List<String> r = new ArrayList<String>();
        for (Component comp : comps) {
            JMenuItem mi = (JMenuItem) comp;
            r.add(mi.getText() + (mi.isEnabled() ? "" : "[disabled]"));
        }
        return r;
    }

}
