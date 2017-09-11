/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    abstract protected Icon getIcon();


    /**
     * Sets icon to tab
     * @param icon tab icon
     */
    abstract protected void setIcon(Icon icon);

    /**
     * Gets current tool tip text
     * @return current tool tip text
     */
    abstract protected String getToolTipText();

    /**
     * Sets tool tip text to tab
     * @param text new tool tip text
     */
    abstract protected void setToolTipText(String text);
}
