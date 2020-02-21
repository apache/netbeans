/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
