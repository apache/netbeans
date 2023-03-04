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
package org.openide.filesystems;


/** Listener to changes in the filesystem pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public interface RepositoryListener extends java.util.EventListener {
    /** Called when new filesystem is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded(RepositoryEvent ev);

    /** Called when a filesystem is removed from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved(RepositoryEvent ev);

    /** Called when a filesystem pool is reordered. */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev);
}
