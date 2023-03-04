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


import org.openide.util.NbBundle;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Instance;


/**
 * Represents org.netbeans.lib.profiler.heap.Instance
 * (which is not PrimitiveArrayInstance nor ObjectArrayInstance)
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ObjectNode_LoopToString=(loop to {0})"
})
public class ObjectNode extends InstanceNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class ArrayItem extends ObjectNode implements org.netbeans.modules.profiler.heapwalk.model.ArrayItem {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private String ownerArrayType;
        private int itemIndex;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ArrayItem(int itemIndex, Instance instance, HeapWalkerNode parent) {
            this(itemIndex, instance, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
        }

        public ArrayItem(int itemIndex, Instance instance, HeapWalkerNode parent, int mode) {
            super(instance, null, parent, mode);

            this.itemIndex = itemIndex;
            this.ownerArrayType = parent.getType();
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getItemIndex() {
            return itemIndex;
        }

        protected String computeName() {
            String name = "[" + itemIndex + "]"; // NOI18N

            if (isLoop()) {
                name += (" " + Bundle.ObjectNode_LoopToString(BrowserUtils.getFullNodeName(getLoopTo())));
            }

            if ((getMode() == HeapWalkerNode.MODE_REFERENCES) && getInstance().isGCRoot()) {
                HeapWalkerNode root = BrowserUtils.getRoot(this);

                if (root instanceof RootNode) {
                    GCRoot gcRoot = ((RootNode) root).getGCRoot(getInstance());

                    if (gcRoot != null) {
                        name += (" (" + gcRoot.getKind() + ")"); // NOI18N
                    }
                }
            }

            return name;
        }

        protected String computeType() {
            if (!hasInstance()) {
                return "<" + BrowserUtils.getArrayItemType(ownerArrayType) + ">"; // NOI18N
            }

            return super.computeType();
        }
        
        private String nodeID;
        public Object getNodeID() {
            if (nodeID == null)
                nodeID = itemIndex + "#" + (hasInstance() ? getInstance().getInstanceId() : "null"); // NOI18N
            return nodeID;
        }
    }

    public abstract static class RootNode extends ObjectNode implements org.netbeans.modules.profiler.heapwalk.model.RootNode {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public RootNode(Instance instance, String name, HeapWalkerNode parent) {
            super(instance, name, parent);
        }

        public RootNode(Instance instance, String name, HeapWalkerNode parent, int mode) {
            super(instance, name, parent, mode);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public abstract void refreshView();
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ObjectNode(Instance instance, String name, HeapWalkerNode parent) {
        super(instance, name, parent);
    }

    public ObjectNode(Instance instance, String name, HeapWalkerNode parent, int mode) {
        super(instance, name, parent, mode);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isArray() {
        return false;
    }

    protected ChildrenComputer getChildrenComputer() {
        return new ChildrenComputer() {
            public HeapWalkerNode[] computeChildren() {
                HeapWalkerNode[] children = null;

                if (isModeFields()) {
                    if (hasInstance()) {
                        ArrayList fieldValues = new ArrayList();
                        fieldValues.addAll(getInstance().getFieldValues());
                        final boolean skipClassLoaders = getMode() == HeapWalkerNode.MODE_FIELDS_NO_CLASSLOADER;
                        for (Object v : getInstance().getStaticFieldValues()) {
                            FieldValue fv = (FieldValue) v;
                            final Field field = fv.getField();
                            if (skipClassLoaders && field.isStatic() && "<classLoader>".equals(field.getName())) { // NOI18N
                                continue;
                            }
                            fieldValues.add(v);
                        }

                        if (fieldValues.isEmpty()) {
                            // Instance has no fields
                            children = new HeapWalkerNode[1];
                            children[0] = HeapWalkerNodeFactory.createNoFieldsNode(ObjectNode.this);
                        } else {
                            // Instance has at least one field
                            children = new HeapWalkerNode[fieldValues.size()];

                            for (int i = 0; i < children.length; i++) {
                                children[i] = HeapWalkerNodeFactory.createFieldNode((FieldValue) fieldValues.get(i),
                                                                                    ObjectNode.this);
                            }
                        }
                    } else {
                        children = new HeapWalkerNode[0];
                    }
                } else if (getMode() == HeapWalkerNode.MODE_REFERENCES) {
                    children = HeapWalkerNodeFactory.createReferences(ObjectNode.this);
                }

                return children;
            }
        };
    }

    protected Icon computeIcon() {
        ImageIcon icon = BrowserUtils.ICON_INSTANCE;

        if ((getMode() == HeapWalkerNode.MODE_REFERENCES) && getInstance().isGCRoot()) {
            icon = BrowserUtils.createGCRootIcon(icon);
        }

        return processLoopIcon(icon);
    }

    protected String computeName() {
        if ((getMode() == HeapWalkerNode.MODE_REFERENCES) && getInstance().isGCRoot()) {
            HeapWalkerNode root = BrowserUtils.getRoot(this);

            if (root instanceof org.netbeans.modules.profiler.heapwalk.model.RootNode) {
                GCRoot gcRoot = ((org.netbeans.modules.profiler.heapwalk.model.RootNode) root).getGCRoot(getInstance());

                if (gcRoot != null) {
                    return super.computeName() + " (" + gcRoot.getKind() + ")"; // NOI18N
                }
            }
        }

        return super.computeName();
    }
}
