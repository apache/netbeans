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

package org.netbeans.lib.profiler.results.memory;

import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A node of the run time Memory Profiling Calling Context Tree (CCT). Unlike the presentation-time CCT, this one
 * contains information in the form that is quickly updateable at run time, but needs further processing for
 * proper presentation. Instances of class RuntimeMemoryCCTNode are used only as non-terminal nodes, and contain
 * minimum information to save space. The information such as the total number of calls, size of allocated objects,
 * etc., which can be calculated  for intermediate nodes if known for terminal nodes, is contained, in runtime CCT,
 * only in specialized terminal nodes (instances of classes RuntimeObjAllocTermCCTNode and
 * RuntimeObjLivenessTermCCTNode).
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class RuntimeMemoryCCTNode implements Cloneable, RuntimeCCTNode {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final RuntimeCCTNode[] EMPTY_CHILDREN = new RuntimeMemoryCCTNode[0];    

    protected static final int TYPE_RuntimeMemoryCCTNode = 1;
    protected static final int TYPE_RuntimeObjAllocTermCCTNode = 2;
    protected static final int TYPE_RuntimeObjLivenessTermCCTNode = 3;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    /** Children nodes in the forward stack trace tree. This field can have three different values depending on the
     * number of children:
     *   null if there are no children
     *   instance of RuntimeMemoryCCTNode if there is exactly one child
     *   instance of RuntimeMemoryCCTNode[] if there are multiple children
     * This is purely a memory consumption optimization, which typically saves about 80% of memory, since most allocation
     * stack traces are a sequence of single-child nodes, and in such case we remove the need to create a one-item array
     */
    public Object children;

    /** unique Id of method - jMethodId from JVM (see MemoryCallGraphBuilder.getNamesForJMethodIds) */
    public int methodId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** For I/O only */
    protected RuntimeMemoryCCTNode() {
    }

    public RuntimeMemoryCCTNode(int methodId) {
        this.methodId = methodId;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getType() {
        return TYPE_RuntimeMemoryCCTNode;
    }

    public static RuntimeMemoryCCTNode create(int type) {
        switch (type) {
            case TYPE_RuntimeMemoryCCTNode:
                return new RuntimeMemoryCCTNode();
            case TYPE_RuntimeObjAllocTermCCTNode:
                return new RuntimeObjAllocTermCCTNode();
            case TYPE_RuntimeObjLivenessTermCCTNode:
                return new RuntimeObjLivenessTermCCTNode();
        }

        throw new IllegalArgumentException("Illegal type: " + type); // NOI18N
    }

    public RuntimeMemoryCCTNode addNewChild(int methodId) {
        if (children == null) {
            children = new RuntimeMemoryCCTNode(methodId);

            return (RuntimeMemoryCCTNode) children;
        } else {
            RuntimeMemoryCCTNode[] ar = addChildEntry();

            return (ar[ar.length - 1] = new RuntimeMemoryCCTNode(methodId));
        }
    }

    public void attachNodeAsChild(RuntimeMemoryCCTNode node) {
        if (children == null) {
            children = node;
        } else {
            RuntimeMemoryCCTNode[] ar = addChildEntry();
            ar[ar.length - 1] = node;
        }
    }

    @Override
    public RuntimeCCTNode[] getChildren() {
        if (children == null) {
            return EMPTY_CHILDREN;
        } else if (children instanceof RuntimeCCTNode) {
            return new RuntimeCCTNode[]{(RuntimeCCTNode)children};
        } else if (children instanceof RuntimeCCTNode[]) {
            return (RuntimeCCTNode[])children;
        }
        return EMPTY_CHILDREN;
    }

    public Object clone() {
        try {
            RuntimeMemoryCCTNode ret = (RuntimeMemoryCCTNode) super.clone();

            if (children != null) {
                if (children instanceof RuntimeMemoryCCTNode) {
                    ret.children = ((RuntimeMemoryCCTNode) children).clone();
                } else {
                    RuntimeMemoryCCTNode[] ar = (RuntimeMemoryCCTNode[]) children;
                    ret.children = new RuntimeMemoryCCTNode[ar.length];

                    for (int i = 0; i < ar.length; i++) {
                        ((RuntimeMemoryCCTNode[]) ret.children)[i] = (RuntimeMemoryCCTNode) ar[i].clone();
                    }
                }
            }

            return ret;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Clone should never fail"); // NOI18N
        }
    }

    public void readFromStream(DataInputStream in) throws IOException {
        methodId = in.readInt();

        int len = in.readInt();

        if (len == 0) {
            children = null;
        } else if (len == 1) {
            int type = in.readInt();
            children = RuntimeMemoryCCTNode.create(type);
            ((RuntimeMemoryCCTNode) children).readFromStream(in);
        } else {
            RuntimeMemoryCCTNode[] ar = new RuntimeMemoryCCTNode[len];

            for (int i = 0; i < len; i++) {
                int type = in.readInt();
                ar[i] = RuntimeMemoryCCTNode.create(type);
                ar[i].readFromStream(in);
            }

            children = ar;
        }
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(methodId);

        if (children == null) {
            out.writeInt(0);
        } else if (children instanceof RuntimeMemoryCCTNode) {
            out.writeInt(1);
            out.writeInt(((RuntimeMemoryCCTNode) children).getType());
            ((RuntimeMemoryCCTNode) children).writeToStream(out);
        } else {
            RuntimeMemoryCCTNode[] ar = (RuntimeMemoryCCTNode[]) children;
            out.writeInt(ar.length);

            for (int i = 0; i < ar.length; i++) {
                out.writeInt(ar[i].getType());
                ar[i].writeToStream(out);
            }
        }
    }

    private RuntimeMemoryCCTNode[] addChildEntry() {
        assert (children != null);

        if (children instanceof RuntimeMemoryCCTNode) {
            // currently just single child
            RuntimeMemoryCCTNode[] ret = new RuntimeMemoryCCTNode[2];
            ret[0] = (RuntimeMemoryCCTNode) children;
            children = ret;

            return ret;
        } else {
            RuntimeMemoryCCTNode[] ar = (RuntimeMemoryCCTNode[]) children;
            RuntimeMemoryCCTNode[] newchildren = new RuntimeMemoryCCTNode[ar.length + 1];
            System.arraycopy(ar, 0, newchildren, 0, ar.length);
            children = newchildren;

            return newchildren;
        }
    }
}
