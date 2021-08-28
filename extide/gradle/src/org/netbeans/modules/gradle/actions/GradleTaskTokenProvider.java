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
package org.netbeans.modules.gradle.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = ReplaceTokenProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleTaskTokenProvider implements ReplaceTokenProvider {

    private static Set<String> SUPPORTED = Collections.unmodifiableSet(new HashSet(Arrays.asList(
       "taskName",
       "taskPath",
       "taskNames",
       "taskPaths"
    )));
    
    @Override
    public Set<String> getSupportedTokens() {
        return SUPPORTED;
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        
        Collection<? extends GradleTask> tasks = context.lookupAll(GradleTask.class);
        if (!tasks.isEmpty()) {
            Map<String, String> ret = new HashMap<>();
            
            GradleTask task = context.lookup(GradleTask.class);
            ret.put("taskName", task.getName());
            ret.put("taskPath", task.getPath());
            
            StringBuilder names = new StringBuilder();
            StringBuilder paths = new StringBuilder();
            String sep = "";
            for (GradleTask t : tasks) {
                names.append(sep).append(t.getName());
                paths.append(sep).append(t.getPath());
                sep = " ";
            }
            ret.put("taskNames", names.toString());
            ret.put("taskPaths", paths.toString());

            return ret;
        }
        return Collections.emptyMap();
    }
    
}
