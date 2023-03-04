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

package org.netbeans.modules.mercurial.ui.actions;

import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.LifecycleManager;
import java.awt.event.ActionEvent;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.ImageUtilities;

/**
 * Base for all context-sensitive HG actions.
 *
 * @author Maros Sandor
 */
public abstract class ContextAction extends NodeAction {

    // it's singleton
    // do not declare any instance data

    protected ContextAction() {
        this(null);
    }

    protected ContextAction (String menuIcon) {
        if (menuIcon == null) {
            setIcon(null);
            putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        } else {
            setIcon(ImageUtilities.loadImageIcon(menuIcon, true));
        }
    }

    /**
     * @return bundle key base name
     * @see #getName
     */
    protected abstract String getBaseName(Node[] activatedNodes);

    protected boolean enable(Node[] nodes) {
        return true;
    }

    /**
     * Synchronizes memory modificatios with disk and calls
     * {@link  #performContextAction}.
     */
    protected void performAction(final Node[] nodes) {
        // TODO try to save files in invocation context only
        // list somehow modified file in the context and save
        // just them.
        // The same (global save) logic is in CVS, no complaint
        LifecycleManager.getDefault().saveAll();
        Utils.logVCSActionEvent("HG");                                  //NOI18N
        performContextAction(nodes);
    }

    protected abstract void performContextAction(Node[] nodes);

    /** Be sure nobody overwrites */
    @Override
    public final boolean isEnabled() {
        return super.isEnabled();
    }

    /** Be sure nobody overwrites */
    @Override
    public final void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /** Be sure nobody overwrites */
    @Override
    public final void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
    }

    /** Be sure nobody overwrites */
    @Override
    public final void performAction() {
        super.performAction();
    }

    public String getName() {
        return getName("", TopComponent.getRegistry().getActivatedNodes()); // NOI18N
    }

    /**
     * Display name, it seeks action class bundle for:
     * <ul>
     *   <li><code>getBaseName()</code> key
     * </ul>
     */
    public String getName(String role, Node[] activatedNodes) {
        String baseName = getBaseName(activatedNodes) + role;
        return NbBundle.getBundle(this.getClass()).getString(baseName);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    protected int getFileEnabledStatus() {
        return ~0;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
