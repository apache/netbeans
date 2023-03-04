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
package org.netbeans.modules.openide.loaders;

import java.util.Map;
import java.util.WeakHashMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.RequestProcessor;

/** Currently allows to share RP for nodes used by different packages
 * of the org.openide.loaders module.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class DataNodeUtils {
    private static final RequestProcessor RP = new RequestProcessor("Data System Nodes"); // NOI18N

    private static final Map<FileSystem, RequestProcessor> FS_TO_RP
            = new WeakHashMap<FileSystem, RequestProcessor>();

    private DataNodeUtils() {
    }

    public static RequestProcessor reqProcessor() {
        return RP;
    }

    /**
     * Get request processor for a file.
     *
     * @param fo Some FileObject.
     * @return Request Processor (newly) assigned to the file's filesystem.
     */
    public static RequestProcessor reqProcessor(FileObject fo) {
        if (fo == null) {
            return RP;
        }
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
            synchronized (FS_TO_RP) {
                RequestProcessor rp = FS_TO_RP.get(fs);
                if (rp == null) {
                    rp = new RequestProcessor("Data System Nodes for " //NOI18N
                            + fs.getDisplayName());
                    FS_TO_RP.put(fs, rp);
                }
                return rp;
            }
        } catch (FileStateInvalidException ex) {
            return RP;
        }
    }
}
