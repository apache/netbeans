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

package org.netbeans.installer.utils.progress;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;

/**
 * A specialized subclass of {@link Progress} which is capable having child 
 * progresses.
 * 
 * <p>
 * Each registered child has an assosiated percentage number. It is called "relative 
 * percentage" and basically means which part of the composite's total percentage 
 * does this child "own". Additionally the composite has its "own percentage", which 
 * is independent of the children.
 * 
 * <p>
 * The composite registers itself as a listener on each added child and updates its
 * total percentage according to the children's states. It also propagates the 
 * change information to its own listeners.
 * 
 * <p>
 * The synchronization behavior of a composite progress is not completely defined, 
 * if the composite serves as the target of a synchronization link. It will behave 
 * exactly as a regular progress if it has no children, but in the other case, 
 * errors may arise if the synchronization routine attempts to set an illegal 
 * percentage, i.e. if this "attempted" percentage will conflict with the children.
 * 
 * <p>
 * An additional capability of a composite progress is its ability to fetch 
 * children's detail status whenever a child is updated. This bahaior is controlled 
 * by the <code>synchronizeDetails</code> flag.
 * 
 * @author Kirill Sorokin
 * 
 * @since 1.0
 */
public final class CompositeProgress extends Progress implements ProgressListener {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The list of child progresses.
     */
    private List<Progress> progresses;
    
    /**
     * The list of childrens' relative percentages.
     */
    private List<Integer>  percentages;
    
    /**
     * The flag which controls whether the composite will adopt its children's 
     * detail values. If it's set to true, then whenever a child's state changes,
     * the composite will update its detail with the detail of the just updated 
     * child.
     */
    private boolean synchronizeDetails;
    
    // constructors /////////////////////////////////////////////////////////////////
    /**
     * Creates a new {@link CompositeProgress} instance.
     */
    public CompositeProgress() {
        super();
        
        progresses = new LinkedList<Progress>();
        percentages = new LinkedList<Integer>();
        
        synchronizeDetails = false;
    }
    
    /**
     * Creates a new {@link CompositeProgress} instance and registers the supplied 
     * listener.
     *
     * @param initialListener A progress listener to register upon progress
     *      creation.
     * 
     * @see #CompositeProgress()
     */
    public CompositeProgress(final ProgressListener initialListener) {
        this();
        
        addProgressListener(initialListener);
    }
    
    // overrides ////////////////////////////////////////////////////////////////////
    /**
     * Returns the total percentage of the composite, i.e. its own percentage and 
     * sum of percentages of its child progresses (each multiplied by the relative 
     * percentage of that child).
     * 
     * @return The total percentage of the composite.
     */
    @Override
    public int getPercentage() {
        int total = 0;
        for (int i = 0; i < progresses.size(); i++) {
            total += progresses.get(i).getPercentage() * percentages.get(i);
        }
        
        total = (total / COMPLETE) + percentage;
        
        return total;
    }
    
    /**
     * Sets the core composite's percentage. The value is updated only if the
     * supplied percentage is different from the current. Also the progress
     * listeners are notified only if the update actually took place.
     *
     * @param percentage The new value for the core composite's percentage.
     *
     * @throws IllegalArgumentException if the supplied percentage cannot be 
     *      set.
     */
    @Override
    public void setPercentage(final int percentage) {
        if (this.percentage != percentage) {
            if (!evaluatePercentage(percentage)) {
                throw new IllegalArgumentException(StringUtils.format(
                        ERROR_WRONG_PERCENTAGE,
                        percentage,
                        START,
                        COMPLETE));
            }
            
            this.percentage = percentage;
            
            notifyListeners();
        }
    }
    
    /**
     * Adds the specified amount to the core composite's percentage. The added
     * amount can be either positive or negative. The percentage value will be
     * updated only if the result of the addition is different from the current core
     * percentage. Also the listeners are notified only is the update actually took
     * place.
     *
     * @param addition The amount to add to the core composite's percentage.
     *
     * @throws {@IllegalArgumentException} if the supplied percentage cannot be
     *      added.
     */
    @Override
    public void addPercentage(final int addition) {
        final int result = this.percentage + addition;
        
        if (this.percentage != result) {
            if (!evaluatePercentage(result)) {
                throw new IllegalArgumentException(StringUtils.format(
                        ERROR_WRONG_PERCENTAGE,
                        addition,
                        START,
                        COMPLETE));
            }
            
            this.percentage = result;
            
            notifyListeners();
        }
    }
    
