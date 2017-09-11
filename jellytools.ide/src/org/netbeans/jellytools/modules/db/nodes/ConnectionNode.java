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

package org.netbeans.jellytools.modules.db.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.db.actions.ConnectAction;
import org.netbeans.jellytools.modules.db.actions.DisconnectAction;
import org.netbeans.jellytools.modules.db.actions.ExecuteCommandAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing "Databases > ${connection}" node in Services tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      ConnectionNode conn = DriversNode.invoke("jdbc:derby:/mydb", "tester", "APP");
 *      conn.connect();
 *      conn.executeCommand();
 *      ....
 *      conn.disconnect();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class ConnectionNode extends Node {
    private static final Action connectAction = new ConnectAction();
    private static final Action disconnectAction = new DisconnectAction();
    private static final Action executeCommandAction = new ExecuteCommandAction();
    private static final Action deleteAction = new DeleteAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new ConnectionNode
     * @param url database URL
     * @param user user name
     * @param schema schema name */
    public ConnectionNode(String url, String user, String schema) {
        super(new RuntimeTabOperator().getRootNode(), DatabasesNode.TREE_PATH+
                "|"+connectionName(url, user, schema));
    }

    /** Finds "Databases > ${connection}" node 
     * @param url database URL
     * @param user user name
     * @param schema schema name */
    public static ConnectionNode invoke(String url, String user, String schema) {
        RuntimeTabOperator.invoke();
        return new ConnectionNode(url, user, schema);
    }

    /** performs ConnectAction with this node */
    public void connect() {
        connectAction.perform(this);
    }

    /** performs DisconnectAction with this node */
    public void disconnect() {
        disconnectAction.perform(this);
    }

    /** performs ExecuteCommandAction with this node */
    public void executeCommand() {
        executeCommandAction.perform(this);
    }

    /** performs DeleteAction with this node */
    public void delete() {
        deleteAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    

    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            connectAction,
            disconnectAction,
            executeCommandAction,
            deleteAction,
            propertiesAction
        });
    }
    
    private static String connectionName(String url, String user, String schema) {
        if (schema == null) {
            schema = Bundle.getStringTrimmed(
                "org.netbeans.modules.db.explorer.Bundle",
                "SchemaIsNotSet");
        }
        return Bundle.getStringTrimmed(
                "org.netbeans.modules.db.explorer.Bundle",
                "ConnectionNodeUniqueName", new Object[] {url, user, schema});
    }

}
