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
