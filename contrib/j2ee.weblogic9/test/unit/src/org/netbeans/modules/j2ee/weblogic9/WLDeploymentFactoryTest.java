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
