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

package org.netbeans.modules.localtasks;

import java.util.Date;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueScheduleProvider;
import org.netbeans.modules.localtasks.task.LocalTask;

/**
 *
 * @author Ondrej Vrabec
 */
class IssueSchedulingProviderImpl implements IssueScheduleProvider<LocalTask> {

    @Override
    public void setSchedule (LocalTask i, IssueScheduleInfo date) {
        i.setTaskScheduleDate(date, true);
    }

    @Override
    public Date getDueDate (LocalTask i) {
        return i.getPersistentDueDate();
    }

    @Override
    public IssueScheduleInfo getSchedule (LocalTask i) {
        return i.getPersistentScheduleInfo();
    }

    }
