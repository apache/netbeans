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
