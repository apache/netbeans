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

import java.awt.Dimension;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.plaf.SeparatorUI;

/**
 * JSeparator applying a workaround for bad separator appearance on Mac OS X
 * broken by the NetBeans Platform.
 *
 * @author Jiri Sedlacek
 */
final class Separator extends JSeparator {

    private static final String SEPARATOR_UI = "SeparatorUI"; // NOI18N
    private static final String MAC_OS_X_SEPARATOR_UI =
            "com.apple.laf.AquaPopupMenuSeparatorUI"; // NOI18N
    private static final String MAC_OS_X_SEPARATOR_UI_NB =
            "org.netbeans.swing.plaf.aqua.AquaSeparatorUI"; // NOI18N
    private static Class<SeparatorUI> MAC_OS_X_SEPARATOR_UI_CLASS;
    private static final String MAC_OS_X_SEPARATOR_COLOR_KEY =
            "InternalFrame.inactiveTitleForeground"; // NOI18N

    private boolean separatorUIInitialized = false;
    private SeparatorUI macOsXSeparatorUI;


    static {
        if (MAC_OS_X_SEPARATOR_UI_NB.
                equals(UIManager.getDefaults().get(SEPARATOR_UI))) {
            try {
                MAC_OS_X_SEPARATOR_UI_CLASS =
                        (Class<SeparatorUI>)Class.forName(MAC_OS_X_SEPARATOR_UI);
            } catch (Throwable e) {
                MAC_OS_X_SEPARATOR_UI_CLASS = null;
            }
        }
    }


    public Separator() {
        super();
    }

    public Separator(int orientation) {
        super(orientation);
    }

    
    public void setUI(SeparatorUI ui) {
        synchronized(this) {
            if (!separatorUIInitialized) {
                macOsXSeparatorUI = createCustomUI(this);
                separatorUIInitialized = true;
            }
        }

        if (macOsXSeparatorUI == null) {
            super.setUI(ui);
        } else {
            super.setUI(macOsXSeparatorUI);
            setForeground(UIManager.getColor(MAC_OS_X_SEPARATOR_COLOR_KEY));
        }
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }


    private static SeparatorUI createCustomUI(JComponent separator) {
        if (MAC_OS_X_SEPARATOR_UI_CLASS != null) {
            try {
                Method m = MAC_OS_X_SEPARATOR_UI_CLASS.getDeclaredMethod(
                           "createUI", JComponent.class); // NOI18N
                return (SeparatorUI)m.invoke(null, separator);
            } catch (Throwable e) {
                return null;
            }
        } else {
            return null;
        }
    }

}
