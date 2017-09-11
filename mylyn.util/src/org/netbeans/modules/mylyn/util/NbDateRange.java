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
