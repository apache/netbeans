/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
            boolean install = (installedFiles == null) || installedFiles.contains(key);
            return new Node[] {new FileNode(key, install)};
        }
    }
    
}
