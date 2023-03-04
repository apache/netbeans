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

package org.netbeans.modules.mylyn.util;

import java.util.Calendar;
import java.util.Date;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;

/**
 *
 * @author Ondrej Vrabec
 */
public final class NbDateRange implements Comparable<NbDateRange> {
    private final DateRange delegate;

    NbDateRange (DateRange delegate) {
        this.delegate = delegate;
    }
    
    public NbDateRange (Calendar startDate, Calendar endDate) {
        this(new DateRange(startDate, endDate));
    }
    
    public NbDateRange (Calendar time) {
        this(new DateRange(time));
    }

    public NbDateRange (IssueScheduleInfo info) {
        this(toDateRange(info));
    }
    
    @Override
    public int compareTo (NbDateRange o) {
        return getDelegate().compareTo(o.getDelegate());
    }
    
    public Calendar getStartDate () {
        return delegate.getStartDate();
    }
    
    public Calendar getEndDate () {
        return delegate.getEndDate();
    }
    
    DateRange getDelegate () {
        return delegate;
    }

    @Override
    public boolean equals (Object obj) {
        if (!(obj instanceof NbDateRange)) {
            return false;
        }
        return delegate.equals(((NbDateRange) obj).getDelegate());
    }

    @Override
    public int hashCode () {
        return delegate.hashCode();
    }
    
    public IssueScheduleInfo toSchedulingInfo () {
        Calendar startDate = getStartDate();
        Calendar endDate = getEndDate();
        int difference = (int) ((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / 1000 / 3600 / 24) + 1;
        return new IssueScheduleInfo(startDate.getTime(), difference);
    }
    
    private static DateRange toDateRange (IssueScheduleInfo info) {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.setTime(info.getDate());
        endDate.setTime(new Date(info.getDate().getTime() + (info.getInterval() * 24 * 3600 - 1) * 1000));
        return new DateRange(startDate, endDate);
    }
}
