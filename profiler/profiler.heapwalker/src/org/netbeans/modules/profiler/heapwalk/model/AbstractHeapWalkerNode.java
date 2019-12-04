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

package org.netbeans.modules.profiler.heapwalk.model;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.SwingUtilities;


/**
 * Implements common methods of all Fields Browser nodes
 *
 * @author Jiri Sedlacek
 */
public abstract class AbstractHeapWalkerNode extends HeapWalkerNode {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeapWalkerNode parent;
    private Icon icon;
    private String name;
    private String type;
    private String value;
    private String size;
    private String retainedSize;
    private HeapWalkerNode[] children;
    private int mode = HeapWalkerNode.MODE_FIELDS;

    private Map<Object, Integer> indexes;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public AbstractHeapWalkerNode(HeapWalkerNode parent) {
        this(parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
    }

    public AbstractHeapWalkerNode(HeapWalkerNode parent, int mode) {
        this.parent = parent;
        this.mode = mode;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HeapWalkerNode getChild(int i) {
        return getChildren()[i];
    }

    public HeapWalkerNode[] getChildren() {
        if (children == null) {
            children = computeChildren();
            indexes = null;
        }

        return children;
    }

    private Map<Object, Integer> getIndexes() {
        if (indexes == null) {
            HeapWalkerNode[] chldrn = getChildren();
            indexes = new HashMap<>(chldrn.length * 4 / 3);
            for (int i = 0; i < chldrn.length; i++)
                indexes.put(chldrn[i], i);
        }

        return indexes;
    }

    public Icon getIcon() {
        if (icon == null) {
            icon = computeIcon();
        }

        return icon;
    }

    public int getIndexOfChild(Object object) {
//        for (int i = 0; i < getChildren().length; i++) {
//            if (getChildren()[i] == object) {
//                return i;
//            }
//        }
//
//        return -1;
        Integer index = getIndexes().get(object);
        return index != null ? index : -1;
    }

    // Should be overridden for lazy populating children
    public boolean isLeaf() {
        return getNChildren() == 0;
    }

    public int getMode() {
        return mode;
    }

    public int getNChildren() {
        if (getChildren() == null) {
            return 0;
        } else {
            return getChildren().length;
        }
    }

    public String getName() {
        if (name == null) {
            name = computeName();
        }

        return name;
    }
    
    public void setParent(HeapWalkerNode parent) {
        this.parent = parent;
    }

    public HeapWalkerNode getParent() {
        return parent;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public String getSimpleType() {
        return BrowserUtils.getSimpleType(getType());
    }

    public String getType() {
        if (type == null) {
            type = computeType();
        }

        return type;
    }

    public String getValue() {
        if (value == null) {
            value = computeValue();
        }

        return value;
    }
    
    public String getDetails() {
        return null;
    }

    public String getSize() {
        if (size == null) {
            size = computeSize();
        }

        return size;
    }

    public String getRetainedSize() {
        if (retainedSize == null) {
            retainedSize = computeRetainedSize();
        }

        return retainedSize;
    }

    // used for testing children for null without lazy-populating invocation
    // note that if false, it means that chilren are not yet computed OR this node is leaf!
    public boolean currentlyHasChildren() {
        return children != null;
    }
    
    protected ChildrenComputer getChildrenComputer() {
        return null;
    }

    public String toString() {
        return getName();
    }
    
    public Object getNodeID() {
        return getName();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof HeapWalkerNode)) return false;
        HeapWalkerNode n = (HeapWalkerNode)o;
        return getNodeID().equals(n.getNodeID());
    }
    
    public int hashCode() {
        return getNodeID().hashCode();
    }

    protected abstract Icon computeIcon();

    protected abstract String computeName();

    protected abstract String computeType();

    protected abstract String computeValue();

    protected abstract String computeSize();

    protected abstract String computeRetainedSize();

    // Used for explicit setting children, shouldn't be used!
    protected void setChildren(HeapWalkerNode[] children) {
        changeChildren(children);
    }

    // Should be overridden for lazy populating children
    protected HeapWalkerNode[] computeChildren() {
        ChildrenComputer ch = getChildrenComputer();
        if (ch != null) return BrowserUtils.lazilyCreateChildren(this, ch);
        else return new HeapWalkerNode[0];
    }

    // Used for updating lazily created children, shouldn't be used for any other purpose!
    void changeChildren(final HeapWalkerNode[] children) {
        Runnable childrenChanger = new Runnable() {
            public void run() {
                AbstractHeapWalkerNode.this.children = children;
                indexes = null;
            }
        };
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(childrenChanger);
            } catch (Exception ex) {}
        } else {
            childrenChanger.run();
        }
    }
}
