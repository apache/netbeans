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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = HardLockRegistry.class, position = 100)
public final class FileBaseHardLockRegistry implements HardLockRegistry {

    public void doHardLock(File hardLock, String absolutePath) throws IOException {
        hardLock.getParentFile().mkdirs();
        hardLock.createNewFile();
        try (OutputStream os = Files.newOutputStream(hardLock.toPath())) {
            os.write(absolutePath.getBytes());
        }
    }

    public boolean doHardUnlock(File hardLock) {
        return hardLock.delete();
    }

    public boolean hardLockExists(File hardLock) throws IOException {
        return FileChangedManager.getInstance().exists(hardLock);
    }

    public String getHardLockContent(File hardLock) {
        if (FileChangedManager.getInstance().exists(hardLock)) {
            try (InputStream is = new FileInputStream(hardLock)) {
                byte[] path = new byte[is.available()];
                if (path.length > 0 && is.read(path) == path.length) {
                    return new String(path);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

}
