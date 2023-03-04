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

package org.netbeans.modules.java.api.common.project.ui;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.SourceGroup;
import org.openide.util.Parameters;


/**
 * LibrariesSourceGroup
 * {@link SourceGroup} implementation passed to
 * {@link org.netbeans.spi.java.project.support.ui.PackageView#createPackageView(SourceGroup)}
 * @author Tomas Zezula
 */
final class LibrariesSourceGroup implements SourceGroup {

    private final FileObject root;
    private final String displayName;
    private final Icon icon;
    private final Icon openIcon;

    /**
     * Creates new LibrariesSourceGroup
     * @param root the classpath root
     * @param displayName the display name presented to user
     */              
    LibrariesSourceGroup (FileObject root, String displayName ) {
        this (root, displayName, null, null);
    }

    /**
     * Creates new LibrariesSourceGroup
     * @param root the classpath root
     * @param displayName the display name presented to user
     * @param icon closed icon
     * @param openIcon opened icon
     */          
    LibrariesSourceGroup (FileObject root, String displayName, Icon icon, Icon openIcon) {
        Parameters.notNull("root", root);   //NOI18N
        assert root.isFolder() : "Root: " + FileUtil.getFileDisplayName(root) +" has to be a folder.";  //NOI18N
        this.root = root;
        this.displayName = displayName;
        this.icon = icon;
        this.openIcon = openIcon;
    }


    public FileObject getRootFolder() {
        return this.root;
    }

    public String getName() {
        return root.toURL().toExternalForm();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Icon getIcon(boolean opened) {
        return opened ? openIcon : icon;
    }

    @Override public boolean contains(FileObject file) {
        return root.equals(file) || FileUtil.isParentOf(root,file);
    }

    @Override
    public boolean equals (Object other) {
        if (!(other instanceof LibrariesSourceGroup)) {
            return false;
        }
        LibrariesSourceGroup osg = (LibrariesSourceGroup) other;
        return displayName == null ? osg.displayName == null : displayName.equals (osg.displayName) &&
            root == null ? osg.root == null : root.equals (osg.root);  
    }

    @Override
    public int hashCode () {
        return ((displayName == null ? 0 : displayName.hashCode())<<16) | ((root==null ? 0 : root.hashCode()) & 0xffff);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //Not needed
    }
}
