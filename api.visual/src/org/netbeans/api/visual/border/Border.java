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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.visual.border;

import java.awt.*;

/**
 * The class is responsible for defining and rendering borders. Border size is defined by insets.
 * <p>
 * Borders can be opaque. If true, then the border has to care about painting all pixels in borders.
 * If false, then the widget background is painted under borders too.
 * <p>
 * This can be used for non-rectagular shapes of borders e.g. returning true from isOpaque and drawing a filled rounded rectangle.
 *
 * @author David Kaspar
 */
// TODO - change abstract class to assure immutable insets?
public interface Border {

    /**
     * Returns layout insets. Insets has to be the same during whole life-cycle of the border.
     * @return the insets
     */
    // WARNING - must be immutable during whole lifecycle
    public Insets getInsets ();

    /**
     * Paints the border to the Graphics2D instance within specific bounds.
     * Borders are always painted immediately after the widget background and before the widget painting itself.
     * @param gr the Graphics2D instance
     * @param bounds the boundary
     */
    public void paint (Graphics2D gr, Rectangle bounds);

    /**
     * Returns whether the border is opaque. The result of the method controls whether a widget background is painted
     * under the border insets too.
     * @return true, if background is painted under the border insets.
     */
    public boolean isOpaque ();

}
