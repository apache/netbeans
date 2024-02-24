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

package org.netbeans.modules.maven.apisupport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.repository.RemoteRepository;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
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
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(src, "manifest.mf")))) {
                    mf.write(os);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            }
            
            src = new File(main, "resources");
            src.mkdirs();
            if (packageName != null) {
                String path = packageName.replace(".", File.separator);
                File res = new File(src, path);
                res.mkdirs();
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(res, "Bundle.properties")))) {
                    Properties p = new Properties();
                    p.store(os, EMPTY_BUNDLE_FILE);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
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
                String snapshotRepoUrl = "https://repository.apache.org/content/repositories/snapshots/";
                if (parent != null) {
                    List<RemoteRepository> repos = parent.getRemoteProjectRepositories();
                    if (repos != null) {
                        OUTER : 
                        for (RemoteRepository repo : repos) {
                            if (snapshotRepoUrl.equals(repo.getUrl()) || (snapshotRepoUrl + "/").equals(repo.getUrl()))
                            {
                                addRepository = false;
                                break;
                            }
                            if (repo.getMirroredRepositories() != null) {
                                for (RemoteRepository mirr : repo.getMirroredRepositories()) {
                                    if (snapshotRepoUrl.equals(mirr.getUrl()) || (snapshotRepoUrl + "/").equals(mirr.getUrl()))
                                    {
                                        addRepository = false;
                                        break OUTER;
                                    }
                                }
                            }
                        }
                    }
                }
                if (addRepository && isSnapshot ) {
                    Repository repo = model.getFactory().createRepository();
                    repo.setId(MavenNbModuleImpl.APACHE_SNAPSHOT_REPO_ID);
                    repo.setName("Repository hosting NetBeans modules");
                    repo.setUrl(snapshotRepoUrl);
                    RepositoryPolicy policy = model.getFactory().createReleaseRepositoryPolicy();
                    policy.setEnabled(false);
                    repo.setReleases(policy);
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
                String pVersion = MavenNbModuleImpl.getLatestNbmPluginVersion();
//                boolean useOsgiDepsSet = false;
                if (parent != null) {
                    //TODO do we want to support the case when the plugin is defined in parent pom with inherited=true?
                    PluginManagement pm = parent.getPluginManagement();
                    if (pm != null) {
                        for (org.apache.maven.model.Plugin p : pm.getPlugins()) {
                            if ((MavenNbModuleImpl.GROUPID_MOJO.equals(p.getGroupId()) || MavenNbModuleImpl.GROUPID_APACHE.equals(p.getGroupId())) && MavenNbModuleImpl.NBM_PLUGIN.equals(p.getArtifactId())) {
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
                    p.setGroupId(MavenNbModuleImpl.GROUPID_APACHE);
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
                pVersion = "3.11.0";
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
                    c.setSimpleParameter("source", "1.8");
                    c.setSimpleParameter("target", "1.8");
                    p.setConfiguration(c);
                    getOrCreateBuild(model).addPlugin(p);
                }
                
                //now the jar plugin
                addPlugin = true;
                managedPVersion = null;
                String useManifest = null;
                pVersion = "3.3.0";
                if (parent != null) {
                    //TODO do we want to support the case when the plugin is defined in parent pom with inherited=true?
                    PluginManagement pm = parent.getPluginManagement();
                    if (pm != null) {
                        for (org.apache.maven.model.Plugin p : pm.getPlugins()) {
                            if (Constants.GROUP_APACHE_PLUGINS.equals(p.getGroupId()) && Constants.PLUGIN_JAR.equals(p.getArtifactId())) {
                                managedPVersion = p.getVersion();
                                Xpp3Dom conf = (Xpp3Dom) p.getConfiguration();
                                if (conf != null) {
                                    if (new ComparableVersion(managedPVersion).compareTo(new ComparableVersion(JAR_PLUGIN_VERSION_MANIFEST_CONFIG_CHANGE)) >= 0) {
                                        Xpp3Dom archive = conf.getChild("archive");
                                        if (archive != null) {
                                            Xpp3Dom manifestFile = archive.getChild("manifestFile");
                                            if (manifestFile != null) {
                                                useManifest = manifestFile.getValue();
                                            }
                                        }
                                    } else {
                                        Xpp3Dom useEl = conf.getChild("useDefaultManifestFile");
                                        if (useEl != null) {
                                            useManifest = useEl.getValue();
                                        }
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
                        managedPVersion = pVersion;
                    }
                    Configuration c = model.getFactory().createConfiguration();
                    if (new ComparableVersion(managedPVersion).compareTo(new ComparableVersion(JAR_PLUGIN_VERSION_MANIFEST_CONFIG_CHANGE)) >= 0) {
                        QName archiveqname = POMQName.createQName("archive", model.getPOMQNames().isNSAware());
                        POMExtensibilityElement archiveelement = model.getFactory().createPOMExtensibilityElement(archiveqname);
                        
                        QName manifestqname = POMQName.createQName("manifestFile", model.getPOMQNames().isNSAware());
                        POMExtensibilityElement manifestelement = model.getFactory().createPOMExtensibilityElement(manifestqname);
                        manifestelement.setElementText("${project.build.outputDirectory}/META-INF/MANIFEST.MF");
                        archiveelement.addAnyElement(manifestelement, 0);
                        
                        c.addExtensibilityElement(archiveelement);
                    } else {
                        c.setSimpleParameter("useDefaultManifestFile", "true");
                    }
                    p.setConfiguration(c);
                    getOrCreateBuild(model).addPlugin(p);
                }
                

            }
        }
        private static final String JAR_PLUGIN_VERSION_MANIFEST_CONFIG_CHANGE = "3.0.0";

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
