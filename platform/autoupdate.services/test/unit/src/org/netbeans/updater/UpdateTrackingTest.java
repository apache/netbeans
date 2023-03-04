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

package org.netbeans.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
        os.write(s.getBytes(StandardCharsets.UTF_8));
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
