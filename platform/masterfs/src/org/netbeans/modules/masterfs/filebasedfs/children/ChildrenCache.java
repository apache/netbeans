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

package org.netbeans.modules.masterfs.filebasedfs.children;

import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.openide.util.Mutex;

import java.util.Map;
import java.util.Set;

public interface ChildrenCache {
    Integer ADDED_CHILD = 0;
    Integer REMOVED_CHILD = 1;

    /**
     * Get child. If some computation or I/O is needed to be performed off the
     * lock, returns null and puts a Runnable into task[0] - in such case run
     * the runnable off the lock and then call the method again with the same
     * parameters.
     *
     * @param childName
     * @param rescan
     * @param task Array of size 1 where the task to be performed off the EDT
     * can be put into, or the runnable that has already by run, and that
     * contains the results. If null, the task will be performed immediately.
     * @return The child, or null (additional computation off the lock may be
     * needed.)
     */
    FileNaming getChild(String childName, boolean rescan, Runnable[] task);

    FileNaming getChild(String childName, boolean rescan);
    void removeChild(FileNaming childName);    
    Set<FileNaming> getChildren(boolean rescan, Runnable[] task);
    //cached existing
    Set<FileNaming> getCachedChildren();
    boolean isCacheInitialized();    
    Map<FileNaming, Integer> refresh(Runnable[] task);
    Mutex.Privileged getMutexPrivileged();
}
