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
package org.netbeans.core.windows.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author davidwolf
 */
public class CloseAllRightOfThisAction extends AbstractAction {

    /**
     * TopComponent index to mark after which tab should all subsequent tabs be
     * closed or -1 for the instance used in the Menu Bar
     */
    private int index;

    /**
     * Constructor used in Menu Bar menu called "Window" in the "Close All
     * Documents Right Of This" option
     */
    public CloseAllRightOfThisAction() {
        this(-1);
    }

    /**
     * Constructor used by right clicking on an opened file.
     *
     * @param index
     */
    public CloseAllRightOfThisAction(int index) {
        this.index = index;
        String key = index == -1 ? "CTL_CloseAllRightOfThisAction_MainMenu" : "CTL_CloseAllRightOfThisAction";
        putValue(NAME, NbBundle.getMessage(CloseAllRightOfThisAction.class, key));
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // index CANNOT be reassigned because this class is used in Menu Bar where the instance is not regenerated
        if (index == -1) {
            ActionUtils.closeRight(TopComponent.getRegistry().getActivated().getTabPosition());
        } else {
            ActionUtils.closeRight(index);
        }
    }

    /**
     * Overriden to share accelerator with
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllRightOfThisAction
     */
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseAllRightOfThisAction", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /**
     * Overriden to share accelerator with
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllRightOfThisAction
     */
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseAllRightOfThisAction"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
    
    @Override
    public boolean isEnabled() {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (!wmi.isOpenedEditorTopComponent(tc)) {
            return false;
        }
        int i = index == -1 ? tc.getTabPosition() : index;
        return i != wmi.getEditorTopComponents().length - 1;
    }

}
