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

package org.netbeans.lib.profiler.results.cpu.cct.nodes;

import org.netbeans.lib.profiler.results.RuntimeCCTNode;


/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class BaseCPUCCTNode implements RuntimeCPUCCTNode {
    
    private static final RuntimeCCTNode[] EMPTY_CHILDREN = new RuntimeCCTNode[0];    
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    /** Children nodes in the RuntimeCPUCCTNode tree. This field can have three different values depending on the
     * number of children:
     *   null if there are no children
     *   instance of RuntimeCPUCCTNode if there is exactly one child
     *   instance of RuntimeCPUCCTNode[] if there are multiple children
     * This is purely a memory consumption optimization, which typically saves about 50% of memory, since a lot of
     * RuntimeCPUCCTNode trees are a sequence of single-child nodes, and in such case we remove the need to 
     * create a one-item array.
     */
    private Object children;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BaseCPUCCTNode */
    public BaseCPUCCTNode() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public RuntimeCCTNode[] getChildren() {
        if (children == null) {
            return EMPTY_CHILDREN;
        } else if (children instanceof RuntimeCPUCCTNode) {
            return new RuntimeCPUCCTNode[]{(RuntimeCPUCCTNode)children};
        }
        return (RuntimeCPUCCTNode[])children;
    }

    public void attachNodeAsChild(RuntimeCPUCCTNode node) {
        if (children == null) {
            children = node;
        } else if (children instanceof RuntimeCPUCCTNode) {
            children = new RuntimeCPUCCTNode[]{(RuntimeCPUCCTNode)children,node};
        } else {
            RuntimeCPUCCTNode[] ch = (RuntimeCPUCCTNode[]) children;
            RuntimeCPUCCTNode[] newChildren = new RuntimeCPUCCTNode[ch.length+1];
            System.arraycopy(ch, 0, newChildren, 0, ch.length);
            newChildren[newChildren.length-1] = node;
            children = newChildren;
        }
    }
}
