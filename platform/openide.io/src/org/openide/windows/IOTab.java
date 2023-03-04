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

package org.openide.windows;

import javax.swing.Icon;
import org.openide.util.Lookup;

/**
 * Settings of tool tip/icon for IO component (tab).
 * <p>
 * Client usage:
 * <pre>
 *  // settings of IO tab icon, tooltip
 *  InputOutput io = ...;
 *  Icon icon = ...;
 *  IOTab.setIcon(io, icon);
 *  IOTab.setToolTipText(io, "text");
 * </pre>
 * How to support {@link IOTab} in own {@link IOProvider} implementation:
 * <ul>
 *   <li> {@link InputOutput} provided by {@link IOProvider} has to implement {@link org.openide.util.Lookup.Provider}
 *   <li> Extend {@link IOTab} and implement its abstract methods
 *   <li> Place instance of {@link IOTab} to {@link Lookup} provided by {@link InputOutput}
 * </ul>
 * @since 1.15
 * @author Tomas Holy
 */
public abstract class IOTab {
    private static IOTab find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOTab.class);
        }
        return null;
    }

    /**
     * Gets current tab icon for specified IO
     * @param io IO to operate on
     * @return current tab icon or null if not supported
     */
    public static Icon getIcon(InputOutput io) {
        IOTab iot = find(io);
        return iot != null ? iot.getIcon() : null;
    }

    /**
     * Sets icon to tab corresponding to specified IO
     * @param io IO to operate on
     * @param icon tab icon
     */
    public static void setIcon(InputOutput io, Icon icon) {
        IOTab iot = find(io);
        if (iot != null) {
            iot.setIcon(icon);
        }
    }

    /**
     * Gets current tool tip text for specified IO
     * @param io IO to operate on
     * @return current tool tip text or null if not supported
     */
    public static String getToolTipText(InputOutput io) {
        IOTab iot = find(io);
        return iot != null ? iot.getToolTipText() : null;
    }

    /**
     * Sets tool tip text to tab corresponding to specified IO
     * @param io IO to operate on
     * @param text new tool tip text
     */
    public static void setToolTipText(InputOutput io, String text) {
        IOTab iot = find(io);
        if (iot != null) {
            iot.setToolTipText(text);
        }
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    /**
     * Gets current tab icon
     * @return current tab icon
     */
    protected abstract Icon getIcon();


    /**
     * Sets icon to tab
     * @param icon tab icon
     */
    protected abstract void setIcon(Icon icon);

    /**
     * Gets current tool tip text
     * @return current tool tip text
     */
    protected abstract String getToolTipText();

    /**
     * Sets tool tip text to tab
     * @param text new tool tip text
     */
    protected abstract void setToolTipText(String text);
}
