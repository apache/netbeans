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
package org.netbeans.modules.maven.debug;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service=OutputProcessorFactory.class)
public class DebugCheckerOutputFactory implements ContextOutputProcessorFactory {

    @Override
    public Set<? extends OutputProcessor> createProcessorsSet(Project project, RunConfig config) {
        String trigger = config.getProperties().get(Constants.ACTION_PROPERTY_JPDAATTACH_TRIGGER);
        if (trigger == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(new JPDATrigger(trigger, config));
    }

    @Override
    public Set<? extends OutputProcessor> createProcessorsSet(Project project) {
        return Collections.emptySet();
    }

    private static class JPDATrigger implements OutputProcessor {

        private String trigger;
        private final RunConfig cfg;

        public JPDATrigger(String trigger, RunConfig cfg) {
            this.trigger = trigger;
            this.cfg = cfg;
        }

        @Override
        public String[] getRegisteredOutputSequences() {
            return new String[] { "project-execute" };
        }

        @Override
        public void processLine(String line, OutputVisitor visitor) {
            if (trigger != null && line.contains(trigger)) {
                trigger = null;
                try {
                    DebuggerChecker.connect(cfg);
                } catch (DebuggerStartException ex) {
                    visitor.setLine(visitor.getLine() + " cannot connect: " + ex.getMessage());
                    visitor.setColor(Color.red);
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        }

        @Override
        public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        }

        @Override
        public void sequenceFail(String sequenceId, OutputVisitor visitor) {
        }
    }
    
}
