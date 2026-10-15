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

import java.util.Set;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instance of this class keeps list of (weak references to) recently activated TopComponents.
 *
 * @author  Marek Slama
 */
final class RecentViewList implements PropertyChangeListener {

    /** List of TopComponent IDs. First is most recently
     * activated. */
    private List<String> tcIdList = new ArrayList<>(20);
    private Map<String, Reference<TopComponent>> tcCache = new HashMap<String, Reference<TopComponent>>(20);

    public RecentViewList (WindowManager wm) {
        // Starts listening on Registry to be notified about activated TopComponent.
        wm.getRegistry().addPropertyChangeListener(this);
    }


    /** Used to get array for view and for persistence */
    public TopComponent [] getTopComponents() {
        List<TopComponent> tcList = new ArrayList<TopComponent>(tcIdList.size());
        WindowManager wm = WindowManager.getDefault();
        for (int i = 0; i < tcIdList.size(); i++) {
            String tcId = tcIdList.get( i );
            TopComponent tc = null;
            Reference<TopComponent> ref = tcCache.get( tcId );
            if( null != ref ) {
                tc = ref.get();
            }
            if( null == tc ) {
                tc = wm.findTopComponent( tcId );
                if( null != tc )
                    tcCache.put( tcId, new WeakReference<TopComponent>( tc ) );
            }
            if ((tc != null) && tc.isOpened()) {
                tcList.add(tc);
            }
        }
        return tcList.toArray(new TopComponent[0]);
    }

    public String [] getTopComponentIDs() {
        return tcIdList.toArray(new String[0]);
    }

    /** Used to set initial values from persistence */
    public void setTopComponents(String[] tcIDs) {
        tcIdList.clear();
        tcIdList.addAll( Arrays.asList( tcIDs ) );
        tcCache.clear();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) evt.getNewValue();
            if (tc != null) {
                String tcId = WindowManager.getDefault().findTopComponentID( tc );
                //Update list
                tcIdList.remove( tcId );
                tcIdList.add( 0, tcId );
                // #69486: ensure all components are listed
                fillList(TopComponent.getRegistry().getOpened());
            }
        }
    }

    /** Fills list of weak references with TCs that are in given
     * input list but are not yet contained in list of weak references.
     */
    private void fillList(Set<TopComponent> openedTCs) {
        tcCache.clear();
        WindowManager wm = WindowManager.getDefault();
        for (TopComponent curTC: openedTCs) {
            String id = wm.findTopComponentID( curTC );
            if( !tcIdList.contains( id ) ) {
                if( tcIdList.size() > 1 )
                    tcIdList.add( 1, id );
                else
                    tcIdList.add( id );
            }
            tcCache.put( id, new WeakReference<TopComponent>( curTC ) );
        }
    }

}
