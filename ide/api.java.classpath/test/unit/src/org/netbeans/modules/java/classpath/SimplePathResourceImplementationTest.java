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

package org.netbeans.modules.java.classpath;

import java.io.File;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class SimplePathResourceImplementationTest extends NbTestCase {

    public SimplePathResourceImplementationTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testVerify() throws Exception {
        try {
            SimplePathResourceImplementation.verify(null,null);
            assertTrue("Verify should fail for null",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/foo.jar"),null);
            assertTrue("Verify should fail for non jar protocol file",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            File f = new File(getWorkDir(),"test.jar");
            f.createNewFile();
            SimplePathResourceImplementation.verify(new URL(BaseUtilities.toURI(f).toString()+'/'),null);
            assertTrue("Verify should fail for non jar protocol file",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/foo"),null);
            assertTrue("Verify should fail for URLs not ending by /",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/../net/foo"),null);
            assertTrue("Verify should fail for URLs having ..",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:///tmp/./foo"),null);
            assertTrue("Verify should fail for URLs having .",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:////server/share/path/"),null);            
        } catch (IllegalArgumentException e) {
            assertTrue("Verify should not fail for UNC URL.",false);
        }
        try {
            SimplePathResourceImplementation.verify(new URL("file:////server/share/path/../foo/"),null);
            assertTrue("Verify should fail for UNC URLs having ..",false);
        } catch (IllegalArgumentException e) {
        }
        try {
            final File wd = getWorkDir();
            final File strangeFolder = new File(wd,"strange.jar");  //NOI18N
            strangeFolder.mkdirs();
            SimplePathResourceImplementation.verify(Utilities.toURI(strangeFolder).toURL(),null);
        } catch (IllegalArgumentException e) {
            assertTrue("Verify should not fail for .jar folder",false);
        }
        try {
            final File wd = getWorkDir();
            final File strangeFile = new File(wd,".jar");
            final URL url = FileUtil.urlForArchiveOrDir(strangeFile);
            SimplePathResourceImplementation.verify(url, null);
        } catch (IllegalArgumentException e) {
            assertTrue("Verify should not fail for .jar folder",false);
        }

        try {
            final File wd = getWorkDir();
            final File strangeFile = new File(wd,".jar");
            strangeFile.createNewFile();
            SimplePathResourceImplementation.verify(Utilities.toURI(strangeFile).toURL(), null);
            assertTrue("Verify should fail for .jar file",false);
        } catch (IllegalArgumentException e) {
        }
    }

}
