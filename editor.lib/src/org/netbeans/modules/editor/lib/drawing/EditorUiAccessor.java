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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Map;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;

/**
 *
 * @author vita
 */
public abstract class EditorUiAccessor {

    private static EditorUiAccessor ACCESSOR = null;

    public static synchronized void register(EditorUiAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized EditorUiAccessor get() {
        // Trying to wake up EditorUI ...
        try {
            Class clazz = Class.forName(EditorUI.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    protected EditorUiAccessor() {
    }

    public abstract boolean isLineNumberVisible(EditorUI eui);
    public abstract Coloring getColoring(EditorUI eui, String coloringName);
    public abstract int getLineNumberMaxDigitCount(EditorUI eui);
    public abstract int getLineNumberWidth(EditorUI eui);
    public abstract int getLineNumberDigitWidth(EditorUI eui);
    public abstract Insets getLineNumberMargin(EditorUI eui);
    public abstract int getLineHeight(EditorUI eui);
    public abstract Coloring getDefaultColoring(EditorUI eui);
    public abstract int getDefaultSpaceWidth(EditorUI eui);
    public abstract Map<?, ?> getRenderingHints(EditorUI eui);
    public abstract Rectangle getExtentBounds(EditorUI eui);
    public abstract Insets getTextMargin(EditorUI eui);
    public abstract int getTextLeftMarginWidth(EditorUI eui);
    public abstract boolean getTextLimitLineVisible(EditorUI eui);
    public abstract Color getTextLimitLineColor(EditorUI eui);
    public abstract int getTextLimitWidth(EditorUI eui);
    public abstract int getLineAscent(EditorUI eui);
    public abstract void paint(EditorUI eui, Graphics g);
    public abstract DrawLayerList getDrawLayerList(EditorUI eui);

}
