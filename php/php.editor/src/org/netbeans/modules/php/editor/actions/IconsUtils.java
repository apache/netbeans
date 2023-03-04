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
package org.netbeans.modules.php.editor.actions;

import java.util.Collection;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class IconsUtils {

    private static final String PNG_EXTENSION = ".png"; //NOI18N
    private static final String GIF_EXTENSION = ".gif"; //NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/php/editor/resources/"; //NOI18N
    private static final String EMPTY_FILE_ICON_BASE = "org/netbeans/modules/csl/source/resources/icons/emptyfile-icon"; //NOI18N

    private IconsUtils() {
    }

    public static ImageIcon getElementIcon(PhpElementKind elementKind, Collection<Modifier> modifiers) {
        ImageIcon imageIcon;
        switch (elementKind) {
            case CLASS: imageIcon = ImageUtilities.loadImageIcon(ICON_BASE + "class" + PNG_EXTENSION, false); //NOI18N
                break;
            case IFACE: imageIcon = ImageUtilities.loadImageIcon(ICON_BASE + "interface" + PNG_EXTENSION, false); //NOI18N
                break;
            case TRAIT: imageIcon = ImageUtilities.loadImageIcon(ICON_BASE + "trait" + PNG_EXTENSION, false); //NOI18N
                break;
            case ENUM: imageIcon = ImageUtilities.loadImageIcon(ICON_BASE + "enum" + PNG_EXTENSION, false); //NOI18N
                break;
            case CONSTANT: imageIcon = ImageUtilities.loadImageIcon(ICON_BASE + "constant" + PNG_EXTENSION, false); //NOI18N
                break;
            case FUNCTION: imageIcon = ImageUtilities.loadImageIcon(ICON_BASE + "function" + PNG_EXTENSION, false); //NOI18N
                break;
            default: imageIcon = ImageUtilities.loadImageIcon(EMPTY_FILE_ICON_BASE + PNG_EXTENSION, false);
        }
        return imageIcon;
    }

    public static ImageIcon getElementIcon(PhpElementKind elementKind) {
        return getElementIcon(elementKind, null);
    }

    public static ImageIcon getErrorGlyphIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "error-glyph" + GIF_EXTENSION, false); //NOI18N
    }

}
