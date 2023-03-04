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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dbschema.jdbcimpl.wizard.RecaptureSchema;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class RecaptureSchemaAction extends CookieAction {

    /** Create. new ObjectViewAction. */
    public RecaptureSchemaAction() {
    }

    /** Name of the action. */
    public String getName () {
        return NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle").getString("ActionNameRecap"); //NOI18N
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return null; //new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

    protected String iconResource () {
        return "org/netbeans/modules/dbschema/jdbcimpl/DBschemaDataIcon.gif"; //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] {
                   DBschemaDataObject.class
               };
    }

    protected int mode() {
        return MODE_ONE;
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public void performAction (Node[] activatedNodes) {
        try {
            if (activatedNodes.length == 1) {
                new RecaptureSchema(activatedNodes[0]).start();
            }
        }
        catch (ClassNotFoundException e) {
            Exceptions.printStackTrace(e);
        }
        catch (SQLException e) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Exception(e, e.getMessage()));
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}
