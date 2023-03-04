/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
