/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

struct bug188925_Real {
};

template<class NodeData, class Real = float>
class bug188925_OctNode {
public:
    NodeData nodeData;

};

typedef bug188925_OctNode<class bug188925_TreeNodeData, bug188925_Real> TreeOctNode;

class bug188925_Octree {
    void setNodeIndices(TreeOctNode& tree, int& idx);
};

class bug188925_TreeNodeData {
public:

    static int UseIndex;

    union {
        int mcIndex;

        struct {
            int nodeIndex;

        };

    };

    bug188925_Real value;

    TreeNodeData(void);

    ~TreeNodeData(void);
};

void bug188925_Octree::setNodeIndices(TreeOctNode& node) {
    node.nodeData.nodeIndex = 1; // UNABLE TO RESOLVE identifier nodeIndex
}