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
package org.netbeans.modules.javascript.cdnjs.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Node that represents files of some library.
 *
 * @author Jan Stola
 */
public class FilesNode extends AbstractNode {
    /** Library represented by this node. */
    private final Library.Version version;

    /**
     * Creates a new {@code FilesNode}.
     * 
     * @param version library version that should be represented by the node.
     * @param installedFiles installed files of the library (can be {@code null}).
     */
    public FilesNode(Library.Version version, String[] installedFiles) {
        super(new FilesChildren(version, installedFiles));
        this.version = version;
    }

    /**
     * Returns the library version whose files reflect the selection made
     * by the user.
     * 
     * @return library version whose files reflect the selection made
     * by the user.
     */
    Library.Version getSelection() {
        if (version == null) {
            return null;
        } else {
            Set<String> refusedFiles = new HashSet<>();
            collectRefusedFiles(refusedFiles);
            Library.Version selection = version.filterVersion(refusedFiles);
            return selection.getFiles().length == 0 ? null : selection;
        }
    }

    /**
     * Collects the names of the files the user is not interested in.
     * 
     * @param refusedFiles collection that should be populated by
     * the refused files.
     */
    void collectRefusedFiles(Collection<String> refusedFiles) {
        for (Node node : getChildren().getNodes(true)) {
            ((FileNode)node).collectRefusedFiles(refusedFiles);
        }
    }

    /**
     * Children of the {@code FilesNode}.
     */
    static class FilesChildren extends Children.Keys<String> {
        /** Installed files of the library. */
        private final Set<String> installedFiles;

        /**
         * Creates a new {@code FilesChildren} for the given library version.
         * 
         * @param version library that should be represented by the node.
         * @param installedFiles installed files of the library.
         */
        FilesChildren(Library.Version version, String[] installedFiles) {
            if (installedFiles == null) {
                this.installedFiles = null;
            } else {
                this.installedFiles = new HashSet<>(Arrays.asList(installedFiles));
            }
            setKeys((version == null) ? new String[0] : version.getFiles());
        }

        @Override
        protected Node[] createNodes(String key) {
            boolean install = (installedFiles != null) && installedFiles.contains(key);
            return new Node[] {new FileNode(key, install)};
        }
    }
    
}
