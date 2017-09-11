/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
