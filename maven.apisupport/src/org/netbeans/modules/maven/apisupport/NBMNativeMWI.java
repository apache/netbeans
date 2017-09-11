/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryPolicy;
import org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
final class NBMNativeMWI {

    static void instantiate(ProjectInfo vi, File projFile, String version, boolean useOsgi, MavenProject mp) {
        CreateProjectBuilder builder = new CreateProjectBuilder(projFile, vi.groupId, vi.artifactId, vi.version)
                .setPackageName(vi.packageName)
                .setPackaging("nbm")
                .setAdditionalNonPomWork(new AdditionalFiles())
                .setAdditionalOperations(new AdditionalOperations(version, useOsgi));
        if (mp != null) {
            builder = builder.setParentProject(mp);
        }
        builder.create();
    }

    private static class AdditionalFiles implements CreateProjectBuilder.AdditionalChangeHandle, Runnable {
        private CreateProjectBuilder.Context context;
        private final String EMPTY_BUNDLE_FILE = "Localized module labels. Defaults taken from POM (<name>, <description>, <groupId>) if unset.\n" +
                                                 "#OpenIDE-Module-Name=\n" +
                                                 "#OpenIDE-Module-Short-Description=\n" +
                                                 "#OpenIDE-Module-Long-Description=\n" +
                                                 "#OpenIDE-Module-Display-Category=";

        public AdditionalFiles() {
        }

        @Override
        public Runnable createAdditionalChange(CreateProjectBuilder.Context context) {
            this.context = context;
            return this;
        }

        @Override
        public void run() {
            File main = new File(context.getProjectDirectory(), "src" + File.separator + "main");
            File src = new File(main, "java");
            src.mkdirs();
            final String packageName = context.getPackageName();
            if (packageName != null) {
                String path = packageName.replace(".", File.separator);
                new File(src, path).mkdirs();
            }
            src = new File(main,  "nbm");
            src.mkdirs();
            EditableManifest mf = new EditableManifest();
            mf.setAttribute("Manifest-Version", "1.0", null);
            if (packageName != null) {
                String path = packageName.replace(".", "/") + "/Bundle.properties";
                mf.setAttribute("OpenIDE-Module-Localizing-Bundle", path, null);
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(new File(src, "manifest.mf")));
                    mf.write(bos);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    IOUtil.close(bos);
                }
                
            }
            
