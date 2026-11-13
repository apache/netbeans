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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = HardLockRegistry.class, position = 1000)
public final class InMemoryHardLockRegistry implements HardLockRegistry {

    private static final Logger LOGGER = Logger.getLogger(InMemoryHardLockRegistry.class.getName());
    private final ConcurrentHashMap<String, String> hardLocks2Content = new ConcurrentHashMap<>();

    private static String getKey(File hardLock) throws IOException {
        Path realParentPath = hardLock.toPath().getParent().toRealPath();
        return realParentPath.resolve(hardLock.getName()).toString();
    }

    public void doHardLock(File hardLock, String absolutePath) throws IOException {
        hardLocks2Content.put(getKey(hardLock), absolutePath);
    }

    public boolean doHardUnlock(File hardLock) {
        try {
            return hardLocks2Content.remove(getKey(hardLock)) != null;
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
            return false;
        }
    }

    public boolean hardLockExists(File hardLock) throws IOException {
        return hardLocks2Content.containsKey(getKey(hardLock));
    }

    public String getHardLockContent(File hardLock) {
        try {
            return hardLocks2Content.get(getKey(hardLock));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

}
