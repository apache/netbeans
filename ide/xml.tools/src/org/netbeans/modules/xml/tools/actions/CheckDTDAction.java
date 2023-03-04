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