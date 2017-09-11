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
