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
package org.netbeans;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/** Delegates to other detectors registered in the NetBeans runtime.
 * <p>
 * {@code Files.probeContentType }loads installed detectors with
 * {@code ServiceLoader.load(FileTypeDetector.class, ClassLoader.getSystemClassLoader())}.
 * In the running IDE registered detectors are however loaded by different classloaders.
 * To mitigate that this class (registered on NetBeans application classapath)
 * obtains all registered detectors (including
 * {@code org.netbeans.modules.openide.filesystems.declmime.NioFileTypeDetector})
 * and delegates to them explicitly.
 */
@ServiceProvider(service = FileTypeDetector.class)
public final class NbFileTypeDetector extends FileTypeDetector {
    private final Lookup.Result<FileTypeDetector> all;

    public NbFileTypeDetector() {
        all = Lookup.getDefault().lookupResult(FileTypeDetector.class);
    }

    @Override
    public String probeContentType(Path path) throws IOException {
        for (FileTypeDetector detector : all.allInstances()) {
            if (detector == this) {
                continue;
            }

            String mime = detector.probeContentType(path);
            if (mime != null) {
                return mime;
            }
        }
        return null;
    }
}
