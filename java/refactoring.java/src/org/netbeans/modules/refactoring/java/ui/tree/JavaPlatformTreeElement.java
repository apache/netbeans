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

package org.netbeans.modules.refactoring.java.ui.tree;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class JavaPlatformTreeElement implements TreeElement {
    
    private final JavaPlatform platform;
    private final Icon icon;
    private final String displayName;
    
    private static final String PLATFORM_ICON = "org/netbeans/modules/java/platform/resources/platform.gif"; //NOI18N
//    private static String PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; // NOI18N

    JavaPlatformTreeElement(JavaPlatform platform) {
        this.platform = platform;

        icon = new ImageIcon(ImageUtilities.loadImage(PLATFORM_ICON));
        displayName = platform.getDisplayName();
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        return null;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return displayName;
    }

    @Override
    public Object getUserObject() {
        return platform;
    }
}

