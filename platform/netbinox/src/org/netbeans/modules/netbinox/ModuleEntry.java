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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;

/** An entry representing a direct entry inside a NetBeans
 * module.
 *
 * @author Jaroslav Tulach
 */
final class ModuleEntry extends BundleEntry {
    private final URL url;
    private final String name;

    public ModuleEntry(URL url, String name) {
        this.url = url;
        this.name = name;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return url.openStream();
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getTime() {
        return 0L;
    }

    @Override
    public URL getLocalURL() {
        return url;
    }

    @Override
    public URL getFileURL() {
        return url;
    }
}
