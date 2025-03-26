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
package org.netbeans.modules.apisupport.installer.maven.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.installer.ui.SuiteInstallerProjectProperties;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Dmitry Lipin <dlipin@netbeans.org>
 */
@ActionID(id = "org.netbeans.modules.apisupport.installer.maven.actions.BuildInstallersAction", category = "Project")
@ActionRegistration(displayName = "#CTL_BuildInstallers", lazy=false)
@ActionReference(position = 1290, path = "Projects/org-netbeans-modules-maven/Actions")
public final class BuildInstallersAction extends AbstractAction implements ContextAwareAction {

    public BuildInstallersAction() {
        putValue(NAME, NbBundle.getMessage(BuildInstallersAction.class, "CTL_BuildInstallers"));
    }

    public
    @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public
    @Override
    Action createContextAwareInstance(Lookup actionContext) {
        return new ContextBuildInstaller(actionContext);
    }

    static class ContextBuildInstaller extends AbstractAction {

        private final Lookup actionContext;

        public ContextBuildInstaller(Lookup actionContext) {
            this.actionContext = actionContext;
            putValue(NAME, NbBundle.getMessage(BuildInstallersAction.class, "CTL_BuildInstallers"));
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            Project project = actionContext.lookup(Project.class);
            if (project == null) {
                setEnabled(false); //#224115
            } else {
                NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
                if (watcher == null
                        || !NbMavenProject.TYPE_NBM_APPLICATION.equalsIgnoreCase(watcher.getPackagingType())) {
                    setEnabled(false);
                } else {
                    String version = PluginPropertyUtils.getPluginVersion(watcher.getMavenProject(), "org.codehaus.mojo", "nbm-maven-plugin");
                    if (version == null || new ComparableVersion(version).compareTo(new ComparableVersion("3.7-SNAPSHOT")) >= 0) {
                        setEnabled(false); // now handled by maven.apisupport
                    }
                }
            }
        }

