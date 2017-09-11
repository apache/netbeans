/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.multitabs.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import org.netbeans.core.multitabs.Controller;
import org.netbeans.core.multitabs.Settings;
import org.netbeans.core.multitabs.impl.ProjectSupport.ProjectProxy;
import org.netbeans.swing.popupswitcher.SwitcherTable;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.awt.CloseButtonFactory;

/**
 * Slightly enhanced switcher table which adds close button to selected item
 * and also shows tooltips.
 *
 * (copied from o.n.swing.tabcontrol module)
 *
 * @author S. Aubrecht
 */
class DocumentSwitcherTable extends SwitcherTable {

    private final JButton btnClose;
    private final Controller controller;
    private final ProjectColorTabDecorator decorator;
    private final ItemBorder ITEM_BORDER = new ItemBorder();
    private final Border SEPARATOR_BORDER = BorderFactory.createEmptyBorder( 2, 2, 0, 5 );

    public DocumentSwitcherTable( Controller controller, SwitcherTableItem[] items, int y ) {
        super( items, y );
        this.controller = controller;
        btnClose = createCloseButton();
        if( Settings.getDefault().isSameProjectSameColor() ) {
            decorator = new ProjectColorTabDecorator();
        } else {
            decorator = null;
        }
        ToolTipManager.sharedInstance().registerComponent( this );
    }

    @Override
    public SwitcherTableItem getSelectedItem() {
        Item res = ( Item ) getValueAt(getSelectedRow(), getSelectedColumn());
        if( null != res && res.isSeparator )
            return null; //don't hide the popup when a project separator is selected
        return res;
    }

    @Override
    public Component prepareRenderer( TableCellRenderer renderer, int row, int column ) {
        Item item = (Item) getModel().getValueAt(row, column);

        boolean selected = row == getSelectedRow() &&
                column == getSelectedColumn() && item != null;
        boolean separator = null != item && item.isSeparator;

        ITEM_BORDER.color = null;
        Component renComponent = super.prepareRenderer( renderer, row, column );
        JLabel lbl = null;
        if( renComponent instanceof JLabel ) {
            lbl = ( JLabel ) renComponent;
            if( separator ) {
                lbl.setBorder( SEPARATOR_BORDER );
                lbl.setIcon( null );
                lbl.setText( item.getHtmlName() );
            } else {
                lbl.setBorder( ITEM_BORDER );
            }
        }
        if( selected && !separator ) {
            JPanel res = new JPanel( new BorderLayout(5, 0) );
            res.add( renComponent, BorderLayout.CENTER );
            if( TabDataRenderer.isClosable( item.getTabData() ) ) {
                res.add( btnClose, BorderLayout.EAST );
            }
            res.setBackground( renComponent.getBackground() );
            return res;
        }
        if( null != decorator && null != item && !selected ) {
            TabData tab = item.getTabData();
            if( null != tab ) {
                ITEM_BORDER.color = decorator.getBackground( tab, selected);
            }
        }
        return renComponent;
    }

    private int lastRow = -1;
    private int lastCol = -1;
    private boolean inCloseButtonRect = false;

    boolean onMouseEvent( MouseEvent e ) {
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint((Component) e.getSource(), p, this);
        int selRow = getSelectedRow();
        int selCol = getSelectedColumn();
        if( selRow < 0 || selCol < 0 )
            return false;
        Rectangle rect = getCellRect( selRow, selCol, false );
        if( rect.contains( p ) ) {
            Dimension size = btnClose.getPreferredSize();
            int x = rect.x+rect.width-size.width;
            int y = rect.y + (rect.height-size.height)/2;
            Rectangle btnRect = new Rectangle( x, y, size.width, size.height);
            boolean inButton = btnRect.contains( p );
            boolean mustRepaint = inCloseButtonRect != inButton;
            inCloseButtonRect = inButton;
            if( inButton ) {
                if( e.getID() == MouseEvent.MOUSE_PRESSED ) {
                    Item item = ( Item ) getModel().getValueAt( selRow, selCol );
                    TabData tab = item.getTabData();
                    int tabIndex = controller.getTabModel().indexOf( tab );
                    if( tabIndex >= 0 ) {
                        TabActionEvent tae = new TabActionEvent( this, TabbedContainer.COMMAND_CLOSE, tabIndex );
                        controller.postActionEvent( tae );
                        return true;
                    }
                }
            }
            if( mustRepaint && lastRow == selRow && lastCol == selCol )
                repaint( btnRect );
            lastCol = selCol;
            lastRow = selRow;
            return inButton;
        }
        return false;
    }

