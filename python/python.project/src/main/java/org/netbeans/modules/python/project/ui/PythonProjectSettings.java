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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.python.project.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preferences for the module.
 * <p>
 * <b>This is copied from the corresponding Java action in java.projects (JavaProjectSettings)</b>
 * </p>
 *
 * @author Tomas Zezula, Jesse Glick
 */
public class PythonProjectSettings {

    private PythonProjectSettings() {}

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(PythonProjectSettings.class);

    /**
     * The package view should be displayed as a list of packages.
     */
    public static final int TYPE_PACKAGE_VIEW = 0;

    /**
     * The package view should be displayed as a tree of folders.
     */
    public static final int TYPE_TREE = 1;

    public static final String PROP_PACKAGE_VIEW_TYPE = "packageViewType"; //NOI18N
//    private static final String PROP_SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    private static Preferences prefs() {
        return NbPreferences.forModule(PythonProjectSettings.class);
    }

    /**
     * Returns how the package view should be displayed.
     * @return {@link #TYPE_PACKAGE_VIEW} or {@link #TYPE_TREE}
     */
    public static int getPackageViewType() {
        return prefs().getInt(PROP_PACKAGE_VIEW_TYPE, TYPE_PACKAGE_VIEW);
    }

    /**
     * Sets how the package view should be displayed.
     * @param type either {@link #TYPE_PACKAGE_VIEW} or {@link #TYPE_TREE}
     */
    public static void setPackageViewType(int type) {
        int currentType = getPackageViewType();
        if (currentType != type) {
            prefs().putInt(PROP_PACKAGE_VIEW_TYPE, type);
            pcs.firePropertyChange(PROP_PACKAGE_VIEW_TYPE, currentType, type);
        }
    }

//    public static boolean isShowAgainBrokenRefAlert() {
//        return prefs().getBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, true);
//    }
//
//    public static void setShowAgainBrokenRefAlert(boolean again) {
//        prefs().putBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, again);
//    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}
