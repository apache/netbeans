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

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.windows.TopComponent;

/**
 * Paints the tabs.
 *
 * @author S. Aubrecht
 */
public abstract class TabDisplayer extends JPanel {

    protected final TabDataModel tabModel;

    /**
     * C'tor
     * @param tabModel 
     */
    public TabDisplayer( TabDataModel tabModel ) {
        this.tabModel = tabModel;
    }

    /**
     * @param tabIndex
     * @return Bounds of the given tab or null if the index is out of bounds.
     */
    public abstract Rectangle getTabBounds( int tabIndex );

    /**
     * @param p
     * @return Tab at given point or null.
     */
    public abstract TabData getTabAt( Point p );

    /**
     * Connect the displayer with given controller.
     * @param controller
     */
    public abstract void attach( Controller controller );

    /**
     * Make the given tab selected.
     * @param index
     */
    public abstract void setSelectedIndex( int index );

    /**
     * @return Tab model.
     */
    public final TabDataModel getModel() {
        return tabModel;
    }

    /**
     * @param screenLocation
     * @return Tab index where a new document would be added when dropped at given
     * screen location or -1 if the location is outside the tab area.
     */
    public abstract int dropIndexOfPoint( Point screenLocation );

    /**
     * @param draggedTC
     * @param screenLocation
     * @return Rectangle to provide drop feedback when a window is being dragged
     * over this displayer.
     */
    public abstract Rectangle dropIndication( TopComponent draggedTC, Point screenLocation );

    /**
     * @return The area where the tabs are being painted.
     */
    public abstract Rectangle getTabsArea();
}
