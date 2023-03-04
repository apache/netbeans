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

package org.netbeans.modules.masterfs;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

import java.net.URL;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;

/**
 * Implements URLMapper for MasterFileSystem.
 * @author  rm111737
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public final class MasterURLMapper extends URLMapper {
    private static final URLMapper delegate = new FileBasedURLMapper();
    /** Creates a new instance of MasterURLMapper */
    public MasterURLMapper() {
    }

    public FileObject[] getFileObjects(final URL url) {                
        return delegate.getFileObjects(url);
    }

    public URL getURL(final FileObject fo, final int type) {
        return delegate.getURL(fo, type);
    }
}
