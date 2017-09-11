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
