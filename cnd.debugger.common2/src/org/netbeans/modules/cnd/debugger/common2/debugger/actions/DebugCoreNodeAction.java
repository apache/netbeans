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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import javax.swing.*;

import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject;
import org.openide.loaders.DataObject;

import org.netbeans.modules.cnd.utils.MIMENames;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Debug corefile represented by selected node.
 * Delegates to DebugCoreAction.
 */

public final class DebugCoreNodeAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6814567172958445516L;    

    public DebugCoreNodeAction() {
        putValue("key", "CndDebugCorefileNodeAction"); //NOI18N
    }   
    
    @Override
    protected boolean enable(final Node[] activatedNodes) {
	if (activatedNodes == null || activatedNodes.length != 1)
	    return false;
	DataObject dao = activatedNodes[0].getCookie(DataObject.class);
	if ((dao != null) && MIMENames.ELF_CORE_MIME_TYPE.equals(IpeUtils.getMime(dao))) {
	    return true;
	}
	return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        final DebugCoreAction debugCoreAction = SharedClassObject.findObject(DebugCoreAction.class, true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                debugCoreAction.performAction(activatedNodes, true);
            }
	});





    }

    /** @return the action's name */
    @Override
    public String getName () {
        return Catalog.get("CTL_DebugCorefile"); // NOI18N
    }

    /** @return the action's help context */
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("Debugging_corefile"); // NOI18N
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    @Override
    protected String iconResource () {
	// XXX need our own icon here later...
        return "org/netbeans/modules/cnd/loaders/CoreElfIcon.gif"; // NOI18N
    }

}
