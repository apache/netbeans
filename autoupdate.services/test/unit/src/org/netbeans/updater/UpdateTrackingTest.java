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
 * "Portions Copyrighted [year] [name of copyright owner]
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.updater.UpdateTracking.Module;
import org.netbeans.updater.UpdateTracking.Version;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UpdateTrackingTest extends NbTestCase {

    public UpdateTrackingTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        File ut = new File(new File(getWorkDir(), "update_tracking"), "my-test.xml");
        ut.getParentFile().mkdirs();
        File module = new File(new File(new File(getWorkDir(), "modules"), "autoload"), "my-test.jar");
        module.getParentFile().mkdirs();
        module.createNewFile();

        new File(getWorkDir(), "rake").createNewFile();

        String s =
"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"<module codename=\"my.test/1\">\n" +
"    <module_version install_time=\"1248262763718\" last=\"true\" origin=\"NetBeans\" specification_version=\"1.5.1\">\n" +
"        <file crc=\"3584009063\" name=\"modules/autoload/my-test.jar\"/>\n" +
"        <file crc=\"201134076\" name=\"rake\"/>\n" +
"    </module_version>\n" +
"</module>";
        
        FileOutputStream os = new FileOutputStream(ut);
        os.write(s.getBytes("UTF-8"));
        os.close();

    }



    public void testUpdateTrackingWithFilesInRootOfCluster() throws IOException {
        MockContext context = new MockContext();
        UpdateTracking ut = UpdateTracking.getTracking(getWorkDir(), true, context);
        assertNotNull("tracking created", ut);
        assertTrue("Installed", ut.isModuleInstalled("my.test"));
        Module m = ut.readModuleTracking("my.test", true);
        Version v = m.addNewVersion ("1.6", "download");
        v.addFileWithCrc("modules/autoload/my-test.jar", "3584009063");
        v.addFileWithCrc("rake", "201134076");

        assertNotNull("Module exists", m);
        // this call used to throw NullPointerException
        m.writeConfigModuleXMLIfMissing();
    }

    private static final class MockContext implements UpdatingContext {
        @Override
        public Collection<File> forInstall() {
            return null;
        }

        @Override
        public boolean isFromIDE() {
            return false;
        }

        @Override
        public void unpackingFinished() {
        }

        @Override
        public void setProgressValue(long bytesRead) {
        }

        @Override
        public void setLabel(String string) {
        }

        @Override
        public void unpackingIsRunning() {
        }

        @Override
        public void setProgressRange(long i, long totalLength) {
        }

        @Override
        public void runningFinished() {
        }

        @Override
        public void disposeSplash() {
        }

        @Override
        public OutputStream createOS(File bckFile) throws FileNotFoundException {
            return new FileOutputStream(bckFile);
        }
    }
}
