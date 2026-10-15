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
package org.netbeans.modules.parsing.ui.indexing;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public final class RefreshAllIndexes implements ActionListener {

    private static final Logger LOG = Logger.getLogger(RefreshAllIndexes.class.getName());

    private final List<DataObject> context;

    public RefreshAllIndexes(List<DataObject> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final Collection<? extends URL> sources = PathRegistry.getDefault().getSources();
        final Set<FileObject> roots = new HashSet<>();
        for (DataObject dobj : context) {
            FileObject root = findRoot(dobj.getPrimaryFile(), sources);
            if (root != null) {
                roots.add(root);
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            for (FileObject root : roots) {
                if (sb.length() > 0) {
                    sb.append(", ");    //NOI18N
                }
                sb.append(FileUtil.getFileDisplayName(root));
            }
            LOG.log(Level.FINE, "Refreshing: {0}", sb.toString());   //NOI18N
        }
        IndexingManager.getDefault().refreshAllIndices(roots.toArray(new FileObject[0]));
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private FileObject findRoot(FileObject fobj, final Collection<? extends URL> roots) {
        while (fobj != null) {
            final URL url = fobj.toURL();
            if (roots.contains(url)) {
                return fobj;
            }
            fobj = fobj.getParent();
        }
        return null;
    }
}
