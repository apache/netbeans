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
package org.netbeans.modules.websvc.rest.projects;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.websvc.api.jaxws.project.LogUtils;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
@ProjectServiceProvider(service={RestSupport.class},
    projectType="org-netbeans-modules-web-project")
public class WebProjectRestSupport extends RestSupport {

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N

    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N

    String[] classPathTypes = new String[] {
                ClassPath.COMPILE
            };

    /** Creates a new instance of WebProjectRestSupport */
    public WebProjectRestSupport(Project project) {
        super(project);
    }

    @Override
    protected void handleSpring() throws IOException {
        if (hasSpringSupport()) {
            addJerseySpringJar();
        }
    }

    @Override
    public File getLocalTargetTestRest(){
        String path = RESTBEANS_TEST_DIR;
        AntProjectHelper helper = getAntProjectHelper();
        EditableProperties projectProps = helper
                .getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String path1 = projectProps
                .getProperty(PROP_RESTBEANS_TEST_DIR);
        if (path1 != null) {
            path = path1;
        }
        return helper.resolveFile(path);
    }
    
    @Override
    public FileObject generateTestClient(File testdir, String url) 
        throws IOException 
   {
        FileObject fileObject = MiscUtilities.generateTestClient(testdir);
        Map<String,String> map = new HashMap<String, String>();
        map.put(BASE_URL_TOKEN, url );
        MiscUtilities.modifyFile( fileObject , map );
        return fileObject;
    }
    
    @Override
    public void deploy() throws IOException{
        FileObject buildFo = Utils.findBuildXml(getProject());
        if (buildFo != null) {
            ExecutorTask task = ActionUtils.runTarget(buildFo,
                    new String[] { COMMAND_DEPLOY },
                    new Properties());
            task.waitFinished();
        }
    }

    @Override
    public void logResourceCreation() {
        Object[] params = new Object[3];
        params[0] = LogUtils.WS_STACK_JAXRS;
        params[1] = getProject().getClass().getName();
        params[2] = "REST RESOURCE"; // NOI18N
        LogUtils.logWsDetect(params);
    }

    @Override
    public String getApplicationPathFromDialog(List<RestApplication> restApplications) {
        if (restApplications.size() == 1) {
            return restApplications.get(0).getApplicationPath();
        } 
        else {
            RestApplicationsPanel panel = new RestApplicationsPanel(restApplications);
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(WebProjectRestSupport.class,"TTL_RestResourcesPath"));
            DialogDisplayer.getDefault().notify(desc);
            if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
                return panel.getApplicationPath();
            }
        }
        return null;
    }
    
    @Override
    protected void extendBuildScripts() throws IOException {
        new AntFilesHelper(this).initRestBuildExtension();
    }


}
