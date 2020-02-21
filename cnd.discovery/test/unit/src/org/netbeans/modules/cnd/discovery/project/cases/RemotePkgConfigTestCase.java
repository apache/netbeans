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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.project.cases;

import org.junit.Test;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.discovery.project.MakeProjectTestBase;
import org.netbeans.modules.cnd.remote.sync.FtpSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;

/**
 *
 */
public class RemotePkgConfigTestCase extends MakeProjectTestBase {

    private ExecutionEnvironment env;

    public RemotePkgConfigTestCase() {
        super("RemotePkgConfig");
        System.setProperty("cnd.remote.default.sync", FtpSyncFactory.ID);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        env = NativeExecutionTestSupport.getTestExecutionEnvironment("intel-S2");
        if (env == null) {
            System.err.println("REMOTE IS NOT SET UP CORRECTLY. Check ~/.cndtestrc");
            return;
        }
        ConnectionManager.getInstance().connectTo(env);
        ServerRecord record = ServerList.get(env);
        record.setUp();
        ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), true, true);
        record.validate(true);
        CompilerSetManager.get(env).initialize(true, true, null);
        CompilerSetManager.get(env).finishInitialization();
    }

    @Override
    protected ExecutionEnvironment getEE() {
        return env;
    }

    @Test
    public void testPkgConfig() throws Exception {
        performTestProject("http://pkgconfig.freedesktop.org/releases/pkg-config-0.25.tar.gz", null, true, "");
    }

}
