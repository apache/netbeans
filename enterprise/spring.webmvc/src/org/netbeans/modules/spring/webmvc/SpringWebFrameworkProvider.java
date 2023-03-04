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
/*
 * Contributor(s): Craig MacKay
 */

package org.netbeans.modules.spring.webmvc;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Provides WebFrameworkProvider implementation for Spring Web MVC.
 *
 * @author Craig MacKay
 */
public class SpringWebFrameworkProvider extends WebFrameworkProvider {

    public static final String CONTEXT_LOADER = "org.springframework.web.context.ContextLoaderListener"; // NOI18N
    public static final String DISPATCHER_SERVLET = "org.springframework.web.servlet.DispatcherServlet"; // NOI18N
    private SpringWebModuleExtender panel;

    public SpringWebFrameworkProvider() {
        super(NbBundle.getMessage(SpringWebFrameworkProvider.class, "LBL_FrameworkName"), NbBundle.getMessage(SpringWebFrameworkProvider.class, "LBL_FrameworkDescription"));
    }

    @Override
    public boolean isInWebModule(WebModule webModule) {
        FileObject dd = webModule.getDeploymentDescriptor();
        if (dd == null) {
            return false;
        }
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            return (webApp.findBeanByName("Servlet", "ServletClass", DISPATCHER_SERVLET) != null) || (webApp.findBeanByName("Listener", "ListenerClass", CONTEXT_LOADER) != null); // NOI18N
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return false;
    }

    @Override
    public File[] getConfigurationFiles(WebModule webModule) {
        return new File[0];
    }
    
    @Override
    public WebModuleExtender createWebModuleExtender(WebModule webModule, ExtenderController controller) {
        boolean defaultValue = (webModule == null || !isInWebModule(webModule));
        panel = new SpringWebModuleExtender(this, controller, webModule, !defaultValue); // NOI18N
        return panel;
    }  
}
