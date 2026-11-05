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
package org.openide.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

final class NodeSupport {

    private NodeSupport() {}

    /** messages to create a resource identification for each type of
    * icon from the base name for the icon.
    */
    private static final String[] icons = {

        // color 16x16
        "", // NOI18N

        // color 32x32
        "32", // NOI18N

        // mono 16x16
        "", // NOI18N

        // mono 32x32
        "32", // NOI18N

        // opened color 16x16
        "Open", // NOI18N

        // opened color 32x32
        "Open32", // NOI18N

        // opened mono 16x16
        "Open", // NOI18N

        // opened mono 32x32
        "Open32" // NOI18N
    };

    /** To index normal icon from previous array use
    *  + ICON_BASE.
    */
    static final int ICON_BASE = -1;

    /** for indexing opened icons */
    static final int OPENED_ICON_BASE = 3;

    /** empty array of paste types */
    static final PasteType[] NO_PASTE_TYPES = {  };

    /** empty array of new types */
    static final NewType[] NO_NEW_TYPES = {  };

    /** default icon base for all nodes */
    static final String DEFAULT_ICON_BASE = "org/openide/nodes/defaultNode"; // NOI18N
    static final String LEGACY_DEFAULT_ICON_EXTENSION = ".gif"; // NOI18N
    static final String DEFAULT_ICON_EXTENSION = ".png"; // NOI18N

    private static final String DEFAULT_ICON = DEFAULT_ICON_BASE + DEFAULT_ICON_EXTENSION;

    /** Tries to find the right icon for the iconbase.
    * @param type type of icon (from BeanInfo constants)
    * @param ib base where to scan in the array
    */
    static Image findIcon(String iconBase, int type, int ib, String iconExtension) {
        String res = iconBase + icons[type + ib] + iconExtension;
        Image im = ImageUtilities.loadImage(res, true);

        if (im != null) {
            return im;
        }

        // try the first icon
        res = iconBase + icons[java.beans.BeanInfo.ICON_COLOR_16x16 + ib] + iconExtension;

        im = ImageUtilities.loadImage(res, true);

        if (im != null) {
            return im;
        }

        if (ib == OPENED_ICON_BASE) {
            // try closed icon also
            return findIcon(iconBase, type, ICON_BASE, iconExtension);
        }

        // if still not found return default icon
        return getDefaultIcon();
    }

    static Image getDefaultIcon() {
        Image i = ImageUtilities.loadImage(DEFAULT_ICON, true);

        if (i == null) {
            throw new MissingResourceException("No default icon", "", DEFAULT_ICON); // NOI18N
        }

        return i;
    }

}
