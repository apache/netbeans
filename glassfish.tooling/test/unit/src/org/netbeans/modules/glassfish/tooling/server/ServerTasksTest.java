/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server;

import java.io.File;
import java.util.*;
import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandStopDAS;
import org.netbeans.modules.glassfish.tooling.admin.ResultProcess;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.StartupArgs;
import org.netbeans.modules.glassfish.tooling.utils.NetUtils;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;

/**
 * Tests for various server tasks.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ServerTasksTest extends CommonTest {

    /** GlassFish test server property file. */
    private static final String GLASSFISH_PROPERTES = "src/test/java/org/netbeans/modules/glassfish/tooling/server/GF.properties";
    
    private static Properties gfProperties;
    private static GlassFishServer gfServer;
    private static StartupArgs args;
    
    @BeforeClass
    public static void init() {
        gfProperties = readProperties(GLASSFISH_PROPERTES);
        gfServer = createGlassfishServer();
        args = new StartupArgs() {
            
            private List<String> javaArgs = Arrays.asList("-Xms128m", "-XX:PermSize=96m", "-Dtest=true");
            private List<String> glassfishArgs = Arrays.asList("--domaindir " + gfServer.getDomainsFolder() + File.separator + gfServer.getDomainName(),
                    "--domain " + gfServer.getDomainName());
            private HashMap<String, String> envVars;

            @Override
            public List<String> getGlassfishArgs() {
                return glassfishArgs;
            }

            @Override
            public List<String> getJavaArgs() {
                return javaArgs;
            }

            @Override
            public Map<String, String> getEnvironmentVars() {
                return null;
            }

            @Override
            public String getJavaHome() {
                return jdkProperties().getProperty(JDKPROP_HOME);
            }
        };
    }
    
    //@Test
    public void startServerNoArgsTest() {
        ResultProcess process = ServerTasks.startServer(gfServer, new StartupArgs() {

            @Override
            public List<String> getGlassfishArgs() {
                return null;
            }

            @Override
            public List<String> getJavaArgs() {
                return null;
            }

            @Override
            public Map<String, String> getEnvironmentVars() {
                return null;
            }

            @Override
            public String getJavaHome() {
                return jdkProperties().getProperty(JDKPROP_HOME);
            }
        });
        
        assertEquals(process.getState(), TaskState.COMPLETED);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            fail();
        }
        assertTrue(ServerUtils.isHttpPortListening(
                gfServer, NetUtils.PORT_CHECK_TIMEOUT));
        CommandStopDAS.stopDAS(gfServer);
    }
    
    //@Test
    public void startServerWithArgsTest() {
        ResultProcess process = ServerTasks.startServer(gfServer, args);
        
        assertEquals(process.getState(), TaskState.COMPLETED);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            fail();
        }
        assertTrue(ServerUtils.isHttpPortListening(
                gfServer, NetUtils.PORT_CHECK_TIMEOUT));
        CommandStopDAS.stopDAS(gfServer);
    }
   
}
