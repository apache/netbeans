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

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * maven implementation of SourceLevelQueryImplementation.
 * checks a property of maven-compiler-plugin
 * @author Milos Kleint
 */
@ProjectServiceProvider(service=SourceLevelQueryImplementation2.class, projectType="org-netbeans-modules-maven")
public class MavenSourceLevelImpl implements SourceLevelQueryImplementation2 {
    
    private static final Logger LOGGER = Logger.getLogger(MavenSourceLevelImpl.class.getName());
    
    static final Pattern PROFILE = Pattern.compile("-profile (compact1|compact2|compact3){1}?");
    private final Project project;

    public MavenSourceLevelImpl(Project proj) {
        project = proj;
    }
    
    private String getSourceLevelString(FileObject javaFile) {
        File file = FileUtil.toFile(javaFile);
        if (file == null) {
            //#128609 something in jar?
            return null;
        }
        URI uri = Utilities.toURI(file);
        assert "file".equals(uri.getScheme());
        String goal = "compile"; //NOI18N
        String property = "maven.compiler.source";
        String param = Constants.SOURCE_PARAM;
        NbMavenProjectImpl nbprj = project.getLookup().lookup(NbMavenProjectImpl.class);
        for (URI testuri : nbprj.getSourceRoots(true)) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
                property = "maven.compiler.testSource";
                param = "testSource";
            }
        }
        for (URI testuri : nbprj.getGeneratedSourceRoots(true)) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
                property = "maven.compiler.testSource";
                param = "testSource";
            }
        }
        String sourceLevel = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,  //NOI18N
                                                              Constants.PLUGIN_COMPILER,  //NOI18N
                                                              param,  //NOI18N
                                                              goal,
                                                              property);
        if (sourceLevel != null) {
            return sourceLevel;
        }
        if ("testCompile".equals(goal)) { //#237986 in tests, first try "testSource" param, then fallback to "source"
            sourceLevel = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,  //NOI18N
                                                              Constants.PLUGIN_COMPILER,  //NOI18N
                                                              Constants.SOURCE_PARAM,  //NOI18N
                                                              "testCompile",
                                                              "maven.compiler.source");            
            if (sourceLevel != null) {
                return sourceLevel;
            }
        }
        
        
        String version = PluginPropertyUtils.getPluginVersion(
                nbprj.getOriginalMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        if (version == null || new DefaultArtifactVersion(version).compareTo(new DefaultArtifactVersion("2.3")) >= 0) {
            return "1.5";
        } else {
            return "1.3";
        }
    }
    
    private SourceLevelQuery.Profile getSourceProfile(FileObject javaFile) {
        File file = FileUtil.toFile(javaFile);
        if (file == null) {
            //#128609 something in jar?
            return SourceLevelQuery.Profile.DEFAULT;
        }
        URI uri = Utilities.toURI(file);
        assert "file".equals(uri.getScheme());
        String goal = "compile"; //NOI18N
        NbMavenProjectImpl nbprj = project.getLookup().lookup(NbMavenProjectImpl.class);
        for (URI testuri : nbprj.getSourceRoots(true)) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
            }
        }
        for (URI testuri : nbprj.getGeneratedSourceRoots(true)) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
            }
        }
        //compilerArguments vs compilerArgument vs compilerArgs - all of them get eventually merged in compiler mojo..
        //--> all need to be checked.
        String args = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,  //NOI18N
                                                              Constants.PLUGIN_COMPILER,  //NOI18N
                                                              "compilerArgument",  //NOI18N
                                                              goal,
                                                              null);
        if (args != null) {
            Matcher match = PROFILE.matcher(args);
            if (match.find()) {
                String prof = match.group(1);
                SourceLevelQuery.Profile toRet = SourceLevelQuery.Profile.forName(prof);
                return toRet != null ? toRet : SourceLevelQuery.Profile.DEFAULT;
            }
        }
        
        
        String compilerArgumentsProfile = PluginPropertyUtils.getPluginPropertyBuildable(project, 
                Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_COMPILER, //NOI18N
                goal,
                new ConfigBuilder());
               
        if (compilerArgumentsProfile != null) {
            SourceLevelQuery.Profile toRet = SourceLevelQuery.Profile.forName(compilerArgumentsProfile);
            return toRet != null ? toRet : SourceLevelQuery.Profile.DEFAULT;
        }
        String[] compilerArgs = PluginPropertyUtils.getPluginPropertyList(project, Constants.GROUP_APACHE_PLUGINS,
                         Constants.PLUGIN_COMPILER, //NOI18N
                         "compilerArgs", "arg", goal);
        if (compilerArgs != null) {
            Iterator<String> it = Arrays.asList(compilerArgs).iterator();
            while (it.hasNext()) {
                String p = it.next();
                if ("-profile".equals(p) && it.hasNext()) {               
                    String prof = it.next();
                    SourceLevelQuery.Profile toRet = SourceLevelQuery.Profile.forName(prof);
                    return toRet != null ? toRet : SourceLevelQuery.Profile.DEFAULT;
                }
            }
        }
        return SourceLevelQuery.Profile.DEFAULT;
    }

    @Override public Result getSourceLevel(final FileObject javaFile) {
        return new ResultImpl(javaFile);
    }
    
    private static class ConfigBuilder implements PluginPropertyUtils.ConfigurationBuilder<String> {

        @Override
        public String build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
            if (configRoot != null) {
                Xpp3Dom args = configRoot.getChild("compilerArguments");
                if (args != null) {
                    Xpp3Dom prof = args.getChild("profile");
                    if (prof != null) {
                        return prof.getValue();
                    }
                }
            }
            return null;
        }
        
    }
    
    private class ResultImpl implements SourceLevelQueryImplementation2.Result2, PropertyChangeListener {
        
        private final FileObject javaFile;
        private final ChangeSupport cs = new ChangeSupport(this);
        private final PropertyChangeListener pcl = WeakListeners.propertyChange(this, project.getLookup().lookup(NbMavenProject.class));
        private String cachedLevel = null;
        private SourceLevelQuery.Profile cachedProfile;
        private final Object CACHE_LOCK = new Object();
        
        ResultImpl(FileObject javaFile) {
            this.javaFile = javaFile;
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(pcl);
        }

        @Override public String getSourceLevel() {
            synchronized (CACHE_LOCK) {
                if (cachedLevel == null) {
                    cachedLevel = getSourceLevelString(javaFile);
                }
                if(LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "MavenSourceLevelQuery: {0} level {1}", new Object[] {javaFile.getPath(), cachedLevel}); // NOI18N
                }
                return cachedLevel;
            }
        }

        @Override public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                Project p = (Project) evt.getSource();
                if (p.getLookup().lookup(NbMavenProject.class).isUnloadable()) {
                    return; //let's just continue with the old value, rescanning classpath for broken project and re-creating it later serves no greater good.
                }
                synchronized (CACHE_LOCK) {
                    cachedLevel = null;
                    cachedProfile = null;
                }
                if(LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "MavenSourceLevelQuery: {0} fire change", javaFile.getPath()); // NOI18N
                }
                cs.fireChange();
            }
        }

        @Override
        public SourceLevelQuery.Profile getProfile() {
            synchronized (CACHE_LOCK) {
                if (cachedProfile == null) {
                    cachedProfile = getSourceProfile(javaFile);
                }
                if(LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "MavenSourceLevelQuery: {0} profile {1}", new Object[] {javaFile.getPath(), cachedProfile});
                }
                return cachedProfile;
            }
        }

    }
    
}
