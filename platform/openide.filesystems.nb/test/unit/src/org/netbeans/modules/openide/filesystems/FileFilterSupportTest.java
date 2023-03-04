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
package org.netbeans.modules.openide.filesystems;

import java.io.File;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.NbBundle;

/**
 * Test registered FileFilters.
 *
 * @author jhavlin
 */
@NbBundle.Messages({
    "RESOLVER=Resolver",
    "FILECHOOSER=BNM Files"
})
@MIMEResolver.Registration(
    displayName = "#RESOLVER",
    resource = "mime-resolver-filechooser.xml",
    showInFileChooser = "#FILECHOOSER", position=543543)
public class FileFilterSupportTest extends NbTestCase {

    public FileFilterSupportTest(String name) {
        super(name);
    }

    /**
     * This test, although it is quite short, tests a lot of ascpects of default
     * file filters. The resolver definition XML file contains several
     * duplicities, which are detected and ignored. If this detection fails,
     * filter description and {@code accept} method is changed, and it is cought
     * by this test.
     */
    public void testRegisteredFilters() {
        List<FileFilter> list = FileFilterSupport.findRegisteredFileFilters();
        assertNotNull(list);
        assertFalse(list.isEmpty());

        boolean found = false;
        for (FileFilter filter : list) {

            if (filter.getDescription().startsWith("BNM Files")) {
                found = true;
                checkBnmFilesFilter(filter);
            }
        }
        assertTrue("Registered File Filter was not found.", found);
    }

    private void checkBnmFilesFilter(FileFilter f) {
        assertEquals("BNM Files [.bnm, bnmHelp, bnmProject, bnminfo, bnmsettings]",
                f.getDescription());
        assertTrue(f.accept(new File("first.bnm")));
        assertTrue(f.accept(new File("second.BNM")));
        assertTrue(f.accept(new File("third.bNm")));
        assertTrue(f.accept(new File("bnmProject")));
        assertTrue(f.accept(new File("PREFIXbnmProjectAndSuFfIx")));
        assertFalse(f.accept(new File("bnmproject")));
        assertTrue(f.accept(new File("bnmSettings")));
        assertTrue(f.accept(new File("BNMSETTINGS")));
        assertFalse(f.accept(new File("bnmSettingsX")));
        assertTrue(f.accept(new File("bnmInfo")));
        assertTrue(f.accept(new File("AbnmInfoB")));
        assertTrue(f.accept(new File("aBNMINFOb")));
        assertTrue(f.accept(new File("bnmHelp")));
        assertFalse(f.accept(new File("bnmhelp")));
        assertFalse(f.accept(new File("bnmHelpX")));
        assertFalse(f.accept(new File("foo.txt")));
    }
}
