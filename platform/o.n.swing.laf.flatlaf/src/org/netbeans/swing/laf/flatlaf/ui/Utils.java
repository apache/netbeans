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
package org.netbeans.swing.laf.flatlaf.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.UIManager;

class Utils {

    static final boolean MAC_USE_QUARTZ = Boolean.getBoolean( "apple.awt.graphics.UseQuartz" );

    static Color getUIColor(String key, Color defaultColor) {
        Color color = UIManager.getColor(key);
        return (color != null) ? color : defaultColor;
    }

    static Color getUIColor(String key, String defaultKey) {
        Color color = UIManager.getColor(key);
        return (color != null) ? color : UIManager.getColor(defaultKey);
    }

    /**
     * Sets rendering hints used for painting.
     */
    static void setRenderingHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                MAC_USE_QUARTZ ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
    }
}
