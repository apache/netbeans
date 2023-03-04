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


import java.text.NumberFormat;
import org.openide.util.NbBundle;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.JavaClass;


/**
 * Implements common methods of all Fields Browser nodes holding reference to org.netbeans.lib.profiler.heap.JavaClass
 *
 * @author Jiri Sedlacek
 */
public class ClassNode extends AbstractHeapWalkerNode {
    
    private static final NumberFormat numberFormat = NumberFormat.getInstance();
    
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public abstract static class RootNode extends ClassNode implements org.netbeans.modules.profiler.heapwalk.model.RootNode {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public RootNode(JavaClass javaClass, String name, HeapWalkerNode parent) {
            super(javaClass, name, parent);
        }

        public RootNode(JavaClass javaClass, String name, HeapWalkerNode parent, int mode) {
            super(javaClass, name, parent, mode);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public abstract void refreshView();
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JavaClass javaClass;
    private String name;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ClassNode(JavaClass javaClass, String name, HeapWalkerNode parent) {
        this(javaClass, name, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
    }

    public ClassNode(JavaClass javaClass, String name, HeapWalkerNode parent, int mode) {
        super(parent, mode);

        this.javaClass = javaClass;

        this.name = name;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public boolean isLeaf() {
        return false;
    }
    
    protected ChildrenComputer getChildrenComputer() {
        return new ChildrenComputer() {
            public HeapWalkerNode[] computeChildren() {
                HeapWalkerNode[] children = null;

                List fieldValues = getJavaClass().getStaticFieldValues();

                if (fieldValues.size() == 0) {
                    // Instance has no fields
                    children = new HeapWalkerNode[1];
                    children[0] = HeapWalkerNodeFactory.createNoFieldsNode(ClassNode.this);
                } else {
                    // Instance has at least one field
                    children = new HeapWalkerNode[fieldValues.size()];

                    for (int i = 0; i < children.length; i++) {
                        children[i] = HeapWalkerNodeFactory.createFieldNode((FieldValue) fieldValues.get(i), ClassNode.this);
                    }
                }

                return children;
            }
        };
    }

    protected ImageIcon computeIcon() {
        return BrowserUtils.ICON_INSTANCE;
    }

    protected String computeName() {
        return name;
    }

    protected String computeType() {
        return javaClass.getName();
    }

    @NbBundle.Messages("ClassNode_NoneString=<none>")
    protected String computeValue() {
        return Bundle.ClassNode_NoneString();
    }

    protected String computeSize() {
        return numberFormat.format(javaClass.getAllInstancesSize());
    }

    protected String computeRetainedSize() {
        return numberFormat.format(javaClass.getRetainedSizeByClass());
    }
    
    public Object getNodeID() {
        return javaClass;
    }
}
