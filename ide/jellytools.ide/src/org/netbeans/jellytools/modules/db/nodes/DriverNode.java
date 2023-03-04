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

package org.netbeans.jellytools.modules.db.nodes;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CustomizeAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.modules.db.actions.ConnectUsingAction;
import org.netbeans.jellytools.nodes.Node;

/** Node representing "Databases > Drivers > ${driver}" node in Runtime tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      DriverNode driver = DriversNode.invoke("Oracle");
 *      driver.connectUsing();
 *      ....
 *      driver.delete();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class DriverNode extends Node {
    private static final Action connectUsingAction = new ConnectUsingAction();
    private static final Action deleteAction = new DeleteAction();
    private static final Action customizeAction = new CustomizeAction();

    /** creates new DriverNode
     * @param name DriverNode display name */
    public DriverNode(String name) {
        super(new RuntimeTabOperator().getRootNode(), DriversNode.TREE_PATH+
                "|"+name);
    }

    /** Finds "Databases > Drivers > ${driver}" node */
    public static DriverNode invoke(String name) {
        RuntimeTabOperator.invoke();
        return new DriverNode(name);
    }
    
    /** performs ConnectUsingAction with this node */
    public void connectUsing() {
        connectUsingAction.perform(this);
    }

    /** performs ConnectUsingAction with this node */
    public void customize() {
        customizeAction.perform(this);
    }

    /** performs DeleteAction with this node */
    public void delete() {
        deleteAction.perform(this);
    }

    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            connectUsingAction,
            customizeAction,
            deleteAction,
        });
    }
}
