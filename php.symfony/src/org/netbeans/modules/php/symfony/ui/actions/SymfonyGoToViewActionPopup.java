/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.symfony.ui.actions;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ondřej Brejla <ondrej@brejla.cz>
 */
public class SymfonyGoToViewActionPopup {

    private static final String POPUP_NAME = "goToViewPopup"; // NOI18N
    
    private static final String CLOSE_KEY = "CloseKey"; //NOI18N
    
    private static JDialog popupWindow;
    
    private HideAWTListener hideListener = new HideAWTListener();
    
    private int offset;
    
    private List<FileObject> views;
    
    public SymfonyGoToViewActionPopup(List<FileObject> views, int offset) {
        this.views = views;
        this.offset = offset;
    }
    
    public void show() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
               showPanel();
            }
        });
    }
    
    private void showPanel() {
        if (popupWindow != null ) {
            return;
        }

        addHideListener();

        popupWindow = new JDialog(getMainWindow());
        popupWindow.setName(POPUP_NAME);
        popupWindow.setUndecorated(true);
        popupWindow.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLOSE_KEY);
        popupWindow.getRootPane().getActionMap().put(CLOSE_KEY, new CloseAction());
        
        JComponent content = new SymfonyGoToViewActionPanel(views, this);
        String a11yName = content.getAccessibleContext().getAccessibleName();
        if(a11yName != null && !a11yName.equals("")) { // NOI18N
            popupWindow.getAccessibleContext().setAccessibleName(a11yName);
        }
        String a11yDesc = content.getAccessibleContext().getAccessibleDescription();
        if(a11yDesc != null && !a11yDesc.equals("")) { // NOI18N
            popupWindow.getAccessibleContext().setAccessibleDescription(a11yDesc);
        }
        
        popupWindow.getContentPane().add(content);
        
        moveToCorrectLocation();
        
        popupWindow.setVisible(true);
        content.requestFocus();
        content.requestFocusInWindow();
    }
    
    private void addHideListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        getMainWindow().addWindowStateListener(hideListener);
        getMainWindow().addComponentListener(hideListener);
    }
    
    private Frame getMainWindow() {
        return WindowManager.getDefault().getMainWindow();
    }
    
    private void moveToCorrectLocation() {
        popupWindow.pack();
        popupWindow.setLocation(fetchCorrectLocation());
    }
    
    private Point fetchCorrectLocation() {
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        Point point = new Point();
        
        if (target != null) {
            try {
                Rectangle rectangle = target.modelToView(offset);
                point = new Point(rectangle.x, rectangle.y + rectangle.height);
                SwingUtilities.convertPointToScreen(point, target);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return fetchAdjustedPosition(point); 
    }
    
    private Point fetchAdjustedPosition(Point p) {
        int INSET = 10;
        Rectangle screen = Utilities.getUsableScreenBounds();
        Point newLocation = p;
        
        if ((p.x + popupWindow.getWidth()) > (screen.x + screen.width - INSET)) {
            newLocation.x = screen.x + screen.width - INSET - popupWindow.getWidth(); 
        }
        
        if ((p.y + popupWindow.getHeight()) > (screen.y + screen.height - INSET)) {
            newLocation.y = p.y - popupWindow.getHeight();
        }
        
        return newLocation;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void hide() {
        if (popupWindow != null) {
            removeHideListener();
            popupWindow.setVisible(false);
            popupWindow.dispose();
        }

        popupWindow = null;
    }
    
    private void removeHideListener() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
        getMainWindow().removeWindowStateListener(hideListener);
        getMainWindow().removeComponentListener(hideListener);
    }

    private class HideAWTListener extends ComponentAdapter implements  AWTEventListener, WindowStateListener {

        @Override
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent) aWTEvent;
                if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                    if (!(aWTEvent.getSource() instanceof Component)) {
                        hide();
                        return;
                    }
                    
                    Component comp = (Component) aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed(POPUP_NAME, comp);
                    if (par == null) {
                        hide();
                    }
                }
            }
        }

        @Override
        public void windowStateChanged(WindowEvent windowEvent) {
            if (popupWindow != null ) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();
            
                if (((oldState & Frame.ICONIFIED) == 0) &&
                    ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hide();
                }
            }
        }
        
        @Override
        public void componentResized(ComponentEvent evt) {
            if (popupWindow != null) {
                moveToCorrectLocation();
            }
        }
        
        @Override
        public void componentMoved(ComponentEvent evt) {
            if (popupWindow!= null) {
                moveToCorrectLocation();
            }
        }  
        
    }
    
    private class CloseAction extends AbstractAction {
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            hide();
        }
        
    }
    
}
