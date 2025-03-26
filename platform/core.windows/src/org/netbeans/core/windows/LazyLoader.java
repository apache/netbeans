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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.persistence.TCRefConfig;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * Deserializes only the selected TopComponent in each Mode, the remaining TopComponents
 * are deserialized and opened after the main window is showing.
 * 
 * @author S. Aubrecht
 */
final class LazyLoader {

    private static final boolean NO_LAZY_LOADING = Boolean.getBoolean( "nb.core.windows.no.lazy.loading" ); //NOI!8N
    private boolean isActive = false;
    private boolean isLoading = false;
    private final Map<ModeImpl, LazyMode> lazyModes = new HashMap<>(15);

    public LazyLoader() {
        WindowManagerImpl.getInstance().addWindowSystemListener( new WindowSystemListener() {

            @Override
            public void beforeLoad( WindowSystemEvent event ) {
                isLoading = false;
            }

            @Override
            public void afterLoad( WindowSystemEvent event ) {
                isActive = true;
                WindowManagerImpl.getInstance().invokeWhenUIReady( new Runnable() {

                    @Override
                    public void run() {
                        startLoading();
                    }
                });
            }

            @Override
            public void beforeSave( WindowSystemEvent event ) {
                isActive = false;
                loadAllNow();
            }

            @Override
            public void afterSave( WindowSystemEvent event ) {
            }
        });
    }

    void loadAllNow() {
        isActive = false;
        isLoading = true;
        PersistenceHandler persistenceHandler = PersistenceHandler.getDefault();
        for( LazyMode lazyMode : lazyModes.values() ) {
            for( String tcId : lazyMode.getTopComponents() ) {
                TopComponent tc = persistenceHandler.getTopComponentForID( tcId, true );
                if( null != tc && !tc.isOpened() ) {
                    lazyMode.mode.addOpenedTopComponent( tc, lazyMode.getPosition( tcId ) );
                }
            }
        }
        lazyModes.clear();
    }

    private void startLoading() {
        isLoading = true;
        ArrayList<LazyMode> sortedLazyModes = new ArrayList<LazyMode>( lazyModes.values() );
        //load view modes first
        Collections.sort( sortedLazyModes );
        for( LazyMode lazyMode : sortedLazyModes ) {
            for( String tcId : lazyMode.getTopComponents() ) {
                final String tcId2Load = tcId;
                final ModeImpl targetMode = lazyMode.mode;
                final int position = lazyMode.getPosition( tcId );
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        loadNow( targetMode, tcId2Load, position );
                    }
                });
            }
        }
    }

    void lazyLoad( ModeImpl mode, String selectedTCid, TCRefConfig tcRefConfig, int index ) {
        if( NO_LAZY_LOADING || tcRefConfig.tc_id.equals( selectedTCid ) || isLoading ) {
            TopComponent tc = PersistenceHandler.getDefault().getTopComponentForID(tcRefConfig.tc_id,true);
            if(tc != null) {
                mode.addOpenedTopComponent(tc);
            }
            if( !NO_LAZY_LOADING ) {
                LazyMode lazyMode = getLazyMode( mode );
                lazyMode.selectedTCposition = index;
            }
        } else {
            mode.addUnloadedTopComponent(tcRefConfig.tc_id);
            LazyMode lazyMode = getLazyMode( mode );
            lazyMode.add( tcRefConfig.tc_id, index );
        }
    }


    private void loadNow( ModeImpl mode, String tcId, int position ) {
        if( !isActive )
            return;

        TopComponent tc = PersistenceHandler.getDefault().getTopComponentForID( tcId, true );
        if( null != tc && !tc.isOpened() ) {
            if( position < 0 )
                position = mode.getOpenedTopComponentsIDs().size();
            mode.addOpenedTopComponent( tc, position );
        }
        remove( mode, tcId );
    }

    private void remove( ModeImpl mode, String tcId ) {
        LazyMode lazyMode = getLazyMode( mode );
        lazyMode.id2position.remove( tcId );
    }

    private LazyMode getLazyMode( ModeImpl mode ) {
        LazyMode res = lazyModes.get( mode );
        if( null == res ) {
            res = new LazyMode( mode );
            lazyModes.put( mode, res );
        }
        return res;
    }

    private static class LazyMode implements Comparable<LazyMode> {
        private int selectedTCposition;
        private final ModeImpl mode;
        private final Map<String, Integer> id2position = new HashMap<>(30);

        public LazyMode( ModeImpl mode ) {
            this.mode = mode;
        }

        private void add( String tc_id, int index ) {
            id2position.put( tc_id, index );
        }

        @Override
        public int compareTo( LazyMode o ) {
            if( mode.getKind() != o.mode.getKind() ) {
                if( mode.getKind() == Constants.MODE_KIND_EDITOR )
                    return 1;
                else if( o.mode.getKind() == Constants.MODE_KIND_EDITOR )
                    return -1;
            }
            return 0;
        }

        Collection<String> getTopComponents() {
            ArrayList<String> res = new ArrayList<String>( id2position.keySet() );
            res.sort(new Comparator<String>() {

                @Override
                public int compare( String o1, String o2 ) {
                    int position1 = id2position.get( o1 );
                    int position2 = id2position.get( o2 );
                    int res = position1 - position2;
                    if( position1 < selectedTCposition && position2 < selectedTCposition )
                        res *= -1;
                    return res;
                }
            } );
            return res;
        }

        int getPosition( String tcId ) {
            if( !id2position.containsKey( tcId ) )
                return -1;
            int position = id2position.get( tcId );
            if( position <= selectedTCposition ) {
                return 0;
            }
            return -1;
        }
    }
}
