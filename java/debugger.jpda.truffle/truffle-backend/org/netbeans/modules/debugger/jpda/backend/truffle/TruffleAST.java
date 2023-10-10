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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author martin
 */
public class TruffleAST {

    private final FrameInstance frameInstance;

    private TruffleAST(FrameInstance frameInstance) {
        this.frameInstance = frameInstance;
    }

    static TruffleAST get(int depth) {
        final AtomicInteger frameDepth = new AtomicInteger(depth);
        FrameInstance frameInstance = Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<FrameInstance>() {
            @Override
            public FrameInstance visitFrame(FrameInstance frameInstance) {
                CallTarget callTarget = frameInstance.getCallTarget();
                if (!(callTarget instanceof RootCallTarget)) {
                    return null;
                }
                RootCallTarget rct = (RootCallTarget) callTarget;
                SourceSection rootSourceSection = rct.getRootNode().getSourceSection();
                if (rootSourceSection != null) {
                    if (frameDepth.getAndDecrement() == 0) {
                        return frameInstance;
                    }
                }
                return null;
            }
        });
        return frameInstance == null ? null : new TruffleAST(frameInstance);
    }

    public Object[] getRawArguments() {
        return frameInstance.getFrame(FrameInstance.FrameAccess.MATERIALIZE).getArguments();
    }
/* TODO: deprecations removed since 22.0
    public Object[] getRawSlots() {
        Frame frame = frameInstance.getFrame(FrameInstance.FrameAccess.MATERIALIZE);
        List<? extends FrameSlot> slots = frame.getFrameDescriptor().getSlots();
        int n = slots.size();
        Object[] slotInfo = new Object[2*n];
        for (int i = 0; i < n; i++) {
            FrameSlot slot = slots.get(i);
            slotInfo[2*i] = slot.getIdentifier();
            slotInfo[2*i + 1] = frame.getValue(slot);
        }
        return slotInfo;
    }
*/
    /**
     * Get the nodes hierarchy. Every node is described by:
     * <ul>
     *  <li>node class</li>
     *  <li>node description</li>
     *  <li>node source section - either an empty line, or following items:</li>
     *  <ul>
     *   <li>URI</li>
     *   <li>&lt;start line&gt;:&lt;start column&gt;-&lt;end line&gt;:&lt;end column&gt;</li>
     *  </ul>
     *  <li>number of children</li>
     *  <li>&lt;child nodes follow...&gt;</li>
     * </ul>
     * @return a newline-separated list of elements describing the nodes hierarchy.
     */
    public String getNodes() {
        StringBuilder nodes = new StringBuilder();
        RootCallTarget rct = (RootCallTarget) frameInstance.getCallTarget();
        RootNode rootNode = rct.getRootNode();
        fillNode(rootNode, nodes);
        return nodes.toString();
    }

    private static void fillNode(Node node, StringBuilder nodes) {
        nodes.append(node.getClass().getName());
        nodes.append('\n');
        nodes.append(node.getDescription());
        nodes.append('\n');
        SourceSection ss = node.getSourceSection();
        if (ss == null) {
            nodes.append('\n');
        } else {
            nodes.append(ss.getSource().getURI().toString());
            nodes.append('\n');
            nodes.append(Integer.toString(ss.getStartLine()));
            nodes.append(':');
            nodes.append(Integer.toString(ss.getStartColumn()));
            nodes.append('-');
            nodes.append(Integer.toString(ss.getEndLine()));
            nodes.append(':');
            nodes.append(Integer.toString(ss.getEndColumn()));
            nodes.append('\n');
            //nodes.add(ss.getCode());
        }
        // TAGS:
        try {
            StringBuilder tags = new StringBuilder();
            if (node instanceof InstrumentableNode) {
                InstrumentableNode inode = (InstrumentableNode) node;
                for (Class<?> tag : StandardTags.class.getDeclaredClasses()) {
                    if (Tag.class.isAssignableFrom(tag)) {
                        if (inode.hasTag(tag.asSubclass(Tag.class))) {
                            if (tags.length() > 0) {
                                tags.append(',');
                            }
                            tags.append(tag.getSimpleName());
                        }
                    }
                }
            }
            nodes.append(tags);
        } catch (Throwable t) {
        }
        nodes.append('\n');
        List<Node> ch = NodeUtil.findNodeChildren(node);
        nodes.append(Integer.toString(ch.size()));
        nodes.append('\n');
        for (Node n : ch) {
            fillNode(n, nodes);
        }
    }
    
}
