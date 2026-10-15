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

package org.netbeans.modules.masterfs.filebasedfs.naming;


import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Radek Matous
 */
public class FolderName extends FileName {
    private static Map<FolderName,File> fileCache = new WeakHashMap<>();


    @SuppressWarnings("LeakingThisInConstructor")
    FolderName(final FileNaming parent, final File file, ID theKey) {
        super(parent, file, theKey);
        synchronized (FolderName.class) {
            FolderName.fileCache.put(this, file);
        }
    }

    @Override
    void updateCase(String name) {
        super.updateCase(name);
        synchronized (FolderName.class) {
            FolderName.fileCache.remove(this);
        }
    }

    public @Override File getFile() {
        File retValue;
        synchronized (FolderName.class) {
            retValue = FolderName.fileCache.get(this);

            if (retValue == null) {
                retValue = super.getFile();
                FolderName.fileCache.put(this, retValue);
            }
        }

        assert retValue != null;
        return retValue;
    }

    static void freeCaches() {
        synchronized (FolderName.class) {
            FolderName.fileCache = new WeakHashMap<>();
        }
    }

    public @Override boolean isFile() {
        return false;
    }

}
