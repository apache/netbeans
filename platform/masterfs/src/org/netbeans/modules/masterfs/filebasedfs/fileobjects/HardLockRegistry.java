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
import org.openide.util.Lookup;

/** A pluggable hard lock handler.
 * @see <a href="@TOP@/architecture-summary.html#lookup-disable.LCK.files">the API usecase.</a>.
 */
public interface HardLockRegistry {

    public static HardLockRegistry getInstance() {
        return HardLockRegistryInstance.INSTANCE;
    }

    public void doHardLock(File hardLock, String absolutePath) throws IOException;

    public boolean doHardUnlock(File hardLock);

    public boolean hardLockExists(File hardLock) throws IOException;

    public String getHardLockContent(File hardLock);

}

//init the HardLockRegistry instance lazily:
final class HardLockRegistryInstance {
    public static final HardLockRegistry INSTANCE = Lookup.getDefault().lookup(HardLockRegistry.class);
}

