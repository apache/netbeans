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

package org.netbeans.spi.server;

import javax.swing.JComponent;
import org.openide.nodes.Node;

/**
 * The representation of the single server instance. Interface describes
 * the instance and provides needed operations.
 *
 * @author Petr Hejl
 * @see ServerInstanceProvider
 */
public interface ServerInstanceImplementation {

    /**
     * Returns the display name of the instance.
     *
     * @return the display name of the instance
     */
    String getDisplayName();

    /**
     * Returns the display name of the server type to which this instance belongs.
     *
     * @return the display name of the server type to which this instance belongs
     */
    String getServerDisplayName();

    /**
     * Returns the node representing the runtime instance. The node should
     * display instance status and provide actions to manage the server.
     *
     * @return the node representing the instance, may return <code>null</code>
     */
    Node getFullNode();

    /**
     * Returns the node representing the instance while configuring it.
     * The node should not display any status, actions or children.
     *
     * @return the node representing the instance, may return <code>null</code>
     */
    Node getBasicNode();

    /**
     * Returns the component allowing the customization of the instance. May
     * return <code>null</code>.
     * <p>
     * Always called from Event Dispatch Thread.
     *
     * @return the component allowing the customization of the instance,
     *             may return <code>null</code>
     */
    JComponent getCustomizer();

    /**
     * Removes the instance. No {@link ServerInstanceProvider} should return
     * this instance once it is removed.
     */
    void remove();

    /**
     * Returns <code>true</code> if the instance can be removed by
     * {@link #remove()}. Otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the instance can be removed
     */
    boolean isRemovable();

}
