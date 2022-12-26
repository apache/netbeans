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

import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputUtils;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.project.Project;
import org.openide.windows.OutputListener;


/**
 * Provides links for stack traces in the output window.
 * 
 * @author mbien
 */
public class JavaStacktraceOutputProcessor implements OutputProcessor {

    private final String[] goals;
    private final NbMavenProjectImpl project;

    /**
     * Creates a JavaStacktraceOutputProcessor with global scope (goal: "session-execute").
     */
    public JavaStacktraceOutputProcessor(NbMavenProjectImpl project) {
        this(project, new String[] { "session-execute" });
    }

    /**
     * Creates a JavaStacktraceOutputProcessor which is only active during the given goals.
     */
    public JavaStacktraceOutputProcessor(NbMavenProjectImpl project, String[] goals) {
        this.project = project;
        this.goals = goals;
    }

    @Override
    public void processLine(String line, OutputVisitor visitor) {
        OutputVisitor.Context context = visitor.getContext();
        Project prj = project;
        if (context != null && context.getCurrentProject() != null) {
            prj = context.getCurrentProject();
        }
        OutputListener list = OutputUtils.matchStackTraceLine(line, prj);
        if (list != null) {
            visitor.setOutputListener(list);
        }
    }

    @Override
    public String[] getRegisteredOutputSequences() {
        return goals;
    }

    @Override
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {}

    @Override
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {}

    @Override
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {}

}
