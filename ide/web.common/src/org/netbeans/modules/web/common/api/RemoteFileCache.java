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

package org.netbeans.modules.web.common.api;

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.web.common.spi.RemoteFileCacheImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * This class retrieves given URL and stores it in IDE's cache directory and returns
 * local FileObject representing the URL.
 */
public final class RemoteFileCache {

    /**
     * Returns local image of remote file. If the file is not in cache the method
     * may return FileObject which is empty and later update its content.
     */
    public static FileObject getRemoteFile(URL url) throws IOException {
        for (RemoteFileCacheImplementation impl : Lookup.getDefault().lookupAll(RemoteFileCacheImplementation.class)) {
            FileObject fo = impl.getRemoteFile(url);
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }

    /**
     * Translates local image of remote file back to its original URL.
     * @return null or URL of the remote file
     */
    public static URL isRemoteFile(FileObject fo) {
        for (RemoteFileCacheImplementation impl : Lookup.getDefault().lookupAll(RemoteFileCacheImplementation.class)) {
            URL url = impl.isRemoteFile(fo);
            if (url != null) {
                return url;
            }
        }
        return null;
    }
    
}
