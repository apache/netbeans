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
