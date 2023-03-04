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

package org.netbeans.modules.xml.wizard.impl;

import java.util.Collection;
import java.util.Collections;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * A placeholder node that displays a "please wait" message while the
 * task to generate the final node is performed.
 *
 * @author  Nathan Fiedler
 */
public class WaitNode extends AbstractNode {
    /** A child key for this node, to be used with Children.Key.setKeys(). */
    public static final Object WAIT_KEY = new Object();

    /**
     * Creates a new instance of WaitNode.
     */
    public WaitNode() {
        super(Children.LEAF);
        setName(NbBundle.getMessage(WaitNode.class, "LBL_WaitNode_Wait"));
        setIconBaseWithExtension("org/netbeans/modules/xml/xam/ui/resources/wait.gif");
    }

    /**
     * Convenience method that creates an array with a single WaitNode.
     *
     * @return  array with a WaitNode.
     */
    public static Node[] createNode() {
        return new Node[] { new WaitNode() };
    }

    /**
     * Convenience method that creates a collection with a single child key
     * entry, that being the WAIT_KEY value.
     *
     * @return  collection with WAIT_KEY.
     */
    public static Collection getKeys() {
        return Collections.singletonList(WaitNode.WAIT_KEY);
    }
}
