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
package org.netbeans.modules.remote.impl.fs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.util.NotImplementedException;

/**
 * Stores information concerning suspended files and subdirectories
 * inside a suspended directory and its children.
 * When you suspend a directory, all child directories become suspended as well,
 * but SuspendInfo corresponds exactly to the directory suspend was called on.
 */
public class SuspendInfo {
    
    private final RemoteDirectory owner;
    private final Object lock = new Object();
    
    private final Map<RemoteDirectory, List<RemoteFileObjectBase>> dummyChildren;
    private final Set<RemoteFileObjectBase> suspended;

    public SuspendInfo(RemoteDirectory owner) {
        this.owner = owner;
        this.dummyChildren = new HashMap<>();
        this.suspended = new HashSet<>();
    }

    /** 
     * Gets *dummy* children for the given directory.
     * Note that there might be non-dummy children as well
     */
    public RemoteFileObject[] getDirectDummyChildren(RemoteDirectory dir) {
        synchronized(lock) {
            List<RemoteFileObjectBase> children = dummyChildren.get(dir);
            RemoteFileObject[] result = new RemoteFileObject[children.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = children.get(i).getOwnerFileObject();
            }
            return result;
        }
    }
    
    /** 
     * Gets *dummy* child of the given directory with the given name.
     * Note that there might be non-dummy children as well
     */
    public RemoteFileObjectBase getDirectDummyChild(RemoteDirectory dir, String nameExt) {
        synchronized(lock) {
            List<RemoteFileObjectBase> children = dummyChildren.get(dir);
            if (children != null) {
                for (RemoteFileObjectBase fo : children) {
                    if (fo.getNameExt().equals(nameExt)) {
                        return fo;
                    }
                }
            }
        }
        return null;
    }
    
    private void addDummyChildImpl(RemoteDirectory dir, RemoteFileObjectBase fo) {
        synchronized (lock) {
            suspended.add(fo);            
            List<RemoteFileObjectBase> children = dummyChildren.get(dir);
            if (children == null) {
                children = new ArrayList<>();
                dummyChildren.put(dir, children);
            }
            children.add(fo);
        }
    }
    
    /** 
     * Adds a dummy child to its parent.
     * automatically calls addSuspendsd() as well 
     */
    public void addDummyChild(RemotePlainFile fo) {
        addDummyChildImpl(fo.getParentImpl(), fo);
    }
    
    /** 
     * Adds a dummy child to its parent.
     * automatically calls addSuspendsd() as well 
     */
    public void addDummyChild(RemoteDirectory fo) {        
        addDummyChildImpl(fo.getParentImpl(), fo);
    }
    
    /** 
     * Gets all suspedned files.
     */
    public List<RemoteFileObjectBase> getAllSuspended() {
        synchronized (lock) {
            return new ArrayList<>(suspended);
        }
    }
    
    /** 
     * Adds a file to suspended list.
     */
    public void addSuspended(RemotePlainFile fo) {
        synchronized (lock) {
            suspended.add(fo);
        }
    }    
    
    public void dispose() {
        
    }
}
