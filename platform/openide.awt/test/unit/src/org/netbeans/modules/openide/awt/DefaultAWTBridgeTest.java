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
