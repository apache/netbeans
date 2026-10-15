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

package org.netbeans.modules.maven.junit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputProcessor;

/**
 *
 * @author Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.api.output.OutputProcessorFactory.class)
public class JUnitOutputProcessorFactory implements ContextOutputProcessorFactory {
    
    /** Creates a new instance of DefaultOutputProcessor */
    public JUnitOutputProcessorFactory() {
    }

    @Override
    public Set<OutputProcessor> createProcessorsSet(Project project) {
        return Collections.<OutputProcessor>emptySet();
    }

    @Override
    public Set<OutputProcessor> createProcessorsSet(Project project, RunConfig config) {
        if (config.getGoals().contains("test") //NOI18N
            || config.getGoals().contains("integration-test") //NOI18N
            || config.getGoals().contains("surefire:test") //NOI81N
            || config.getGoals().contains("failsafe:integration-test") //NOI18N
            || config.getGoals().contains("verify")) { //NOI18N
            Set<OutputProcessor> toReturn = new HashSet<>();
            if (project != null) {
                toReturn.add(new JUnitOutputListenerProvider(config));
            }
            return toReturn;
        }
        return Collections.<OutputProcessor>emptySet();
    }
    
}
