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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 * Use only for creating ScrollBars which mimic insets of JScrollPane's SBs.
 *
 * @author Jiri Sedlacek
 */
public class ScrollBar extends JScrollBar {

    public ScrollBar(int orientation) {
        super(orientation);

        if (UIUtils.isGTKLookAndFeel()) {
            Insets insets = getBorder().getBorderInsets(this);
            // Typically the insets are 2 for GTK themes except for Nimbus theme
            // which uses 3 and requires 1 (other themes seem to require 0). Lets
            // lower the insets to mimic JScrollBars used in JScrollPanes.
            setBorder(BorderFactory.createEmptyBorder(Math.max(insets.top - 2, 0),
                                                    Math.max(insets.left - 2, 0),
                                                    Math.max(insets.bottom - 2, 0),
                                                    Math.max(insets.right - 2, 0)
                                                   ));
        }
    }

}
