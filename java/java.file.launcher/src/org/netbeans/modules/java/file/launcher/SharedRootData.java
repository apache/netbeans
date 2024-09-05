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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.java.file.launcher.api.SourceLauncher;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class SharedRootData {

    private static final Logger LOG = Logger.getLogger(SharedRootData.class.getName());

    private static final Map<FileObject, SharedRootData> root2Data = new HashMap<>();

    public static synchronized void ensureRootRegistered(FileObject root) {
        if (root2Data.get(root) != null) {
            return ;
        }

        SharedRootData data = root2Data.computeIfAbsent(root, r -> new SharedRootData(r));

        data.init();
    }

    public static synchronized @CheckForNull SharedRootData getDataForRoot(FileObject root) {
        return root2Data.get(root);
    }

    private final FileObject root;
    private final Map<String, FileProperties> properties = new TreeMap<>();
    private final FileChangeListener listener = new FileChangeAdapter() {
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            Map<String, FileProperties> newProperties = new HashMap<>();

            addPropertiesFor(fe.getFile(), newProperties);
            setNewProperties(newProperties);
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            Map<String, FileProperties> newProperties = new HashMap<>();

            newProperties.put(FileUtil.getRelativePath(root, fe.getFile()), null);
            setNewProperties(newProperties);
        }
    };

    private SharedRootData(FileObject root) {
        this.root = root;
    }

    private void init() {
        root.addRecursiveListener(listener);
        Enumeration<? extends FileObject> todo = root.getChildren(true);
        Map<String, FileProperties> newProperties = new HashMap<>();
        while (todo.hasMoreElements()) {
            FileObject current = todo.nextElement();
            addPropertiesFor(current, newProperties);
        }
        setNewProperties(newProperties);
    }

    private void addPropertiesFor(FileObject file, Map<String, FileProperties> newProperties) {
        if (file.isData() && "text/x-java".equals(file.getMIMEType())) {
            newProperties.put(FileUtil.getRelativePath(root, file), new FileProperties((String) file.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS),
                                                                                       SingleSourceFileUtil.isTrue(file.getAttribute(SingleSourceFileUtil.FILE_REGISTER_ROOT))));
        }
    }

    private synchronized void setNewProperties(Map<String, FileProperties> newProperties) {
        if (newProperties.isEmpty()) {
            return ;
        }
        for (String key : newProperties.keySet()) {
            FileProperties fileProperties = newProperties.get(key);
            if (fileProperties == null) {
                properties.remove(key);
            } else {
                properties.put(key, fileProperties);
            }
        }

        List<String> vmOptions = properties.values()
                                           .stream()
                                           .map(p -> p.vmOptions)
                                           .filter(p -> p != null)
                                           .collect(Collectors.toList());
        String joinedCommandLine = SourceLauncher.joinCommandLines(vmOptions);
        try {
            if (!joinedCommandLine.equals(root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS))) {
                root.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, joinedCommandLine);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Failed to set " + SingleSourceFileUtil.FILE_VM_OPTIONS + " for " + root.getPath(), ex);
        }
        Boolean registerRoot = properties.values()
                                         .stream()
                                         .map(p -> p.registerRoot)
                                         .filter(r -> r)
                                         .findAny()
                                         .isPresent();
        try {
            if (!registerRoot.equals(root.getAttribute(SingleSourceFileUtil.FILE_REGISTER_ROOT))) {
                root.setAttribute(SingleSourceFileUtil.FILE_REGISTER_ROOT, registerRoot);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Failed to set " + SingleSourceFileUtil.FILE_REGISTER_ROOT + " for " + root.getPath(), ex);
        }
    }

    record FileProperties(String vmOptions, boolean registerRoot) {}

}
