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
package org.netbeans.modules.dlight.sendto.action;

import org.netbeans.modules.dlight.sendto.config.ConfigureAction;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class DynamicMenu extends JMenu {

    private static final Action validating;
    private static final Action emptyAction;
    private final Object countersLock = new Object();
    private final List<Action> actions = new ArrayList<>();
    private int vcount = 0;

    public DynamicMenu(String name) {
        super(name);
        add(validating);
    }

    void setEmpty() {
        synchronized (countersLock) {
            actions.clear();
            actions.add(emptyAction);
            vcount = 0;
            refresh();
        }
    }

    private void refresh() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    removeAll();

                    if (vcount == 0 && actions.isEmpty()) {
                        add(emptyAction);
                    } else {
                        for (Action action : actions) {
                            add(action);
                        }

                        if (vcount > 0) {
                            add(validating);
                        }
                    }

                    add(new Separator());
                    add(ConfigureAction.getInstance());

                    if (isPopupMenuVisible()) {
                        setPopupMenuVisible(false);
                        setPopupMenuVisible(true);
                    }
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void addDynamicItem(final Action action) {
        synchronized (countersLock) {
            // Will not add same actions...
            // Actually providers may report their status as disabled actions
            // Like host not connected ... 
            // To avoid multiple same entries to this 'filtering'

            if (!actions.contains(action)) {
                actions.add(action);
            }
            refresh();
        }
    }

    void addValidatingItem() {
        synchronized (countersLock) {
            vcount++;
            refresh();
        }
    }

    void removeValidatingItem() {
        synchronized (countersLock) {
            vcount--;
            refresh();
        }
    }

    // Static actions initialization
    static {
        emptyAction = new AbstractAction(NbBundle.getMessage(DynamicMenu.class, "EmptyAction.text")) {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        validating = new AbstractAction(NbBundle.getMessage(DynamicMenu.class, "ValidatingAction.text"), // NOI18N
                ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/sendto/resources/wait.png", false)) { // NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        emptyAction.setEnabled(false);
        validating.setEnabled(false);
    }
}