    /**
     * Sets the value of the <code>canceled</code> property. The value is updated
     * only if the supplied value is different from the current. Also the progress
     * listeners are notified only if the update actually took place.
     *
     * <p>
     * If the progress is in being synchronized from another progress. The cancelled
     * status will be propagated to the synchronization source.
     *
     * <p>
     * Additionally this method propagates the <code>canceled</code> value to the
     * child progresses.
     *
     * @param canceled The new value for the <code>canceled</code> property.
     */
    @Override
    public void setCanceled(final boolean canceled) {
        super.setCanceled(canceled);
        
        for (Progress child: progresses) {
            child.setCanceled(canceled);
        }
    }
    
    // composite-specific methods ///////////////////////////////////////////////////
    /**
     * Add a new child progress to the composite.
     *
     * @param progress The child progress to add to the composite.
     * @param percentage The child's relative percentage within the composite.
     *
     * @throws IllegalArgumentException if the supplied percentage cannot be
     *      added.
     */
    public void addChild(final Progress progress, final int percentage) {
        // check wehther we can add a new child with the given percentage       
        if (!evaluatePercentage(percentage)) {
            throw new IllegalArgumentException(StringUtils.format(
                    ERROR_WRONG_PERCENTAGE,
                    percentage,
                    START,
                    COMPLETE));
        }
        
        progresses.add(progress);
        percentages.add(percentage);
        
        progress.addProgressListener(this);
        
        notifyListeners();
    }
    /**
     * Remove a child progress from the composite.
     *
     * @param progress The child progress to remove from the composite.     
     *
     * @throws IllegalArgumentException if the supplied progress is not in 
     *      composite.
     */
    public void removeChild(final Progress progress) {                    
        final int index = progresses.indexOf(progress);
        if(index != -1) {
            percentages.remove(index);
            progresses.remove(index);
        }else {
             throw new IllegalArgumentException(StringUtils.format(
                    ERROR_WRONG_PROGRESS));           
        } 
        progress.removeProgressListener(this);
    }    
    
    /**
     * Sets the value of the <code>synchronizeDetails</code> property.
     *
     * @param synchronizeDetails The new value for the
     *      <code>synchronizeDetails</code> property.
     */
    public void synchronizeDetails(final boolean synchronizeDetails) {
        this.synchronizeDetails = synchronizeDetails;
    }
    
    // progress listener implementation /////////////////////////////////////////////
    /**
     * This method will get called by the child progresses as they change state -
     * the composite automatically registers itself as a listener on each of the
     * children.
     *
     * @param progress The child progress whose state has changed.
     */
    public void progressUpdated(final Progress progress) {
        if (synchronizeDetails) {
            setDetail(progress.getDetail());
        }
        
        notifyListeners();
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * This methods evaluates the given percentage in terms of whether it could be
     * added to the composite's total percentage. It sums the composite's own
     * percentage, the declared percentages of all the children and the percentage
     * being evaluated. The return value depends on whether the sum fits into the
     * allowed percentage range or not.
     *
     * @param percentage The percentage to evaluate.
     * @return <code>true</code> if the specified percentage can be added to the
     *      composite, <code>false</code> otherwise.
     */
    private boolean evaluatePercentage(final int percentage) {
        int total = percentage;        
        for (Integer value: percentages) {
            total += value;
        }      
        
        return (total >= START) && (total <= COMPLETE);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The error message which will be displayed when a user tries to set an invalid
     * percentage either directly or via a child.
     */
    public static final String ERROR_WRONG_PERCENTAGE =
            ResourceUtils.getString(CompositeProgress.class,
            "CP.error.percentage"); // NOI18N
    /**
     * The error message which will be displayed when a user tries to remove
     * a non-existent child.
     */    
    public static final String ERROR_WRONG_PROGRESS =
            ResourceUtils.getString(CompositeProgress.class,
            "CP.error.progress"); // NOI18N
}
