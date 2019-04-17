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

package org.netbeans.modules.gradle.tooling;

import org.netbeans.modules.gradle.api.NbProjectInfo;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Laszlo Kishalmi
 */
public class NbProjectInfoModel extends BaseModel implements NbProjectInfo {

    Map<String, Object> info = new HashMap<>();
    Map<String, Object> ext = new HashMap<>();
    Set<String> problems = new LinkedHashSet<>();
    boolean miscOnly = false;

    @Override
    public Map<String, Object> getInfo() {
        return info;
    }

    @Override
    public Map<String, Object> getExt() {
        return ext;
    }

    @Override
    public Set<String> getProblems() {
        return problems;
    }

    void noteProblem(Exception e) {
        StringBuilder problem = new StringBuilder(e.getMessage());
        if (e.getCause() != null) {
            problem.append('\n').append(e.getCause().getMessage());
        }
        problems.add(problem.toString());
    }

    @Override
    public boolean getMiscOnly() {
        return miscOnly;
    }
}
