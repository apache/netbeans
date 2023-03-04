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

package org.netbeans.modules.git.ui.diff;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public class DiffTopComponent extends TopComponent {

    private final MultiDiffPanelController controller;

    DiffTopComponent (MultiDiffPanelController controller) {
        setLayout(new BorderLayout());
        this.controller = controller;
        JPanel panel = controller.getPanel();
        panel.putClientProperty(TopComponent.class, this);
        add(panel, BorderLayout.CENTER);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
    }

    @Override
    public boolean canClose () {
        return controller.canClose();
    }

    @Override
    public UndoRedo getUndoRedo () {
        return controller.getUndoRedo();
    }

    @Override
    public int getPersistenceType (){
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed () {
        controller.componentClosed();
        super.componentClosed();
    }

    @Override
    protected String preferredID () {
        return "PERSISTENCE_NEVER-DiffTopComponent";    // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    @Override
    protected void componentActivated () {
        super.componentActivated();
        controller.setFocused(true);
    }

    @Override
    protected void componentDeactivated () {
        super.componentDeactivated();
        controller.setFocused(false);
    }
    
}
