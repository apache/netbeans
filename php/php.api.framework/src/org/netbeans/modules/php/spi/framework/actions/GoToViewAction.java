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
 * Base Go To View action.
 * <p>
 * This implementation just sets the correct name and runs the method
 * {@link #goToView() goToView()} in its
 * own {@link RequestProcessor request processor}.
 * @author Tomas Mysik
 * @see GoToActionAction
 */
public abstract class GoToViewAction extends AbstractAction {
    /**
     * Default offset ({@value #DEFAULT_OFFSET}) that can be used when opening a View.
     */
    protected static final int DEFAULT_OFFSET = 0;

    private static final RequestProcessor RP = new RequestProcessor(GoToViewAction.class);

    public GoToViewAction() {
        String name = NbBundle.getMessage(GoToViewAction.class, "LBL_GoToView");
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
    }

    /**
     * Method where the navigation itself happens. A message is displayed
     * if no View is found (it means that this method returns {@code false}).
     * <p>
     * It is run in its own {@link RequestProcessor request processor}.
     * @return {@code true} if successful (typically the View is found and opened)
     */
    public abstract boolean goToView();

    @Override
    public final void actionPerformed(ActionEvent e) {
        RP.execute(new Runnable() {
            @Override
            public void run() {
                if (!goToView()) {
                    DialogDisplayer.getDefault().notifyLater(new DialogDescriptor.Message(NbBundle.getMessage(GoToViewAction.class, "MSG_ViewNotFound")));
                }
            }
        });
    }
}
