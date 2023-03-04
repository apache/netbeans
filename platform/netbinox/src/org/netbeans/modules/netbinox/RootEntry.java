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
package org.netbeans.modules.netbinox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;

/** An entry representing root of a JAR file.
 *
 * @author Jaroslav Tulach
 */
final class RootEntry extends BundleEntry {
    private static final byte[] EMPTY = new byte[0];
    private final JarBundleFile bundleFile;
    RootEntry(JarBundleFile bf) {
        this.bundleFile = bf;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(EMPTY);
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public String getName() {
        return "/"; // NOI18N
    }

    @Override
    public long getTime() {
        return 0L;
    }

    @Override
    public URL getLocalURL() {
        try {
            return new URL("jar:" + org.openide.util.Utilities.toURI(bundleFile.getBaseFile()).toURL() + "!/");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public URL getFileURL() {
        try {
            return org.openide.util.Utilities.toURI(bundleFile.getBaseFile()).toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
