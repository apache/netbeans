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
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.awt.Image;
import org.openide.util.Lookup;

/**
 * Annotator of WebKit nodes. It allows modifications of icons associated
 * with WebKit nodes. For example, the debugger may add a breakpoint badge
 * to nodes on which DOM breakpoints are set.
 *
 * @author Jan Stola
 */
public class NodeAnnotator {
    /** The only instance of this class. */
    private static final NodeAnnotator INSTANCE = new NodeAnnotator();

    /**
     * Creates a new {@code NodeAnnotator}.
     */
    private NodeAnnotator() {
    }

    /**
     * Returns the {@NodeAnnotator} singleton.
     * 
     * @return the {@NodeAnnotator} singleton.
     */
    public static NodeAnnotator getDefault() {
        return INSTANCE;
    }

    /**
     * Adds a badge to a node.
     * 
     * @param node node to add a badge to.
     * @param badge badge to add.
     */
    public void annotate(Node node, Image badge) {
        for (Impl annotator : Lookup.getDefault().lookupAll(Impl.class)) {
            annotator.annotate(node, badge);
        }
    }

    /**
     * An implementation of {@NodeAnnotator}. Implementations of this interface
     * that are present in the global lookup are notified whenever some node
     * is annotated.
     */
    public static interface Impl {
        /**
         * Adds a badge to a node.
         * 
         * @param node node to add a badge to.
         * @param badge badge to add.
         */
        void annotate(Node node, Image badge);
    }

}
