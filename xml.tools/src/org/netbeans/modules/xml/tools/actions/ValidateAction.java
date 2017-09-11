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

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.actions.CollectXMLAction;
import org.netbeans.modules.xml.actions.InputOutputReporter;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 * Validates XML file sending results to output window.
 *
 * @author  Petr Kuzel
 */
public class ValidateAction extends CookieAction implements CollectXMLAction.XMLAction {

    /** Serial Version UID */
    private static final long serialVersionUID = -8772119268950444993L;

    
    /** Be hooked on XMLDataObjectLook narking XML nodes. */
    protected Class[] cookieClasses () {
        return new Class[] { ValidateXMLCookie.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;

        RequestProcessor.getDefault().post(
               new ValidateAction.RunAction (nodes)
        );
    }

    /** Human presentable name. */
    public String getName() {
        //the way its working is that this menu item will is visible for all files
        //for non-xml files it will be disabled, so wanted to keep a generic menu item name
        if(this.isEnabled())
           return NbBundle.getMessage(ValidateAction.class, "NAME_Validate_XML");
        else
            return NbBundle.getMessage(ValidateAction.class, "NAME_Validate_File");
   }

    protected String iconResource () {
        return "org/netbeans/modules/xml/tools/resources/validate_xml.png";   // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ValidateAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }

    private class RunAction implements Runnable{
        private Node[] nodes;

        RunAction (Node[] nodes){
            this.nodes = nodes;
        }

        public void run() {
            InputOutputReporter console = new InputOutputReporter();
            console.message(NbBundle.getMessage(ValidateAction.class, "MSG_XML_valid_start"));
            console.moveToFront();
            for (int i = 0; i<nodes.length; i++) {
                Node node = nodes[i];
                ValidateXMLCookie cake = (ValidateXMLCookie) node.getCookie(ValidateXMLCookie.class);
                if (cake == null) continue;
                console.setNode(node); //??? how can console determine which editor to highlight
                cake.validateXML(console);
            }

            console.message(NbBundle.getMessage(ValidateAction.class, "MSG_XML_valid_end"));
            console.moveToFront(true);
       }
    }
}
