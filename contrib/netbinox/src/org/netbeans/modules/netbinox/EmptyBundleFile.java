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
package org.netbeans.modules.netbinox;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class EmptyBundleFile extends BundleFile {
    public static final BundleFile EMPTY = new EmptyBundleFile();

    private EmptyBundleFile() {
    }

    @Override
    public File getFile(String string, boolean bln) {
        return null;
    }

    @Override
    public BundleEntry getEntry(String string) {
        return null;
    }

    @Override
    public Enumeration<String> getEntryPaths(String string) {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public boolean containsDir(String string) {
        return false;
    }
}
