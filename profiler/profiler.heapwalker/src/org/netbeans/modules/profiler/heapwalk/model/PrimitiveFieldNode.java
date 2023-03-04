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


import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.lib.profiler.heap.FieldValue;


/**
 *
 * @author Jiri Sedlacek
 */
public class PrimitiveFieldNode extends AbstractHeapWalkerNode implements HeapWalkerFieldNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class ArrayItem extends PrimitiveFieldNode implements org.netbeans.modules.profiler.heapwalk.model.ArrayItem {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private String type;
        private String value;
        private int itemIndex;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ArrayItem(int itemIndex, String type, String value, HeapWalkerNode parent) {
            this(itemIndex, type, value, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
        }

        public ArrayItem(int itemIndex, String type, String value, HeapWalkerNode parent, int mode) {
            super(null, parent, mode);

            this.itemIndex = itemIndex;
            this.type = type;
            this.value = value;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getItemIndex() {
            return itemIndex;
        }

        public boolean isStatic() {
            return false;
        }

        protected String computeName() {
            return "[" + itemIndex + "]"; // NOI18N
        }

        protected String computeType() {
            return type;
        }

        protected String computeValue() {
            return value;
        }
        
        public Object getNodeID() {
            return itemIndex;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldValue fieldValue;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public PrimitiveFieldNode(FieldValue fieldValue, HeapWalkerNode parent) {
        this(fieldValue, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
    }

    public PrimitiveFieldNode(FieldValue fieldValue, HeapWalkerNode parent, int mode) {
        super(parent, mode);
        this.fieldValue = fieldValue;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public FieldValue getFieldValue() {
        return fieldValue;
    }

    public boolean isLeaf() {
        return true;
    }

    public boolean isStatic() {
        return fieldValue.getField().isStatic();
    }

    protected Icon computeIcon() {
        ImageIcon icon = BrowserUtils.ICON_PRIMITIVE;

        if (isStatic()) {
            icon = BrowserUtils.createStaticIcon(icon);
        }

        return icon;
    }

    protected String computeName() {
        return fieldValue.getField().getName();
    }

    protected String computeType() {
        return fieldValue.getField().getType().getName();
    }

    protected String computeValue() {
        return fieldValue.getValue();
    }

    protected String computeSize() {
        return "-"; // NOI18N
    }

    protected String computeRetainedSize() {
        return "-"; // NOI18N
    }
    
    public Object getNodeID() {
        return fieldValue;
    }
}
