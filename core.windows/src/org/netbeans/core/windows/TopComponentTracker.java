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
    public synchronized static TopComponentTracker getDefault() {
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
                boolean view = prefs.getBoolean( key, false );
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
