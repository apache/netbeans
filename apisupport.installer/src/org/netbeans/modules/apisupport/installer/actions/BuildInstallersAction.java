/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.apisupport.installer.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.openide.ErrorManager;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.installer.ui.SuiteInstallerProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

@ActionID(id = "org.netbeans.modules.apisupport.installer.actions.BuildInstallersAction", category = "Project")
@ActionRegistration(displayName = "#CTL_BuildInstallers", /* XXX might work to be context action on List<Project> */ lazy=false)
@ActionReference(position = 400, path = "Projects/org-netbeans-modules-apisupport-project-suite-package/Actions")
public final class BuildInstallersAction extends AbstractAction implements ContextAwareAction {

    public BuildInstallersAction() {
        putValue(NAME, NbBundle.getMessage(BuildInstallersAction.class, "CTL_BuildInstallers"));
    }

    public @Override void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override Action createContextAwareInstance(Lookup actionContext) {
        return new ContextBuildInstaller(actionContext);
    }

    static class ContextBuildInstaller extends AbstractAction {

        private final Lookup actionContext;

        public ContextBuildInstaller(Lookup actionContext) {
            this.actionContext = actionContext;
            putValue(NAME, NbBundle.getMessage(BuildInstallersAction.class, "CTL_BuildInstallers"));
        }

        @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE") // mkdirs
        public @Override void actionPerformed(ActionEvent e) {
                for (Project prj : actionContext.lookupAll(Project.class)) {
                        Preferences prefs = SuiteInstallerProjectProperties.prefs(prj);
                        File suiteLocation = FileUtil.toFile(prj.getProjectDirectory());
                        String appName = "";
                        String appIcon = null;
                        String appIconIcns = null;
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
                        boolean usePack200 = prefs.getBoolean(SuiteInstallerProjectProperties.USE_PACK200_COMPRESSION, false);

                        try {
                            FileObject propertiesFile = prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            InputStream is = propertiesFile.getInputStream();
                            Properties ps = new Properties();
                            try {
                                ps.load(is);
                            } finally {
                                is.close();
                            }
                            appName = ps.getProperty("app.name");
                            if (appName != null && appName.contains("$")) {
                                appName = ProjectUtils.getInformation(prj).getName();
                            }
                            
                            appIcon = ps.getProperty("app.icon");
                            appIconIcns = ps.getProperty("app.icon.icns", null);
                            if(appIconIcns!=null) {
                                appIconIcnsFile = PropertyUtils.resolveFile(suiteLocation, appIconIcns);
                            } else {
                                //${harness.dir}/etc/applicationIcon.icns
                                appIconIcnsFile = new File(InstalledFileLocator.getDefault().locate(
                                    "etc/applicationIcon.icns",
                                    "org.netbeans.modules.apisupport.harness", false).getAbsolutePath().replace("\\", "/"));
                            }


                            if (appName == null) {
                                //suite, not standalone app
                                RequestProcessor.getDefault().post(new Runnable() {
                                    public @Override void run() {
                                        DialogDescriptor d = new DialogDescriptor(
                                                NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotApp.Warning.Message"),
                                                NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotApp.Warning.Title"));
                                        d.setModal(true);
                                        JButton accept = new JButton(NbBundle.getMessage(BuildInstallersAction.class, "BuildInstallersAction.NotApp.Warning.OK"));
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
                        } catch (IOException ex) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "Cannot load properties", ex);
                        }

                        
                        if (licenseFile==null && licenseType != null && !licenseType.equals(SuiteInstallerProjectProperties.LICENSE_TYPE_NO)) {
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
                                        licenseFile = File.createTempFile("license", ".txt");
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
                        props.put("suite.location", suiteLocation.getAbsolutePath().replace("\\", "/"));
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
                                    "License file is at {0}, exist = {1}", new Object[] {licenseFile, licenseFile.exists()});
                            props.put(
                                    "nbi.license.file", licenseFile.getAbsolutePath());
                        }

                        List<String> platforms = new ArrayList<String>();

                        boolean installerConfDefined = false;
                        for (String k : new String[] {
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
                                public @Override void run() {
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
                        props.put(
                                "pack200.enabled", "" + usePack200);

                        if(appIcon!=null) {
                            File appIconFile = new File(appIcon);
                            if(!appIconFile.equals(appIconFile.getAbsoluteFile())) {
                                //path is relative to suite directory
                                appIconFile = new File(suiteLocation, appIcon);
                            }
                            props.put(
                                "nbi.icon.file", appIconFile.getAbsolutePath());

                        }
                        
                        if(appIconIcnsFile!=null) {
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


