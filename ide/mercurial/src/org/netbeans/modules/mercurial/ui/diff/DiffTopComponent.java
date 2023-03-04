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

package org.netbeans.modules.mercurial.ui.diff;

import java.awt.*;
import java.util.*;
import org.openide.util.*;
import org.openide.windows.*;
import org.openide.awt.UndoRedo;

/**
 * Diff TopComponent, synchronizing selected node and providing
 * diff setup source.
 *
 * @author Petr Kuzel
 */
public class DiffTopComponent extends TopComponent implements DiffSetupSource {

    private final MultiDiffPanel panel;

    DiffTopComponent(MultiDiffPanel c) {
        setLayout(new BorderLayout());
        c.putClientProperty(TopComponent.class, this);
        add(c, BorderLayout.CENTER);
        panel = c;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
    }

    @Override
    public boolean canClose() {
        return panel.canClose();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return panel.getUndoRedo();
    }
    
    @Override
    public int getPersistenceType(){
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed() {
        panel.componentClosed();
        super.componentClosed();
    }

    @Override
    protected String preferredID(){
        return "PERSISTENCE_NEVER-DiffTopComponent";    // NOI18N       
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        panel.requestActive();
    }

    public Collection<Setup> getSetups() {
        DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
        return mainPanel.getSetups();
    }

    public String getSetupDisplayName() {
        DiffSetupSource mainPanel = ((DiffSetupSource) getComponent(0));
        return mainPanel.getSetupDisplayName();
    }
    
}
