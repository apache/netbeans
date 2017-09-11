/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.updateprovider;

import java.awt.Image;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/** Represents provider category.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ProviderCategory {
    private final String displayName;
    private final String iconBase;
    private final CATEGORY category;

    private ProviderCategory(String displayName, String iconBase, CATEGORY category) {
        assert (category != null) != (displayName != null && iconBase != null) : 
            "Category: " + category + " displayName: " + displayName
                    + " iconBase: " + iconBase;
            this.displayName = displayName;
        this.iconBase = iconBase;
        this.category = category;
    }

    public static ProviderCategory create(String iconBase, String categoryDisplayName) {
        return new ProviderCategory(categoryDisplayName, iconBase, null);
    }
    public String getDisplayName() {
        String name = category != null ? getCategoryName(category) : displayName; 
        if (name == null) {
            name = forValue(CATEGORY.COMMUNITY).getDisplayName();
            assert name != null;
        }
        return name;
    }
    public String getName() {
        if (category != null) {
            return category.name();
        }
        assert displayName != null;
        return displayName;
    }
    public Image getIcon() {
        Image img = ImageUtilities.loadImage(getIconBase(), true);
        if (img == null) {
            img = forValue(CATEGORY.COMMUNITY).getIcon();
            assert img != null;
        }
        return img;
    }
    
    public static ProviderCategory forValue(CATEGORY c) {
        return new ProviderCategory(null, null, c);
    }

    CATEGORY toEnum() {
        return category == null ? CATEGORY.COMMUNITY : category;
    }
    
    static String getCategoryName(CATEGORY category) {
        String key = null;
        switch (category) {
            case STANDARD:
                key = "AvailableTab_SourceCategory_Tooltip_STANDARD"; //NOI18N
                break;
            case BETA:
                key = "AvailableTab_SourceCategory_Tooltip_BETA"; //NOI18N
                break;
            case COMMUNITY:
                key = "AvailableTab_SourceCategory_Tooltip_COMMUNITY"; //NOI18N
                break;
        }
        return (key != null) ? NbBundle.getMessage(ProviderCategory.class, key) : null;
    }
    
    public final String getIconBase() {
        if (iconBase != null) {
            return iconBase;
        }
        switch (category) {
            case BETA: 
                return "org/netbeans/modules/autoupdate/services/resources/icon-beta.png"; // NOI18N
            case STANDARD:
                return "org/netbeans/modules/autoupdate/services/resources/icon-standard.png"; // NOI18N
            default:
                return "org/netbeans/modules/autoupdate/services/resources/icon-community.png"; // NOI18N
        }
    }
}
