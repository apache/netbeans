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
package org.netbeans.swing.outline;

import java.awt.Color;
import javax.swing.Icon;

/** A class which can provide rendering data for the tree portion an Outline,
 * such as converting values to text, providing tooltip text and icons.
 * Makes it possible to provide most of the interesting data that affects
 * display without needing to provide a custom cell renderer.
 *
 * @author  Tim Boudreau
 */
public interface RenderDataProvider {
    /** Convert an object in the tree to the string that should be used to
     * display its node */
    public String getDisplayName (Object o);
    /** Returns true of the display name for this object should use HTML 
     * rendering (future support for integration of the lightweight HTML
     * renderer into NetBeans).  */
    public boolean isHtmlDisplayName (Object o);
    /** Get the background color to be used for rendering this node.  Return
     * null if the standard table background or selected color should be used.
     */
    public Color getBackground (Object o);
    /** Get the foreground color to be used for rendering this node.  Return
     * null if the standard table foreground or selected foreground should be
     * used. */
    public Color getForeground (Object o);
    /** Get a description for this object suitable for use in a tooltip.  Return
     * null if no tooltip is desired.  */
    public String getTooltipText (Object o);
    /** Get an icon to be used for this object.  Return null if the look and 
     * feel's default tree folder/leaf icons should be used as appropriate. */
    public Icon getIcon (Object o);
}
