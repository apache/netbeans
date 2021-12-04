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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Laszlo Kishalmi
 */
public class NbProjectInfoModel extends BaseModel implements NbProjectInfo {

    private final Map<String, Object> info = new HashMap<>();
    private final Map<String, Object> ext = new HashMap<>();
    private final Set<String> problems = new LinkedHashSet<>();
    private boolean miscOnly = false;

    public NbProjectInfoModel() {
        ext.put("perf", new LinkedHashMap());
    }

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

    void noteProblem(String s) {
        problems.add(s);
    }

    void noteProblem(Exception e) {
        StringBuilder problem = new StringBuilder(e.getMessage());
        if (e.getCause() != null) {
            problem.append('\n').append(e.getCause().getMessage());
        }
        problems.add(problem.toString());
    }

    public void setMiscOnly(boolean miscOnly) {
        this.miscOnly = miscOnly;
    }

    @Override
    public boolean getMiscOnly() {
        return miscOnly;
    }

    public void registerPerf(String name, Object runtime) {
        ((LinkedHashMap<String,Object>) ext.get("perf")).put(name, runtime);
    }
}
