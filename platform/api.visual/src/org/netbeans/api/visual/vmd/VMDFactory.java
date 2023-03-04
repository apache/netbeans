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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.border.Border;
import org.netbeans.modules.visual.vmd.VMDNetBeans60ColorScheme;
import org.netbeans.modules.visual.vmd.VMDOriginalColorScheme;

import java.awt.*;

/**
 * Used as a factory class for objects defined in VMD visualization style.
 *
 * @author David Kaspar
 */
public final class VMDFactory {

    private static VMDColorScheme SCHEME_ORIGINAL = new VMDOriginalColorScheme ();
    private static VMDColorScheme SCHEME_NB60 = new VMDNetBeans60ColorScheme ();

    private VMDFactory () {
    }

    /**
     * Creates the original vmd color scheme. Used by default.
     * @return the color scheme
     * @since 2.5
     */
    public static VMDColorScheme getOriginalScheme () {
        return SCHEME_ORIGINAL;
    }

    /**
     * Creates the NetBeans 6.0 vmd color scheme.
     * @return the color scheme
     * @since 2.5
     */
    public static VMDColorScheme getNetBeans60Scheme () {
        return SCHEME_NB60;
    }

    /**
     * Creates a border used by VMD node.
     * @return the VMD node border
     */
    public static Border createVMDNodeBorder () {
        return VMDOriginalColorScheme.BORDER_NODE;
    }

    /**
     * Creates a border used by VMD node with a specific colors.
     * @return the VMD node border
     * @param borderColor the border color
     * @param borderThickness the border thickness
     * @param color1 1. color of gradient background
     * @param color2 2. color of gradient background
     * @param color3 3. color of gradient background
     * @param color4 4. color of gradient background
     * @param color5 5. color of gradient background
     * @since 2.5
     */
    public static Border createVMDNodeBorder (Color borderColor, int borderThickness, Color color1, Color color2, Color color3, Color color4, Color color5) {
        return new VMDNodeBorder (borderColor, borderThickness, color1, color2, color3, color4, color5);
    }

}
