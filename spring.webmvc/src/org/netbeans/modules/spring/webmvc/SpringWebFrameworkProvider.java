/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
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
