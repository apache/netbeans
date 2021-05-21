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
package org.netbeans.modules.xml.actions;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import org.netbeans.modules.xml.actions.CollectXMLAction;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.modules.xml.util.Util;

/**
 * Checks well-formess of XML entity sending results to output window.
 *
 * @author  Petr Kuzel
 * @version 1.0
 * @deprecated To be eliminated once a API CheckXMLAction will be introduced
 */
@Deprecated
public class CheckEntityAction extends CookieAction implements CollectXMLAction.XMLAction {

    /** Serial Version UID */
    private static final long serialVersionUID = -4617456591768900199L;

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

        InputOutputReporter console = new InputOutputReporter();
        
        for (int i = 0; i<nodes.length; i++) {
            Node node = nodes[i];
            CheckXMLCookie cake = node.getCookie(CheckXMLCookie.class);
            if (cake == null) continue;
            console.setNode(node); //??? how can console determine which editor to highlight
            cake.checkXML(console);
        }
        
        console.message(Util.THIS.getString(CheckEntityAction.class, "MSG_XML_entity_check_end"));
        console.moveToFront(true);
    }

    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString(CheckEntityAction.class, "NAME_Check_XML_entity");
    }

    /** Do not slow by any icon. */
    protected String iconResource () {
        return null;
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CheckEntityAction.class);
    }

}
