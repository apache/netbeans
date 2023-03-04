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

import java.awt.BorderLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.openide.windows.TopComponent;

/**
 * Visual component that has a TabDisplayer component and the actual document
 * tab components arranged in StackLayout.
 * 
 * @author S. Aubrecht
 */
public final class TabContainer extends JPanel implements Tabbed.Accessor, ChangeListener {

    private final TabbedImpl tabbedImpl;
    private final JPanel tcPanel;
    private final TabDisplayer displayer;
    private final StackLayout layout = new StackLayout();

    TabContainer( TabbedImpl tabbedImpl, TabDisplayer tabDisplayer, int orientation ) {
        super( new BorderLayout(0, 0) );
        this.tabbedImpl = tabbedImpl;
        this.displayer = tabDisplayer;
        tcPanel = new JPanel( layout );
        add( tcPanel, BorderLayout.CENTER );
        tabbedImpl.getSelectionModel().addChangeListener( this );
        String lafId = UIManager.getLookAndFeel().getID();
        if( "Nimbus".equals( lafId ) ) {
            setBorder( new MatteBorder(1, 1, 1, 1, UIManager.getColor("nimbusBorder"))); //NOI18N
        } else if( "Aqua".equals( lafId ) ) {
            setBorder( BorderFactory.createEmptyBorder() );
        } else {
            setBorder( UIManager.getBorder( "Nb.ScrollPane.border" ) ); //NOI18N
        }
        switch( orientation ) {
            case JTabbedPane.TOP:
                add( displayer, BorderLayout.NORTH );
                break;
            case JTabbedPane.LEFT:
                add( displayer, BorderLayout.WEST );
                break;
            case JTabbedPane.RIGHT:
                add( displayer, BorderLayout.EAST );
                break;
            case JTabbedPane.BOTTOM:
                add( displayer, BorderLayout.SOUTH );
                break;
            default:
                throw new IllegalArgumentException( "Invalid orientation: " + orientation ); //NOI18N
        }
        stateChanged( null );
    }
    
    @Override
    public final Tabbed getTabbed() {
        return tabbedImpl;
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
        TopComponent tc = tabbedImpl.getSelectedTopComponent();
        if( tc != null ) {
            boolean wasVisible = tc.isVisible();
            layout.showComponent( tc, tcPanel );
            if( !wasVisible )
                tc.requestFocusInWindow();
        }
    }

    Rectangle getContentArea() {
        return tcPanel.getBounds();
    }

    TabDisplayer getTabDisplayer() {
        return displayer;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        tabbedImpl.getTabModel().addChangeListener( this );
        stateChanged( null );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        tabbedImpl.getTabModel().removeChangeListener( this );
    }
}
