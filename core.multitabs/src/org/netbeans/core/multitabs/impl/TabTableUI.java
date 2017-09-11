/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.multitabs.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;

/**
 *
 * @author S. Aubrecht
 */
final class TabTableUI extends BasicTableUI {

    static final boolean IS_AQUA = "Aqua".equals( UIManager.getLookAndFeel().getID() );
    
    static Border createTabBorder( JTable table, int tabsLocation ) {
        if( IS_AQUA ) {
            return BorderFactory.createMatteBorder( 1, 0, 0, 0, table.getGridColor());
        } else {
            if( tabsLocation != JTabbedPane.TOP )
                return BorderFactory.createMatteBorder( 1, 0, 0, 0, table.getGridColor());
        }
        return BorderFactory.createEmptyBorder();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults(); //To change body of generated methods, choose Tools | Templates.
        String lafId = UIManager.getLookAndFeel().getID();
        if( "Windows".equals( lafId ) ) { //NOI18N
            Color background = UIManager.getColor( "TabbedPane.background"); //NOI18N
            Color highglightBackground = UIManager.getColor( "TabbedPane.highlight" ); //NOI18N
            if( null != background && null != highglightBackground ) {
                final int factor = 16;
                table.setBackground( new Color( Math.max( background.getRed()-factor, 0),
                        Math.max( background.getGreen()-factor, 0 ),
                        Math.max( background.getBlue()-factor, 0 ) ) );
                table.setSelectionBackground( highglightBackground );
                table.setSelectionForeground( table.getForeground() );
            }
        } else if( "Metal".equals( lafId ) ) { //NOI18N
            Color background = UIManager.getColor( "inactiveCaption"); //NOI18N
            Color highglightBackground = UIManager.getColor( "activeCaption" ); //NOI18N
            if( null != background && null != highglightBackground ) {
                table.setBackground( background );
                table.setSelectionBackground( highglightBackground );
                table.setSelectionForeground( table.getForeground() );
            }
        } else if( "Nimbus".equals( lafId ) || "GTK".equals( lafId ) ) { //NOI18N
            Color highglightBackground = UIManager.getColor( "TabbedPane.highlight"); //NOI18N
            Color background = UIManager.getColor( "TabbedPane.background" ); //NOI18N
            if( null != background && null != highglightBackground ) {
                table.setBackground( new Color(background.getRGB()) );
                table.setSelectionBackground( new Color(highglightBackground.getRGB()) );
                table.setSelectionForeground( new Color(table.getForeground().getRGB()) );
                Color grid = UIManager.getColor( "InternalFrame.borderShadow" );//NOI18N
                if( null == grid )
                    grid = UIManager.getColor( "controlDkShadow");//NOI18N
                if( null != grid ) {
                    table.setGridColor( new Color(grid.getRGB()));
                }
            }
            table.setShowGrid( true );
        } else if( "Aqua".equals( lafId ) ) { //NOI18N
            table.setShowGrid( true );
            table.setBackground( new Color(178,178,178) );
            table.setSelectionBackground( new Color(226,226,226) );
            table.setSelectionForeground( table.getForeground() );
            table.setGridColor( new Color(49,49,49) );
            Font txtFont = (Font) UIManager.get("windowTitleFont"); //NOI18N
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11); //NOI18N
            } else if (txtFont.isBold()) {
                // don't use deriveFont() - see #49973 for details
                txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
            }
            table.setFont( txtFont );
        }
    }

    @Override
    protected void installKeyboardActions() {
        //no keyboard actions
    }

    @Override
    protected MouseInputListener createMouseInputListener() {
        final MouseInputListener orig = super.createMouseInputListener();
        return new MouseInputListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                orig.mouseClicked( e );
            }

            @Override
            public void mousePressed( MouseEvent e ) {
                TabTable tabTable = ( TabTable ) table;
                Point p = e.getPoint();
                int row = table.rowAtPoint( p );
                int col = table.columnAtPoint( p );
                if( row >= 0 && col >= 0 ) {
                    if( tabTable.isCloseButtonHighlighted( row, col ) ) {
                        return;
                    }
                }
                orig.mousePressed( e );
            }

            @Override
            public void mouseReleased( MouseEvent e ) {
                orig.mouseReleased( e );
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                orig.mouseEntered( e );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                orig.mouseExited( e );
            }

            @Override
            public void mouseDragged( MouseEvent e ) {
                orig.mouseDragged( e );
            }

            @Override
            public void mouseMoved( MouseEvent e ) {
                orig.mouseMoved( e );
            }
        };
    }
    
}
