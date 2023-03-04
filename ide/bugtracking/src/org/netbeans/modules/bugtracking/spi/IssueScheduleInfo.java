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
