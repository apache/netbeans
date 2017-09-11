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
package org.netbeans.modules.xml.tools.actions;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.xml.actions.*;
import org.netbeans.api.xml.cookies.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * checks DTD file sending results to output window.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CheckDTDAction extends CookieAction implements CollectDTDAction.DTDAction {
    /** serialVersionUID */
    private static final long serialVersionUID = -8772119268950444992L;

    /** Be hooked on XMLDataObjectLook narking XML nodes. */
    protected Class[] cookieClasses () {
        return new Class[] { CheckXMLCookie.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;
        
        RequestProcessor.postRequest(
               new CheckDTDAction.RunAction (nodes));

    }
    
    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name. */
    public String getName() {
        return NbBundle.getMessage(CheckDTDAction.class, "NAME_Validate_DTD");
    }

    protected String iconResource () {
        return "org/netbeans/modules/xml/tools/resources/checkDTDAction.gif";   // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CheckDTDAction.class);
    }

    private class RunAction implements Runnable{
        private Node[] nodes;

        RunAction (Node[] nodes){
            this.nodes = nodes;
        }

        public void run() {
            InputOutputReporter console = new InputOutputReporter();
            
            console.message(NbBundle.getMessage(CheckDTDAction.class, "MSG_DTD_valid_start"));
            console.moveToFront();
            
            for (int i = 0; i<nodes.length; i++) {
                Node node = nodes[i];
                CheckXMLCookie cake = (CheckXMLCookie) node.getCookie(CheckXMLCookie.class);
                if (cake == null) continue;
                console.setNode(node); //??? how can console determine which editor to highlight
                cake.checkXML(console);
            }

            console.message(NbBundle.getMessage(CheckDTDAction.class, "MSG_DTD_valid_end"));
            console.moveToFront(true);
        }
    }
}