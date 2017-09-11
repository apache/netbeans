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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
