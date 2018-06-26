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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.beans.navigation;

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
