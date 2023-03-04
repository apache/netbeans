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
package org.netbeans.modules.web.common.remote;

import java.io.File;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 * Assures serialization and de-serialization of file objects from RemoteFS.
 * Serves only files, that are cached already.
 * 
 * @author Martin
 */
@ServiceProvider(service=URLMapper.class)
public class RemoteURLMapper extends URLMapper {
    
    @Override
    public URL getURL(FileObject fo, int type) {
        URL url = RemoteFilesCache.getDefault().isRemoteFile(fo);
        return url;
    }

    @Override
    public FileObject[] getFileObjects(URL url) {
        String protocol = url.getProtocol();
        if ("http".equals(protocol) || "https".equals(protocol)) {      // NOI18N
            File cachedFile = RemoteFilesCache.getCachedFileName(url);
            if (cachedFile.exists()) {
                FileObject fo = RemoteFS.getDefault().getFileForURL(url);
                if (fo != null) {
                    return new FileObject[] { fo };
                }
            }
        }
        return null;
    }
    
}
