/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/*
 * TabPainter.java
 *
 * Created on December 3, 2003, 4:22 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * An extention to Border which can provide a non-rectangular interior region
 * that will contain the tab's content, and actually paint that interior. The
 * goal of this class is to make it extremely easy to plug in different painting
 * logic without having to write an entire UI delegate.
 *
 * @author Tim Boudreau
 */
public interface TabPainter extends Border {

    /**
     * Get the polygon representing the tag.  Clicks outside this polygon inside
     * the tab's rectangle will be ignored. This polygon makes up the bounds of
     * the tab. <p><code>AbstractTabsUI</code> contains generic support for
     * drawing drag and drop target indications.  If want to use it rather than
     * write your own, you need to specify the polygon returned by this method
     * with the following point order:  The last two points in the point array
     * of the polygon <strong>must be the bottom left corner, followed by the
     * bottom right corner</strong>.  In other words, start at the upper left
     * corner when constructing the polygon, and end at the bottom right corner,
     * using no more than one point for the bottom left and right corners:
     * <pre>
     * start here -->    /---------
     *                            |
     * finish here -->   ----------
     * </pre>
     */
    Polygon getInteriorPolygon(Component renderer);

    /**
     * Paint the interior (as defined by getInteriorPolygon()) as appropriate
     * for the tab.  Implementations will presumably use different colors to
     * manage selection, activated state, etc.
     */
    void paintInterior(Graphics g, Component renderer);

    /**
     * Get the close button rectangle for this tab. May contain no implementation 
     * if supportsCloseButton() returns false."
     *
     * @param jc     The current renderer
     * @param rect   A rectangle that should be configured with the close button
     *               bounds
     * @param bounds The bounds relative to which the close button rectangle
     *               should be determined
     */
    void getCloseButtonRectangle(JComponent jc, Rectangle rect,
                                 Rectangle bounds);

    /** 
     * Returns true if close button is supported, false otherwise.
     * 
     * @param renderer     The current renderer
     * @return true if close button is supported, false otherwise
     */
    boolean supportsCloseButton(JComponent renderer);
}
