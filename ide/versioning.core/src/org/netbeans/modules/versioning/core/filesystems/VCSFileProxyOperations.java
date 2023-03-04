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
package org.netbeans.modules.versioning.core.filesystems;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Operations available on a file represented by {@link VCSFileProxy}.<br>
 * Filesystem implementors interested in VCSFileProxy should provide for each FileObject
 * an instance available via {@link FileObject#getAttribute(java.lang.String)}.
 *
 * @author Vladimir Voskresensky
 */
public interface VCSFileProxyOperations {

    public interface Provider {
        VCSFileProxyOperations getVCSFileProxyOperations(URI uri);
        VCSFileProxyOperations getVCSFileProxyOperations(FileSystem fs);
    }

    public static final String ATTRIBUTE = "FileProxyOperations";

    String getName(VCSFileProxy file);

    boolean isDirectory(VCSFileProxy file);

    boolean isFile(VCSFileProxy file);

    boolean canWrite(VCSFileProxy file);

    VCSFileProxy getParentFile(VCSFileProxy file);

    String getAbsolutePath(VCSFileProxy file);

    boolean exists(VCSFileProxy file);

    VCSFileProxy normalize(VCSFileProxy file);

    FileObject toFileObject(VCSFileProxy file);

    VCSFileProxy[] list(VCSFileProxy file);

    ProcessBuilder createProcessBuilder(VCSFileProxy file);

    void refreshFor(VCSFileProxy ... files);

    long lastModified(VCSFileProxy file);

    URI toURI(VCSFileProxy file) throws URISyntaxException;

    InputStream getInputStream(VCSFileProxy file, boolean checkLock) throws FileNotFoundException;
}
