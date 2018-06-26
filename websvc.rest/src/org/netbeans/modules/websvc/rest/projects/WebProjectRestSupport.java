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
