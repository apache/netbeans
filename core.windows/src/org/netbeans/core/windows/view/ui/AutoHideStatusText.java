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
package org.netbeans.core.windows.view.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.EditorOnlyDisplayer;
import org.netbeans.core.windows.view.ui.slides.SlideBar;
import org.openide.awt.StatusDisplayer;

/**
 * Status line text that shows at the bottom of the main IDE window only when 
 * there's any status text available and auto-hides when the status text is empty.
 * 
 * @author S. Aubrecht
 */
final class AutoHideStatusText implements ChangeListener, Runnable {
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel lblStatus = new JLabel();
    private String text;
    private final JPanel statusContainer;
    
    private AutoHideStatusText( JFrame frame, JPanel statusContainer  ) {
        this.statusContainer = statusContainer;
        Border outerBorder = UIManager.getBorder( "Nb.ScrollPane.border" ); //NOI18N
        if( null == outerBorder ) {
            outerBorder = BorderFactory.createEtchedBorder();
        }
        panel.setBorder( BorderFactory.createCompoundBorder( outerBorder, 
                BorderFactory.createEmptyBorder(3,3,3,3) ) );
        lblStatus.setName("AutoHideStatusTextLabel"); //NOI18N
        panel.add( lblStatus, BorderLayout.CENTER );
        frame.getLayeredPane().add( panel, Integer.valueOf( 101 ) );
        StatusDisplayer.getDefault().addChangeListener( this );

        frame.addComponentListener( new ComponentAdapter() {
            @Override
            public void componentResized( ComponentEvent e ) {
                run();
            }
        });
    }
    
    static void install( JFrame frame, JPanel statusContainer ) {
        new AutoHideStatusText( frame, statusContainer );
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
        text = StatusDisplayer.getDefault().getStatusText();
        String oldValue = lblStatus.getText();
        if( text == null ? oldValue == null : text.equals( oldValue ) ) {
            // no change needed
            return;
        }
        if( SwingUtilities.isEventDispatchThread() ) {
            run();
        } else {
            SwingUtilities.invokeLater( this );
        }
    }
    
    @Override
    public void run() {
        lblStatus.setText( text );
        if( EditorOnlyDisplayer.getInstance().isActive() )
            return;
        if( null == text || text.isEmpty() ) {
            panel.setVisible( false );
            Container parent = panel.getParent();
            if( parent instanceof JLayeredPane ) {
                JLayeredPane pane = (JLayeredPane) parent;
                pane.moveToBack( panel );
            }
        } else {
            panel.setVisible( true );
            Container parent = panel.getParent();
            Dimension dim = panel.getPreferredSize();
            Rectangle rect = parent.getBounds();
            Component slideBar = findSlideBar();
            if( null != slideBar ) {
                int slideWidth = slideBar.getWidth();
                if( slideWidth > 0 ) {
                    rect.x += slideWidth + 10;
                }
            }
            panel.setBounds( rect.x-1, rect.y+rect.height-dim.height+1, dim.width, dim.height+1 );
            if( parent instanceof JLayeredPane ) {
                JLayeredPane pane = (JLayeredPane) parent;
                if( pane.getComponentZOrder(panel) >= 0 ) { //#241059 
                    pane.moveToFront( panel );
                }
            }
        }
    }
    
    private Component findSlideBar() {
        if( null == statusContainer )
            return null;
        for( Component c : statusContainer.getComponents() ) {
            if( c instanceof SlideBar ) {
                return c;
            }
        }
        return null;
    }
}
