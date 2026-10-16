/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.ui;

import java.util.Comparator;
import org.netbeans.api.project.Project;

/**
 * Compares projects by their real path. As a result siblings and nested
 * projects are co-located to each other.
 */
public final class ProjectByPathComparator implements Comparator<Project> {
    private ProjectByPathComparator() {
    }

    public static Comparator<? super Project> projectByPath() {
        return new ProjectByPathComparator();
    }

    @Override
    public int compare(Project p1, Project p2) {
        if (p1 == null && p2 == null) {
            return 0;
        }
        if (p1 == null) {
            return -1;
        }
        if (p2 == null) {
            return 1;
        }
        return p1.getProjectDirectory().getPath().compareTo(p2.getProjectDirectory().getPath());
    }

}
