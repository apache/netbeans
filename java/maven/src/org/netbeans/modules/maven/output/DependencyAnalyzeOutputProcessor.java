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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import static org.netbeans.modules.maven.output.Bundle.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author mkleint
 */
public class DependencyAnalyzeOutputProcessor implements OutputProcessor {
    
    private static final String[] DEPGOALS = new String[] {
        "mojo-execute#dependency:analyze" //NOI18N
    };
    private final Pattern start;
    private boolean started;
    private final Pattern dependency;
    private final Project project;
    
    /** Creates a new instance of JavadocOutputProcessor */
    DependencyAnalyzeOutputProcessor(Project project) {
        started = false;
        start = Pattern.compile(".*Used undeclared dependencies.*", Pattern.DOTALL); //NOI18N
        dependency = Pattern.compile("\\s*(?:\\[WARNING|WARN\\])?\\s*(.*):(.*):(.*):(.*):(.*)", Pattern.DOTALL); //NOI18N
        this.project = project;
    }

    @Override
    public String[] getRegisteredOutputSequences() {
        return DEPGOALS;
    }
    
    @Override
    public void processLine(String line, OutputVisitor visitor) {
        if (started) {
            Matcher match = dependency.matcher(line);
            if (match.matches() && match.groupCount() >= 5) {
                String gr = match.group(1);
                String ar = match.group(2);
                String type = match.group(3);
                String ver = match.group(4);
                String sc = match.group(5);
                visitor.setLine(line + " (Click to add to pom.xml)"); //NOI18N - part of maven output
                OutputVisitor.Context context = visitor.getContext();
                Project prj = project;
                if (context != null && context.getCurrentProject() != null) {
                    prj = context.getCurrentProject();
                }
                visitor.setOutputListener(new Listener(prj, gr, ar, type, ver, sc), false);
            } else {
                started = false;
            }
        }
        if (!started) {
            Matcher match = start.matcher(line);
            if (match.matches()) {
                started = true;
            }
        }
    }
    
    @Override
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        started = false;
    }
    
    @Override
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
    }
    
    @Override
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
    private static class Listener implements OutputListener {
        private final String group;
        private final String scope;
        private final String version;
        private final String type;
        private final String artifact;
        private final Project project;
        
        private Listener(Project prj, String gr, String ar, String type, String ver, String sc) {
            group = gr;
            artifact = ar;
            this.type = type;
            version = ver;
            scope = sc;
            project = prj;
        }
        @Override
        public void outputLineSelected(OutputEvent arg0) {
        }
        
        @Messages({"# {0} - groupId:artifactId", "MSG_Dependency=Dependency {0} added to project''s POM."})
        @Override
        public void outputLineAction(OutputEvent arg0) {
            ModelUtils.addDependency(project.getProjectDirectory().getFileObject("pom.xml")/*NOI18N*/,
                    group, artifact, version, type, scope, null,false);
            NotifyDescriptor nd = new NotifyDescriptor.Message(MSG_Dependency(group + ":" + artifact));
            DialogDisplayer.getDefault().notify(nd);
        }
        
        @Override
        public void outputLineCleared(OutputEvent arg0) {
        }
    }
}
