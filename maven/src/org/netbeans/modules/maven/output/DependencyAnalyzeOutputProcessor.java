/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
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
        dependency = Pattern.compile("\\s*(?:\\[WARNING\\])?\\s*(.*):(.*):(.*):(.*):(.*)", Pattern.DOTALL); //NOI18N
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
