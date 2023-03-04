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
package org.netbeans.modules.xml;

import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockLookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Petr Hejl
 */
public class XMLDataObjectTest extends NbTestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.DataEditorSupportTest$Lkp");
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    public XMLDataObjectTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
    }

    public void testLookup() throws Exception {
        MockLookup.setInstances(XMLDataLoader.getLoader(XMLDataLoader.class));

        FileObject document = FileUtil.toFileObject(
                FileUtil.normalizeFile(getDataDir())).getFileObject("dummyXMLDocument.xml");

        assertNotNull(document);

        DataObject object = DataObject.find(document);
        assertTrue(object instanceof XMLDataObject);

        XMLDataObject dataObject = (XMLDataObject) object;
        assertNotNull(dataObject.getLookup().lookup(FileObject.class));

        EditCookie ec = (EditCookie)dataObject.getCookie(EditCookie.class);
        assertNotNull("Editor cookie found", ec);

        EditCookie lkp = dataObject.getLookup().lookup(EditCookie.class);
        assertEquals("Cookies are the same", ec, lkp);

        OpenCookie lkp2 = dataObject.getLookup().lookup(OpenCookie.class);

        lkp.edit();
        lkp2.open();

        Set<?> all = null;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            all = WindowManager.getDefault().getRegistry().getOpened();
            if (all.size() > 0) {
                break;
            }
        }

        assertEquals("There is just one TC: " + all, 1, all.size());

        assertEquals("Cookies are the same", lkp, lkp2);
    }
}
