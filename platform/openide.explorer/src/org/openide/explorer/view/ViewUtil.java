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

package org.openide.explorer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Utility class
 *
 * @author S. Aubrecht
 */
class ViewUtil {
    
    public static final boolean isAquaLaF =
            "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private static final boolean useDefaultBackground =
            Boolean.getBoolean("nb.explorerview.aqua.defaultbackground"); //NOI18N

    private static final RequestProcessor RP = new RequestProcessor("Explorer Views"); // NOI18N

    private ViewUtil() {
    }

    static RequestProcessor uiProcessor() {
        return RP;
    }

    /**
     * Change background of given component to light gray on Mac look and feel
     * when the component is in a tabbed container and its background hasn't been
     * already changed (is instance of UIResource).
     * @param c
     */
    static void adjustBackground( JComponent c ) {
        if( !isAquaLaF || useDefaultBackground )
            return;

        if( !isInTabbedContainer(c) )
            return;

        Color currentBackground = c.getBackground();
        if( currentBackground instanceof UIResource ) {
            c.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }


    private static boolean isInTabbedContainer( Component c ) {
        Component parent = c.getParent();
        while( null != parent ) {
            if( parent instanceof JComponent
                    && "TabbedContainerUI".equals( ((JComponent)parent).getUIClassID() ) ) //NOI18N
                return true;
            parent = parent.getParent();
        }
        return false;
    }

    static void nodeRename(final Node n, final String newStr) {
        // bugfix #21589 don't update name if there is not any change
        if (n.getName().equals(newStr)) {
            return;
        }
        if (EventQueue.isDispatchThread() && Boolean.TRUE.equals(n.getValue("slowRename"))) { // NOI18N
            RP.post(new Runnable() {
                @Override
                public void run() {
                    nodeRename(n, newStr);
                }
            });
            return;
        }
        try {
            n.setName(newStr);
        } catch (IllegalArgumentException exc) {
            boolean needToAnnotate = Exceptions.findLocalizedMessage(exc) == null;

            // annotate new localized message only if there is no localized message yet
            if (needToAnnotate) {
                String msg = NbBundle.getMessage(
                        TreeViewCellEditor.class, "RenameFailed", n.getName(), newStr
                    );
                Exceptions.attachLocalizedMessage(exc, msg);
            }

            Exceptions.printStackTrace(exc);
        }
    }

}
