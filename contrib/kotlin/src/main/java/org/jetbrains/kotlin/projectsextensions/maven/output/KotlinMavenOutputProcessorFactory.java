/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.projectsextensions.maven.output;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=OutputProcessorFactory.class)
public class KotlinMavenOutputProcessorFactory implements ContextOutputProcessorFactory {
    
    @Override 
    public Set<OutputProcessor> createProcessorsSet(Project project) {
        Set<OutputProcessor> toReturn = new HashSet<>();
        if (project != null) {
            toReturn.add(new KotlinMavenOutputProcessor());
        }
        return toReturn;
    }

    @Override
    public Set<? extends OutputProcessor> createProcessorsSet(Project project, RunConfig config) {
        return Collections.emptySet();
    }
    
}
