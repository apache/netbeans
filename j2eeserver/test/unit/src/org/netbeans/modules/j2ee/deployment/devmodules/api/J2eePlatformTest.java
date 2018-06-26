/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
public class J2eePlatformTest extends ServerRegistryTestBase {

    private static final String TEST_URL = "fooservice:j2eePlatformTest";

    private static final String TEST_USERNAME = "username";

    private static final String TEST_PASSWORD = "password";

    private static final String TEST_DISPLAY_NAME = "j2eePlatformTest";

    public J2eePlatformTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Map<String, String> props = new HashMap<String, String>();
        props.put(TestDeploymentManager.PLATFORM_ROOT_PROPERTY, getWorkDirPath());

        InstanceProperties.createInstanceProperties(TEST_URL,
                TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
    }

    public void testLookup() {
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(TEST_URL);
        assertNotNull(platform.getLookup());
    }
}
