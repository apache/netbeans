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
package org.netbeans.core.multitabs;

import org.netbeans.core.multitabs.prefs.SettingsImpl;

/**
 * Read-only access to multi-tab settings.
 *
 * @author S. Aubrecht
 */
public final class Settings {

    private final SettingsImpl impl = new SettingsImpl();
    private static Settings theInstance;

    private Settings() {
    }

    /**
     * @return The one and only instance.
     */
    public static Settings getDefault() {
        synchronized( Settings.class ) {
            if( null == theInstance ) {
                theInstance = new Settings();
            }
        }
        return theInstance;
    }

    /**
     * @return True if multi-tabs are enabled, false otherwise.
     */
    public boolean isEnabled() {
        return impl.isEnabled();
    }

    /**
     * @return One of JTabbedPane.TOP BOTTOM LEFT or RIGHT constants.
     * @see javax.swing.JTabbedPane#getTabPlacement() 
     */
    public int getTabsLocation() {
        return impl.getTabsLocation();
    }

    /**
     * @return True to show full file path of currently selected document.
     */
    public boolean isShowFullPath() {
        return impl.isShowFullPath();
    }

    /**
     * @return True to use same color for all tabs from the same project.
     */
    public boolean isSameProjectSameColor() {
        return impl.isSameProjectSameColor();
    }

    /**
     * @return True to sort drop-down document list by project.
     */
    public boolean isSortDocumentListByProject() {
        return impl.isSortDocumentListByProject();
    }

    /**
     * @return True to show one row of tabs per project.
     */
    public boolean isTabRowPerProject() {
        return impl.isTabRowPerProject();
    }

    /**
     * @return Maximum tab row count.
     */
    public int getRowCount() {
        return impl.getRowCount();
    }
}
