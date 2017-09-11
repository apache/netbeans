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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;
import org.netbeans.core.multitabs.impl.TabListPopupAction;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Factory that create buttons to be displayed next to document tabs. The buttons
 * can maximize document window, show drop-down list of documents, scroll the documents left or right.
 *
 * @author S. Aubrecht
 */
public final class ButtonFactory {

    /**
     * Creates button to show drop-down list of documents from the given tab displayer.
     * @param controller Tab displayer's controller.
     * @return Drop-down list button.
     */
    public static JButton createDropDownButton( Controller controller ) {
        final JButton btn = new JButton( new TabListPopupAction(controller) );
        btn.setIcon( ImageUtilities.loadImageIcon( "org/netbeans/core/multitabs/resources/down.png", true) ); //NOI18N
        btn.setActionCommand( "pressed"); //NOI18N
        btn.setFocusable( false );
        btn.setToolTipText( NbBundle.getMessage(ButtonFactory.class, "Hint_DocumentList") );
        return btn;
    }

    /**
     * Creates button to maximize currently selected document in the given tab displayer.
     * @param controller Tab displayer's controller.
     * @return Button to maximize selected document tab.
     */
    public static JButton createMaximizeButton( final Controller controller ) {
        final JButton btn = new JButton();
        btn.setIcon( ImageUtilities.loadImageIcon( "org/netbeans/core/multitabs/resources/maximize.png", true) ); //NOI18N
        btn.setToolTipText( NbBundle.getMessage(ButtonFactory.class, "Hint_MaximizeRestore") );
        btn.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                controller.postActionEvent( new TabActionEvent( btn, TabbedContainer.COMMAND_MAXIMIZE, -1 ) );
            }
        });
        btn.setFocusable( false );
        return btn;
    }

    /**
     * Button to scroll tabs left/up. The button sets up a timer when pressed
     * to call the scroll action repeatedly until released or until the action
     * becomes disabled.
     * @param scrollAction Action implementing the actual scrolling.
     * @return Button to scroll left/up.
     */
    public static JButton createScrollLeftButton( Action scrollAction ) {
        JButton btn = new TimerButton( scrollAction );
        Icon icon = ImageUtilities.loadImageIcon( "org/netbeans/core/multitabs/resources/left.png", true); //NOI18N
        btn.setIcon( icon );
        btn.setToolTipText( NbBundle.getMessage(ButtonFactory.class, "Hint_ScrollLeft") );
        return btn;
    }

    /**
     * Button to scroll tabs right/down. The button sets up a timer when pressed
     * to call the scroll action repeatedly until released or until the action
     * becomes disabled.
     * @param scrollAction Action implementing the actual scrolling.
     * @return Button to scroll right/down.
     */
    public static JButton createScrollRightButton( Action scrollAction ) {
        JButton btn = new TimerButton( scrollAction );
        Icon icon = ImageUtilities.loadImageIcon( "org/netbeans/core/multitabs/resources/right.png", true); //NOI18N
        btn.setIcon( icon );
        btn.setToolTipText( NbBundle.getMessage(ButtonFactory.class, "Hint_ScrollRight") );
        return btn;
    }

    /**
     * A convenience button class which will continue re-firing its action
     * on a timer for as long as the button is depressed.  Used for left-right scroll
     * buttons.
     */
    private static class TimerButton extends JButton implements ActionListener {
        Timer timer = null;

        public TimerButton( Action a ) {
            setAction( a );
            setFocusable( false );
        }

        private Timer getTimer() {
            if (timer == null) {
                timer = new Timer(400, this);
                timer.setRepeats(true);
            }
            return timer;
        }

        int count = 0;

        @Override
        public void actionPerformed( ActionEvent e ) {
            count++;
            if (count > 2) {
                if (count > 5) {
                    timer.setDelay(75);
                } else {
                    timer.setDelay(200);
                }
            }
            performAction();
        }

        private void performAction() {
            if (!isEnabled()) {
                stopTimer();
                return;
            }
            getAction().actionPerformed(new ActionEvent(this,
                                                        ActionEvent.ACTION_PERFORMED,
                                                        getActionCommand()));
        }

        private void startTimer() {
            Timer t = getTimer();
            if (t.isRunning()) {
                return;
            }
            repaint();
            t.setDelay(400);
            t.start();
        }

        private void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
            repaint();
            count = 0;
        }

        @Override
        protected void processMouseEvent(MouseEvent me) {
            if (isEnabled() && me.getID() == MouseEvent.MOUSE_PRESSED) {
                startTimer();
            } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
                stopTimer();
            }
            super.processMouseEvent(me);
        }

        @Override
        protected void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);
            if (fe.getID() == FocusEvent.FOCUS_LOST) {
                stopTimer();
            }
        }
    }
}
