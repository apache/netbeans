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
package org.netbeans.modules.java.file.launcher;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=SingleFileOptionsQueryImplementation.class)
public class AttributeBasedSingleFileOptions implements SingleFileOptionsQueryImplementation {

    @Override
    public Result optionsFor(FileObject file) {
        if (!SingleSourceFileUtil.isSupportedFile(file)) {
            return null;
        }

        if (file.isData() && !"text/x-java".equals(FileUtil.getMIMEType(file))) {
            return null;
        }

        FileObject root = Lookup.getDefault().lookup(MultiSourceRootProvider.class).getSourceRoot(file);

        if (!file.isData()) {
            file = null;
        }

        if (root != null || file != null) {
            return new ResultImpl(root, file);
        }

        return null;
    }

    private static final class ResultImpl implements Result {

        private final ChangeSupport cs;
        private final FileObject root;
        private final FileObject source;
        private final FileChangeListener attributeChanges = new FileChangeAdapter() {
            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {
                cs.fireChange();
            }
        };

        ResultImpl(FileObject root, FileObject source) {
            this.cs = new ChangeSupport(this);
            this.root = root;
            this.source = source;
            if (source != null) {
                source.addFileChangeListener(WeakListeners.create(FileChangeListener.class, attributeChanges, source));
            }
            if (root != null) {
                root.addFileChangeListener(WeakListeners.create(FileChangeListener.class, attributeChanges, root));
            }
        }

        @Override
        public String getOptions() {
            Object vmOptionsObj = source != null ? source.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS) : null;

            if (vmOptionsObj != null) {
                return (String) vmOptionsObj;
            }

            vmOptionsObj = root != null ? root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS) : null;

            return vmOptionsObj != null ? (String) vmOptionsObj : "";
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }
    }
}