            src = new File(main, "resources");
            src.mkdirs();
            if (packageName != null) {
                String path = packageName.replace(".", File.separator);
                File res = new File(src, path);
                res.mkdirs();
                OutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(new File(res, "Bundle.properties")));
                    Properties p = new Properties();
                    p.store(bos, EMPTY_BUNDLE_FILE);
                    
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    IOUtil.close(bos);
                }
                
            }
            
        }
    }

    private static class AdditionalOperations implements CreateProjectBuilder.PomOperationsHandle, ModelOperation<POMModel> {
        private CreateProjectBuilder.Context context;
        private final String netbeansDependencyVersion;
        private final boolean useOsgi;

        private AdditionalOperations(String netbeansDependencyVersion, boolean useOsgi) {
            this.netbeansDependencyVersion = netbeansDependencyVersion;
            this.useOsgi = useOsgi;
        }

        @Override
        public List<ModelOperation<POMModel>> createPomOperations(CreateProjectBuilder.Context context) {
            this.context = context;
            return Collections.<ModelOperation<POMModel>>singletonList(this);
        }

        @Override
        public void performOperation(POMModel model) {
            Project root = model.getProject();
            if (root != null) {
                MavenProject parent = context.getParent();
                //add repository
                boolean addRepository = true;
                boolean isSnapshot = NbmWizardIterator.SNAPSHOT_VERSION.equals(netbeansDependencyVersion);
                String repoUrl = isSnapshot ? "http://bits.netbeans.org/nexus/content/repositories/snapshots" : "http://bits.netbeans.org/nexus/content/groups/netbeans";
                String oldRepoUrl = isSnapshot ? "http://bits.netbeans.org/netbeans/trunk/maven-snapshot" : "http://bits.netbeans.org/maven2";
                if (parent != null) {
                    List<ArtifactRepository> repos = parent.getRemoteArtifactRepositories();
                    if (repos != null) {
                        OUTER : 
                        for (ArtifactRepository repo : repos) {
                            if (repoUrl.equals(repo.getUrl()) || (repoUrl + "/").equals(repo.getUrl()) || 
                                oldRepoUrl.equals(repo.getUrl()) || (oldRepoUrl + "/").equals(repo.getUrl()))
                            {
                                addRepository = false;
                                break;
                            }
                            if (repo.getMirroredRepositories() != null) {
                                for (ArtifactRepository mirr : repo.getMirroredRepositories()) {
                                    if (repoUrl.equals(mirr.getUrl()) || (repoUrl + "/").equals(mirr.getUrl()) || 
                                        oldRepoUrl.equals(mirr.getUrl()) || (oldRepoUrl + "/").equals(mirr.getUrl()))
                                    {
                                        addRepository = false;
                                        break OUTER;
                                    }
                                }
                            }
                        }
                    }
                }
                if (addRepository) {
                    Repository repo = model.getFactory().createRepository();
                    repo.setId(isSnapshot ? MavenNbModuleImpl.NETBEANS_SNAPSHOT_REPO_ID : MavenNbModuleImpl.NETBEANS_REPO_ID);
                    repo.setName("Repository hosting NetBeans modules");
                    repo.setUrl(repoUrl);
                    if (isSnapshot) {
                        RepositoryPolicy policy = model.getFactory().createReleaseRepositoryPolicy();
                        policy.setEnabled(false);
                        repo.setReleases(policy);
                    } else {
                        RepositoryPolicy policy = model.getFactory().createSnapshotRepositoryPolicy();
                        policy.setEnabled(false);
                        repo.setSnapshots(policy);
                    }
                    root.addRepository(repo);
                }
                
                //add dependency
                boolean addDependency = true;
                String existingVersion = null;
                String managedVersion = null;
                String netbeansVersioProperty = null;
                if (parent != null) {
                    if (parent.getDependencies() != null) {
                        for (Dependency dep : parent.getDependencies()) {
                            if (MavenNbModuleImpl.NETBEANSAPI_GROUPID.equals(dep.getGroupId()) && "org-netbeans-api-annotations-common".equals(dep.getArtifactId())) {
                                addDependency = false;
                                if (dep.getVersion() != null) {
                                    existingVersion = dep.getVersion();
                                }
                                break;
                            }
                        }
                    }
                    if (parent.getDependencyManagement() != null && parent.getDependencyManagement().getDependencies() != null) {
                        for (Dependency dep : parent.getDependencyManagement().getDependencies()) {
                            if (MavenNbModuleImpl.NETBEANSAPI_GROUPID.equals(dep.getGroupId()) && "org-netbeans-api-annotations-common".equals(dep.getArtifactId())) {
                                if (dep.getVersion() != null) {
                                    managedVersion = dep.getVersion();
                                    break;
                                }
                            }
                        }
                    }
                    netbeansVersioProperty = parent.getProperties().getProperty("netbeans.version");
                }
                if (!addDependency && !netbeansDependencyVersion.equals(existingVersion)) {
                    //what to do if parent defines different version than was selected by user?
                    //stick with parent or override it? Should the UI reflect the problem?
                    addDependency = true;
                }
                if (addDependency) {
                    org.netbeans.modules.maven.model.pom.Dependency d = model.getFactory().createDependency();
                    d.setGroupId(MavenNbModuleImpl.NETBEANSAPI_GROUPID);
                    d.setArtifactId("org-netbeans-api-annotations-common");
                    String version = netbeansDependencyVersion;
                    if (!version.equals(managedVersion)) {
                        if (version.equals(netbeansVersioProperty)) {
                            version = "${netbeans.version}";
                        }
                        d.setVersion(version);
                    }
                    root.addDependency(d);
                } 
                
                //nbm-maven-plugin
                boolean addPlugin = true;
                String managedPVersion = null;
                String pVersion = MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION;
//                boolean useOsgiDepsSet = false;
                if (parent != null) {
                    //TODO do we want to support the case when the plugin is defined in parent pom with inherited=true?
                    PluginManagement pm = parent.getPluginManagement();
                    if (pm != null) {
                        for (org.apache.maven.model.Plugin p : pm.getPlugins()) {
                            if (MavenNbModuleImpl.GROUPID_MOJO.equals(p.getGroupId()) && MavenNbModuleImpl.NBM_PLUGIN.equals(p.getArtifactId())) {
                                managedPVersion = p.getVersion();
//                                Xpp3Dom conf = (Xpp3Dom) p.getConfiguration();
//                                if (conf != null) {
//                                    Xpp3Dom sourceEl = conf.getChild("useOSGiDependencies");
//                                    if (sourceEl != null) {
//                                        useOsgiDepsSet = false;
//                                    }
//                                }
//                                
                                break;
                            }
                        }
                    }
                }
                if (addPlugin) {
                    Plugin p = model.getFactory().createPlugin();
                    p.setGroupId(MavenNbModuleImpl.GROUPID_MOJO);
                    p.setArtifactId(MavenNbModuleImpl.NBM_PLUGIN);
                    if (managedPVersion == null) {
                        p.setVersion(pVersion);
                    }
                    p.setExtensions(true);
                    if (useOsgi) {
                        Configuration c = model.getFactory().createConfiguration();
                        c.setSimpleParameter("useOSGiDependencies", "true");
                        p.setConfiguration(c);
                    }
                    getOrCreateBuild(model).addPlugin(p);
                }
                
                //now comes the compiler plugin
                addPlugin = true;
                managedPVersion = null;
                String source = null;
                String target = null;
                pVersion = "2.5.1";
                if (parent != null) {
                    //TODO do we want to support the case when the plugin is defined in parent pom with inherited=true?
                    PluginManagement pm = parent.getPluginManagement();
                    if (pm != null) {
                        for (org.apache.maven.model.Plugin p : pm.getPlugins()) {
                            if (Constants.GROUP_APACHE_PLUGINS.equals(p.getGroupId()) && Constants.PLUGIN_COMPILER.equals(p.getArtifactId())) {
                                managedPVersion = p.getVersion();
                                Xpp3Dom conf = (Xpp3Dom) p.getConfiguration();
                                if (conf != null) {
                                    Xpp3Dom sourceEl = conf.getChild("source");
                                    if (sourceEl != null) {
                                        source = sourceEl.getValue();
                                    }
                                    Xpp3Dom targetEl = conf.getChild("target");
                                    if (targetEl != null) {
                                        target = targetEl.getValue();
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                addPlugin = target == null || source == null;
                if (addPlugin) {
                    Plugin p = model.getFactory().createPlugin();
                    p.setGroupId(Constants.GROUP_APACHE_PLUGINS);
                    p.setArtifactId(Constants.PLUGIN_COMPILER);
                    if (managedPVersion == null) {
                        p.setVersion(pVersion);
                    }
                    Configuration c = model.getFactory().createConfiguration();
                    c.setSimpleParameter("source", "1.6");
                    c.setSimpleParameter("target", "1.6");
                    p.setConfiguration(c);
                    getOrCreateBuild(model).addPlugin(p);
                }
                
                //now the jar plugin
                addPlugin = true;
                managedPVersion = null;
                String useManifest = null;
                pVersion = "2.4";
                if (parent != null) {
                    //TODO do we want to support the case when the plugin is defined in parent pom with inherited=true?
                    PluginManagement pm = parent.getPluginManagement();
                    if (pm != null) {
                        for (org.apache.maven.model.Plugin p : pm.getPlugins()) {
                            if (Constants.GROUP_APACHE_PLUGINS.equals(p.getGroupId()) && Constants.PLUGIN_JAR.equals(p.getArtifactId())) {
                                managedPVersion = p.getVersion();
                                Xpp3Dom conf = (Xpp3Dom) p.getConfiguration();
                                if (conf != null) {
                                    Xpp3Dom useEl = conf.getChild("useDefaultManifestFile");
                                    if (useEl != null) {
                                        useManifest = useEl.getValue();
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                addPlugin = useManifest == null;
                if (addPlugin) {
                    Plugin p = model.getFactory().createPlugin();
                    p.setGroupId(Constants.GROUP_APACHE_PLUGINS);
                    p.setArtifactId(Constants.PLUGIN_JAR);
                    if (managedPVersion == null) {
                        p.setVersion(pVersion);
                    }
                    Configuration c = model.getFactory().createConfiguration();
                    c.setSimpleParameter("useDefaultManifestFile", "true");
                    p.setConfiguration(c);
                    getOrCreateBuild(model).addPlugin(p);
                }
                

            }
        }

        private Build getOrCreateBuild(POMModel model) {
            Build bld = model.getProject().getBuild();
            if (bld == null) {
                bld = model.getFactory().createBuild();
                model.getProject().setBuild(bld);
            }
            return bld;
        }
    }

}
