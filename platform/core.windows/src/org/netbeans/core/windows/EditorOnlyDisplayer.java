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
package org.netbeans.core.windows;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 * Replaces main window's content pane with currently selected editor TopComponent
 * to provide distraction-free editing environment. When deactivated or when
 * other TopComponent is activated it puts back the original content pane.
 *
 * @author S. Aubrecht
 */
public class EditorOnlyDisplayer {

    private static EditorOnlyDisplayer theInstance;

    private final PropertyChangeListener registryListener;
    private Container originalContentPane = null;
    private boolean originalShowEditorToolbar = true;

    private EditorOnlyDisplayer() {
        registryListener = new PropertyChangeListener() {
            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                onRegistryChange( evt );
            }
        };
    }

    public static EditorOnlyDisplayer getInstance() {
        synchronized( EditorOnlyDisplayer.class ) {
            if( null == theInstance ) {
                theInstance = new EditorOnlyDisplayer();
            }
        }
        return theInstance;
    }

    public boolean isActive() {
        return null != originalContentPane;
    }

    public void setActive( boolean activate ) {
        if( activate == isActive() )
            return;
        if( isActive() ) {
            cancel(true);
        } else {
            activate();
        }
    }

    private void onRegistryChange( PropertyChangeEvent evt ) {
        if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {
            
            final TopComponent tc = TopComponent.getRegistry().getActivated();
            if( null != tc ) {
                //#237857 
                Window activeWindow = SwingUtilities.getWindowAncestor(tc);
                if( null != activeWindow && !activeWindow.equals(WindowManagerImpl.getInstance().getMainWindow()) )
                    return;
            }
            if( switchCurrentEditor() ) {
                return;
            }
            cancel( true );
        }
    }

    private boolean switchCurrentEditor() {
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null == tc || !TopComponentTracker.getDefault().isEditorTopComponent( tc ) )
            return false;

        final WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        final JFrame mainWnd = ( JFrame ) wmi.getMainWindow();
        if( SwingUtilities.isDescendingFrom( tc, mainWnd.getContentPane() ) )
            return true;
        JPanel panel = new JPanel( new BorderLayout() );
        panel.add( tc, BorderLayout.CENTER  );
        try {
            mainWnd.setContentPane( panel );
        } catch( IndexOutOfBoundsException e ) {
            Logger.getLogger(EditorOnlyDisplayer.class.getName()).log(Level.INFO, "Error while switching current editor.", e);
            //#245541 - something is broken in the component hierarchy, let's try restoring to the default mode
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    cancel(false);
                }
            });
        }
        mainWnd.invalidate();
        mainWnd.revalidate();
        mainWnd.repaint();
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                tc.requestFocusInWindow();
            }
        });
        return true;
    }

    public void cancel( boolean restoreFocus ) {
        if( !isActive() )
            return;
        TopComponent.getRegistry().removePropertyChangeListener( registryListener );
        JFrame frame = ( JFrame ) WindowManagerImpl.getInstance().getMainWindow();
        frame.setContentPane( originalContentPane );
        originalContentPane = null;
        frame.invalidate();
        frame.revalidate();
        frame.repaint();
        setShowEditorToolbar( originalShowEditorToolbar );
        if( restoreFocus )
            restoreFocus();
    }
    
    private void restoreFocus() {
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null != tc ) {
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    tc.requestFocusInWindow();
                }
            });
        }
    }

    private void activate() {
        assert null == originalContentPane;

        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null == tc || !TopComponentTracker.getDefault().isEditorTopComponent( tc ) )
            return;

        final WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        final JFrame mainWnd = ( JFrame ) wmi.getMainWindow();
        
        StatusDisplayer.getDefault().setStatusText(null);

        originalContentPane = mainWnd.getContentPane();

        JPanel panel = new JPanel( new BorderLayout() );
        panel.add( tc, BorderLayout.CENTER  );
        mainWnd.setContentPane( panel );
        mainWnd.invalidate();
        mainWnd.revalidate();
        mainWnd.repaint();

        wmi.getRegistry().addPropertyChangeListener( registryListener );

        originalShowEditorToolbar = setShowEditorToolbar( false );

        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                tc.requestFocusInWindow();
            }
        });
    }

    public static boolean setShowEditorToolbar( boolean show ) {
        boolean res = true;
        Action toggleEditorToolbar = FileUtil.getConfigObject( "Editors/Actions/toggle-toolbar.instance", Action.class ); //NOI18N
        if( null != toggleEditorToolbar ) {
            if( toggleEditorToolbar instanceof Presenter.Menu ) {
                JMenuItem menuItem = ((Presenter.Menu)toggleEditorToolbar).getMenuPresenter();
                if( menuItem instanceof JCheckBoxMenuItem ) {
                    JCheckBoxMenuItem checkBoxMenu = ( JCheckBoxMenuItem ) menuItem;
                    res = checkBoxMenu.isSelected();
                    if( checkBoxMenu.isSelected() != show ) {
                        try {
                            toggleEditorToolbar.actionPerformed( new ActionEvent( menuItem, 0, "")); //NOII18N
                        } catch( Exception ex ) {
                            //don't worry too much if it isn't working, we're just trying to be helpful here
                            Logger.getLogger( EditorOnlyDisplayer.class.getName()).log( Level.FINE, null, ex );
                        }
                    }
                }
            }
        }

        return res;
    }
}
