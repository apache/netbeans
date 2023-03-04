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
package org.netbeans.modules.maven.jaxws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.websvc.api.jaxws.project.LogUtils;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
@ProjectServiceProvider(service={RestSupport.class},
    projectType="org-netbeans-modules-maven/war")
public class MavenProjectRestSupport extends RestSupport {

    private static final String DEPLOYMENT_GOAL = "package";             //NOI18N   

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N
    
    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N
    
    public static final String ACTION_PROPERTY_DEPLOY_OPEN = "netbeans.deploy.open.in.browser"; //NOI18N
    
    private static final String TEST_SERVICES_HTML = "test-services.html"; //NOI18N

    String[] classPathTypes = new String[]{
                ClassPath.COMPILE
            };

    /** Creates a new instance of WebProjectRestSupport */
    public MavenProjectRestSupport(Project project) {
        super(project);
    }
    
    @Override
    public String getBaseURL() {
        String applicationPath = getApplicationPath();
        if (applicationPath != null) {
            if (!applicationPath.startsWith("/")) {
                applicationPath = "/"+applicationPath;
            }
        }
        return MiscUtilities.getContextRootURL(getProject())+"||"+applicationPath;            //NOI18N
    }

    @Override
    public boolean isRestSupportOn() {
        return getProjectProperty(PROP_REST_CONFIG_TYPE) != null;
    }
    
    @Override
    public FileObject generateTestClient(File testdir, String url ) throws IOException {
        return generateMavenTester(testdir, url );
    }
    
    @Override
    public void deploy() {
        RunConfig config = RunUtils.createRunConfig(FileUtil.toFile(
                getProject().getProjectDirectory()), getProject(),
                NbBundle.getMessage(MavenProjectRestSupport.class, "MSG_Deploy",    // NOI18N
                        getProject().getLookup().lookup(
                                ProjectInformation.class).getDisplayName()), 
                Collections.singletonList(DEPLOYMENT_GOAL));
        config.setProperty(ACTION_PROPERTY_DEPLOY_OPEN, Boolean.FALSE.toString() );
        ExecutorTask task = RunUtils.executeMaven(config);
        task.waitFinished();
    }
    
    @Override
    public File getLocalTargetTestRest(){
        try {
            FileObject mainFolder = getProject().getProjectDirectory()
                    .getFileObject("src/main"); // NOI18N
            if (mainFolder != null) {
                FileObject resourcesFolder = mainFolder
                        .getFileObject("resources"); // NOI18N
                if (resourcesFolder == null) {
                    resourcesFolder = mainFolder.createFolder("resources"); // NOI18N
                }
                if (resourcesFolder != null) {
                    FileObject restFolder = resourcesFolder
                            .getFileObject("rest"); // NOI18N
                    if (restFolder == null) {
                        restFolder = resourcesFolder.createFolder("rest"); // NOI18N
                    }
                    return FileUtil.toFile(restFolder);
                }
            }
        }
        catch (IOException e) {
            Logger.getLogger( MavenProjectRestSupport.class.getName() ).log(Level.WARNING, 
                    null, e);
        }
        return null;
    }

