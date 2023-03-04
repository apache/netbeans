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

package org.netbeans.modules.java.platform;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
public class PlatformInstallTest extends NbTestCase {

    public PlatformInstallTest(final String name) {
        super (name);
    }

    public static Test suite() {
        return NbModuleSuite.
                createConfiguration(PlatformInstallTest.class).
                gui(false).
                clusters("extide"). //NOI18N
                enableModules("org.netbeans.modules.java.j2seplatform").    //NOI18N
                suite();
    }

    public void testLookup () {
        final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers"; // NOI18N
        final Lookup lkp = Lookups.forPath(INSTALLER_REGISTRY_FOLDER);
        
        lkp.lookupAll(GeneralPlatformInstall.class);
        assertPlatformInstalls(
            lkp.lookupAll(CustomPlatformInstall.class),
            "org.netbeans.modules.java.j2seembedded.wizard.RemotePlatformInstall"); //NOI18N
        assertPlatformInstalls(
            lkp.lookupAll(PlatformInstall.class),
            "org.netbeans.modules.java.j2seplatform.J2SEInstallImpl");              //NOI18N
        assertPlatformInstalls(
            lkp.lookupAll(GeneralPlatformInstall.class),
            "org.netbeans.modules.java.j2seplatform.J2SEInstallImpl",               //NOI18N
            "org.netbeans.modules.java.j2seembedded.wizard.RemotePlatformInstall"); //NOI18N
    }

    private static void assertPlatformInstalls(
        final Collection<? extends GeneralPlatformInstall> result,
        final String... expectedClasses) {
        if (result.size() != expectedClasses.length) {
            assertTrue(result.toString(), false);
        }
        final Set<String> expected = new HashSet<String>(Arrays.asList(expectedClasses));
        for (GeneralPlatformInstall i : result) {
            assertTrue(result.toString(), expected.remove(i.getClass().getName()));
        }
    }
}
