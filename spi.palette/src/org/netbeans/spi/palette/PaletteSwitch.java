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

package org.netbeans.spi.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * A class that listens to changes to the set of opened TopComponents and to the
 * set of activated Nodes to show/hide the palette window when a TopComponent that
 * supports the palette is activated/deactivated.
 *
 * @author S. Aubrecht
 */
final class PaletteSwitch implements Runnable, LookupListener {
    
    static final String PROP_PALETTE_CONTENTS = "component_palette_contents"; //NOI18N
    
    private static PaletteSwitch theInstance;
    
    private PropertyChangeListener registryListener;
    
    private PropertyChangeSupport propertySupport;
    
    private PaletteController currentPalette;
    private Lookup.Result lookupRes;

    private Object currentToken;

    private static final RequestProcessor RP = new RequestProcessor("PaletteSwitch"); //NOI18N
    
    /** Creates a new instance of PaletteSwitcher */
    private PaletteSwitch() {
        
        propertySupport = new PropertyChangeSupport( this );
    }
    
    public synchronized static PaletteSwitch getDefault() {
        if( null == theInstance ) {
            theInstance = new PaletteSwitch();
        }
        return theInstance;
    }
    
    public void startListening() {
        if( !isPaletteWindowEnabled() ) {
            return;
        }
        synchronized( theInstance ) {
            if( null == registryListener ) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener( registryListener );
                switchLookupListener();
                run();
            }
        }
    }
    
    public void stopListening() {
        synchronized( theInstance ) {
            if( null != registryListener ) {
                TopComponent.getRegistry().removePropertyChangeListener( registryListener );
                registryListener = null;
                currentPalette = null;
            }
        }
    }

    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertySupport.removePropertyChangeListener( l );
    }
    
    public PaletteController getCurrentPalette() {
        return currentPalette;
    }
    
    @Override
    public void run() {
        if( !SwingUtilities.isEventDispatchThread() ) {
            SwingUtilities.invokeLater( this );
            return;
        }

        currentToken = new Object();
        TopComponent.Registry registry = TopComponent.getRegistry();
        final TopComponent activeTc = registry.getActivated();
        final Set<TopComponent> opened = new HashSet<TopComponent>(registry.getOpened());
        final PaletteController existingPalette = currentPalette;
        final boolean isMaximized = isPaletteMaximized();
        final Object token = currentToken;
        RP.post(new Runnable() {
            @Override
            public void run() {
                findNewPalette(existingPalette, activeTc, opened, isMaximized, token);
            }
        });
    }

    private boolean isPaletteMaximized() {
        boolean isMaximized = true;
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set openedTcs = registry.getOpened();
        for( Iterator i=openedTcs.iterator(); i.hasNext(); ) {
            TopComponent tc = (TopComponent)i.next();

            if( tc.isShowing() && !(tc instanceof PaletteTopComponent) ) {
                //other window(s) than the Palette are showing
                isMaximized = false;
                break;
            }
        }
        return isMaximized;
    }
    
    PaletteController getPaletteFromTopComponent( TopComponent tc, boolean mustBeShowing, boolean isOpened ) {
        if( null == tc || (!tc.isShowing() && mustBeShowing) )
            return null;
        
        PaletteController pc = (PaletteController)tc.getLookup().lookup( PaletteController.class );
        //#231997 - TopComponent.getSubComponents() can be called from EDT only
        //The only drawback of commenting out the code below is that a split view of
        //a form designer showing source and design hides the palette window
        //when the source split is the active one and some other TopComponent is activated
//	if (pc == null && isOpened) {
//	    TopComponent.SubComponent[] subComponents = tc.getSubComponents();
//	    for (int i = 0; i < subComponents.length; i++) {
//		TopComponent.SubComponent subComponent = subComponents[i];
//		Lookup subComponentLookup = subComponent.getLookup();
//		if (subComponentLookup != null) {
//		    pc = (PaletteController) subComponentLookup.lookup(PaletteController.class);
//		    if (pc != null && (subComponent.isActive() || subComponent.isShowing())) {
//			break;
//		    }
//		}
//	    }
//	}
        if( null == pc && isOpened ) {
            //check if there's any palette assigned to TopComponent's mime type
            Node[] activeNodes = tc.getActivatedNodes();
            if( null != activeNodes && activeNodes.length > 0 ) {
                DataObject dob = activeNodes[0].getLookup().lookup( DataObject.class );
                if( null != dob ) {
                    while( dob instanceof DataShadow ) {
                        dob = ((DataShadow)dob).getOriginal();
                    }
                    FileObject fo = dob.getPrimaryFile();
                    if( !fo.isVirtual() ) {
                        String mimeType = fo.getMIMEType();
                        pc = getPaletteFromMimeType( mimeType );
                    }
                }
            }
        }
        return pc;
    }
    
    /** 
     * Finds appropriate PaletteController for given mime type.
     *
     * @param mimeType Mime type to check for associated palette content.
     * 
     * @return PaletteController that is associated with the given mime type and that should
     * be displayed in the Common Palette when an editor window with the given mime type is activated.
     * @since 1.10
     */
    PaletteController getPaletteFromMimeType( String mimeType ) {
        MimePath path = MimePath.get( mimeType );
        Lookup lkp = MimeLookup.getLookup( path );
        return lkp.lookup( PaletteController.class );
    }
    
    private void showHidePaletteTopComponent(PaletteController newPalette, boolean isNewVisible) {
        PaletteController oldPalette = currentPalette;
        currentPalette = newPalette;
        
        if (isNewVisible) {
            PaletteTopComponent.showPalette();
            PaletteVisibility.setVisible(newPalette, true);
        } else {
            PaletteTopComponent.hidePalette();
        }

        propertySupport.firePropertyChange( PROP_PALETTE_CONTENTS, oldPalette, currentPalette );
    }
    
    /**
     * multiview components do not fire events when switching their inner tabs
     * so we have to listen to changes in lookup contents
     */
    private void switchLookupListener() {
        TopComponent active = TopComponent.getRegistry().getActivated();
        if( null != lookupRes ) {
            lookupRes.removeLookupListener( PaletteSwitch.this );
            lookupRes = null;
        }
        if( null != active ) {
            lookupRes = active.getLookup().lookup( new Lookup.Template<PaletteController>( PaletteController.class ) );
            lookupRes.addLookupListener( PaletteSwitch.this );
            lookupRes.allItems();
        }
    }
    
    private PropertyChangeListener createRegistryListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                if( TopComponent.Registry.PROP_CURRENT_NODES.equals( evt.getPropertyName() )
                    || TopComponent.Registry.PROP_OPENED.equals( evt.getPropertyName() )
                    || TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {

                    if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() )
                        || TopComponent.Registry.PROP_OPENED.equals( evt.getPropertyName() ) ) {
                        //listen to Lookup changes of showing editor windows
                        watchOpenedTCs();
                    }
                    
                    if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {
                        //switch lookup listener for the activated TC
                        switchLookupListener();
                    }
                    run();
                }
            }
        };
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        run();
    }
    
    private Map<TopComponent, Lookup.Result> watchedLkpResults = new WeakHashMap<TopComponent, Lookup.Result>(3);
    
    private void watchOpenedTCs() {
        ArrayList<TopComponent> windowsToWatch = findShowingTCs();
        ArrayList<TopComponent> toAddListeners = new ArrayList<TopComponent>( windowsToWatch );
        toAddListeners.removeAll(watchedLkpResults.keySet());
        
        ArrayList<TopComponent> toRemoveListeners = new ArrayList<TopComponent>( watchedLkpResults.keySet() );
        toRemoveListeners.removeAll(windowsToWatch);
        
        for( TopComponent tc : toRemoveListeners ) {
            Lookup.Result res = watchedLkpResults.get(tc);
            if( null != res ) {
                res.removeLookupListener(this);
                watchedLkpResults.remove(tc);
            }
        }
        
        for( TopComponent tc : toAddListeners ) {
            Lookup.Result res = tc.getLookup().lookup( new Lookup.Template<PaletteController>( PaletteController.class ) );
            res.addLookupListener( this );
            res.allItems();
            watchedLkpResults.put( tc, res );
        }
    }
    
    private ArrayList<TopComponent> findShowingTCs() {
        ArrayList<TopComponent> res = new ArrayList<TopComponent>( 3 );
        for( TopComponent tc : TopComponent.getRegistry().getOpened() ) {
            if( tc.isShowing() )
                res.add( tc );
        }
        return res;
    }
            
    private void findNewPalette( final PaletteController existingPalette, TopComponent activeTc,
                Set<TopComponent> openedTcs, boolean paletteIsMaximized, final Object token ) {
        PaletteController palette;
        palette = getPaletteFromTopComponent( activeTc, true, true );

        paletteIsMaximized &= isCurrentPaletteAvailable(existingPalette, openedTcs);

        ArrayList<PaletteController> availablePalettes = new ArrayList<PaletteController>(3);
        if( null == palette ) {
            for( Iterator i=openedTcs.iterator(); i.hasNext(); ) {
                TopComponent tc = (TopComponent)i.next();

                palette = getPaletteFromTopComponent( tc, true, true );
                if( null != palette ) {
                    availablePalettes.add( palette );
                }
            }
            if( null != existingPalette && (availablePalettes.contains( existingPalette ) || paletteIsMaximized) )
                palette = existingPalette;
            else if( availablePalettes.size() > 0 )
                palette = availablePalettes.get( 0 );
        }
        final PaletteController newPalette = palette;
        if (existingPalette == newPalette && null != newPalette) {
            return;
        }
        final boolean isNewVisible = PaletteVisibility.isVisible(newPalette) || PaletteVisibility.isVisible(null);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if( currentToken == token )
                    showHidePaletteTopComponent(newPalette, isNewVisible);
            }
        });
    }

    private boolean isCurrentPaletteAvailable( PaletteController existingPalette, Set<TopComponent> openedTcs ) {
        for( TopComponent tc : openedTcs ) {
            //check whether the window with the current palette controller wasn't closed
            PaletteController palette = getPaletteFromTopComponent( tc, false, true );
            if( null != palette && palette == existingPalette ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return True to auto-show/hide palette window when an editor with palette content
     * is activated, false to let the user open palette window manually.
     * @since 1.29
     */
    private static boolean isPaletteWindowEnabled() {
        boolean result = true;
        try {
            String resValue = NbBundle.getMessage(PaletteModule.class, "Palette.Window.Enabled" ); //NOI18N
            result = "true".equals( resValue.toLowerCase() ); //NOI18N
        } catch( MissingResourceException mrE ) {
            //ignore
        }
        return result;
    }
}
