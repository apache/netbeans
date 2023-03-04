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

package org.netbeans.modules.gsf.testrunner.ui.api;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Node representing a callstack
 * @author Marian Petras
 */
public class CallstackFrameNode extends AbstractNode {
    
    /** */
    protected final String frameInfo;
    
    /** Creates a new instance of CallstackFrameNode */
    public CallstackFrameNode(final String frameInfo) {
        this(frameInfo, null);
    }
    
    /**
     * Creates a new instance of CallstackFrameNode
     *
     * @param  frameInfo  line of a callstack, e.g. <code>foo.bar.Baz:314</code>
     * @param  displayName  display name for the node, or <code>null</code>
     *                      to use the default display name for the given
     *                      callstack frame info
     */
    public CallstackFrameNode(final String frameInfo,
                              final String displayName) {
        super(Children.LEAF);
        setDisplayName(displayName != null
                       ? displayName
                       : frameInfo);                            //NOI18N

        this.frameInfo = frameInfo;
    }
    
    /**
     * Gets preferred action.
     * @return preferred action which defaults to {@code null}
     */
    @Override
    public Action getPreferredAction() {
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    @Override
    public String getDisplayName() {
        return TestsuiteNode.cutLine(super.getDisplayName(),
                TestsuiteNode.MAX_MSG_LINE_LENGTH, false); // Issue #172772
    }

}
