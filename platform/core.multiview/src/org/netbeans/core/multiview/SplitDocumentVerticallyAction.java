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
package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSplitPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Christian Lenz (Chrizzly)
 */
@ActionID(
    category = "Window",
    id = "org.netbeans.core.multiview.SplitDocumentVerticallyAction"
)
@ActionRegistration(
    displayName = "#LBL_ValueSplitVertical"
)
@ActionReference(path = "Shortcuts", name = "DOS-V")
@NbBundle.Messages({
    "LBL_SplitDocumentActionVertical=&Vertically",
    "LBL_ValueSplitVertical=Split vertically"
})
public final class SplitDocumentVerticallyAction extends AbstractAction {

    public SplitDocumentVerticallyAction() {
        putValue(Action.NAME, Bundle.LBL_SplitDocumentActionVertical());
        //hack to insert extra actions into JDev's popup menu
        putValue("_nb_action_id_", Bundle.LBL_ValueSplitVertical()); //NOI18N
    }

    @Override
    public final void actionPerformed(ActionEvent evt) {
        final TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();

        if (tc != null) {
            SplitAction.splitWindow(tc, JSplitPane.VERTICAL_SPLIT, -1);
        }
    }
}
