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
