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
package org.netbeans.modules.java.file.launcher.indexer;

import org.netbeans.modules.java.file.launcher.SharedRootData;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class CompilerOptionsIndexer extends CustomIndexer {


    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        FileObject root = context.getRoot();

        if (root == null) {
            return ; //ignore
        }

        if (!SingleSourceFileUtil.isSupportedFile(root)) {
            return ; //ignore roots under projects
        }

        SharedRootData.ensureRootRegistered(root);
    }


    @MimeRegistration(mimeType="text/x-java", service=CustomIndexerFactory.class, position=1_000)
    public static final class FactoryImpl extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new CompilerOptionsIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            //nothing
        }

        @Override
        public String getIndexerName() {
            return "java-launcher-compiler-options";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    }
}
