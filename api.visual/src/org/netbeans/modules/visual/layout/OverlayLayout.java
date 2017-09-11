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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class OverlayLayout implements Layout {

    public void layout (Widget widget) {
        Dimension total = new Dimension ();
        for (Widget child : widget.getChildren ()) {
            if (! child.isVisible ())
                continue;
            Dimension size = child.getPreferredBounds ().getSize ();
            if (size.width > total.width)
                total.width = size.width;
            if (size.height > total.height)
                total.height = size.height;
        }
        for (Widget child : widget.getChildren ()) {
            Point location = child.getPreferredBounds ().getLocation ();
            child.resolveBounds (new Point (- location.x, - location.y), new Rectangle (location, total));
        }
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        Rectangle clientArea = widget.getClientArea ();
        for (Widget child : widget.getChildren ()) {
            if (child.isVisible ()) {
                Point location = child.getPreferredBounds ().getLocation ();
                child.resolveBounds (new Point (clientArea.x - location.x, clientArea.y - location.y), new Rectangle (location, clientArea.getSize ()));
            } else {
                child.resolveBounds (clientArea.getLocation (), new Rectangle ());
            }
        }
    }

}
