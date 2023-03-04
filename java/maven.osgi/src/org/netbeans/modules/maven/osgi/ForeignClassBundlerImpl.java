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

package org.netbeans.modules.maven.osgi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.osgi.Bundle.*;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.RequestProcessor;

@NbBundle.Messages({
    "PRBL_Name=Export-Package/Private-Package contains packages from dependencies",
    "PRBL_DESC=When the final bundle jar contains classes not originating in current project, NetBeans internal compiler cannot use the sources of the project. Then changes done in project's source code only appears in depending projects when project is recompiled. Also applies to features like Refactoring which will not be able to find usages in depending projects."
})
public class ForeignClassBundlerImpl implements ForeignClassBundler, ProjectProblemsProvider { // #179521
    private static final ProjectProblem PROBLEM_REPORT = ProjectProblem.createWarning(PRBL_Name(), PRBL_DESC());
    private static final RequestProcessor RP = new RequestProcessor(ForeignClassBundlerImpl.class);
    
    private final AtomicBoolean hasProblem = new AtomicBoolean(false);
    
    private final Project project;
    private boolean calculated = false;
    private boolean calculatedValue = false;
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);


    public ForeignClassBundlerImpl(Project p) {
        project = p;
    }
    
    @Override 
    public synchronized boolean preferSources() {
        if (calculated) {
            return calculatedValue;
        }
        calculatedValue = calculateValue(); 
        calculated = true;
        return calculatedValue;
    }

    private boolean calculateValue() {
        NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
        if (nbmp == null) {
            return true;
        }
        boolean oldVal = hasProblem.get();
        boolean newVal = false;
        try {
        MavenProject mp = nbmp.getMavenProject();
        Properties props = PluginPropertyUtils.getPluginPropertyParameter(project, "org.apache.felix", "maven-bundle-plugin", "instructions", "bundle");
        if (props != null) {
            //String embed = props.getProperty("Embed-Dependency"); //TODO should we parse it somehow?
            //are embedded ones a problem? not on CP I guess
//            if (embed != null && embed.contains("inline=true")) {
//                return false;
//            }
            String exportedPack = props.getProperty("Export-Package");
            String privatePack = props.getProperty("Private-Package");
            if (exportedPack != null || privatePack != null) {
                Matcher exported = new Matcher(exportedPack);
                Matcher prived = new Matcher(privatePack);
                for (Artifact a : mp.getRuntimeArtifacts()) { //TODO runtime or compile??
                    File f = a.getFile();
                    if (f != null && f.isFile()) {
                        try {
                            JarFile jf = new JarFile(f);
                            Enumeration<JarEntry> en = jf.entries();
                            while (en.hasMoreElements()) {
                                JarEntry je = en.nextElement();
                                if (je.isDirectory() && !je.getName().startsWith("META-INF")) { //is this optimization correct?
                                    String pack = je.getName().substring(0, je.getName().length() - 1).replace("/", "."); //last char is /
                                    if (exported.matches(pack) || prived.matches(pack)) {
                                        newVal = true;
                                        return false;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        } finally {
            if (newVal != oldVal) {
                hasProblem.set(newVal);
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        pchs.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
                    }
                });
            }
        }
        //according to http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html default value is just 
        //project's own sources
        return true;
    }

    @Override
    public synchronized void resetCachedValue() {
        calculated = false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pchs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pchs.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return hasProblem.get() ? Collections.singleton(PROBLEM_REPORT) : Collections.<ProjectProblem>emptySet();
    }

    
}
