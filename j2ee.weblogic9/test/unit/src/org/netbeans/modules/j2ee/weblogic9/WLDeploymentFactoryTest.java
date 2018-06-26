/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class WLDeploymentFactoryTest extends NbTestCase {

    public WLDeploymentFactoryTest(String name) {
        super(name);
    }

    public void testDeploymentManagerCleared() throws Exception {
        File dir = getWorkDir();
        FileObject server = FileUtil.createFolder(new File(dir, "server"));
        FileObject domain = FileUtil.createFolder(new File(dir, "domain"));
        FileObject config = domain.createFolder("config");
        config.createData("config", "xml");

        String serverPath = FileUtil.toFile(server).getAbsolutePath();
        String domainPath = FileUtil.toFile(domain).getAbsolutePath();
        String url = WLDeploymentFactory.getUrl("localhost", 7001, serverPath, domainPath);
        Map<String, String> props = new HashMap<String, String>();
        props.put(WLPluginProperties.SERVER_ROOT_ATTR, serverPath);
        props.put(WLPluginProperties.DOMAIN_ROOT_ATTR, domainPath);

        InstanceProperties ip = InstanceProperties.createInstancePropertiesNonPersistent(
                url, "test", "test", "test", props);
        WLDeploymentManager manager = (WLDeploymentManager) WLDeploymentFactory.getInstance().getDisconnectedDeploymentManager(url);
        assertEquals(ip, manager.getInstanceProperties());

        InstanceProperties.removeInstance(url);
        WeakReference<InstanceProperties> ipRef = new WeakReference<InstanceProperties>(ip);
        ip = null;
        WeakReference<WLDeploymentManager> ref = new WeakReference<WLDeploymentManager>(manager);
        manager = null;

        assertGC("InstanceProperties leaking", ipRef);

        // lets indirectly touch the cache
        url = url + "_other";
        InstanceProperties.createInstancePropertiesNonPersistent(url, "test", "test", "test",
                        props);
        WLDeploymentManager managerOther = (WLDeploymentManager) WLDeploymentFactory.getInstance().getDisconnectedDeploymentManager(url);

        assertGC("WLDeploymentManager leaking", ref);
    }
}
