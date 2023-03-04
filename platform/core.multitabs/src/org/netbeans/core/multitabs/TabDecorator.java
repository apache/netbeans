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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.Icon;
import org.netbeans.swing.tabcontrol.TabData;

/**
 * Register implementations of this class in global Lookup to adjust tab rendering.
 * 
 * @author S. Aubrecht
 */
public abstract class TabDecorator {

    /**
     *
     * @param tab
     * @return Text to use as tab title or null to use default value.
     */
    public String getText( TabData tab ) {
        return null;
    }

    /**
     *
     * @param tab
     * @return Icon for the given tab or null to use default value.
     */
    public Icon getIcon( TabData tab ) {
        return null;
    }

    /**
     *
     * @param tab
     * @param selected
     * @return Background color for the given tab or null to use default value.
     */
    public Color getBackground( TabData tab, boolean selected ) {
        return null;
    }

    /**
     * 
     * @param tab
     * @param selected
     * @return Foreground color for the given tab or null to use default value.
     */
    public Color getForeground( TabData tab, boolean selected ) {
        return null;
    }

    /**
     * Perform any additional painting into tab's rectangle.
     * @param tab
     * @param g
     * @param tabRect 
     */
    public void paintAfter( TabData tab, Graphics g, Rectangle tabRect, boolean isSelected ) {
    }
}
