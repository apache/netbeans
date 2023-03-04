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
package org.netbeans.modules.html.editor;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import org.netbeans.modules.html.editor.lib.api.dtd.ReaderProvider;
import org.netbeans.modules.html.editor.lib.api.dtd.ReaderProviderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = ReaderProviderFactory.class)
public class NbReaderProviderFactory extends ReaderProviderFactory {

    private final String DTD_FOLDER = "DTDs"; // NOI18 // NOI18N
    private Collection<ReaderProvider> PROVIDERS;

    @Override
    public Collection<ReaderProvider> getProviders() {
        if (PROVIDERS == null) {
            initializeProviders();
        }
        return PROVIDERS;
    }

    private void initializeProviders() {
        PROVIDERS = new LinkedList<>();
        FileObject rootFolder = FileUtil.getConfigRoot();
        FileObject dtdFolder = rootFolder.getFileObject(DTD_FOLDER);
        if (dtdFolder != null) {
            processSubfolders(dtdFolder);
        }

    }

    private void processSubfolders(FileObject dtdFolder) {
        for (Enumeration en = dtdFolder.getFolders(false); en.hasMoreElements();) {
            FileObject folder = (FileObject) en.nextElement();
            addFolder(folder);
        }
    }

    private void addFolder(FileObject folder) {
        PROVIDERS.add(new NbReaderProvider(folder));
    }
    
}
