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

package org.netbeans.modules.lsp.client.bindings;

import java.awt.Image;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.SymbolKind;
import org.openide.util.ImageUtilities;

/**
 * Based on:
 * java.source/src/org/netbeans/modules/java/ui/Icons.java
 * @author Petr Hrebejk
 */
public final class Icons {

    private static final String ICON_BASE = "org/netbeans/modules/lsp/client/bindings/icons/";
    private static final String GIF_EXTENSION = ".gif";
    private static final String PNG_EXTENSION = ".png";
        
    private Icons() {
    }
    
    public static Icon getCompletionIcon(CompletionItemKind completionKind) {
        Icon icon = null;

        if (completionKind != null) {
            icon = ImageUtilities.loadIcon(ICON_BASE + completionKind.name().toLowerCase(Locale.US) + PNG_EXTENSION);

            if (icon == null) {
                icon = ImageUtilities.loadIcon(ICON_BASE + completionKind.name().toLowerCase(Locale.US) + GIF_EXTENSION);
            }
        }
        
        if (icon == null) {
            icon = ImageUtilities.loadIcon(ICON_BASE + "variable" + GIF_EXTENSION);
        }
        
        return icon;
    }
    
    public static String getSymbolIconBase(Enum<?> symbolKind) {
        if (symbolKind == null) {
            return ICON_BASE + "empty.png";
        }

        for (String variant : new String[] {
            ICON_BASE + symbolKind.name().toLowerCase(Locale.US) + PNG_EXTENSION,
            ICON_BASE + symbolKind.name().toLowerCase(Locale.US) + GIF_EXTENSION,
            ICON_BASE + "variable" + GIF_EXTENSION
        }) {
            if (ImageUtilities.loadImage(variant) != null)
                return variant;
        }
        return null;
    }

    public static Icon getSymbolIcon(SymbolKind symbolKind) {
        return ImageUtilities.loadImageIcon(getSymbolIconBase(symbolKind), false);
    }
}
