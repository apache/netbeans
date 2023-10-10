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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

/**
 * Keeps track of all non-document windows. It is required to correctly reset the
 * whole window system because not it is allowed to mix view and editor windows
 * together.
 * 
 * @author S. Aubrecht
 * @since 2.33
 */
public final class TopComponentTracker {
    
    private final Set<String> viewIds = new HashSet<String>(30);
    private final Set<String> editorIds = new HashSet<String>(30);
    private final Set<TopComponent> editors = new WeakSet<TopComponent>( 100 );
    
    private static TopComponentTracker theInstance;
    
    private TopComponentTracker() {
    }
    
    /**
     * @return The one and only instance.
     */
    public static synchronized TopComponentTracker getDefault() {
        if( null == theInstance ) {
            theInstance = new TopComponentTracker();
        }
        return theInstance;
    }
    
    /**
     * Clear the list (when resetting/reloading the window system).
     */
    void clear() {
        viewIds.clear();
        editorIds.clear();
        editors.clear();
    }
    
    /**
     * Load window list from previous session.
     */
    void load() {
        Preferences prefs = getPreferences();
        try {
            for( String key : prefs.keys() ) {
                boolean view;
                try {
                   view = prefs.getBoolean( key, false );
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(TopComponentTracker.class.getName()).log(Level.INFO, "invalid preferences key", ex);
                    continue; // e.g invalid code point JDK-8075156
                }
                if( view )
                    viewIds.add( key );
                else
                    editorIds.add( key );
            }
        } catch( BackingStoreException ex ) {
            Logger.getLogger( TopComponentTracker.class.getName() ).log( Level.INFO, null, ex );
        }
    }
    
    /**
     * Store window list
     */
    void save() {
        Preferences prefs = getPreferences();
        try {
            prefs.clear();
        } catch( BackingStoreException ex ) {
            Logger.getLogger( TopComponentTracker.class.getName() ).log( Level.INFO, null, ex );
        }
        
        for( String id : viewIds ) {
            prefs.putBoolean( id, true );
        }
        for( String id : editorIds ) {
            prefs.putBoolean( id, false );
        }
    }
    
    /**
     * Track the given window
     * @param tc TopComponent
     * @param mode Mode the window is docked to
     */
    void add( TopComponent tc, ModeImpl mode ) {
        if( tc.getPersistenceType() == TopComponent.PERSISTENCE_NEVER )
            return;
        String tcId = WindowManagerImpl.getInstance().findTopComponentID( tc );
        if( null == tcId )
            return;
        if( viewIds.contains( tcId ) || editorIds.contains( tcId ) )
            return;
        if( mode.getKind() != Constants.MODE_KIND_EDITOR ) {
            if( editors.contains( tc ) ) {
                editorIds.add( tcId );
            } else {
                viewIds.add( tcId );
            }
        } else {
            editors.add( tc );
        }
    }
    
    /**
     * Track the given window
     * @param tc TopComponent id
     * @param mode Mode the window is docked to
     */
    void add( String tcId, ModeImpl mode ) {
        if( viewIds.contains( tcId ) || editorIds.contains( tcId ) )
            return;
        if( mode.getKind() != Constants.MODE_KIND_EDITOR )
            viewIds.add( tcId );
    }
    
    /**
     * @param tc
     * @return True if the given TopComponent is not a 'view'.
     */
    public boolean isEditorTopComponent( TopComponent tc ) {
        return !isViewTopComponent( tc );
    }
    
    /**
     * Check whether the given TopComponent is an editor window or a view window.
     * @param tc
     * @return True if the given TopComponent has persistence type <code>PERSISTENCE_ALWAYS</code>
     * or <code>PERSISTENCE_ONLY_OPENED</code> and was initially opened in a view or
     * sliding mode. Also returns true if the given TopComponent has peristence type
     * <code>PERSISTENCE_NEVER</code> and is docked in non-editor mode.
     * Returns false in all other cases.
     */
    public boolean isViewTopComponent( TopComponent tc ) {
        if( tc.getPersistenceType() == TopComponent.PERSISTENCE_NEVER ) {
            ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc );
            return null != mode && mode.getKind() != Constants.MODE_KIND_EDITOR;
        }
        String id = WindowManagerImpl.getInstance().findTopComponentID( tc );
        return id != null && viewIds.contains( id );
    }

    private Preferences getPreferences() {
        Preferences prefs = NbPreferences.forModule( TopComponentTracker.class ).node( "tctracker" ); //NOI18N
        String role = WindowManagerImpl.getInstance().getRole();
        if( null != role )
            prefs = prefs.node( role );
        return prefs;
    }
}
