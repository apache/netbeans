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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db;

import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseModuleTest extends TestBase {

    // TODO should also test that connections are disconnected

    // TODO should also test that no errors are only logged to ErrorManager with EM.notify(INFORMATIONAL, e)
    
    public DatabaseModuleTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }

    public void testRuntimesAreStopped() throws Exception {
        FileObject runtimeFolder = FileUtil.getConfigFile("Databases/Runtimes");
        FileObject runtime1 = FileUtil.createData(runtimeFolder, "runtime1.instance");
        runtime1.setAttribute("instanceOf", DatabaseRuntime.class.getName());
        runtime1.setAttribute("instanceCreate", new DatabaseRuntimeImpl());
        FileObject runtime2 = FileUtil.createData(runtimeFolder, "runtime2.instance");
        runtime2.setAttribute("instanceOf", DatabaseRuntime.class.getName());
        runtime2.setAttribute("instanceCreate", new DatabaseRuntimeImpl());
        
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
        new DatabaseModule().close();
        
        int checked = 0;
        for (int i = 0; i < runtimes.length; i++) {
            if (runtimes[i] instanceof DatabaseRuntimeImpl) {
                assertTrue(((DatabaseRuntimeImpl)runtimes[i]).stopped);
                checked++;
            }
        }
        // check we have really tested our DatabaseRuntime implementations
        assertTrue(checked == 2);
    }
    
    static final class DatabaseRuntimeImpl implements DatabaseRuntime {
        
        boolean stopped;
        
        public boolean acceptsDatabaseURL(String url) {
            return true;
        }

        public void stop() {
            stopped = true;
        }

        public void start() {
        }

        public boolean isRunning() {
            return true;
        }

        public String getJDBCDriverClass() {
            return null;
        }

        public boolean canStart() {
            return true;
        }
    }
}
