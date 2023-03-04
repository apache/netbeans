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

import org.netbeans.lib.profiler.heap.*;
import org.openide.util.NbBundle;
import java.util.List;


/**
 * Represents org.netbeans.lib.profiler.heap.PrimitiveArrayInstance
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "PrimitiveArrayNode_TruncatedString=...<truncated>",
    "PrimitiveArrayNode_ItemsNumberString=({0} items)",
    "PrimitiveArrayNode_LoopToString=(loop to {0})"
})
public class PrimitiveArrayNode extends ArrayNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class ArrayItem extends PrimitiveArrayNode implements org.netbeans.modules.profiler.heapwalk.model.ArrayItem {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private int itemIndex;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ArrayItem(int itemIndex, PrimitiveArrayInstance instance, HeapWalkerNode parent) {
            this(itemIndex, instance, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
        }

        public ArrayItem(int itemIndex, PrimitiveArrayInstance instance, HeapWalkerNode parent, int mode) {
            super(instance, null, parent, mode);

            this.itemIndex = itemIndex;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getItemIndex() {
            return itemIndex;
        }

        protected String computeName() {
            String name = "[" + itemIndex + "]"; // NOI18N

            if (isLoop()) {
                return name + " " // NOI18N
                       + Bundle.PrimitiveArrayNode_LoopToString(BrowserUtils.getFullNodeName(getLoopTo()));
            }

            return name;
        }

        protected String computeType() {
            if (!hasInstance()) {
                return BrowserUtils.getArrayItemType(getType());
            }

            return super.computeType();
        }
        
        public Object getNodeID() {
            return itemIndex;
        }
    }

    public abstract static class RootNode extends PrimitiveArrayNode implements org.netbeans.modules.profiler.heapwalk.model.RootNode {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public RootNode(PrimitiveArrayInstance instance, String name, HeapWalkerNode parent) {
            super(instance, name, parent);
        }

        public RootNode(PrimitiveArrayInstance instance, String name, HeapWalkerNode parent, int mode) {
            super(instance, name, parent, mode);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public abstract void refreshView();
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final int MAX_CHAR_ARRAY_STRING_SIZE = 128;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public PrimitiveArrayNode(PrimitiveArrayInstance instance, String name, HeapWalkerNode parent) {
        super(instance, name, parent);
    }

    public PrimitiveArrayNode(PrimitiveArrayInstance instance, String name, HeapWalkerNode parent, int mode) {
        super(instance, name, parent, mode);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public PrimitiveArrayInstance getInstance() {
        return (PrimitiveArrayInstance) super.getInstance();
    }

    public boolean isPrimitive() {
        return true;
    }

    protected ChildrenComputer getChildrenComputer() {
        return new ChildrenComputer() {
            public HeapWalkerNode[] computeChildren() {
                HeapWalkerNode[] children = null;

                if (isModeFields()) {
                    int fieldsSize = getInstance().getLength();

                    if (fieldsSize == 0) {
                        // Array has no items
                        children = new HeapWalkerNode[1];
                        children[0] = HeapWalkerNodeFactory.createNoItemsNode(PrimitiveArrayNode.this);
                    } else if (fieldsSize > HeapWalkerNodeFactory.ITEMS_COLLAPSE_UNIT_SIZE) {
                        int childrenCount = fieldsSize;
                        BrowserUtils.GroupingInfo groupingInfo = BrowserUtils.getGroupingInfo(childrenCount);
                        int containersCount = groupingInfo.containersCount;
                        int collapseUnitSize = groupingInfo.collapseUnitSize;

                        children = new HeapWalkerNode[containersCount];

                        for (int i = 0; i < containersCount; i++) {
                            int unitStartIndex = collapseUnitSize * i;
                            int unitEndIndex = Math.min(unitStartIndex + collapseUnitSize, childrenCount) - 1;
                            children[i] = HeapWalkerNodeFactory.createArrayItemContainerNode(PrimitiveArrayNode.this,
                                                                                             unitStartIndex, unitEndIndex);
                        }
                    } else {
                        // TODO: currently the below is a kind of logical view - fields view should also be available!
                        List fields = getInstance().getValues();
                        children = new HeapWalkerNode[fields.size()];

                        for (int i = 0; i < children.length; i++) {
                            children[i] = HeapWalkerNodeFactory.createPrimitiveArrayItemNode(PrimitiveArrayNode.this, i,
                                                                                             (String) fields.get(i));
                        }
                    }
                } else if (getMode() == HeapWalkerNode.MODE_REFERENCES) {
                    children = HeapWalkerNodeFactory.createReferences(PrimitiveArrayNode.this);
                }

                return children;
            }
        };
    }

}
