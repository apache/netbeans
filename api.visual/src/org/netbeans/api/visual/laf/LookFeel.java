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
package org.netbeans.api.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

import java.awt.*;

/**
 * This class is defining a reusable LookAndFeel fragments e.g. colors.
 *
 * @author David Kaspar
 */
public abstract class LookFeel {

    private static final LookFeel DEFAULT = new DefaultLookFeel ();

    /**
     * Creates a default look and feel. The instance can be shared by multiple scenes.
     * @return the default look and feel
     */
    public static LookFeel createDefaultLookFeel () {
        return DEFAULT;
    }

    /**
     * Returns a default scene background
     * @return the default background
     */
    public abstract Paint getBackground ();

    /**
     * Returns a default scene foreground
     * @return the default foreground
     */
    public abstract Color getForeground ();

    /**
     * Returns a border for a specific state.
     * @param state the state
     * @return the border
     */
    public abstract Border getBorder (ObjectState state);

    /**
     * Returns a minimalistic version of border for a specific state.
     * @param state the state
     * @return the mini-border
     */
    public abstract Border getMiniBorder (ObjectState state);

    /**
     * Returns a opacity value for a specific state.
     * @param state the state
     * @return the opacity value
     */
    public abstract boolean getOpaque (ObjectState state);

    /**
     * Returns a line color for a specific state.
     * @param state the state
     * @return the line color
     */
    public abstract Color getLineColor (ObjectState state);

    /**
     * Returns a background for a specific state.
     * @param state the state
     * @return the background
     */
    public abstract Paint getBackground (ObjectState state);

    /**
     * Returns a foreground for a specific state.
     * @param state the state
     * @return the foreground
     */
    public abstract Color getForeground (ObjectState state);

    /**
     * Returns a margin for a specific state. It is used with borders - usually the value is a inset value of a border.
     * @return the margin
     */
    // TODO - is naming correct?
    public abstract int getMargin ();
/*
    public void updateWidget (Widget widget, WidgetState state) {
        widget.setBorder (getBorder (state));
        widget.setOpaque (getOpaque (state));
        widget.setBackground (getBackground (state));
        widget.setForeground (getForeground (state));
    }
*/
}
