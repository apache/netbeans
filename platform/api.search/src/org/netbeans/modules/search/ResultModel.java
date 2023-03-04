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


package org.netbeans.modules.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.charset.Charset;
import java.util.List;
import org.netbeans.modules.search.Constants.Limit;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;


/**
 * Holds search result data.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public final class ResultModel {

    public static final String PROP_SELECTION = "selection";            //NOI18N
    public static final String PROP_VALID = "valid";                    //NOI18N
    public static final String PROP_MATCHING_OBJECTS =
            "matchingObjects";                                          //NOI18N
    /** Fired when results were modified by the user. */
    public static final String PROP_RESULTS_EDIT = "resultsEdit";       //NOI18N

    /** */
    private long startTime;
    /** */
    private int totalDetailsCount = 0;
    /**
    
    /**
     * limit (number of found files or matches) reached during search
     */
    private Limit limitReached = null;
    /** */
    final BasicSearchCriteria basicCriteria;
    /** */
    final BasicComposition basicComposition;
    /** */
    private final boolean isFullText;
    /** */
    final String replaceString;
    /** */
    final boolean searchAndReplace;
    /** list of matching objects (usually {@code DataObject}s) */
    private final List<MatchingObject> matchingObjects =
            new ArraySet<MatchingObject>(Constants.COUNT_LIMIT).ordering(false).
            nullIsAllowed(false);

    /** Contains optional finnish message often reason why finished. */
    private String finishMessage;
    /** model is valid if all selected matching objects are valid */
    private volatile boolean valid = true;
    /** Property change support */
    private final PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(this);
    private int selectedMatches = 0;
    private final MatchingObjectListener matchingObjectListener
            = new MatchingObjectListener();

    /** Creates new <code>ResultModel</code>. */
    ResultModel(BasicSearchCriteria basicSearchCriteria,
                       String replaceString, BasicComposition basicComposition) {

        this.replaceString = replaceString;
        this.searchAndReplace = (replaceString != null);
        this.basicComposition = basicComposition;

	basicCriteria = basicSearchCriteria;
	isFullText = (basicCriteria != null) && basicCriteria.isFullText();        
        startTime = -1;
    }

    /**
     * Remove the {@link MatchingObject} from the model and inform the
     * listeners.
     *
     * @param mo Matching object to remove.
     */
    public synchronized boolean remove(MatchingObject mo) {
        if (matchingObjects.remove(mo)) {
            totalDetailsCount -= mo.getMatchesCount();
            int deselected = 0;
            if (mo.getTextDetails() != null) {
                for (TextDetail td : mo.getTextDetails()) {
                    deselected += td.isSelected() ? -1 : 0;
                }
            }
            mo.cleanup();
            // inform model listeners, old object contains removed object
            propertyChangeSupport.firePropertyChange(PROP_RESULTS_EDIT,
                    null, null);
            if (deselected < 0) {
                updateSelected(deselected);
            }
            if (mo.isSelected() && !mo.isValid()) {
                checkValid();
            }
            return true;
        }
        return false;
    }

    public synchronized void removeDetailMatch(MatchingObject mo,
            TextDetail txtDetail) {

        if (txtDetail.isSelected()) {
            updateSelected(-1);
        }
        totalDetailsCount--;
        propertyChangeSupport.firePropertyChange(PROP_RESULTS_EDIT,
                null, null);
        // delete parent node if no children left
        if (mo.textDetails.isEmpty()) {
            remove(mo);
        }
    }
    
    /**
     */
    synchronized long getStartTime() {
        if (startTime == -1) {
            throw new IllegalStateException(
                    "Search has not started yet");                      //NOI18N
        }
        return startTime;
    }
    
    /**
     * Clean the allocated resources. Do not rely on GC there as we are often
     * referenced from various objects. So keep leak as small as possible.
     * */
    public synchronized void close() {
        if ((matchingObjects != null) && !matchingObjects.isEmpty()) {
            for (MatchingObject matchingObj : matchingObjects) {
                matchingObj.cleanup();
            }
        }
        
        // eliminate search group content
        // no other way then leaving it on GC, it should work because
        // search group is always recreated by a it's factory and
        // nobody keeps reference to it. 7th May 2004
    }

    /**
     * Notifies ths result model of a newly found matching object.
     *
     * @param  object  matching object
     * @return  {@code true} if this result model accepted the found object,
     *          {@code false} if number of found objects reached the limit, or
     *          the specified {@code object} already exists in the result model.
     * @param  charset  charset used for full-text search of the object,
     *                  or {@code null} if the object was not full-text searched
     */
    public synchronized boolean objectFound(FileObject object, Charset charset,
            List<TextDetail> textDetails) {
        assert limitReached == null;
        MatchingObject mo = new MatchingObject(this, object, charset,
                textDetails);
        boolean added = add(mo);
        if(added) {
            totalDetailsCount += getDetailsCount(mo);
            int newSelectedMatches = 0;
            if (mo.getTextDetails() != null) {
                mo.addPropertyChangeListener(matchingObjectListener);
                for (TextDetail td : mo.getTextDetails()) {
                    if (td.isSelected()) {
                        newSelectedMatches++;
                    }
                }
                updateSelected(newSelectedMatches);
            }
            propertyChangeSupport.firePropertyChange(PROP_MATCHING_OBJECTS,
                    null, null);
        } else {
            mo.cleanup();
        }
        checkLimits();
        return added;
    }

    private boolean add(MatchingObject matchingObject) {
        try {
            return matchingObjects.add(matchingObject);
        } catch (IllegalStateException ise) {
            limitReached = Limit.FILES_COUNT_LIMIT;
            return false;
        } catch(IllegalArgumentException iae) {
            return false; // matchingObject already added.
        }
    }

    private boolean checkLimits() {
// ArraySet and add(MatchingObject matchingObject) do it.
//        if (size() >= COUNT_LIMIT) {
//            limitReached = Limit.FILES_COUNT_LIMIT;
//            return true;
//        }
//        else
        if (totalDetailsCount >= Constants.DETAILS_COUNT_LIMIT) {
            limitReached = Limit.MATCHES_COUNT_LIMIT;
            return true;
        }
        return false;
    }

    /**
     * Called when an invalid object was selected, or when a selected object
     * became invalid. If validity state of the model changes, fire changes.
     */
    private void setInvalid() {
        if (valid) {
            valid = false;
            propertyChangeSupport.firePropertyChange(PROP_VALID, true, false);
        }
    }

    /**
     * If an invalid object was deselected, or when a selected object became
     * valid again, we have to check whether all selected objects are now valid.
     * If validity state of the model changes, fire changes.
     */
    private void checkValid() {

        boolean allValid = true;
        for (MatchingObject mo : getMatchingObjects()) {
            if (mo.isSelected() && !mo.isValid()) {
                allValid = false;
            }
        }

        if (valid != allValid) {
            valid = allValid;
            propertyChangeSupport.firePropertyChange(PROP_VALID,
                    !allValid, allValid);
        }
    }

    private void objectValidityChanged(MatchingObject mo) {
        if (mo.isSelected()) {
            if (mo.isValid()) {
                synchronized (this) {
                    totalDetailsCount = 0;
                    for (MatchingObject item : matchingObjects) {
                        totalDetailsCount += item.getDetailsCount();
                    }
                }
                checkValid();
            } else {
                setInvalid();
            }
        }
    }

    private void objectSelectionChanged(MatchingObject mo) {
        if (!mo.isValid()) {
            if (mo.isSelected()) {
                setInvalid();
            } else {
                checkValid();
            }
        }
    }

    /**
     */
    public synchronized int getTotalDetailsCount() {
        return totalDetailsCount;
    }
    
    /**
     * @return a list of the {@code MatchingObject}s associated to this
     * {@code ResultModel}.
     */
    public synchronized List<MatchingObject> getMatchingObjects() {
        return matchingObjects;
    }
    
    /**
     */
    public boolean hasDetails() {
        return totalDetailsCount != 0;      //PENDING - synchronization?
    }
    
    /**
     * Check whether search results have text details.
     */
     public boolean canHaveDetails() {
       return isFullText;
    }
    
    /*
     * A cache exists for information about a single MatchingObject
     * to prevent from repetitive calls of time-consuming queries on
     * number of details and list of details. These calls are initiated
     * by the node renderer (class NodeRenderer).
     */
    
    private MatchingObject infoCacheMatchingObject;
    private Boolean infoCacheHasDetails;
    private int infoCacheDetailsCount;
    private Node[] infoCacheDetailNodes;
    private final Node[] EMPTY_NODES_ARRAY = new Node[0];
    
    /**
     */
    private void prepareCacheFor(MatchingObject matchingObject) {
        if (matchingObject != infoCacheMatchingObject) {
            infoCacheHasDetails = null;
            infoCacheDetailsCount = -1;
            infoCacheDetailNodes = null;
            infoCacheMatchingObject = matchingObject;
        }
    }
    
    /**
     */
    synchronized int getDetailsCount(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        if (infoCacheDetailsCount == -1) {
            infoCacheDetailsCount = getDetailsCountReal(matchingObject);
            if (infoCacheDetailsCount == 0) {
                infoCacheDetailNodes = EMPTY_NODES_ARRAY;
            }
        }
        
        assert infoCacheDetailsCount >= 0;
        return infoCacheDetailsCount;
    }
    
    /**
     * Returns number of detail nodes available to the given found object.
     *
     * @param  matchingObject a reference the search criteria
     * @return  number of detail items (represented by individual nodes)
     *          available for the given object (usually {@code DataObject})
     */
    private int getDetailsCountReal(MatchingObject matchingObject) {
        int count = isFullText ? 
                matchingObject.getDetailsCount() : 0;
        
        return count;
    }
    
    /**
     * Gets detail nodes associated with the specified {@code MatchingObject}.
     * @param matchingObject the {@code MatchingObject} or {@code null}.
     * @return  non-empty array of detail nodes
     *          or {@code null} if either there are no associated detail nodes
     *          or {@code matchingObject} is {@code null}.
     */
    synchronized Node[] getDetails(MatchingObject matchingObject) {
        if(matchingObject == null) {
            return null;
        }
        prepareCacheFor(matchingObject);
        Node[] detailNodes;
        if (infoCacheDetailNodes == null) {
            detailNodes = getDetailsReal(matchingObject);
            infoCacheDetailNodes = (detailNodes != null)
                                   ? detailNodes
                                   : EMPTY_NODES_ARRAY;
            infoCacheDetailsCount = infoCacheDetailNodes.length;
        } else {
            detailNodes = (infoCacheDetailNodes != EMPTY_NODES_ARRAY)
                          ? infoCacheDetailNodes
                          : null;
        }
        
        assert (infoCacheDetailNodes != null)
               && ((infoCacheDetailNodes == EMPTY_NODES_ARRAY)
                   || (infoCacheDetailNodes.length > 0));
        assert (detailNodes == null) || (detailNodes.length > 0);
        return detailNodes;
    }
    
    /**
     * 
     * @return  non-empty array of detail nodes
     *          or {@code null} if there are no detail nodes
     */
    private Node[] getDetailsReal(MatchingObject matchingObject) {
        Node[] nodesTotal = null;
        if (basicCriteria != null) {
            nodesTotal = basicCriteria.isFullText()
                         ? matchingObject.getDetails()
                         : null;
	}
        
        return nodesTotal;                
    }
    
    /**
     */
    public synchronized int size() {
        return matchingObjects.size();
    }

    /**
     */
    public synchronized boolean wasLimitReached() {
        return limitReached != null;
    }

    /**
     */
    public String getLimitDisplayName() {
        return (limitReached != null) ? limitReached.getDisplayName() : null;
    }

    /** This exception stoped search */
    synchronized void searchException(RuntimeException ex) {
        ErrorManager.Annotation[] annotations =
                ErrorManager.getDefault().findAnnotations(ex);
        for (ErrorManager.Annotation annotation : annotations) {
            if (annotation.getSeverity() == ErrorManager.USER) {
                finishMessage = annotation.getLocalizedMessage();
                if (finishMessage != null) return;
            }
        }
        finishMessage = ex.getLocalizedMessage();
    }
    
    /**
     */
    public synchronized String getExceptionMsg() {
        return finishMessage;
    }

    public boolean isSearchAndReplace() {
        return searchAndReplace;
    }

    public synchronized int getSelectedMatchesCount() {
        return selectedMatches;
    }

    public boolean isValid() {
        return valid;
    }

    private void updateSelected(final int inc) {
        int origSelected;
        int newSelected;
        synchronized (this) {
            origSelected = selectedMatches;
            newSelected = origSelected + inc;
            selectedMatches = newSelected;
        }
        propertyChangeSupport.firePropertyChange(PROP_SELECTION,
                origSelected, newSelected);
    }

    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName,
                listener);
    }

    synchronized void setStartTime() {
        if (startTime != -1) {
            throw new IllegalStateException();
        } else {
            startTime = System.currentTimeMillis();
        }
    }

    private class MatchingObjectListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            MatchingObject mo;
            if (source instanceof MatchingObject) {
                mo = (MatchingObject) source;
            } else {
                throw new IllegalArgumentException();
            }
            String pn = evt.getPropertyName();
            if (MatchingObject.PROP_MATCHES_SELECTED.equals(pn)) {
                Object newVal = evt.getNewValue();
                Object oldVal = evt.getOldValue();
                if (newVal instanceof Integer && oldVal instanceof Integer) {
                    updateSelected((Integer) newVal - (Integer) oldVal);
                } else {
                    throw new IllegalArgumentException();
                }
            } else if (MatchingObject.PROP_INVALIDITY_STATUS.equals(pn)) {
                objectValidityChanged(mo);
            } else if (MatchingObject.PROP_SELECTED.equals(pn)) {
                objectSelectionChanged(mo);
            }
        }
    }
}
