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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.maven.jaxws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
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
import org.openide.util.Exceptions;
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
            writer = new BufferedWriter(new OutputStreamWriter(os, 
                    Charset.forName("UTF-8")));         // NOI18N
            InputStream is = RestSupport.class.getResourceAsStream("resources/"+name);
            reader = new BufferedReader(new InputStreamReader(is, 
                    Charset.forName("UTF-8")));         // NOI18N
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
