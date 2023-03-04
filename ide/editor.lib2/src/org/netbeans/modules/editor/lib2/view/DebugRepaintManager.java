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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Rectangle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.openide.util.WeakSet;

/**
 * Repaint manager showing repaints in a particular component.
 *
 * @author Miloslav Metelka
 */

final class DebugRepaintManager extends RepaintManager {
    
    private static final DebugRepaintManager INSTANCE = new DebugRepaintManager();

    public static void register(JComponent component) {
        if (RepaintManager.currentManager(component) != INSTANCE) {
            RepaintManager.setCurrentManager(INSTANCE);
        }
        INSTANCE.addLogComponent(component);
    }

    private final Set<JComponent> logComponents = new WeakSet<JComponent>();
    

    private DebugRepaintManager() {
    }

    public void addLogComponent(JComponent component) {
        logComponents.add(component);
    }

    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        for (JComponent dc : logComponents) {
            if (SwingUtilities.isDescendingFrom(dc, c)) {
                String boundsMsg = ViewUtils.toString(new Rectangle(x, y, w, h));
                ViewHierarchyImpl.REPAINT_LOG.log(Level.FINER,
                        "Component-REPAINT: " + boundsMsg + // NOI18N
                        " c:" + ViewUtils.toString(c), // NOI18N
                        new Exception("Component-Repaint of " + boundsMsg + " cause:")); // NOI18N
                break;
            }
        }

        super.addDirtyRegion(c, x, y, w, h);
    }

}
