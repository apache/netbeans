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


package org.netbeans.core.windows.model;


import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.options.WinSysPrefs;

/**
 * Model which stored TopComponents in one mode. It manages opened, closed
 * and selected TopComponent.
 * This sub model is not thread safe. It is supposed to be just part of DefaultModeModel
 * which is responsible for the synch.
 *
 * @author  Peter Zavadsky
 */
final class TopComponentSubModel {

    /** List of opened TopComponents. */
    private final List<TopComponent> openedTopComponents = new ArrayList<TopComponent>(10);
    /** List of all TopComponent IDs (both opened and closed). */
    private final List<String> tcIDs = new ArrayList<String>(10);
    /** kind of mode model this sub model is part of */
    private final int kind;
    /** Selected TopComponent ID. Has to be present in openedTopComponenets. */
    private String selectedTopComponentID;
    /** ID of top component that was the selected one before switching to/from maximized mode */
    private String previousSelectedTopComponentID;

    public TopComponentSubModel(int kind) {
        this.kind = kind;
    }
    
    public List<TopComponent> getTopComponents() {
        List<TopComponent> l = new ArrayList<TopComponent>(openedTopComponents);
        
        List<String> ids = new ArrayList<String>(tcIDs);
        List<TopComponent> ll = new ArrayList<TopComponent>(ids.size());
        for(Iterator<String> it = ids.iterator(); it.hasNext(); ) {
            String tcID = it.next();
            TopComponent tc = getTopComponent(tcID);
            if(tc != null) {
                ll.add(tc);
            } else {
                // XXX TopComponent was garbaged, remove its ID.
                it.remove();
            }
        }
        ll.removeAll(openedTopComponents);
        l.addAll(ll);
        
        return l;
    }
    
    public List<TopComponent> getOpenedTopComponents() {
        return new ArrayList<TopComponent>(openedTopComponents);
    }

    public boolean addOpenedTopComponent(TopComponent tc) {
        if(openedTopComponents.contains(tc)) {
            return false;
        }

        String tcID = getID(tc);
        int index = tcIDs.indexOf(tcID);
        
        int position = openedTopComponents.size();
        if( index >= 0 ) {
            for( TopComponent otc : openedTopComponents ) {
                String otcID = getID(otc);
                int openedIndex = tcIDs.indexOf( otcID );
                if( openedIndex >= index ) {
                    position = openedTopComponents.indexOf( otc );
                    break;
                }
            }
        }
        if( kind == Constants.MODE_KIND_EDITOR 
                && WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.OPEN_DOCUMENTS_NEXT_TO_ACTIVE_TAB, false)
                && selectedTopComponentID != null ) {
            
            for (int i = 0; i < openedTopComponents.size(); i++) {
                if (selectedTopComponentID.equals(getID(openedTopComponents.get(i)))) {
                    position = i + 1;
                    break;
                }
            }
        }
        // additional double check if we got the same instance of topcomponent
        //#39914 + #43401 - no need to remove this one without fixing the inconsistency, it will fail later on TabbedAdapter.
        TopComponent persTC = getTopComponent(tcID);
        if (persTC != tc) {
            String message = "Model in inconsistent state, generated TC ID=" + tcID + " for " + tc.getClass() + ":" + tc.hashCode() + " but" +
            " that ID is reserved for TC=" + persTC.getClass() + ":" + persTC.hashCode();
            assert false : message;
        }
        //-- end of check..
        openedTopComponents.add(position, tc);
        if(!tcIDs.contains(tcID)) {
            tcIDs.add(tcID);
        }
        
        if(selectedTopComponentID == null && !isNullSelectionAllowed()) {
            selectedTopComponentID = tcID;
        }
        
        // XXX - should be deleted after TopComponent.isSliding is introduced
        if (kind == Constants.MODE_KIND_SLIDING) {
            setSlidingProperty(tc);
        } else {
            clearSlidingProperty(tc);
        }
        
