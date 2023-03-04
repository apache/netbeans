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

package org.netbeans.modules.profiler.j2se;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.spi.project.support.ant.*;
import org.openide.filesystems.FileObject;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.modules.InstalledFileLocator;


/**
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@ProjectServiceProvider(service=ProjectProfilingSupportProvider.class, 
                        projectTypes={
                            @ProjectType(id="org-netbeans-modules-java-j2seproject",position=550),
                            @ProjectType(id="org-netbeans-modules-java-j2semodule",position=550)
                        }) // NOI18N
public class J2SEProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    private static final Logger LOG = Logger.getLogger(J2SEProjectProfilingSupportProvider.class.getName());

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

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
//    String mainClassSetManually = null; // used for case when the main class is not set in project and user is prompted for it

    //~ Methods ------------------------------------------------------------------------------------------------------------------

//    @Override
//    public boolean isFileObjectSupported(FileObject fo) {
//        if (!"java".equals(fo.getExt()) && !"class".equals(fo.getExt())) {
//            return false; // NOI18N
//        }
//        return super.isFileObjectSupported(fo);
//    }
    
    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        PropertyEvaluator props = getProjectProperties(getProject());
        String platformName = props.getProperty("platform.runtime");    //NOI18N
        if (platformName == null || platformName.isEmpty()) {
            platformName = props.getProperty("platform.active"); // NOI18N
        }

        if (platformName == null) {
            return null; // not provided for some reason
        }

        return JavaPlatform.getJavaPlatformById(platformName);
    }

////    @Override
////    public boolean checkProjectCanBeProfiled(final FileObject profiledClassFile) {
////        if (profiledClassFile == null) {
////            Project p = getProject();
////            final PropertyEvaluator pp = getProjectProperties(p);
////            String profiledClass = pp.getProperty("main.class"); // NOI18N
////
////            if ((profiledClass == null) || "".equals(profiledClass)
////                    || (ProfilerTypeUtils.resolveClass(profiledClass, p) == null)) { // NOI18N
////                mainClassSetManually = ProjectUtilities.selectMainClass(p, null, ProjectUtilities.getProjectName(p),
////                                                                        -1);
////
////                //        Profiler.getDefault().displayError("No class to profile. To set up main class for a Project, go to \n" +
////                //            "Project | Properties and select the main class in the Running Project section.");
////                if (mainClassSetManually == null) {
////                    return false;
////                }
////            }
////
////            // the following code to check the main class is way too slow to perform here
////            /*      if (profiledClass != null && !"".equals(profiledClass)) {
////               final FileObject fo = SourceUtilities.findFileForClass(new String[] { profiledClass, "" }, true);
////               if (fo == null) res = false;
////               else res = (SourceUtilities.hasMainMethod(fo) || SourceUtilities.isApplet(fo));
////               } */
////            return true;
////        } else {
////            return isFileObjectSupported(profiledClassFile);
////        }
////    }

    

    @Override
    public void setupProjectSessionSettings(SessionSettings ss) {
        final PropertyEvaluator pp = getProjectProperties(getProject());

        this.setMainClass(pp, ss);

        // is this all really necessary???
        String appArgs = pp.getProperty("application.args"); // NOI18N
        ss.setMainArgs((appArgs != null) ? appArgs : ""); // NOI18N

        String runCP = pp.getProperty("run.classpath"); // NOI18N
        ss.setMainClassPath((runCP != null) ? runCP : ""); // NOI18N

        String jvmArgs = pp.getProperty("run.jvmargs"); // NOI18N
        ss.setJVMArgs((jvmArgs != null) ? jvmArgs : ""); // NOI18N
        
        String host;
        
        if ((host=getRemotePlatformHost(getProjectJavaPlatform())) != null) {
            ss.setRemoteHost(host);
        }
        
        super.setupProjectSessionSettings(ss);
    }
    
    protected void setMainClass(final PropertyEvaluator pp, SessionSettings ss) {
////        if (mainClassSetManually == null) {
            String mainClass = pp.getProperty("main.class"); // NOI18N
            ss.setMainClass((mainClass != null) ? mainClass : ""); // NOI18N
////        } else {
////            ss.setMainClass(mainClassSetManually);
////        }
    }
    
    private static String getRemotePlatformHost(JavaPlatform platform) {
        for (org.netbeans.api.java.platform.JavaPlatform jp : JavaPlatformManager.getDefault().getPlatforms(
                null,
                new Specification("j2se-remote", null))) {  //NOI18N
            if (platform.getPlatformId().equals(jp.getProperties().get("platform.ant.name"))) { //NOI18N
                return jp.getProperties().get("platform.host"); //NOI18N
            }
        }
        return null;
    }

    static PropertyEvaluator getProjectProperties(final Project project) {
        final Properties privateProps = new Properties();
        final Properties projectProps = new Properties();
        final Properties userPropsProps = new Properties();
        final Properties configProps = new Properties();

        final FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false); // NOI18N
        final FileObject configPropsFile = project.getProjectDirectory().getFileObject("nbproject/private/config.properties"); // NOI18N
        final FileObject configPropsDir = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N

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
                            LOG.log(Level.INFO, null, e);
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
                            LOG.log(Level.INFO, null, e);
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
                            LOG.log(Level.INFO, null, e);
                        }
                    }

                    if ((configPropsDir != null) && (configPropsFile != null)) {
                        try {
                            InputStream is = configPropsFile.getInputStream();
                            Properties activeConfigProps = new Properties();

                            try {
                                activeConfigProps.load(is);

                                String activeConfig = activeConfigProps.getProperty("config"); // NOI18N

                                if ((activeConfig != null) && (activeConfig.length() > 0)) {
                                    FileObject configSpecPropFile = configPropsDir.getFileObject(activeConfig + ".properties"); // NOI18N

                                    if (configSpecPropFile != null) {
                                        InputStream configSpecIn = configSpecPropFile.getInputStream();
                                        try {
                                            configProps.load(configSpecIn);
                                        } finally {
                                            configSpecIn.close();
                                        }
                                    }
                                }
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            LOG.log(Level.INFO, null, e);
                        }
                    }
                }
            });

        PropertyEvaluator pe = PropertyUtils.sequentialPropertyEvaluator(null,
                                                                         new PropertyProvider[] {
                                                                             new MyPropertyProvider(configProps),
                                                                             new MyPropertyProvider(privateProps),
                                                                             new MyPropertyProvider(userPropsProps),
                                                                             new MyPropertyProvider(projectProps)
                                                                         });

        return pe;
    }
    
    public J2SEProjectProfilingSupportProvider(Project project) {
        super(project);
    }
}