        @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE") // mkdirs
        public
        @Override
        void actionPerformed(ActionEvent e) {
            Project project = actionContext.lookup(Project.class);
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            if (watcher != null && NbMavenProject.TYPE_NBM_APPLICATION.equalsIgnoreCase(watcher.getPackagingType())) {

                //Preferences prefs = SuiteInstallerProjectProperties.prefs(project);
                File suiteLocation = FileUtil.toFile(project.getProjectDirectory());
                //FileObject propertiesFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String zipName = watcher.getMavenProject().getArtifactId()
                        + "-"
                        + watcher.getMavenProject().getVersion()
                        + ".zip";
                File zipFile = new File(suiteLocation, "target/" + zipName);
                Logger.getLogger(BuildInstallersAction.class.getName()).log(
                        Level.WARNING, "Running Build Installers action for (existing={1}) zip file {0}",
                        new Object[]{zipFile, zipFile.exists()});

                Preferences prefs = SuiteInstallerProjectProperties.prefs(project);

                
                
                
                String appName = watcher.getMavenProject().getParent().getArtifactId();

                File appIconIcnsFile = null;
                String licenseType = prefs.get(SuiteInstallerProjectProperties.LICENSE_TYPE, null);
                File licenseFile = null;
                String licenseFileProp = prefs.get(SuiteInstallerProjectProperties.LICENSE_FILE, null);
                if (licenseFileProp != null) {
                    licenseFile = PropertyUtils.resolveFile(suiteLocation, licenseFileProp);
                    //if(!licenseFile.exists()) {
                    //    licenseFile = null;
                    //}
                }

                //${harness.dir}/etc/applicationIcon.icns
                appIconIcnsFile = new File(InstalledFileLocator.getDefault().locate(
                        "etc/applicationIcon.icns",
                        "org.netbeans.modules.apisupport.harness", false).getAbsolutePath().replace("\\", "/"));

                if (licenseFile == null && licenseType != null && !licenseType.equals(SuiteInstallerProjectProperties.LICENSE_TYPE_NO)) {
                    Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.FINE, "License type defined to {0}", licenseType);
                    String licenseResource = null;
                    try {
                        licenseResource = NbBundle.getMessage(SuiteInstallerProjectProperties.class,
                                "SuiteInstallerProjectProperties.license.file." + licenseType);
                    } catch (MissingResourceException ex) {
                        Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "License resource not found");
                    }

                    if (licenseResource != null) {
                        InputStream is = null;
                        try {
                            URL url = new URL(licenseResource);
                            is = url.openStream();
                            if (is != null) {
                                licenseFile = Files.createTempFile("license", ".txt").toFile();
                                licenseFile.getParentFile().mkdirs();
                                licenseFile.deleteOnExit();

                                OutputStream os = new FileOutputStream(licenseFile);
                                byte[] bytes = new byte[4096];
                                int read = 0;
                                while ((read = is.read(bytes)) > 0) {
                                    os.write(bytes, 0, read);
                                }
                                os.flush();
                                os.close();
                            } else {
                                Logger.getLogger(BuildInstallersAction.class.getName()).log(
                                        Level.WARNING, "License resource {0} not found", licenseResource);
                            }
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING,
                                    "Can`t parse URL", ex);
                        } catch (IOException ex) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING,
                                    "Input/Output error", ex);
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException ex) {
                                }
                            }
                        }
                    }
                }

                //Logger.getLogger(BuildInstallersAction.class.getName()).warning("actionPerformed for " + suiteLocation);
                Properties props = new Properties();
                File parentProjectDir = watcher.getMavenProject().getParent().getBasedir();
                //Logger.getLogger(MavenBuildInstallersAction.class.getName()).warning("parent dir " + parentProjectDir);
                props.put("suite.location", parentProjectDir.getAbsolutePath().replace("\\", "/"));
                props.put("suite.dist.zip", zipFile.getAbsolutePath().replace("\\", "/"));
                props.put("suite.dist.directory", new File(suiteLocation, "target").getAbsolutePath().replace("\\", "/"));
                props.put("installer.build.dir", new File(suiteLocation, "target/installerbuild").getAbsolutePath().replace("\\", "/"));
                props.put("installers.file.prefix",
                        watcher.getMavenProject().getArtifactId()
                        + "-" + watcher.getMavenProject().getVersion());

                props.put("suite.nbi.product.uid",
                        appName.replaceAll("[0-9]+", "").replace("_", "-").toLowerCase(Locale.ENGLISH));


                props.put("nbi.stub.location", InstalledFileLocator.getDefault().locate(
                        "nbi/stub",
                        "org.netbeans.libs.nbi.ant", false).getAbsolutePath().replace("\\", "/"));
                props.put(
                        "nbi.stub.common.location", InstalledFileLocator.getDefault().locate(
                        "nbi/.common",
                        "org.netbeans.libs.nbi.ant", false).getAbsolutePath().replace("\\", "/"));

                props.put(
                        "nbi.ant.tasks.jar", InstalledFileLocator.getDefault().locate(
                        "modules/ext/nbi-ant-tasks.jar",
                        "org.netbeans.libs.nbi.ant", false).getAbsolutePath().replace("\\", "/"));
                props.put(
                        "nbi.registries.management.jar", InstalledFileLocator.getDefault().locate(
                        "modules/ext/nbi-registries-management.jar",
                        "org.netbeans.libs.nbi.ant", false).getAbsolutePath().replace("\\", "/"));
                props.put(
                        "nbi.engine.jar", InstalledFileLocator.getDefault().locate(
                        "modules/ext/nbi-engine.jar",
                        "org.netbeans.libs.nbi.engine", false).getAbsolutePath().replace("\\", "/"));
                if (licenseFile != null) {
                    Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.FINE,
                            "License file is at {0}, exist = {1}", new Object[]{licenseFile, licenseFile.exists()});
                    props.put(
                            "nbi.license.file", licenseFile.getAbsolutePath());
                }

                List<String> platforms = new ArrayList<String>();

                boolean installerConfDefined = false;
                for (String k : new String[]{
                            SuiteInstallerProjectProperties.GENERATE_FOR_WINDOWS,
                            SuiteInstallerProjectProperties.GENERATE_FOR_LINUX,
                            SuiteInstallerProjectProperties.GENERATE_FOR_SOLARIS,
                            SuiteInstallerProjectProperties.GENERATE_FOR_MAC
                        }) {
                    if (prefs.getBoolean(k, false)) {
                        installerConfDefined = true;
                        platforms.add(k.replaceFirst("^os-", ""));
                    }
                }
                if (!installerConfDefined) {
                    if (Utilities.isWindows()) {
                        platforms.add("windows");
                    } else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                        platforms.add("linux");
                    } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                        platforms.add("solaris");
                    } else if (Utilities.isMac()) {
                        platforms.add("macosx");
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < platforms.size(); i++) {
                    if (i != 0) {
                        sb.append(" ");
                    }
                    sb.append(platforms.get(i));
                }
                if (sb.length() == 0) {
                    //nothing to build
                    RequestProcessor.getDefault().post(new Runnable() {

                        public
                        @Override
                        void run() {
                            DialogDescriptor d = new DialogDescriptor(
                                    NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotConfigured.Warning.Message"),
                                    NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotConfigured.Warning.Title"));
                            d.setModal(true);
                            JButton accept = new JButton(NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotConfigured.Warning.OK"));
                            accept.setDefaultCapable(true);
                            d.setOptions(new Object[]{
                                        accept});
                            d.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                            if (DialogDisplayer.getDefault().notify(d).equals(accept)) {
                                //SuiteCustomizer cpi = prj.getLookup().lookup(org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer.class);
                                //cpi.showCustomizer(SuiteCustomizer.APPLICATION, SuiteCustomizer.APPLICATION_CREATE_STANDALONE_APPLICATION);
                            }
                        }
                    });
                    return;
                }

                props.put("generate.installer.for.platforms",
                        sb.toString());



                File javaHome = new File(System.getProperty("java.home"));
                if (new File(javaHome,
                        "lib/rt.jar").exists() && javaHome.getName().equals("jre")) {
                    javaHome = javaHome.getParentFile();
                }
                props.put(
                        "generator-jdk-location-forward-slashes", javaHome.getAbsolutePath().replace("\\", "/"));
                /*
                props.put(
                "generated-installers-location-forward-slashes",
                new File(suiteLocation, "dist").getAbsolutePath().replace("\\", "/"));
                 */

                /*
                if(appIcon!=null) {
                File appIconFile = new File(appIcon);
                if(!appIconFile.equals(appIconFile.getAbsoluteFile())) {
                //path is relative to suite directory
                appIconFile = new File(suiteLocation, appIcon);
                }
                props.put(
                "nbi.icon.file", appIconFile.getAbsolutePath());

                }
                 */

                if (appIconIcnsFile != null) {
                    props.put(
                            "nbi.dock.icon.file", appIconIcnsFile.getAbsolutePath());
                }

                try {
                    ActionUtils.runTarget(findGenXml(), new String[]{"build"}, props);
                } catch (FileStateInvalidException ex) {
                    ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                } catch (IOException ex) {
                    ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                }
            }
        }

        private static FileObject findGenXml() {
            return FileUtil.toFileObject(InstalledFileLocator.getDefault().locate(
                    "nbi/stub/template.xml",
                    "org.netbeans.libs.nbi.ant", false));
        }
    }
}
