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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * Panel whose dimension can be restricted by its preferred size.
 * It is supposed to be used in containers using <code>BoxLayout</code>.
 *
 * @see  javax.swing.BoxLayout  BoxLayout
 * @author  Marian Petras
 */
public class SizeRestrictedPanel extends JPanel {

    /** whether the panel's width is restricted */
    private final boolean widthRestriction;
    /** whether the panel's height is restricted */
    private final boolean heightRestriction;
    
    /**
     * Creates a panel with flow layout, restricted in both directions.
     */
    public SizeRestrictedPanel() {
        this(true, true);
    }
    
    /**
     * Creates a panel with flow layout, with width and/or height restricted.
     *
     * @param  widthRestriction  whether the panel's width should be restricted
     * @param  heightRestriction whether the panel's height should be restricted
     */
    public SizeRestrictedPanel(boolean widthRestriction,
                               boolean heightRestriction) {
        super();
        this.widthRestriction = widthRestriction;
        this.heightRestriction = heightRestriction;
    }
    
    /**
     * Creates a panel with the specified layout manager and with size
     * restricted in both directions.
     *
     * @param  layoutMgr  layout manager for this panel
     */
    public SizeRestrictedPanel(LayoutManager layoutMgr) {
        this(layoutMgr, true, true);
    }
    
    /**
     * Creates a panel with the specified layout manager and with width and/or
     * height restricted.
     *
     * @param  layoutMgr  layout manager for this panel
     * @param  widthRestriction  whether the panel's width should be restricted
     * @param  heightRestriction whether the panel's height should be restricted
     */
    public SizeRestrictedPanel(LayoutManager layoutMgr,
                               boolean widthRestriction,
                               boolean heightRestriction) {
        super(layoutMgr);
        this.widthRestriction = widthRestriction;
        this.heightRestriction = heightRestriction;
    }
    
    /**
     * Returns maximum size of this panel.
     * The maximum size can be restricted in width, height or in both
     * directions, depending on parameters passed to the constructor.
     *
     * @return  dimension returned from original <code>getMaximumSize()</code>
     *          and then modified according to restrictions specified
     *          by the constructor's parameters
     */
    public Dimension getMaximumSize() {
        if (widthRestriction && heightRestriction) {    //both true
            return getPreferredSize();
        }
        if (widthRestriction == heightRestriction) {    //both false
            return super.getMaximumSize();
        }
        
        Dimension maximumSize = super.getMaximumSize();
        if (widthRestriction) {
            maximumSize.width = getPreferredSize().width;
        } else {
            maximumSize.height = getPreferredSize().height;
        }
        return maximumSize;
    }
    
}
