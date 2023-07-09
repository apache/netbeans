/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.jakarta.web.beans.navigation;
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
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.openide.windows.WindowManager;

/**
 * Based on PopupUtil from csl.api
 * 
 * @author ads
 */
public final class PopupUtil  {
    
    private static final String CLOSE_KEY = "CloseKey"; //NOI18N
    private static final Action CLOSE_ACTION = new CloseAction();
    private static final KeyStroke ESC_KEY_STROKE = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ); 
        
    private static final String POPUP_NAME = "popupComponent"; //NOI18N
    private static JDialog popupWindow;
    private static HideAWTListener hideListener = new HideAWTListener();
    
    // Singleton
    private PopupUtil() {
    }
    
    public static boolean isPopupShowing() {
        return popupWindow != null;
    }
    
    public static void showPopup( JComponent content, String title, int x, int y) {        
        if (popupWindow != null ) {
            return; // Content already showing
        }
                           
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        
        // NOT using PopupFactory
        // 1. on linux, creates mediumweight popup taht doesn't refresh behind visible glasspane
        // 2. on mac, needs an owner frame otherwise hiding tooltip also hides the popup. (linux requires no owner frame to force heavyweight)
        // 3. the created window is not focusable window
        
        popupWindow = new JDialog( getMainWindow() );
        popupWindow.setName( POPUP_NAME );
        popupWindow.setUndecorated(true);
        popupWindow.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( ESC_KEY_STROKE, CLOSE_KEY );
        popupWindow.getRootPane().getActionMap().put( CLOSE_KEY, CLOSE_ACTION );
	
	//set a11y
	String a11yName = content.getAccessibleContext().getAccessibleName();
	if(a11yName != null && !a11yName.equals(""))
	    popupWindow.getAccessibleContext().setAccessibleName(a11yName);
	String a11yDesc = content.getAccessibleContext().getAccessibleDescription();
	if(a11yDesc != null && !a11yDesc.equals(""))
	    popupWindow.getAccessibleContext().setAccessibleDescription(a11yDesc);
	    
        popupWindow.getContentPane().add(content);
                
        WindowManager.getDefault().getMainWindow().addWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().addComponentListener(hideListener);
        resizePopup();
        
        if (x != (-1)) {
            Point p = fitToScreen( x, y, 0 );
            popupWindow.setLocation(p.x, p.y);
            
        }
        
        popupWindow.setVisible( true );
        content.requestFocus();
        content.requestFocusInWindow();
    }
    
    public static void hidePopup() {
        if (popupWindow != null) {
//            popupWindow.getContentPane().removeAll();
            Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
            
            popupWindow.setVisible( false );
            popupWindow.dispose();
        }
        WindowManager.getDefault().getMainWindow().removeWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeComponentListener(hideListener);
        popupWindow = null;
    }
    
    public static JTextComponent findEditor(Document doc) {
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        if (comp.getDocument() == doc) {
            return comp;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            if (comp.getDocument() == doc) {
                return comp;
            }
        }

        return null;
    }

    
    private static void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0,0);
        SwingUtilities.convertPointToScreen(point, getMainWindow());
        popupWindow.setLocation( point.x + (getMainWindow().getWidth() - popupWindow.getWidth()) / 2, 
                                 point.y + (getMainWindow().getHeight() - popupWindow.getHeight()) / 3);
    }
    
    private static final int X_INSET = 10;
    
    private static Point fitToScreen( int x, int y, int altHeight ) {
        
        Rectangle screen = org.openide.util.Utilities.getUsableScreenBounds();
                
        Point p = new Point( x, y );
        
        // Adjust the x postition if necessary
        if ( ( p.x + popupWindow.getWidth() ) > ( screen.x + screen.width - X_INSET ) ) {
            p.x = screen.x + screen.width - X_INSET - popupWindow.getWidth(); 
        }
        
        // Adjust the y position if necessary
        if ( ( p.y + popupWindow.getHeight() ) > ( screen.y + screen.height - X_INSET ) ) {
            p.y = p.y - popupWindow.getHeight() - altHeight;
        }
        
        return p;     
    }

    
    private static Frame getMainWindow() {
        return WindowManager.getDefault().getMainWindow();
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class HideAWTListener extends ComponentAdapter implements  AWTEventListener, WindowStateListener {
        
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent)aWTEvent;
                if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                    //#118828
                    if (! (aWTEvent.getSource() instanceof Component)) {
                        hidePopup();
                        return;
                    }
                    
                    Component comp = (Component)aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed(POPUP_NAME, comp); //NOI18N
                    if ( par == null ) {
                        hidePopup();
                    }
                }
            }
        }

        public void windowStateChanged(WindowEvent windowEvent) {
            if (popupWindow != null ) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();
            
                if (((oldState & Frame.ICONIFIED) == 0) &&
                    ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hidePopup();
                }
            }

        }
        
        public void componentResized(ComponentEvent evt) {
            if (popupWindow != null) {
                resizePopup();
            }
        }
        
        public void componentMoved(ComponentEvent evt) {
            if (popupWindow!= null) {
                resizePopup();
            }
        }        
        
    }
    
    private static class CloseAction extends AbstractAction {
        
        private static final long serialVersionUID = -6991538204946001510L;

        public void actionPerformed(java.awt.event.ActionEvent e) {
            hidePopup();
        }
                
                
    }
    
}
