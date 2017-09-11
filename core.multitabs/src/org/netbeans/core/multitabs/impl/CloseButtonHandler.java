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
package org.netbeans.core.multitabs.impl;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.TableCellRenderer;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

/**
 * Highlights close button in table cell on mouse-over and removes a tab when
 * its close button is clicked.
 * 
 * @author S. Aubrecht
 */
class CloseButtonHandler extends MouseAdapter {

    private final TabDisplayer displayer;
    private final Controller controller;

    public CloseButtonHandler( TabDisplayer displayer, Controller controller ) {
        this.controller = controller;
        this.displayer = displayer;
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if( e.getSource() instanceof TabTable ) {
            TabTable table = ( TabTable ) e.getSource();
            if( !table.isShowing() )
                return;
            Point p = e.getPoint();
            int row = table.rowAtPoint( p );
            int col = table.columnAtPoint( p );
            if( row >= 0 && col >= 0 ) {
                if( table.isCloseButtonHighlighted( row, col ) ) {
                    TabData tab = table.getTabAt( p );
                    if( null != tab ) {
                        int tabIndex = displayer.getModel().indexOf( tab );
                        if( tabIndex >= 0 && e.getButton() == MouseEvent.BUTTON1 ) {
                            TabActionEvent tae = null;
                            if( (e.getModifiersEx()& MouseEvent.SHIFT_DOWN_MASK) > 0 ) {
                                tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE_ALL, tabIndex );
                            } else if( (e.getModifiersEx()& MouseEvent.ALT_DOWN_MASK) > 0  ) {
                                tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS, tabIndex );
                            } else {
                                tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE, tabIndex );
                            }
                            if( null != tae )
                                controller.postActionEvent( tae );
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
        mouseMoved( e );
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        mouseMoved( e );
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        if( e.getSource() instanceof TabTable ) {
            int closeButtonRow = -1;
            int closeButtonColumn = -1;
            TabTable table = ( TabTable ) e.getSource();
            Point p = e.getPoint();
            int row = table.rowAtPoint( p );
            int col = table.columnAtPoint( p );
            if( row >= 0 && col >= 0 ) {
                TableCellRenderer ren = table.getCellRenderer( row, col );
                if( ren instanceof TabDataRenderer ) {
                    TabDataRenderer tabRenderer = ( TabDataRenderer ) ren;
                    if( tabRenderer.isInCloseButton( table.getCellRect( row, col, true ), p ) ) {
                        closeButtonRow = row;
                        closeButtonColumn = col;
                    }
                }
            }
            table.setCurrentCloseButtonCoords( closeButtonRow, closeButtonColumn );
        }
    }

}
