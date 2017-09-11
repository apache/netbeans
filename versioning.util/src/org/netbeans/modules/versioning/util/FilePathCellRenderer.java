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

package org.netbeans.modules.versioning.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * Treats values in cells as file paths and renders them so that the end of the path is always visible.
 *
 * @author Maros Sandor
 */
public class FilePathCellRenderer extends DefaultTableCellRenderer {

    private static final int VISIBLE_START_CHARS = 0;

    private String computeFitText(String text) {
        if (text == null || text.length() <= VISIBLE_START_CHARS + 3) return text;

        FontMetrics fm = getFontMetrics(getFont());
        int width = getSize().width;
            
        String prefix = text.substring(0, VISIBLE_START_CHARS) + "...";
        int prefixLength = fm.stringWidth(prefix);
        int desired = width - prefixLength - 2;
        if (desired <= 0) return text;
        
        for (int i = text.length() - 1; i >= 0; i--) {
            String suffix = text.substring(i);
            int swidth = fm.stringWidth(suffix);
            if (swidth >= desired) {
                return suffix.length() > 0 ? prefix + suffix.substring(1) : text;
            }
        }
        return text;
    }
    
    protected void paintComponent(Graphics g) {
        setText(computeFitText(getText()));
        super.paintComponent(g);
    }
}
