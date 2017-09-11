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
package org.netbeans.core.windows.view.ui.popupswitcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.openide.awt.StatusDisplayer;

/**
 * Visual panel with a table of TopComponents to switch to and a status-like
 * label showing the description of currently selected item.
 *
 * @since 2.46
 * @author S. Aubrecht
 */
class PopupSwitcher extends JPanel {

    private final Table table;
    private final JScrollPane scrollPane;
    private final JLabel lblDescription;
    private final ListSelectionListener selectionListener;

    public PopupSwitcher( boolean documentsOnly, final int hits, final boolean forward ) {
        this( new Model(documentsOnly), hits, forward );
    }

    /**
     * For (unit) testing only
     * @param model
     * @param hits
     * @param forward 
     */
    PopupSwitcher( Model model, final int hits, final boolean forward ) {
        super( new BorderLayout() );
        this.table = new Table(model);
        scrollPane = new JScrollPane( table );
        lblDescription = new JLabel(" "); //NOI18N //NOI18N
        lblDescription.setBorder( BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder( 1, 0, 0, 0, table.getGridColor()),
                BorderFactory.createEmptyBorder( 1, 1, 1, 1) ) );
        configureScrollPane();
        add( scrollPane, BorderLayout.CENTER );
        add( lblDescription, BorderLayout.SOUTH );
        Border b = UIManager.getBorder( "nb.popupswitcher.border" ); //NOI18N
        if( null == b )
            b = BorderFactory.createLineBorder(table.getGridColor());
        setBorder(b);
        setBackground( getDefaultBackground() );

        selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged( ListSelectionEvent e ) {
                updateDescription();
            }
        };
        table.getSelectionModel().addListSelectionListener( selectionListener );
        table.getColumnModel().getSelectionModel().addListSelectionListener( selectionListener );

        table.setInitialSelection( hits, forward );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _updateDescription();
    }

    private void configureScrollPane() {
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setBorder( BorderFactory.createEmptyBorder() );
        Color bkColor = getDefaultBackground();
        scrollPane.getViewport().setBackground( bkColor );
        scrollPane.setBackground( bkColor );
    }

    Table getTable() {
        return table;
    }

    static Color getDefaultForeground() {
        Color foreground = UIManager.getColor( "nb.popupswitcher.foreground" ); //NOI18N
        if (foreground == null)
            foreground = UIManager.getColor("ComboBox.foreground"); //NOI18N
        return foreground;
    }

    static Color getDefaultBackground() {
        Color background = UIManager.getColor( "nb.popupswitcher.background" ); //NOI18N
        if (background == null)
            background = UIManager.getColor("ComboBox.background"); //NOI18N
        return background;
    }

    static Color getSelectionForeground() {
        Color selForeground = UIManager.getColor( "nb.popupswitcher.selectionForeground" ); //NOI18N
        if (selForeground == null)
            selForeground = UIManager.getColor("ComboBox.selectionForeground"); //NOI18N
        return selForeground;
    }

    static Color getSelectionBackground() {

        Color selBackground = UIManager.getColor( "nb.popupswitcher.selectionBackground" ); //NOI18N
        if (selBackground == null)
            selBackground = UIManager.getColor("ComboBox.selectionBackground"); //NOI18N
        return selBackground;
    }

    private void updateDescription() {
        if( !lblDescription.isShowing() )
            return;
        _updateDescription();
    }

    private void _updateDescription() {
        if( !lblDescription.isValid() ) {
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    updateDescription();
                }
            });
            return;
        }
        Item item = table.getSelectedItem();
        String statusText = item == null ? null : item.getDescription();
        StatusDisplayer.getDefault().setStatusText( statusText );
        if( null == statusText ) {
            int selRow = table.getSelectedRow();
            int selCol = table.getSelectedColumn();
            if( selRow >= 0 && selCol >= 0 ) {
                TableCellRenderer ren = table.getCellRenderer( selRow, selCol );
                Component c = table.prepareRenderer( ren, selRow, selCol);
                if( c.getPreferredSize().width > table.getColumnModel().getColumn( selCol ).getWidth() ) {
                    statusText = table.getSelectedItem().getDisplayName();
                }
            }
        }
        lblDescription.setText( truncateText( statusText, lblDescription.getWidth() ) );
    }

    static final char DOTS = '…';

    static String truncateText( String s, int availPixels ) {
        if( null == s )
            return " "; //NOI18N //NOI18N
        if( s.length() < 3 ) {
            return s;
        }
        s = Table.stripHtml( s );
        if( s.length() < 2 ) {
            return DOTS + s; //NOI18N
        }

        JLabel lbl = new JLabel( s );
        while( lbl.getPreferredSize().width > availPixels && s.length() > 0 ) {
            s = s.substring( 1 );
            lbl.setText( DOTS + s );
        }
        return lbl.getText();
    }
}
