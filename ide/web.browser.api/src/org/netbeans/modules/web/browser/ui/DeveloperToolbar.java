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
package org.netbeans.modules.web.browser.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.core.HtmlBrowserComponent;
import org.netbeans.modules.web.browser.api.ResizeOption;
import org.netbeans.modules.web.browser.api.ResizeOptions;
import org.netbeans.modules.web.browser.spi.Resizable;
import org.netbeans.modules.web.browser.spi.Zoomable;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Toolbar with web-developer tools.
 * 
 * @author S. Aubrecht
 */
public class DeveloperToolbar {

    private final JPanel panel;
    private Lookup context;
    private ResizeOption currentSize = ResizeOption.SIZE_TO_FIT;
    private final JToolBar resizeBar;
    private final ArrayList<BrowserResizeButton> resizeButtons;
    private final JToggleButton btnResizeMenu;
    private final ItemListener resizeListener;
    private boolean ignoreSelectionChanges;
    private final JComboBox comboZoom = new JComboBox();
    private JToolBar customToolbar;
    private final ContainerListener toolbarListener;

    private DeveloperToolbar() {
        panel = new JPanel( new FlowLayout(FlowLayout.LEFT) );
        panel.setOpaque(false);
        resizeBar = new JToolBar();
        resizeBar.setFloatable( false );
        resizeBar.setFocusable( false );
        panel.add( resizeBar );
        resizeButtons = new ArrayList<BrowserResizeButton>( 15 );
        resizeListener = new ItemListener() {
            @Override
            public void itemStateChanged( ItemEvent e ) {
                if( ignoreSelectionChanges )
                    return;
                if( e.getSource() instanceof BrowserResizeButton ) {
                    setBrowserSize( ((BrowserResizeButton)e.getSource()).getResizeOption() );
                }
            }
        };
        comboZoom.addItemListener( new ItemListener() {

            @Override
            public void itemStateChanged( ItemEvent e ) {
                if( e.getStateChange() == ItemEvent.DESELECTED )
                    return;
                String newZoom = zoom( comboZoom.getSelectedItem().toString() );
                comboZoom.setSelectedItem( newZoom );
            }
        });
        toolbarListener = new ContainerListener() {

            @Override
            public void componentAdded( ContainerEvent e ) {
                initActions( customToolbar );
            }

            @Override
            public void componentRemoved( ContainerEvent e ) {
                //TODO remove action from TC's map
            }
        };
        btnResizeMenu = new JToggleButton( ImageUtilities.loadImageIcon( "org/netbeans/modules/web/browser/ui/resources/menu.png", true ) ); //NOI18N
        btnResizeMenu.setToolTipText( NbBundle.getMessage( DeveloperToolbar.class, "Tip_ResizeOptions") ); //NOI18N
        btnResizeMenu.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                JPopupMenu popup = buildResizePopup();
                popup.addPopupMenuListener( new PopupMenuListener() {

                    @Override
                    public void popupMenuWillBecomeVisible( PopupMenuEvent e ) {
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible( PopupMenuEvent e ) {
                        setBrowserSize( currentSize );
                    }

                    @Override
                    public void popupMenuCanceled( PopupMenuEvent e ) {
                        setBrowserSize( currentSize );
                    }
                });
                popup.show( btnResizeMenu, 0, btnResizeMenu.getHeight() );
            }
        });
    }

    public static DeveloperToolbar create() {
        return new DeveloperToolbar();
    }

    public Component getComponent() {
        return panel;
    }

    public void intialize( Lookup context ) {
        this.context = context;
        JToolBar bar = context.lookup( JToolBar.class );
        if( null == bar ) {
            bar = new JToolBar();
        }
        bar.setFloatable( false );
        bar.setFocusable( false );
        bar.setOpaque(false);
        if( null != customToolbar )
            panel.remove( customToolbar );
        panel.add( bar );

        //ZOOM combo box
        DefaultComboBoxModel<String> zoomModel = new DefaultComboBoxModel<>();
        zoomModel.addElement( "200%" ); //NOI18N
        zoomModel.addElement( "150%" ); //NOI18N
        zoomModel.addElement( "100%" ); //NOI18N
        zoomModel.addElement( "75%" ); //NOI18N
        zoomModel.addElement( "50%" ); //NOI18N
        comboZoom.setModel(zoomModel);
        comboZoom.setEditable( true );
        if( comboZoom.getEditor().getEditorComponent() instanceof JTextField )
            ((JTextField)comboZoom.getEditor().getEditorComponent()).setColumns( 4 );
        comboZoom.setSelectedItem( "100%" ); //NOI18N
        comboZoom.setEnabled( null != getLookup().lookup( Zoomable.class ) );

        fillResizeBar();

        initActions(bar);

        if( null != customToolbar )
            customToolbar.removeContainerListener( toolbarListener );
        bar.addContainerListener( toolbarListener );
        customToolbar = bar;
    }

    private Lookup getLookup() {
        return null == context ? Lookup.EMPTY : context;
    }

    private String zoom( String zoomFactor ) {
        if( zoomFactor.trim().isEmpty() )
            return null;

        Zoomable zoomable = getLookup().lookup( Zoomable.class );
        if( null == zoomable )
            return null;
        try {
            zoomFactor = zoomFactor.replaceAll( "\\%", ""); //NOI18N
            zoomFactor = zoomFactor.trim();
            double zoom = Double.parseDouble( zoomFactor );
            zoom = Math.abs( zoom )/100;
            if( zoom <= 0.0 )
                return null;
            zoomable.zoom( zoom );
            return (int)(100*zoom) + "%"; //NOI18N
        } catch( NumberFormatException nfe ) {
            //ignore
        }
        return null;
    }

    private void fillResizeBar() {
        resizeBar.removeAll();
        resizeButtons.clear();
        resizeBar.setOpaque(false);

        final boolean resizingEnabled = null != context.lookup( Resizable.class );
        List<ResizeOption> options = ResizeOptions.getDefault().loadAll();
        options.add( ResizeOption.SIZE_TO_FIT );
        for( ResizeOption ro : options ) {
            if( !ro.isShowInToolbar() )
                continue;
            BrowserResizeButton button = BrowserResizeButton.create( ro );
            resizeBar.add( button );
            button.setSelected( ro.equals( currentSize ) );
            button.addItemListener( resizeListener );
            resizeButtons.add( button );
            button.setEnabled( resizingEnabled );
        }

        resizeBar.add( btnResizeMenu );

        resizeBar.add( comboZoom );
    }

    private void setBrowserSize( ResizeOption resizeOption ) {
        ignoreSelectionChanges = true;
        boolean doResize = !resizeOption.equals( currentSize );
        this.currentSize = resizeOption;
        boolean isToolbarSelection = false;
        for( BrowserResizeButton b : resizeButtons ) {
            b.setSelected( b.getResizeOption().equals( resizeOption ) );
            isToolbarSelection |= b.isSelected();
        }

        ignoreSelectionChanges = false;
        btnResizeMenu.setSelected( !isToolbarSelection );
        if( isToolbarSelection || null == resizeOption ) {
            btnResizeMenu.setToolTipText( NbBundle.getMessage( DeveloperToolbar.class, "Tip_ResizeOptions") ); //NOI18N
        } else {
            btnResizeMenu.setToolTipText( resizeOption.getToolTip() );
        }
        if( doResize ) {
            doResize( resizeOption.getWidth(), resizeOption.getHeight() );
        }
    }

    private JPopupMenu buildResizePopup() {
        JPopupMenu res = new JPopupMenu();

        List<ResizeOption> options = ResizeOptions.getDefault().loadAll();
        options.add( ResizeOption.SIZE_TO_FIT );
        for( ResizeOption ro : options ) {
            final ResizeOption size = ro;
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem( ro.getToolTip() );
            res.add( menuItem );
            menuItem.setSelected( size.equals( currentSize ) );
            menuItem.addItemListener( new ItemListener() {
                @Override
                public void itemStateChanged( ItemEvent e ) {
                    setBrowserSize( size );
                }
            });
            menuItem.setIcon( BrowserResizeButton.toIcon( ro ) );
        }
        res.addSeparator();
        
        JMenuItem menu = new JMenuItem( NbBundle.getMessage(DeveloperToolbar.class, "Lbl_CUSTOMIZE") );
        menu.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                ResizeOptionsCustomizer customizer = new ResizeOptionsCustomizer();
                if( customizer.showCustomizer() ) {
                    List<ResizeOption> newOptions = customizer.getResizeOptions();
                    ResizeOptions.getDefault().saveAll( newOptions );
                    fillResizeBar();
                }
            }
        });
        res.add( menu );

        return res;
    }

    void doResize( final int width, final int height ) {
        Resizable resizable = context.lookup( Resizable.class );
        if( null == resizable )
            return;

        if( width < 0 || height < 0 ) {
            resizable.autofit();
        } else {
            resizable.resize( width, height );
        }
    }

    /**
     * If any action in the toolbar has an ACCELERATOR_KEY value set it will be
     * added to browser's TC input map.
     * If there's a JToggleButton in the toolbar and it has client property Action.ACCELERATOR_KEY
     * set to requested KeyStroke, the shortcut will be added to browser's TC input map as well.
     * The toggle button must also have a non-null name.
     */
    private void initActions(JToolBar bar) {
        final HtmlBrowserComponent tc = ( HtmlBrowserComponent ) SwingUtilities.getAncestorOfClass( HtmlBrowserComponent.class, panel );
        if( null == tc ) {
            return;
        }
        ActionMap am = tc.getActionMap();
        InputMap im = tc.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        for( Component c : bar.getComponents() ) {
            if( c instanceof AbstractButton ) {
                if( c instanceof JToggleButton ) {
                    final JToggleButton toggle = ( JToggleButton ) c;
                    Object ks = toggle.getClientProperty( Action.ACCELERATOR_KEY );
                    if( ks instanceof KeyStroke && null != toggle.getName() ) {
                        KeyStroke key = ( KeyStroke ) ks;
                        im.put( key, toggle.getName() );
                        am.put( toggle.getName(), new AbstractAction() {
                            @Override
                            public void actionPerformed( ActionEvent e ) {
                                toggle.setSelected( !toggle.isSelected() );
                            }
                        });
                        continue;
                    }
                }
                AbstractButton button = ( AbstractButton ) c;
                Action a = button.getAction();
                if( null == a || null == a.getValue( Action.ACCELERATOR_KEY ) 
                        || null == a.getValue( Action.NAME ) )
                    continue;
                String accelerator = a.getValue( Action.ACCELERATOR_KEY ).toString();
                Object name = a.getValue( Action.NAME );
                am.put( name, a );
                im.put( KeyStroke.getKeyStroke( accelerator), name );
            }
        }
    }
}
