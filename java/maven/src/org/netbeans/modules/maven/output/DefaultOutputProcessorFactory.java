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

package org.netbeans.modules.maven.output;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=OutputProcessorFactory.class)
public class DefaultOutputProcessorFactory implements ContextOutputProcessorFactory {
    
    @Override public Set<OutputProcessor> createProcessorsSet(Project project) {
        Set<OutputProcessor> toReturn = new HashSet<>();
        if (project != null) {
            toReturn.add(new JavadocOutputProcessor());
            toReturn.add(new TestOutputListenerProvider());
            toReturn.add(new SiteOutputProcessor(project));
            NbMavenProjectImpl nbprj = project.getLookup().lookup(NbMavenProjectImpl.class);
            toReturn.add(new JavaStacktraceOutputProcessor(nbprj));
            toReturn.add(new DependencyAnalyzeOutputProcessor(nbprj));
        }
        return toReturn;
    }

    @Override public Set<? extends OutputProcessor> createProcessorsSet(Project project, RunConfig config) {
        Set<OutputProcessor> toReturn = new HashSet<>(); 
        toReturn.add(new JavaOutputListenerProvider(config));
        toReturn.add(new GlobalOutputProcessor(config));
        return toReturn;
    }
    
}
