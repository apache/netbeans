/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.weblogic.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Collection;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.api.WebLogicDeployer;
import org.netbeans.modules.weblogic.common.api.WebLogicRuntime;

/**
 *
 * @author Petr Hejl
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String serverHome = "/home/petr/software/wls12130/wlserver";//args[0];
        String domainHome = "/home/petr/software/wls12130/user_projects/domains/mydomain";//;args[1];
        //String artifact = args[2];
        WebLogicConfiguration config = WebLogicConfiguration.forLocalDomain(
                new File(serverHome), new File(domainHome), new WebLogicConfiguration.Credentials() {

            @Override
            public String getUsername() {
                return "weblogic";
            }

            @Override
            public String getPassword() {
                return "welcome1";
            }
        });
//        WebLogicConfiguration config = WebLogicConfiguration.forRemoteDomain(
//                new File(serverHome), "192.168.56.101", 7001, false, new WebLogicConfiguration.Credentials() {
//
//            @Override
//            public String getUsername() {
//                return "weblogic";
//            }
//
//            @Override
//            public String getPassword() {
//                return "welcome1";
//            }
//        });

        WebLogicRuntime runtime = WebLogicRuntime.getInstance(config);
        runtime.startAndWait(new DefaultFactory(), new DefaultFactory(), null);
        System.out.println("Started");

        runtime.startAndWait(new DefaultFactory(), new DefaultFactory(), null);
        System.out.println("Started again");

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(config, null, null);
        Collection<WebLogicDeployer.Application> apps = deployer.list(null).get();
        for (WebLogicDeployer.Application app : apps) {
            System.out.println("Application " + app);
        }

        runtime.stopAndWait(new DefaultFactory(), new DefaultFactory());
        System.out.println("Stopped");

//        WebLogicDeployer deployer = WebLogicDeployer.getInstance(config, null);
//        Future<Boolean> ret = deployer.deploy(new File(artifact), null, new String[0]);
//        System.out.println("Deployed: " + ret.get());
    }

    private static class DefaultFactory implements BaseExecutionDescriptor.InputProcessorFactory {

        @Override
        public InputProcessor newInputProcessor() {
            return InputProcessors.copying(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
    }
}
