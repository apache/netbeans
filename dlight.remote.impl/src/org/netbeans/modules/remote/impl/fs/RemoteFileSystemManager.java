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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.Exceptions;

/**
 * Manages instances of the RemoteFileSystem class
 *
 * TODO: release instances when they are not used
 *
 */
public class RemoteFileSystemManager {
    
    private static final RemoteFileSystemManager INSTANCE = new RemoteFileSystemManager();

    private final Object lock = new Object();
    
    private final Map<ExecutionEnvironment, SoftReference<RemoteFileSystem>> fileSystems =
            new HashMap<>();

    private final List<FileChangeListener> globalListsners = new ArrayList<>();

    public static RemoteFileSystemManager getInstance() {
        return INSTANCE;
    }

    public void resetFileSystem(ExecutionEnvironment execEnv, boolean clearCache) {
        synchronized(lock) {
            SoftReference<RemoteFileSystem> ref = fileSystems.remove(execEnv);
            if (ref != null) {
                RemoteFileSystem fs = ref.get();
                if (fs != null) {
                    fs.dispose();
                }
                if (clearCache) {
                    RemoteFileSystemUtils.deleteRecursively(fs.getCache());
                }
            }
        }
    }

    public RemoteFileSystem getFileSystem(ExecutionEnvironment execEnv) {
        synchronized(lock) {
            SoftReference<RemoteFileSystem> ref = fileSystems.get(execEnv);
            RemoteFileSystem result = (ref == null) ? null : ref.get();
            if (result == null) {
                try {
                    result = new RemoteFileSystem(execEnv);
                    fileSystems.put(execEnv, new SoftReference<>(result));
                    for (FileChangeListener listener : globalListsners) {
                        result.addFileChangeListener(listener);
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            return result;
        }
    }

    public void addFileChangeListener(FileChangeListener listener) {
        synchronized(lock) {
            globalListsners.add(listener);
            for (SoftReference<RemoteFileSystem> ref : fileSystems.values()) {
                RemoteFileSystem fs = ref.get();
                if (fs != null) {
                    fs.addFileChangeListener(listener);
                }
            }
        }
    }
    
    public void removeFileChangeListener(FileChangeListener listener) {
        synchronized(lock) {
            globalListsners.add(listener);
            for (SoftReference<RemoteFileSystem> ref : fileSystems.values()) {
                RemoteFileSystem fs = ref.get();
                if (fs != null) {
                    fs.removeFileChangeListener(listener);
                }
            }
        }
    }

    public Collection<RemoteFileSystem> getAllFileSystems(){
        Collection<RemoteFileSystem> res = new ArrayList<>();
        synchronized(lock) {
            for (SoftReference<RemoteFileSystem> ref : fileSystems.values()) {
                RemoteFileSystem fs = ref.get();
                if (fs != null) {
                    res.add(fs);
                }
            }
        }
        return res;
    }
}
