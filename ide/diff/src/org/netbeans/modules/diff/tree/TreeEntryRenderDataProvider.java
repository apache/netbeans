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

package org.netbeans.modules.diff.tree;

import java.awt.Color;
import javax.swing.Icon;
import org.netbeans.swing.outline.RenderDataProvider;


public class TreeEntryRenderDataProvider implements RenderDataProvider {
    public static final java.awt.Color COLOR_MISSING = new java.awt.Color(255, 160, 180);
    public static final java.awt.Color COLOR_ADDED = new java.awt.Color(180, 255, 180);
    public static final java.awt.Color COLOR_CHANGED = new java.awt.Color(160, 200, 255);

    private final boolean useRelativePath;

    public TreeEntryRenderDataProvider(boolean useRelativePath) {
        this.useRelativePath = useRelativePath;
    }

    @Override
    public String getDisplayName(Object o) {
        TreeEntry te = (TreeEntry) o;
        StringBuilder sb = new StringBuilder();
        if(te.isModified()) {
            sb.append("<b>");
        }
        if(useRelativePath) {
            sb.append(te.getRelativePath());
        } else {
            sb.append(te.getName());
        }
        if (te.isModified()) {
            sb.append("</b>");
        }
        return sb.toString();
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return true;
    }

    @Override
    public Color getBackground(Object o) {
        TreeEntry te = (TreeEntry) o;
        if (!te.isFilesIdentical()) {
            if (te.getFile1() == null) {
                return COLOR_ADDED;
            } else if (te.getFile2() == null) {
                return COLOR_MISSING;
            } else {
                return COLOR_CHANGED;
            }
        } else {
            return null;
        }
    }

    @Override
    public Color getForeground(Object o) {
        return null;
    }

    @Override
    public String getTooltipText(Object o) {
        if (! useRelativePath) {
            return ((TreeEntry) o).getRelativePath();
        } else {
            return ((TreeEntry) o).getName();
        }
    }

    @Override
    public Icon getIcon(Object o) {
        return null;
    }

}
