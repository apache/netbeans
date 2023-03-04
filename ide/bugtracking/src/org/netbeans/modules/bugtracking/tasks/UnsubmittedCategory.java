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

package org.netbeans.modules.bugtracking.tasks;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.openide.util.NbBundle;

public class UnsubmittedCategory extends Category {

    private RepositoryImpl repository;

    public UnsubmittedCategory(List<IssueImpl> tasks, RepositoryImpl repository) {
        super(NbBundle.getMessage(UnsubmittedCategory.class, "LBL_Unsubmitted") + " [" + repository.getDisplayName() + "]", tasks, true);
        this.repository = repository;
    }

    public UnsubmittedCategory(RepositoryImpl repository) {
        this(new ArrayList<IssueImpl>(0), repository);
    }

    @Override
    public boolean persist() {
        return false;
    }

    @Override
    public void reload() {
    }

    @Override
    public List<IssueImpl> getTasks() {
        return new ArrayList<IssueImpl>(repository.getUnsubmittedIssues());
    }

    @Override
    public int sortIndex() {
        return 900;
    }

}
