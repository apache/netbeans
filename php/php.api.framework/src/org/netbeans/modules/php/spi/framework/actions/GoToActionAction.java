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
package org.netbeans.modules.php.spi.framework.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Base Go To Action action.
 * <p>
 * This implementation just sets the correct name and runs the method
 * {@link #goToAction() goToAction()} in its
 * own {@link RequestProcessor request processor}.
 * @author Tomas Mysik
 */
public abstract class GoToActionAction extends AbstractAction {
    /**
     * Default offset ({@value #DEFAULT_OFFSET}) that can be used when opening an Action.
     */
    protected static final int DEFAULT_OFFSET = 0;

    private static final RequestProcessor RP = new RequestProcessor(GoToActionAction.class);

    public GoToActionAction() {
        String name = NbBundle.getMessage(GoToActionAction.class, "LBL_GoToAction");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
    }

    /**
     * Method where the navigation itself happens. A message is displayed
     * if no Action is found (it means that this method returns {@code false}).
     * <p>
     * It is run in its own {@link RequestProcessor request processor}.
     * @return {@code true} if successful (typically the Action is found and opened)
     */
    public abstract boolean goToAction();

    @Override
    public final void actionPerformed(ActionEvent e) {
        RP.execute(new Runnable() {
            @Override
            public void run() {
                if (!goToAction()) {
                    DialogDisplayer.getDefault().notifyLater(new DialogDescriptor.Message(NbBundle.getMessage(GoToActionAction.class, "MSG_ActionNotFound")));
                }
            }
        });
    }
}