    private FileObject generateMavenTester(File testdir, String baseURL) throws IOException {
        String[] replaceKeys1 = {
            "TTL_TEST_RESBEANS", "MSG_TEST_RESBEANS_INFO"
        };
        String[] replaceKeys2 = {
            "MSG_TEST_RESBEANS_wadlErr", "MSG_TEST_RESBEANS_No_AJAX", "MSG_TEST_RESBEANS_Resource",
            "MSG_TEST_RESBEANS_See", "MSG_TEST_RESBEANS_No_Container", "MSG_TEST_RESBEANS_Content",
            "MSG_TEST_RESBEANS_TabularView", "MSG_TEST_RESBEANS_RawView", "MSG_TEST_RESBEANS_ResponseHeaders",
            "MSG_TEST_RESBEANS_Help", "MSG_TEST_RESBEANS_TestButton", "MSG_TEST_RESBEANS_Loading",
            "MSG_TEST_RESBEANS_Status", "MSG_TEST_RESBEANS_Headers", "MSG_TEST_RESBEANS_HeaderName",
            "MSG_TEST_RESBEANS_HeaderValue", "MSG_TEST_RESBEANS_Insert", "MSG_TEST_RESBEANS_NoContents",
            "MSG_TEST_RESBEANS_AddParamButton", "MSG_TEST_RESBEANS_Monitor", "MSG_TEST_RESBEANS_No_SubResources",
            "MSG_TEST_RESBEANS_SubResources", "MSG_TEST_RESBEANS_ChooseMethod", "MSG_TEST_RESBEANS_ChooseMime",
            "MSG_TEST_RESBEANS_Continue", "MSG_TEST_RESBEANS_AdditionalParams", "MSG_TEST_RESBEANS_INFO",
            "MSG_TEST_RESBEANS_Request", "MSG_TEST_RESBEANS_Sent", "MSG_TEST_RESBEANS_Received",
            "MSG_TEST_RESBEANS_TimeStamp", "MSG_TEST_RESBEANS_Response", "MSG_TEST_RESBEANS_CurrentSelection",
            "MSG_TEST_RESBEANS_DebugWindow", "MSG_TEST_RESBEANS_Wadl", "MSG_TEST_RESBEANS_RequestFailed"

        };
        FileObject testFO = copyFileAndReplaceBaseUrl(testdir, TEST_SERVICES_HTML, replaceKeys1, baseURL);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_JS, replaceKeys2, false);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_CSS);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_CSS2);
        MiscUtilities.copyFile(testdir, "expand.gif");
        MiscUtilities.copyFile(testdir, "collapse.gif");
        MiscUtilities.copyFile(testdir, "item.gif");
        MiscUtilities.copyFile(testdir, "cc.gif");
        MiscUtilities.copyFile(testdir, "og.gif");
        MiscUtilities.copyFile(testdir, "cg.gif");
        MiscUtilities.copyFile(testdir, "app.gif");

        File testdir2 = new File(testdir, "images");
        testdir2.mkdir();
        MiscUtilities.copyFile(testdir, "images/background_border_bottom.gif");
        MiscUtilities.copyFile(testdir, "images/pbsel.png");
        MiscUtilities.copyFile(testdir, "images/bg_gradient.gif");
        MiscUtilities.copyFile(testdir, "images/pname.png");
        MiscUtilities.copyFile(testdir, "images/level1_selected-1lvl.jpg");
        MiscUtilities.copyFile(testdir, "images/primary-enabled.gif");
        MiscUtilities.copyFile(testdir, "images/masthead.png");
        MiscUtilities.copyFile(testdir, "images/primary-roll.gif");
        MiscUtilities.copyFile(testdir, "images/pbdis.png");
        MiscUtilities.copyFile(testdir, "images/secondary-enabled.gif");
        MiscUtilities.copyFile(testdir, "images/pbena.png");
        MiscUtilities.copyFile(testdir, "images/tbsel.png");
        MiscUtilities.copyFile(testdir, "images/pbmou.png");
        MiscUtilities.copyFile(testdir, "images/tbuns.png");
        return testFO;
    }

    /*
     * Copy File, as well as replace tokens, overwrite if specified
     */
    private FileObject copyFileAndReplaceBaseUrl(File testdir, String name, String[] replaceKeys, String baseURL) throws IOException {
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject fo = dir.getFileObject(name);
        if (fo == null) {
            fo = dir.createData(name);
        }
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            InputStream is = RestSupport.class.getResourceAsStream("resources/"+name);
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            String lineSep = "\n";//Unix
            if(File.separatorChar == '\\')//Windows
                lineSep = "\r\n";
            String[] replaceValues = null;
            if(replaceKeys != null) {
                replaceValues = new String[replaceKeys.length];
                for(int i=0;i<replaceKeys.length;i++)
                    replaceValues[i] = NbBundle.getMessage(RestSupport.class, replaceKeys[i]);
            }
            while((line = reader.readLine()) != null) {
                for(int i=0;i<replaceKeys.length;i++) {
                    line = line.replaceAll(replaceKeys[i], replaceValues[i]);
                }
                line = line.replace("${BASE_URL}", baseURL);
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (lock != null) lock.releaseLock();
            if (reader != null) {
                reader.close();
            }
        }
        return fo;
    }

    @Override
    public void logResourceCreation() {
        Object[] params = new Object[3];
        params[0] = LogUtils.WS_STACK_JAXRS;
        params[1] = getProject().getClass().getName();
        params[2] = "RESOURCE"; // NOI18N
        LogUtils.logWsDetect(params);
    }

    @Override
    public String getProjectProperty(String name) {
        Preferences prefs = ProjectUtils.getPreferences(getProject(), MavenProjectRestSupport.class, true);
        if (prefs != null) {
            return prefs.get(name, null);
        }
        return null;
    }
    
    @Override
    public void setPrivateProjectProperty(String name, String value) {
        setProjectProperty(name, value);
    }

    @Override
    public void setProjectProperty(String name, String value) {
        Preferences prefs = ProjectUtils.getPreferences(getProject(), MavenProjectRestSupport.class, true);
        if (prefs != null) {
            prefs.put(name, value);
        }
    }

    @Override
    public void removeProjectProperties(String[] propertyNames) {
        Preferences prefs = ProjectUtils.getPreferences(getProject(), MavenProjectRestSupport.class, true);
        if (prefs != null) {
            for (String p : propertyNames) {
                prefs.remove(p);
            }
        }
    }

    @Override
    public int getProjectType() {
        NbMavenProject nbMavenProject = getProject().getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject != null) {
            String packagingType = nbMavenProject.getPackagingType();
            if (packagingType != null)
            if (NbMavenProject.TYPE_JAR.equals(packagingType)) {
                return PROJECT_TYPE_DESKTOP;
            } else if (NbMavenProject.TYPE_WAR.equals(packagingType)) {
                return PROJECT_TYPE_WEB;
            } else if (NbMavenProject.TYPE_NBM.equals(packagingType) ||
                       NbMavenProject.TYPE_NBM_APPLICATION.equals(packagingType)) {
                return PROJECT_TYPE_NB_MODULE;
            }
        }
        return PROJECT_TYPE_DESKTOP;
    }

    @Override
    public String getApplicationPathFromDialog(List<RestApplication> restApplications) {
        if (restApplications.size() == 1) {
            return restApplications.get(0).getApplicationPath();
        }
        return null;
    }

    @Override
    protected void extendBuildScripts() throws IOException {
        //
    }

    @Override
    protected void handleSpring() throws IOException {
        // TBD ?
    }

    @Override
    protected void extendJerseyClasspath() {
        // extend Jersey Classpath only for JavaEE 5  project types
        if (!MiscUtilities.isJavaEE6AndHigher(getProject())) {
            super.extendJerseyClasspath();
        }
    }
    
}
