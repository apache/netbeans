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

package org.netbeans.modules.profiler.j2ee;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.spi.project.support.ant.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.j2ee.impl.ServerJavaPlatform;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;


/**
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "J2EEProjectTypeProfiler_ProfilingNotSupportedMsg=Target server does not support profiling.\nTo profile project running on current server, use Attach to Project or Attach to External Process.\nTo change server for this project, right-click the project and select Properties | Run | Server.\n",
    "J2EEProjectTypeProfiler_ProfilingFileNotSupportedMsg=Profiling {0} is not supported in project {1}.",
    "J2EEProjectTypeProfiler_SkipButtonName=Skip",
    "J2EEProjectTypeProfiler_NoServerFoundMsg=<html><b>Failed to obtain server information.</b><br>Check if the server for the profiled project has been set correctly.</html>",
    "TTL_setServletExecutionUri=Provide Servlet Request Parameters"
})
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider.class, 
                        projectTypes={
                            @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"), 
                            @ProjectType(id="org-netbeans-modules-j2ee-earproject"), 
                            @ProjectType(id="org-netbeans-modules-web-project")
                        }
)
public final class J2EEProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

//    private static class JSPNameFormatter implements org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter {
//        //~ Methods --------------------------------------------------------------------------------------------------------------
//
//        public Formattable formatMethodName(final SourceCodeSelection sourceCodeSelection) {
//            return new Formattable() {
//                    public String toFormatted() {
//                        String name = WebProjectUtils.getJSPPath(sourceCodeSelection);
//
//                        return name;
//                    }
//                };
//        }
//
//        public Formattable formatMethodName(final String className, final String methodName, final String signature) {
//            return new Formattable() {
//                    public String toFormatted() {
//                        ClientUtils.SourceCodeSelection tmpSelection = new ClientUtils.SourceCodeSelection(className, methodName,
//                                                                                                           signature);
//                        String name = WebProjectUtils.getJSPPath(tmpSelection);
//
//                        return name;
//                    }
//                };
//        }
//    }

    private static class MyPropertyProvider implements PropertyProvider {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Properties props;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private MyPropertyProvider(Properties props) {
            this.props = props;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Map /*<String,String>*/ getProperties() {
            return props;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.j2ee"); // NOI18N
    // not very clean, consider implementing differently!
    // stores last generated agent ID
    private static int lastAgentID = -1;

    // stores last used agent port
    private static int lastAgentPort = 5140;


    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public J2EEProjectProfilingSupportProvider(Project project) {
        super(project);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static int getLastAgentID() {
        return lastAgentID;
    }

    public static int getLastAgentPort() {
        return lastAgentPort;
    }
    
    public static void resetLastValues() {
        lastAgentID = -1;
        lastAgentPort = -1;
    }

    public static String getServerInstanceID(final Project project) {
        J2eeModuleProvider serverInstanceModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);

        if (serverInstanceModuleProvider == null) {
            return null;
        }

        return serverInstanceModuleProvider.getServerInstanceID();
    }

    // --- ProjectTypeProfiler implementation ------------------------------------------------------------------------------

    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        String serverInstanceID = getServerInstanceID(getProject());

        if (serverInstanceID == null) {
            return null;
        }

        return ServerJavaPlatform.getPlatform(serverInstanceID);
    }

