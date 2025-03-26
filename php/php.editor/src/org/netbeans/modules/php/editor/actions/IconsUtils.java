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
            case CLASS:
                imageIcon = loadClassIcon();
                break;
            case IFACE:
                imageIcon = loadInterfaceIcon();
                break;
            case TRAIT:
                imageIcon = loadTraitIcon();
                break;
            case ENUM:
                imageIcon = loadEnumIcon();
                break;
            case CONSTANT:
                imageIcon = loadConstantIcon();
                break;
            case FUNCTION:
                imageIcon = loadFunctionIcon();
                break;
            default:
                imageIcon = ImageUtilities.loadImageIcon(EMPTY_FILE_ICON_BASE + PNG_EXTENSION, false);
                break;
        }
        return imageIcon;
    }

    public static ImageIcon getElementIcon(PhpElementKind elementKind) {
        return getElementIcon(elementKind, null);
    }

    public static ImageIcon getErrorGlyphIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "error-glyph" + GIF_EXTENSION, false); //NOI18N
    }

    public static ImageIcon loadClassIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "class" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadInterfaceIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "interface" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadTraitIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "trait" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadEnumIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "enum" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadFunctionIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "function" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadConstantIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "constant" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadEnumCaseIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "enumCase" + PNG_EXTENSION, false); // NOI18N
    }

    public static ImageIcon loadKeywordIcon() {
        return ImageUtilities.loadImageIcon(ICON_BASE + "php16Key" + PNG_EXTENSION, false); // NOI18N
    }
}
