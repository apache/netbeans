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

package org.netbeans.modules.versioning.util;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Panel that, when asked to do so, resizes the windows ancestor
 * ({@code JDialog}, {@code JFrame}, {@code JWindow}) such that it fits
 * this panel at its preferred size.
 * To trigger the resize operation, call method {@link #resizeAsNecessary}.
 *
 * @author Marian Petras
 */
public class AutoResizingPanel extends JPanel {

    private Dimension requestedSize;

    public AutoResizingPanel() {
        super();
    }

    public AutoResizingPanel(LayoutManager layout) {
        super(layout);
    }

    public AutoResizingPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public AutoResizingPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public void enlargeHorizontallyAsNecessary() {
        int currWidth = getWidth();
        int currHeight = getHeight();
        Dimension prefSize = getPreferredSize();
        enlargeAsNecessary(currWidth,
                           currHeight,
                           Math.max(currWidth, prefSize.width),
                           currHeight);
    }

    public void enlargeVerticallyAsNecessary() {
        int currWidth = getWidth();
        int currHeight = getHeight();
        Dimension prefSize = getPreferredSize();
        enlargeAsNecessary(currWidth,
                           currHeight,
                           currWidth,
                           Math.max(currHeight, prefSize.height));
    }

    public void enlargeAsNecessary() {
        int currWidth = getWidth();
        int currHeight = getHeight();
        Dimension prefSize = getPreferredSize();
        enlargeAsNecessary(currWidth,
                           currHeight,
                           Math.max(currWidth, prefSize.width),
                           Math.max(currHeight, prefSize.height));
    }

    private void enlargeAsNecessary(int currentWidth,
                                    int currentHeight,
                                    int requestedWidth,
                                    int requestedHeight) {
        if ((currentWidth >= requestedWidth) && (currentHeight >= requestedHeight)) {
            /* the panel is large enough */
            return;
        }

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window == null) {
            return;
        }

        try {
            requestedSize = new Dimension(requestedWidth, requestedHeight);
            window.pack();
        } finally {
            requestedSize = null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return (requestedSize != null) ? requestedSize
                                       : super.getPreferredSize();
    }

}