        return true;
    }
    
    public boolean insertOpenedTopComponent(TopComponent tc, int index) {
        if(index >= 0
        && !openedTopComponents.isEmpty()
        && openedTopComponents.size() > index
        && openedTopComponents.get(index) == tc) {
            return false;
        }
        
        // Remove from previous index
        openedTopComponents.remove(tc);
        
        int position = index;
        if(position < 0) {
            position = 0;
        } else if(position > openedTopComponents.size()) {
            position = openedTopComponents.size();
        }

        String tcID = getID(tc);
        tcIDs.remove(tcID);
        openedTopComponents.add(position, tc);
        if(position == 0) {
            tcIDs.add(0, tcID);
        } else {
            TopComponent previous = (TopComponent)openedTopComponents.get(position - 1);
            int previousIndex = tcIDs.indexOf(getID(previous));
            tcIDs.add(previousIndex + 1, tcID);
        }
        
        if(selectedTopComponentID == null && !isNullSelectionAllowed()) {
            selectedTopComponentID = getID(tc);
        }
        
        // XXX - should be deleted after TopComponent.isSliding is introduced
        if (kind == Constants.MODE_KIND_SLIDING) {
            setSlidingProperty(tc);
        } else {
            clearSlidingProperty(tc);
        }
        
        return true;
    }
    
    public boolean addClosedTopComponent(TopComponent tc) {
        int index = openedTopComponents.indexOf(tc);
        String tcID = getID(tc);
        if (!tcIDs.contains(tcID)) {
            tcIDs.add(tcID);
        }
        if(index != -1) {
            openedTopComponents.remove(tc);
            if (selectedTopComponentID != null && selectedTopComponentID.equals(getID(tc))) {
                adjustSelectedTopComponent(index, null);
            }
        } 
        
        
        // XXX - should be deleted after TopComponent.isSliding is introduced
        if (kind == Constants.MODE_KIND_SLIDING) {
            setSlidingProperty(tc);
        } else {
            clearSlidingProperty(tc);
        }
        
        return true;
    }
    
    public boolean addUnloadedTopComponent(String tcID, int index) {
        if(!tcIDs.contains(tcID)) {
            if( index >= 0 && index < tcIDs.size() )
                tcIDs.add(index, tcID);
            else
                tcIDs.add(tcID);
        }
        
        return true;
    }
    
    /**
     * Remove the given TopComponent from this Mode.
     * @param tc TopComponent to be removed
     * @param recentTc TopComponent to select if the removed one was selected. 
     * If null then the TopComponent nearest to the removed one will be selected.
     */
    public boolean removeTopComponent(TopComponent tc, TopComponent recentTc) {
        boolean res;
        String tcID = getID(tc);
        if(openedTopComponents.contains(tc)) {
            if(selectedTopComponentID != null && selectedTopComponentID.equals(tcID)) {
                int index = openedTopComponents.indexOf(getTopComponent(selectedTopComponentID));
                openedTopComponents.remove(tc);
                adjustSelectedTopComponent(index, recentTc);
            } else {
                openedTopComponents.remove(tc);
            }
            tcIDs.remove(tcID);
            
            res = true;
        } else if(tcIDs.contains(tcID)) {
            tcIDs.remove(tcID);
            res = true;
        } else {
            res = false;
        }

        // XXX - should be deleted after TopComponent.isSliding is introduced
        clearSlidingProperty(tc);
        
        return res;
    }

    public boolean containsTopComponent(TopComponent tc) {
        return openedTopComponents.contains(tc) || tcIDs.contains(getID(tc));
    }
    
    public int getOpenedTopComponentTabPosition (TopComponent tc) {
        return openedTopComponents.indexOf(tc);
    }
    
    public boolean isEmpty() {
        return tcIDs.isEmpty();
    }
    
    private void adjustSelectedTopComponent(int index, TopComponent recentTc) {
        if(openedTopComponents.isEmpty() || isNullSelectionAllowed()) {
            selectedTopComponentID = null;
            return;
        }
        
        if( null != recentTc && openedTopComponents.contains( recentTc ) ) {
            selectedTopComponentID = getID(recentTc);
        } else {
            if(index > openedTopComponents.size() - 1) {
                index = openedTopComponents.size() - 1;
            }

            selectedTopComponentID = getID((TopComponent)openedTopComponents.get(index));
        }
    }

    /** @return true for sliding kind of model, false otherwise. It means that
     * null selection is valid only in sliding kind of model.
     */
    private boolean isNullSelectionAllowed() {
        return kind == Constants.MODE_KIND_SLIDING;
    }

    /** Sets selected component. Note that for sliding kind null selection
     * is allowed
     */
    public void setSelectedTopComponent(TopComponent tc) {
        if(tc != null && !openedTopComponents.contains(tc)) {
            return;
        }
        
        if (tc == null && isNullSelectionAllowed()) {
            selectedTopComponentID = null;
        } else {
            selectedTopComponentID = getID(tc);
        }
    }
    
    public void setPreviousSelectedTopComponentID(String tcId) {
        previousSelectedTopComponentID = tcId;
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        if(tcID != null && !getOpenedTopComponentsIDs().contains(tcID)) {
            return;
        }
        
        selectedTopComponentID = tcID;
    }
    
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        previousSelectedTopComponentID = tcID;
    }
    
    public List<String> getOpenedTopComponentsIDs() {
        List<String> l = new ArrayList<String>(openedTopComponents.size());
        for(Iterator<TopComponent> it = openedTopComponents.iterator(); it.hasNext(); ) {
            l.add(getID(it.next()));
        }
        return l;
    }
    
    // XXX
    public List<String> getClosedTopComponentsIDs() {
        List<String> closedIDs = new ArrayList<String>(tcIDs);
        closedIDs.removeAll(getOpenedTopComponentsIDs());
        return closedIDs;
    }

    // XXX
    public List<String> getTopComponentsIDs() {
        return new ArrayList<String>(tcIDs);
    }
    
    // XXX
    public void removeClosedTopComponentID(String tcID) {
        tcIDs.remove(tcID);
    }
    
    public TopComponent getSelectedTopComponent() {
        return getTopComponent(selectedTopComponentID);
    }
    
    public String getPreviousSelectedTopComponentID() {
        return previousSelectedTopComponentID;
    }

    private static TopComponent getTopComponent(String tcID) {
        return WindowManagerImpl.getInstance().getTopComponentForID(tcID);
    }
    
    private static String getID(TopComponent tc) {
        return WindowManagerImpl.getInstance().findTopComponentID(tc);
    }

    
    // XXX - should be deleted after TopComponent.isSliding is introduced
    private static final String IS_SLIDING = "isSliding";
    
    // XXX - should be deleted after TopComponent.isSliding is introduced
    private void setSlidingProperty(TopComponent tc) {
        tc.putClientProperty(IS_SLIDING, Boolean.TRUE);
    }

    // XXX - should be deleted after TopComponent.isSliding is introduced
    private void clearSlidingProperty(TopComponent tc) {
        tc.putClientProperty(IS_SLIDING, null);
    }

    void setOpenedTopComponents( List<TopComponent> opened ) {
        this.openedTopComponents.clear();
        this.openedTopComponents.addAll( opened );
    }
}
