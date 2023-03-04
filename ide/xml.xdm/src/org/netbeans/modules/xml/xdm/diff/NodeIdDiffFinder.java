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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.diff.DiffFinder.SiblingInfo;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;

/**
 *
 * @author Nam Nguyen
 */
public class NodeIdDiffFinder extends DiffFinder {
    
    /** Creates a new instance of NodeIdDiffFinder */
    public NodeIdDiffFinder() {
    }
    
    protected Node findMatch(Element child, List<Node> childNodes, org.w3c.dom.Node parent1) {
        return findMatchedNode(child, childNodes);
    }
    
    protected Node findMatch(Text child, List<Node> childNodes) {
        return findMatchedNode(child, childNodes);
    }
    
    private Node findMatchedNode(Node child, List<Node> childNodes) {
        if (childNodes != null) {
            for (int i=0; i<childNodes.size(); i++) {
                Node otherChild = (Node) childNodes.get(i);
                if (otherChild.getId() == child.getId()) {
                    return otherChild;
                }
            }
        }
        return null;
    }
    
}

