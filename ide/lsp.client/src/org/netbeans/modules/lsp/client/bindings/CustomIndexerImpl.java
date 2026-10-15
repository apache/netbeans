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
package org.netbeans.modules.lsp.client.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class CustomIndexerImpl extends CustomIndexer {

    private static final RequestProcessor WORKER = new RequestProcessor(CustomIndexerImpl.class.getName(), 1, false, false);

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        FileObject root = context.getRoot();

        if (root == null) {
            return ; //ignore
        }

        handleStoredFiles(context, props -> {
            for (Indexable i : files) {
                FileObject file = root.getFileObject(i.getRelativePath());
                if (file != null) {
                    props.setProperty(i.getRelativePath(), FileUtil.getMIMEType(file));
                }
            }

            @SuppressWarnings("unchecked")
            Set<String> mimeTypes = new HashSet<>((Collection) props.values());
            Project prj = FileOwnerQuery.getOwner(root);

            if (prj != null) {
                WORKER.post(() -> {
                    for (String mimeType : mimeTypes) {
                        LSPBindings.ensureServerRunning(prj, mimeType);
                    }
                });
            }
        });
    }

    private static final String INDEX_FILE_NAME = "index.properties";

    private static void handleStoredFiles(Context context, Consumer<Properties> handleProperties) {
        Properties props = new Properties();
        FileObject index = context.getIndexFolder().getFileObject(INDEX_FILE_NAME);

        if (index != null) {
            try (InputStream in = index.getInputStream()) {
                props.load(in); // can throw IAE when illegal characters are read
            } catch (IOException | IllegalArgumentException ex) {
                Logger.getLogger(CustomIndexerImpl.class.getName()).log(Level.WARNING, "can not load '"+FileUtil.toFile(index)+"', resetting", ex);
            }
        }

        Properties old = (Properties) props.clone();

        handleProperties.accept(props);

        if (!old.equals(props)) {
            try {
                if (index == null) {
                    index = context.getIndexFolder().createData(INDEX_FILE_NAME);
                }
                try (OutputStream out = index.getOutputStream()) {
                    props.store(out, "");
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @MimeRegistration(mimeType="", service=CustomIndexerFactory.class)
    public static final class FactoryImpl extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new CustomIndexerImpl();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            handleStoredFiles(context, props -> {
                for (Indexable d : deleted) {
                    props.remove(d.getRelativePath());
                }
            });
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return "lsp-indexer";
        }

        @Override
        public int getIndexVersion() {
            return 0;
        }

    }
}
