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

package org.netbeans.modules.xml.xdm.diff;

import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * This class represents element add between 2 DOM tree
 *
 * @author Ayub Khan
 */
public class Add extends Difference {
    
    /** Creates a new instance of DiffEvent */
    public Add(NodeInfo.NodeType nodeType,
            List<Node> ancestors1, List<Node> ancestors2, Node n, int pos) {
        super(nodeType, ancestors1, ancestors2, null, n, -1, pos);
    }
    
    public List<Node> getNewAncestors() {
        return getNewNodeInfo().getNewAncestors();
    }
    
    public void setNewParent(Node p) {
        getNewNodeInfo().setNewParent(p);
    }
    
    public Node getNewParent() {
        return getNewNodeInfo().getNewParent();
    }
    
    public String toString() {
        return "ADD("+ getNewNodeInfo() + ")";
    }
}
