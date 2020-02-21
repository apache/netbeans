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

package org.netbeans.modules.cnd.discovery.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 */
public interface DiscoveryExtensionInterface extends IteratorExtension {
    boolean canApply(Map<String,Object> map, Project project);
    boolean canApply(Map<String,Object> map, Project project, Interrupter interrupter);

    void apply(Map<String,Object> map, Project project) throws IOException;
    void apply(Map<String,Object> map, Project project, Interrupter interrupter) throws IOException;

    interface Applicable {
        boolean isApplicable();

        int getPriority();

        String getCompilerName();

        boolean isSunStudio();

        List<String> getDependencies();

        List<String> getSearchPaths();

        String getSourceRoot();

        Position getMainFunction();

        List<String> getErrors();
    }

    interface Position {
        String getFilePath();

        int getLine();
    }
}
