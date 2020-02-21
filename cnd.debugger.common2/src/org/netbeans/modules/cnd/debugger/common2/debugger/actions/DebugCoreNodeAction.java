/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
