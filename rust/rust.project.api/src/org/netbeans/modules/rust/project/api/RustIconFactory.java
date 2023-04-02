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
package org.netbeans.modules.rust.project.api;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

/**
 * Images/icons for different Rust nodes.
 */
public final class RustIconFactory {

    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/rust/project/api/defaultFolderOpen.gif";  // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/rust/project/api/defaultFolder.gif"; // NOI18N

    /**
     * Returns an icon for Rust.
     *
     * @return The icon.
     */
    public static Image getRustIcon() {
        return ImageUtilities.loadImage(RustProjectAPI.ICON);
    }

    /**
     * Returns a folder icon suitable for light/dark themes. Vectorized.
     *
     * @param opened true for opened icon, false otherwise.
     * @return The folder icon
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }

    /**
     * Returns a folder icon, optionally opened, with a "source" badge.
     *
     * @param opened True for opened icon
     * @return The icon
     */
    public static Image getSourceFolderIcon(boolean opened) {
        Image base = getTreeFolderIcon(opened);
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/rust/project/api/sources-badge.png"); // NOI18N
        return ImageUtilities.mergeImages(base, badge, 8, 8);
    }

    /**
     * Returns a folder icon, optionally opened, with a "dependencies" badge.
     * @param opened True for opened icon.
     * @return The icon
     */
    public static Image getDependenciesFolderIcon(boolean opened) {
        Image base = getTreeFolderIcon(opened);
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/rust/project/api/libraries-badge.png"); // NOI18N
        return ImageUtilities.mergeImages(base, badge, 8, 8);
    }

    /**
     * Returns a folder icon, optionally opened, with a "config" badge.
     * @param opened True for opened icon.
     * @return The icon
     */
    public static Image getImportantFilesFolderIcon(boolean opened) {
        Image base = getTreeFolderIcon(opened);
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/rust/project/api/config-badge.gif"); // NOI18N
        return ImageUtilities.mergeImages(base, badge, 8, 8);
    }

    /**
     * Returns a folder icon, optionally opened, with a "workspace" badge.
     * @param opened True for opened icon.
     * @return The icon
     */
    public static Image getWorkspaceFolderIcon(boolean opened) {
        Image base = getTreeFolderIcon(opened);
        Image badge = ImageUtilities.loadImage("org/netbeans/modules/rust/project/api/rust-badge.png"); // NOI18N
        return ImageUtilities.mergeImages(base, badge, 8, 8);
    }

}
