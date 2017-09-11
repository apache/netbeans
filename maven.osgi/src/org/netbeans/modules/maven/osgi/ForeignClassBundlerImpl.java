/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
