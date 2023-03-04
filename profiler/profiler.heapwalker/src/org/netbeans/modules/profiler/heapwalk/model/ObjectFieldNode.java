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
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 *
 * @author Jiri Sedlacek
 */
public class ObjectFieldNode extends ObjectNode implements HeapWalkerFieldNode {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ObjectFieldValue fieldValue;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ObjectFieldNode(ObjectFieldValue fieldValue, HeapWalkerNode parent) {
        this(fieldValue, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
    }

    public ObjectFieldNode(ObjectFieldValue fieldValue, HeapWalkerNode parent, int mode) {
        super((mode != HeapWalkerNode.MODE_REFERENCES) ? fieldValue.getInstance() : fieldValue.getDefiningInstance(),
              fieldValue.getField().getName(), parent, mode);
        this.fieldValue = fieldValue;

        if (!isLoop() && (getMode() == HeapWalkerNode.MODE_REFERENCES) && isStatic()) {
            loopTo = computeClassLoopTo();
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public ObjectFieldValue getFieldValue() {
        return fieldValue;
    }

    public boolean isStatic() {
        return fieldValue.getField().isStatic();
    }

    protected Icon computeIcon() {
        ImageIcon icon = BrowserUtils.ICON_INSTANCE;

        if (isStatic()) {
            icon = BrowserUtils.createStaticIcon(icon);
        }

        if ((getMode() == HeapWalkerNode.MODE_REFERENCES) && getInstance().isGCRoot()) {
            icon = BrowserUtils.createGCRootIcon(icon);
        }

        return processLoopIcon(icon);
    }

    private HeapWalkerNode computeClassLoopTo() {
        JavaClass declaringClass = fieldValue.getField().getDeclaringClass();
        HeapWalkerNode parent = getParent();

        while (parent instanceof HeapWalkerInstanceNode) {
            if (parent instanceof HeapWalkerFieldNode) {
                HeapWalkerFieldNode parentF = (HeapWalkerFieldNode) parent;

                if (parentF.isStatic() && parentF.getFieldValue().getField().getDeclaringClass().equals(declaringClass)) {
                    return parent;
                }
            }

            parent = parent.getParent();
        }

        return null;
    }
    
    public Object getNodeID() {
        return fieldValue;
    }
    
}
