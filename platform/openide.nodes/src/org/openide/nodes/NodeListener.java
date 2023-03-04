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
package org.openide.nodes;


/** Listener to special changes in <code>Node</code>s. Is a property
* change listener so that all changes in properties in the {@link Node node} can be fired
* in the usual way.
* <P>
* Methods childrenAdded, childrenRemoved and childrenReordered are called
* with Children.MUTEX.writeAccess which guarantees that no other thread
* can change the hierarchy during that time, but also requires proper
* implementation of all NodeListeners which should avoid calls
* to other threads which might require access
* to Children.MUTEX due to changes nodes hierarchy or do any other kind of
* starvation.
*
*
* @author Jaroslav Tulach
*/
public interface NodeListener extends java.beans.PropertyChangeListener {
    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev);

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev);

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev);

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed(NodeEvent ev);
}
