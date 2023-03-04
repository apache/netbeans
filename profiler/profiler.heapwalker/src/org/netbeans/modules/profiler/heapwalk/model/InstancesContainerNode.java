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

package org.netbeans.modules.profiler.heapwalk.model;

import java.util.List;
import javax.swing.Icon;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.Value;

/**
 *
 * @author Jiri Sedlacek
 */
public class InstancesContainerNode extends AbstractHeapWalkerNode {

    private final String name;
    private List<Value> childrenValues;
    private final List<Instance> instances;


    public InstancesContainerNode(String name, HeapWalkerNode parent, List<Value> childrenValues,
                                  List<Instance> instances) {
        super(parent);
        this.name = name;
        this.childrenValues = childrenValues;
        this.instances = instances;
    }


    public List<Instance> getInstances() {
        return instances;
    }


    protected String computeName() {
        return name;
    }

    protected String computeType() {
        return "-"; // NOI18N
    }

    protected String computeValue() {
        return "-"; // NOI18N
    }

    protected String computeSize() {
        return "-"; // NOI18N
    }

    protected String computeRetainedSize() {
        return "-"; // NOI18N
    }

    protected Icon computeIcon() {
        return BrowserUtils.ICON_INSTANCE;
    }

    protected HeapWalkerNode[] computeChildren() {
        HeapWalkerNode[] nodes = new HeapWalkerNode[childrenValues.size()];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = HeapWalkerNodeFactory.createReferenceNode(childrenValues.get(i), this);
        childrenValues = null;
        return nodes;
    }
    
    public Object getNodeID() {
        return instances;
    }

}