//    private static JavaPlatform getServerJavaPlatform(String serverInstanceID) {
//        J2eePlatform j2eePlatform = getJ2eePlatform(serverInstanceID);
//
//        if (j2eePlatform == null) {
//            return null;
//        }
//
//        org.netbeans.api.java.platform.JavaPlatform jp = j2eePlatform.getJavaPlatform();
//        if (jp == null) {
//            return null;
//        }
//        return JavaPlatform.getJavaPlatformById(jp.getProperties().get("platform.ant.name")); // NOI18N
//    }

    @Override
    public boolean checkProjectCanBeProfiled(FileObject profiledClassFile) {
//        // Unsupported project type
//        if (!isProfilingSupported()) {
//            return false;
//        }

        // Check if server supports profiling
        J2eePlatform j2eePlatform = getJ2eePlatform(getProject());

//        if (j2eePlatform == null) {
//            ProfilerDialogs.displayError(Bundle.J2EEProjectTypeProfiler_NoServerFoundMsg());
//
//            return false;
//        }

        if (j2eePlatform != null && !j2eePlatform.supportsProfiling()) {
            // Server doesn't support profiling
            ProfilerDialogs.displayWarning(Bundle.J2EEProjectTypeProfiler_ProfilingNotSupportedMsg());

            return false;
        }
        
        if (profiledClassFile != null) {
            if (isFileObjectSupported(profiledClassFile)) {
                return true;
            } else {
                ProfilerDialogs.displayWarning(Bundle.J2EEProjectTypeProfiler_ProfilingFileNotSupportedMsg(
                                               profiledClassFile.getNameExt(), ProjectUtilities.getDisplayName(getProject())));
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isFileObjectSupported(FileObject file) {
        Project project = getProject();
        return ((WebProjectUtils.isJSP(file) && WebProjectUtils.isWebDocumentSource(file, project)) // jsp from /web directory               
                  || (WebProjectUtils.isHttpServlet(file) && WebProjectUtils.isWebJavaSource(file, project)
                  /*&& WebProjectUtils.isMappedServlet(file, project, true)*/) // mapped servlet from /src directory
                  /*|| super.isFileObjectSupported(file)*/); // regular java file
    }

    // --- Profiler SPI support --------------------------------------------------------------------------------------------
    public static int generateAgentID() {
        int newAgentID = generateAgentNumber(); // generate new agent ID

        while (newAgentID == lastAgentID) {
            newAgentID = generateAgentNumber(); // ensure that it's different from previous ID
        }

        lastAgentID = newAgentID; // assign new agent ID

        return getLastAgentID();
    }

    public static JavaPlatform generateAgentJavaPlatform(String serverInstanceID) {
        return ServerJavaPlatform.getPlatform(serverInstanceID);
    }

    public static int generateAgentPort() {
        lastAgentPort = ProfilerIDESettings.getInstance().getPortNo(); // should be reimplemented, may return different port than the passed by AntActions to target JVM

        return getLastAgentPort();
    }

    @Override
    public void setupProjectSessionSettings(SessionSettings ss) {
        Project project = getProject();
        // settings required for code fragment profiling
        final PropertyEvaluator pp = getProjectProperties(project);
        ss.setMainClass(""); // NOI18N

        String appArgs = pp.getProperty("application.args"); // NOI18N
        ss.setMainArgs((appArgs != null) ? appArgs : ""); // NOI18N

        String runCP = pp.getProperty("build.classes.dir"); // NOI18N
        ss.setMainClassPath((runCP != null)
                            ? MiscUtils.getAbsoluteFilePath(runCP,
                                                            FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath()) : ""); // NOI18N
        ss.setJVMArgs(""); // NOI18N
        ss.setWorkingDir(""); // NOI18N
        
        super.setupProjectSessionSettings(ss);
    }

    private static J2eePlatform getJ2eePlatform(final Project project) {
        String serverInstanceID = getServerInstanceID(project);

        if (serverInstanceID == null) {
            return null;
        }

        return getJ2eePlatform(serverInstanceID);
    }

    private static J2eePlatform getJ2eePlatform(String serverInstanceID) {
        return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
    }

//    private static JavaPlatform getJavaPlatformFromAntName(Project project, Map<String, String> props) {
//        String javaPlatformAntName = props.get("profiler.info.javaPlatform"); // NOI18N
//
//        if (javaPlatformAntName.equals("default_platform")) {
//            return JavaPlatform.getDefaultPlatform();
//        }
//
//        return JavaPlatform.getJavaPlatformById(javaPlatformAntName);
//    }

    // --- Private methods -------------------------------------------------------------------------------------------------
    private static int generateAgentNumber() {
        return (int) (Math.random() * (float) Integer.MAX_VALUE);
    }

    private PropertyEvaluator getProjectProperties(final Project project) {
        final Properties privateProps = new Properties();
        final Properties projectProps = new Properties();
        final Properties userPropsProps = new Properties();

        final FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false); // NOI18N

        ProjectManager.mutex().readAccess(new Runnable() {
                public void run() {
                    // the order is 1. private, 2. project, 3. user to reflect how Ant handles property definitions (immutable, once set property value cannot be changed)
                    if (privatePropsFile != null) {
                        try {
                            final InputStream is = privatePropsFile.getInputStream();

                            try {
                                privateProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        } 
                    }

                    if (projectPropsFile != null) {
                        try {
                            final InputStream is = projectPropsFile.getInputStream();

                            try {
                                projectProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }

                    if (userPropsFile != null) {
                        try {
                            final InputStream is = new BufferedInputStream(new FileInputStream(userPropsFile));

                            try {
                                userPropsProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                }
            });

        PropertyEvaluator pe = PropertyUtils.sequentialPropertyEvaluator(null,
                                                                         new PropertyProvider[] {
                                                                             new J2EEProjectProfilingSupportProvider.MyPropertyProvider(privateProps),
                                                                             new J2EEProjectProfilingSupportProvider.MyPropertyProvider(userPropsProps),
                                                                             new J2EEProjectProfilingSupportProvider.MyPropertyProvider(projectProps)
                                                                         });

        return pe;
    }

//    private void addJspMarker(MethodMarker marker, Mark mark, Project project) {
//        ClientUtils.SourceCodeSelection[] jspmethods = WebProjectUtils.getJSPRootMethods(project, true);
//
//        if (jspmethods != null) {
//            for (int i = 0; i < jspmethods.length; i++) {
//                marker.addMethodMark(jspmethods[i].getClassName(), jspmethods[i].getMethodName(),
//                                     jspmethods[i].getMethodSignature(), mark);
//            }
//        }
//    }
}
