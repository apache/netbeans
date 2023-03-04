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
package org.netbeans.modules.bugtracking.ui.repository;

import java.util.Comparator;
import org.netbeans.modules.bugtracking.api.Repository;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryComparator implements Comparator<Repository> {
    public int compare(Repository r1, Repository r2) {
        if(r1 == null && r2 == null) return 0;
        if(r1 == null) return -1;
        if(r2 == null) return 1;
        return r1.getDisplayName().compareTo(r2.getDisplayName());
    }
}