    @Override
    public String getToolTipText( MouseEvent event ) {
        int row = rowAtPoint( event.getPoint() );
        int col = columnAtPoint( event.getPoint() );
        if( row >= 0 && col <= 0 ) {
            SwitcherTableItem item = ( SwitcherTableItem ) getModel().getValueAt( row, col );
            return item.getDescription();
        }
        return null;
    }

    private JButton createCloseButton() {
        JButton res = CloseButtonFactory.createBigCloseButton();
        res.setModel( new DefaultButtonModel() {
            @Override
            public boolean isRollover() {
                return inCloseButtonRect;
            }
        });
        //allow third party look and feels to provide their own icons
        Icon defaultIcon = UIManager.getIcon( "nb.popupswitcher.closebutton.defaultIcon" ); //NOI18N
        if( null != defaultIcon )
            res.setIcon( defaultIcon );
        Icon rolloverIcon = UIManager.getIcon( "nb.popupswitcher.closebutton.rolloverIcon" ); //NOI18N
        if( null != rolloverIcon )
            res.setRolloverIcon( rolloverIcon );
        return res;
    }

    static class Item extends SwitcherTableItem {

        private final TabData tabData;
        private final ProjectProxy project;
        private final boolean isSeparator;

        public Item( SwitcherTableItem.Activatable activatable, String name, String htmlName,
                TabData tab, boolean active, ProjectProxy project ) {
            super( activatable, name, htmlName, tab.getIcon(), active, tab.getTooltip() );
            this.tabData = tab;
            this.project = project;
            isSeparator = false;
        }

        public static Item create( ProjectProxy project ) {
            String projectFolder = null;
            projectFolder = project.getPath();
            return new Item( project.getDisplayName(), "<html><b>"+project.getDisplayName(), projectFolder, project ); //NOI18N
        }

        private Item( String name, String htmlName, String tooltip, ProjectProxy project ) {
            super( NONACTIVATABLE, name, htmlName, null, false, tooltip );
            tabData = null;
            this.project = project;
            isSeparator = true;
        }

        public TabData getTabData() {
            return tabData;
        }

        @Override
        public int compareTo( Object o ) {
            if( o instanceof Item ) {
                ProjectProxy otherProject = ((Item)o).project;
                if( null == project && null != otherProject )
                    return 1;
                if( null != project && null == otherProject )
                    return -1;
                if( null != project && null != otherProject ) {

                    int res = project.getDisplayName().compareTo( otherProject.getDisplayName() );
                    if( res != 0 )
                        return res;
                }
            }
            return super.compareTo( o );
        }

        ProjectProxy getProject() {
            return project;
        }
    }
    
    private static final SwitcherTableItem.Activatable NONACTIVATABLE = new SwitcherTableItem.Activatable() {
        @Override
        public void activate() {
            //NOOP
        }
    };

    static final Item NO_PROJECT_SEPARATOR = new Item("<no project>", "<html><b>&lt;no project&gt;", null, null);

    private static class ItemBorder implements Border {

        private Color color;

        @Override
        public void paintBorder( Component c, Graphics g, int x, int y, int width, int height ) {
            if( null != color ) {
                g.setColor( color );
                g.fillRect( x, y, 5, height);
            }
        }

        @Override
        public Insets getBorderInsets( Component c ) {
           return new Insets(2, 5, 0, 5);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
