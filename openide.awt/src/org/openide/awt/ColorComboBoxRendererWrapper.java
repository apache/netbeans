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

package org.openide.awt;

import java.awt.Component;
import javax.swing.*;
import javax.swing.plaf.UIResource;


/**
 * Renderer for color JComboBox.
 *
 * @author S. Aubrecht
 */
class ColorComboBoxRendererWrapper implements ListCellRenderer, UIResource {

    private final ListCellRenderer renderer;
    private static final boolean isGTK = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    ColorComboBoxRendererWrapper (JComboBox comboBox) {
        this.renderer = comboBox.getRenderer();
        if( renderer instanceof ColorComboBoxRendererWrapper ) {
            throw new IllegalStateException("Custom renderer is already initialized."); //NOI18N
        }
        comboBox.setRenderer( this );
    }

    @Override
    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
        Component res = renderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
        if( res instanceof JLabel ) {
            synchronized( renderer ) {
                JLabel label = ( JLabel ) res;
                int height = isGTK ? 10 : Math.max( res.getPreferredSize().height - 4, 4 );
                Icon icon = null;
                if( value instanceof ColorValue ) {
                    ColorValue color = ( ColorValue ) value;
                    if( value == ColorValue.CUSTOM_COLOR ) {
                        icon = null;
                    } else {
                        icon = new ColorIcon( color.color, height );
                    }
                    label.setText( color.text );
                } else {
                    icon = null;
                }
                label.setIcon( icon );
            }
        }
        return res;
    }


}