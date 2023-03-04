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

package org.netbeans.api.progress.aggregate;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.progress.module.LoggingUtils;

/**
 * A contributor to the aggragete progress indication.
 * <b> This class is not threadsafe, you should access the contributor from
 * one thread only. </b>
 * @see AggregateProgressHandle#addContributor(ProgressContributor)
 *
 * @author mkleint
 */
public final class ProgressContributor {

    private static final Logger LOG = Logger.getLogger(ProgressContributor.class.getName());

    private String id;
    private int workunits;
    private int current;
    private int parentUnits;
    private int lastParentedUnit;
    private AggregateProgressHandle parent;

    /** Creates a new instance of ProgressContributor */
    ProgressContributor(String id) {
        this.id = id;
        workunits = 0;
        current = 0;
        lastParentedUnit = 0;
    }
    
    /**
     * an id that allows identification of the progress contributor by the monitor.
     */
    public String getTrackingId() {
        return id;
    }
    
    void setParent(AggregateProgressHandle par) {
        parent = par;
    }
    
    int getWorkUnits() {
        return workunits;
    }
    
    int getRemainingParentWorkUnits() {
        return parentUnits;
    }
    
    void setAvailableParentWorkUnits(int newCount) {
        parentUnits = newCount;
    }
    
    double getCompletedRatio() {
        return workunits == 0 ? 0 : (double)(current / workunits);
    }
    
    /**
     * start the progress indication for a task with known number of steps.
     * @param workunits a total number of workunits that this contributor will process.
     */
    public void start(int workunits) {
        if (parent == null) {
            return;
        }
        this.workunits = workunits;
        parent.processContributorStart(this, null);
    }
    
    
    /**
     * finish the contributor, possibly also the whole task if this instance was
     * the last one to finish.
     */
    public void finish() {
        if (parent == null) {
            return;
        }
        if (current < workunits) {
            progress(null, workunits);
        }
        parent.processContributorFinish(this);
    }
    
    
    /**
     * Notify the user about completed workunits.
     * @param workunit a cumulative number of workunits completed so far
     */
    public void progress(int workunit) {
        progress(null, workunit);
    }
    
    /**
     * Notify the user about progress by showing message with details.
     * @param message detailed info about current progress
     */
    public void progress(String message) {
        progress(message, current);
    }
    
    /**
     * Notify the user about completed workunits.
     * @param message detailed info about current progress
     * @param unit a cumulative number of workunits completed so far
     */
    public void progress(String message, int unit) {
        if (parent == null) {
            return;
        }
        if (unit < current || unit > workunits) {
            LOG.log(Level.WARNING, "logged ''{0}'' @{1} <{2} or >{3} in {4} at {5}", new Object[] {message, unit, current, workunits, parent.displayName, LoggingUtils.findCaller()});
            return;
        }
        if (message != null && unit == current) {
            // we need to process the message in any case..
            parent.processContributorStep(this, message, 0);
            return;
        }
        current = unit;
        int delta = current - lastParentedUnit;
        double step = (1 / ((double)parentUnits / (double)(workunits - lastParentedUnit)));
//        System.out.println("progress.. current=" + current + " latparented=" + lastParentedUnit);
//        System.out.println("           parent units=" + parentUnits);
//        System.out.println("           delta=" + delta);
//        System.out.println("           step=" + step);
        if (delta >= step) {
            int count = (int) (delta / step);
            boolean log = current < 0 || delta < 0 || count < 0;
            if (log) {
                LOG.log(Level.WARNING, "Progress Contributor before progress: current={0} lastParentedUnit={1}, parentUnits={2}, delta={3}, step={4}", new Object[] {current, lastParentedUnit, parentUnits, delta, step});
            }
            lastParentedUnit = lastParentedUnit + (int)(count * step);
            parentUnits = parentUnits - count;
            if (log) {
                LOG.log(Level.WARNING, "Progress Contributor after progress: count={0}, new parentUnits={2}, new lastParentedUnits={1}", new Object[] {count, lastParentedUnit, parentUnits});
            }
//            System.out.println("   count=" + count);
//            System.out.println("   newparented=" + lastParentedUnit);
//            System.out.println("   parentUnits=" + parentUnits);
            // call parent..
            parent.processContributorStep(this, message, count);
        }
    }    
}
