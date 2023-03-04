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


/** Empty adapter for <code>NodeListener</code>.
*
* @author Jaroslav Tulach
* @version 0.10, Jan 16, 1998
*/
public class NodeAdapter extends Object implements NodeListener {
    /* Change in some own node's property.
    * @param ev the event
    */
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
    }

    /* Informs that a set of new children has been added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev) {
    }

    /* Informs that a set of children has been removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev) {
    }

    /* Fired when the order of children has changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev) {
    }

    /* Informs that the node has been deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed(NodeEvent ev) {
    }
}
