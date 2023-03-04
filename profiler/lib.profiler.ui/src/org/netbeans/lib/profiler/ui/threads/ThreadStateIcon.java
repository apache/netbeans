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

package org.netbeans.lib.profiler.ui.threads;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import org.netbeans.lib.profiler.results.threads.ThreadData;
import javax.swing.Icon;


/**
 * @author Jiri Sedlacek
 */
public class ThreadStateIcon implements Icon {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int ICON_NONE = -100;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected Color threadStateColor;
    protected int height;
    protected int width;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ThreadStateIcon(int threadState, int width, int height) {
        this.threadStateColor = getThreadStateColor(threadState);
        this.width = width;
        this.height = height;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getIconHeight() {
        return height;
    }

    public int getIconWidth() {
        return width;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (threadStateColor != null) {
            g.setColor(threadStateColor);
            g.fillRect(x + 1, y + 1, width - 1, height - 1);
        }
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width - 1, height - 1);
    }

    protected Color getThreadStateColor(int threadState) {
        if (threadState == ICON_NONE) return null;
        return ThreadData.getThreadStateColor(threadState);
    }
}
