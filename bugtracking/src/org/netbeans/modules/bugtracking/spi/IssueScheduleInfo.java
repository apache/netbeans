/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

import java.util.Date;

/**
 * Represents the date period for which an Issue is scheduled. 
 * This can be one specific day as well an interval of days given then by a 
 * beginning date and an amount of days. 
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class IssueScheduleInfo {

    private final Date date;
    private final int interval;

    /**
     * Creates a IssueScheduleInfo representing one specific day for which an 
     * Issue is scheduled to start to work on it.
     * 
     * @param date 
     * @since 1.85
     */
    public IssueScheduleInfo(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        this.date = date;
        this.interval = 1;
    }

    /**
     * Creates a IssueScheduleInfo representing one or more days for which an 
     * Issue is scheduled to start to work on it.
     * 
     * @param startDate determines the day from which this issue is scheduled
     * @param interval determines for how many days an issue is scheduled. Allowed values are >=1.
     * @since 1.85
     */
    public IssueScheduleInfo(Date startDate, int interval) {
        if(interval < 1) {
            throw new IllegalArgumentException("interval must be >= 1");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null");
        }
        this.date = startDate;
        this.interval = interval;
    }

    /**
     * Returns the beginning date of the time period when the work on a Issue should start.
     * 
     * In case an Issue is scheduled for more than one day, then this would be the starting date.
     * @return the beginning date
     * @since 1.85
     */
    public Date getDate() {
        return date;
    }

    /**
     * Determines for how many days an issue was scheduled. 
     * Obviously then, 1 stands for one day given by the start date - {@link #getDate()}.
     * 
     * @return the interval in days
     * @since 1.85
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Compares two instances of IssueSchedulingInfo. This instance equals to
     * the other object if the other object is an IssueSchedulingInfo and both
     * date and interval (values returned by {@link #getDate() } and {@link #getInterval()
     * }) are equal for both the instances.
     *
     * @param obj another object to compare.
     * @return true if the two objects are equal.
     * @since 1.85
     */
    @Override
    public boolean equals (Object obj) {
        if (obj instanceof IssueScheduleInfo) {
            IssueScheduleInfo other = (IssueScheduleInfo) obj;
            return date.equals(other.date)
                    && interval == other.interval;
        }
        return false;
    }

    /**
     * Builds a hash code for this instance created from the values of date and
     * interval, i.e. values returned by {@link #getDate()} and
     * {@link #getInterval()}.
     *
     * @return hash code
     * @since 1.85
     */
    @Override
    public int hashCode () {
        int hash = 3;
        hash = 67 * hash + (this.date != null ? this.date.hashCode() : 0);
        hash = 67 * hash + this.interval;
        return hash;
    }
    
}
