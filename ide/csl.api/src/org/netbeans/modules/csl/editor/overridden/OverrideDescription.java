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

package org.netbeans.modules.csl.editor.overridden;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.navigation.Icons;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lahvac
 */
public class OverrideDescription {

    public final @NonNull AlternativeLocation location;
    private final boolean overriddenFlag;

    public OverrideDescription(@NonNull AlternativeLocation location, boolean overriddenFlag) {
        this.location = location;
        this.overriddenFlag = overriddenFlag;
    }

    public Icon getIcon() {
        Image badge;

        if (overriddenFlag) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/is-overridden-badge.png");
        } else {
            badge = ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/overrides-badge.png");
        }

        ImageIcon icon = Icons.getElementIcon(location.getElement().getKind(), location.getElement().getModifiers());

        return ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.icon2Image(icon), badge, 16, 0));
    }

    public boolean isOverridden() {
        return overriddenFlag;
    }
    
}
