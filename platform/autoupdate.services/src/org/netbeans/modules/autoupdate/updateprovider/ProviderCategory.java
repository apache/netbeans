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
