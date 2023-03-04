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

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.db.actions.AddDriverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;

/** Node representing "Databases > Drivers" node in Services tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      DriversNode drivers = DriversNode.invoke();
 *      drivers.addDriver();
 *      ....
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class DriversNode extends Node {
    static final String TREE_PATH = DatabasesNode.TREE_PATH+"|"+
            Bundle.getStringTrimmed(
            "org.netbeans.modules.db.explorer.node.Bundle",
            "DriverListNode_DISPLAYNAME");
    private static final Action addDriverAction = new AddDriverAction();

    /** Finds "Databases > Drivers" node */
    public static DriversNode invoke() {
        RuntimeTabOperator.invoke();
        return new DriversNode();
    }
    
    /** Creates new DriversNode */
    public DriversNode() {
        super(new RuntimeTabOperator().getRootNode(), TREE_PATH);
    }
    
    /** performs AddDriverAction with this node */
    public void addDriver() {
        addDriverAction.perform(this);
    }
    
    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            addDriverAction,
        });
    }
}
