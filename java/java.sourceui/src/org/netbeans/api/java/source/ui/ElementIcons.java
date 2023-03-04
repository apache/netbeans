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
package org.netbeans.api.java.source.ui;

import java.util.Collection;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.UiUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/**
 *
 * @author phrebejk
 */
public class ElementIcons {

    private static final String EXPORTS_ICON = "org/netbeans/modules/java/source/resources/icons/exports.png";
    private static final String OPENS_ICON = "org/netbeans/modules/java/source/resources/icons/opens.png";
    private static final String REQUIRES_ICON = "org/netbeans/modules/java/source/resources/icons/requires.png";
    private static final String USES_ICON = "org/netbeans/modules/java/source/resources/icons/uses.png";
    private static final String PROVIDES_ICON = "org/netbeans/modules/java/source/resources/icons/provides.png";

    private ElementIcons() {}

    /** Gets correct icon for given ElementKind.
     * @param elementKind Kind of the element the Icon is required for
     * @param modifiers Can be null for empty modifiers collection
     * @return Icon which should be used for the element throughout the IDE.
     */
    public static Icon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        return UiUtils.getElementIcon(elementKind, modifiers);
    }

    /**
     * Returns an icon for the given {@link ModuleElement.DirectiveKind}.
     * @param kind the {@link ModuleElement.DirectiveKind} to return an icon for.
     * @return the icon
     * @since 1.45
     */
    public static Icon getModuleDirectiveIcon(@NonNull final ModuleElement.DirectiveKind kind) {
        Parameters.notNull("kind", kind);   //NOI18N
        switch (kind) {
            case EXPORTS:
                return ImageUtilities.loadImageIcon(EXPORTS_ICON, true);
            case REQUIRES:
                return ImageUtilities.loadImageIcon(REQUIRES_ICON, true);
            case USES:
                return ImageUtilities.loadImageIcon(USES_ICON, true);
            case PROVIDES:
                return ImageUtilities.loadImageIcon(PROVIDES_ICON, true);
            case OPENS:
                return ImageUtilities.loadImageIcon(OPENS_ICON, true);
            default:
                throw new IllegalArgumentException(kind.toString());
        }
    }
}
