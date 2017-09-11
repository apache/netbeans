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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
