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
package org.netbeans.modules.jakarta.web.beans.navigation;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.prefs.Preferences;

import org.openide.util.NbPreferences;


/**
 * @author ads
 *
 */
final class WebBeansNavigationOptions {
    
    private static final String PROP_caseSensitive = "caseSensitive";      // NOI18N
    private static final String PROP_showFQN = "showFQN";                  // NOI18N   
    private static final String PROP_lastBoundsX = "lastBoundsX";          // NOI18N
    private static final String PROP_lastBoundsY = "lastBoundsY";          // NOI18N
    private static final String PROP_lastBoundsWidth = "lastBoundsWidth";  // NOI18N
    private static final String PROP_lastBoundsHeight = "lastBoundsHeight";// NOI18N
    private static final String PROP_hierarchyDividerLocation = 
                                            "hierarchyDividerLocation";    // NOI18N
    
    private static final Preferences getPreferences() {
        return NbPreferences.forModule(WebBeansNavigationOptions.class);
    }

    static boolean isCaseSensitive() {
        return getPreferences().getBoolean(PROP_caseSensitive, myCaseSensitive);
    }

    static boolean isShowFQN() {
        return getPreferences().getBoolean(PROP_showFQN, myShowFQN);
    }

    static void setCaseSensitive( boolean selected ) {
        getPreferences().putBoolean(PROP_caseSensitive, myCaseSensitive);
    }

    static void setShowFQN( boolean selected ) {
        getPreferences().putBoolean(PROP_showFQN, selected);         
    }

    static void setLastBounds( Rectangle bounds ) {
        if (bounds != null) {
            getPreferences().putInt(PROP_lastBoundsX, bounds.x);
            getPreferences().putInt(PROP_lastBoundsY, bounds.y);
            getPreferences().putInt(PROP_lastBoundsWidth, bounds.width);
            getPreferences().putInt(PROP_lastBoundsHeight, bounds.height);
        }
    }
    
    static int getHierarchyDividerLocation() {
        return getPreferences().getInt(PROP_hierarchyDividerLocation, 
                myHierarchyDividerLocation);
    }
    
    static void setHierarchyDividerLocation(int hierarchyDividerLocation) {
        getPreferences().putInt(PROP_hierarchyDividerLocation, 
                hierarchyDividerLocation);
    }

    static Rectangle getLastBounds() {
        int x = getPreferences().getInt(PROP_lastBoundsX, myLastBounds.x);
        int y = getPreferences().getInt(PROP_lastBoundsY, myLastBounds.y);
        int width = getPreferences().getInt(PROP_lastBoundsWidth, myLastBounds.width);
        int height = getPreferences().getInt(PROP_lastBoundsHeight, myLastBounds.height);
        
        return new Rectangle(x, y, width, height);
    }
    
    private static boolean myCaseSensitive = false;
    
    private static int myHierarchyDividerLocation = 350;   
    
    private static boolean myShowFQN = false;

    private static Rectangle myLastBounds;
    
    static
    {
        Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();        
        myLastBounds = new Rectangle(((dimensions.width / 2) - 410), 
                ((dimensions.height / 2) - 300), 820, 600);
    }
}
