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
package org.netbeans.core.multitabs;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;

/**
 *
 * @author S. Aubrecht
 */
public final class Controller implements MouseListener {

    private List<ActionListener> actionListenerList;

    private final SingleSelectionModel selectionModel = new DefaultSingleSelectionModel();
    private final TabDataModel tabModel;
    private final TabDisplayer displayer;

    public Controller( final TabDisplayer displayer ) {
        this.displayer = displayer;
        this.tabModel = displayer.getModel();
        displayer.attach( this );
        selectionModel.addChangeListener( new ChangeListener() {

            @Override
            public void stateChanged( ChangeEvent e ) {
                displayer.setSelectedIndex( getSelectedIndex() );
            }
        });
    }

    SingleSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public TabDataModel getTabModel() {
        return tabModel;
    }

    void addActionListener( ActionListener al ) {
        synchronized( this ) {
            if (actionListenerList == null) {
                actionListenerList = new ArrayList<ActionListener>(5);
            }
            actionListenerList.add(al);
        }
    }

    void removeActionListener( ActionListener al ) {
        synchronized( this ) {
            if (actionListenerList != null) {
                actionListenerList.remove(al);
                if (actionListenerList.isEmpty()) {
                    actionListenerList = null;
                }
            }
        }
    }

    public void addSelectionChangeListener( ChangeListener listener ) {
        selectionModel.addChangeListener( listener );
    }

    public void removeSelectionChangeListener( ChangeListener listener ) {
        selectionModel.removeChangeListener( listener );
    }

    public void postActionEvent( TabActionEvent event ) {
        List<ActionListener> list;
        synchronized( this ) {
            if( actionListenerList == null ) {
                return;
            }
            list = Collections.unmodifiableList( actionListenerList );
        }
        for( ActionListener l : list ) {
            l.actionPerformed( event );
        }
    }

    public void setSelectedIndex( int index ) {
        selectionModel.setSelectedIndex( index );
    }

    public int getSelectedIndex() {
        return selectionModel.getSelectedIndex();
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint( e.getComponent(), p, displayer );
        TabData tab = displayer.getTabAt( p );
        if( null == tab )
            return;
        final int tabIndex = tabModel.indexOf( tab );
        if( e.getClickCount() > 1 && e.getButton() == MouseEvent.BUTTON1 ) {
            //maximize/restore
            TabActionEvent tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_MAXIMIZE, tabIndex );
            postActionEvent( tae );
        } else if( e.getButton() == MouseEvent.BUTTON2 ) {
            //close tab
            TabActionEvent tae = new TabActionEvent( displayer, TabbedContainer.COMMAND_CLOSE, tabIndex );
            postActionEvent( tae );
        }
    }

    @Override
    public void mousePressed( MouseEvent e ) {
        maybeShowPopup( e );
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        maybeShowPopup( e );
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
    }

    @Override
    public void mouseExited( MouseEvent e ) {
    }

    private void maybeShowPopup( MouseEvent e ) {
        if( !e.isPopupTrigger() )
            return;
        Point p = e.getPoint();
        p = SwingUtilities.convertPoint( e.getComponent(), p, displayer );
        TabData tab = displayer.getTabAt( p );
        if( null == tab )
            return;
        final int tabIndex = tabModel.indexOf( tab );
        //popup menu
        TabActionEvent tae = new TabActionEvent( this, TabbedContainer.COMMAND_POPUP_REQUEST, tabIndex, e );
        postActionEvent( tae );
    }
}
