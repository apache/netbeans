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

package org.netbeans.modules.xml.actions;

import java.util.*;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.modules.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

/**
 * Represents a list  of optional (enableness driven) actions.
 * Possible actions are hardcoded in {@link #getPossibleActions()}.
 *
 * @author Libor Kramolis
 */
final class XMLViewActions extends CollectSystemAction {

    /** Serial Version UID */
    private static final long serialVersionUID = 8223872687291078210L;

    /**
     */
    @Override
    protected final Class getActionLookClass () {
        // will not be called because rewritten getPossibleActions by subclasses
        return null;
    }

    @Override
    protected Collection getPossibleActions () {
        Collection actions = new Vector(2);
        
        // XXX #48712 heuristics: enable open action only if tree editor installed
        boolean visualEditorInstalled = false;
        Lookup lookup = Lookup.getDefault();
        Lookup.Template t = new Lookup.Template(ModuleInfo.class);
        Iterator it = lookup.lookup(t).allInstances().iterator();
        while (it.hasNext()) {
            ModuleInfo next = (ModuleInfo) it.next();
            if (next.getCodeNameBase().equals("org.netbeans.modules.xml.tree") && next.isEnabled()) {  // NOI18N
                visualEditorInstalled = true;
                break;
            }
        }
        if (visualEditorInstalled) {                
            actions.add (SystemAction.get (OpenAction.class));
        }
        actions.add (SystemAction.get (ViewAction.class));
        return actions;
    }

    @Override
    protected void addRegisteredAction() {}

    /* Do nothing.
     * This action itself does nothing, it only presents other actions.
     * @param ev ignored
     */
    @Override
    public void actionPerformed (java.awt.event.ActionEvent e) {
    }


    /* Getter for name
     */
    @Override
    public String getName () {
        return NbBundle.getMessage(XMLViewActions.class, "NAME_WeakXMLActions");
    }

    /* Getter for help.
     */
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx (XMLViewActions.class);
    }
}
