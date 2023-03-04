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
package org.openide.awt;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.JComponent;

public final class GraphicsUtils {
    private GraphicsUtils() {
    }

    private static final boolean antialias =
        // System property to automatically turn on antialiasing for html strings
        Boolean.getBoolean("nb.cellrenderer.antialiasing") // NOI18N
         ||Boolean.getBoolean("swing.aatext") // NOI18N
         ||(isGTK() && gtkShouldAntialias()) // NOI18N
         || isAqua();
    private static Boolean gtkAA;
    private static Map<Object,Object> hintsMap;

    private static boolean isAqua () {
        return "Aqua".equals(UIManager.getLookAndFeel().getID());
    }

    private static boolean isGTK () {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }

    private static final boolean gtkShouldAntialias() {
        if (gtkAA == null) {
            Object o = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/Antialias"); //NOI18N
            gtkAA = Integer.valueOf(1).equals(o);
        }

        return gtkAA.booleanValue();
    }

    /**
     * Configure default IDE-wide rendering hints on the supplied {@link Graphics} object. This
     * enables anti-aliasing of manually painted text and 2D graphics, using settings that are
     * consistent throughout the IDE. This method is typically called at the beginning of custom
     * {@link JComponent#paint(Graphics)} implementations. By convention, callers passing the
     * {@code Graphics} object from {@code paint} do not need to bother restoring the old rendering
     * hints after they are done using the {@code Graphics} object.
     */
    public static void configureDefaultRenderingHints(Graphics graphics) {
        if (graphics == null) {
            throw new NullPointerException();
        }
        if (graphics instanceof Graphics2D) {
            ((Graphics2D) graphics).addRenderingHints(getRenderingHints());
        }
    }

    @SuppressWarnings("unchecked")
    private static final Map<?,?> getRenderingHints() {
        Map<Object,Object> ret = hintsMap;
        if (ret == null) {
            //Thanks to Phil Race for making this possible
            ret = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (ret == null) {
                ret = new HashMap<Object,Object>();
                if (antialias) {
                    ret.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
            if (antialias ||
                !RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(ret.get(RenderingHints.KEY_TEXT_ANTIALIASING)))
            {
                // Required to get non-text antialiasing on Windows.
                ret.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            hintsMap = Collections.unmodifiableMap(ret);
        }
        return ret;
    }
}
