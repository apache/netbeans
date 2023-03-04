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


package org.netbeans.tests.j2eeserver.plugin.registry;

import org.openide.nodes.*;
import org.openide.util.actions.*;
import java.util.*;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;

/**
 *
 * @author  nn136682
 */
public class TargNode extends AbstractNode {
    static Node[] getNodes() {
        Node[] bogusNodes = new Node[] { Node.EMPTY.cloneNode(), Node.EMPTY.cloneNode() };
        bogusNodes[0].setName("Bogus1"); bogusNodes[0].setDisplayName("Bogus 1");
        bogusNodes[1].setName("Bogus2"); bogusNodes[1].setDisplayName("Bogus 2");
        return bogusNodes;
    }
    
    public TargNode(TestTarget targ) {
        super(new MyChildren(Arrays.asList(getNodes())));
        setDisplayName("Original:"+ targ.getName());
        setIconBase("org/netbeans/tests/j2eeserver/plugin/registry/target");
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        return new javax.swing.Action[] { 
            SystemAction.get(TargetAction.class) 
        };
    }
    
    public PropertySet[] getPropertySets() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.setName("TargetServer");
        ps.setDisplayName("Target Server");
        ps.put(new PropertySupport.ReadWrite(
            "DebugPort",  //NOI18N
            String.class,
            "Debug Name",   
            "Debug port number or share memory name") {
                public Object getValue() {
                    return "7485";
                }
                public void setValue(Object home) {
                }
        });
        return new PropertySet[] { ps };
    }

    public static class MyChildren extends Children.Array {
        public MyChildren(Collection nodes) {
            super(nodes);
        }
    }
    
    public static class TargetAction extends NodeAction {
        public String getName () { return "Target Action"; }
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        protected void performAction(Node[] activatedNodes) {
            System.out.println("Some one called target?");
        }
    }
}
