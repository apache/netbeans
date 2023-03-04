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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.TextESAccessor;
import org.netbeans.modules.xml.text.TextEditorSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupportRedirector;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.test.MockLookup;

/**
 *
 * @author alsimon
 */
public class XMLCESTest extends NbTestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.DataEditorSupportTest$Lkp");
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    public XMLCESTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
    }

    public void testCES() throws Exception {
        MockLookup.setInstances(XMLDataLoader.getLoader(XMLDataLoader.class), new XMLCESTest.Redirector());

        FileObject document = FileUtil.toFileObject(
                FileUtil.normalizeFile(getDataDir())).getFileObject("dummyXMLDocument.xml");

        assertNotNull(document);

        DataObject object = DataObject.find(document);
        assertTrue(object instanceof XMLDataObject);

        XMLDataObject dataObject = (XMLDataObject) object;

        CloneableEditorSupport ces = (CloneableEditorSupport)dataObject.getLookup().lookup(CloneableEditorSupport.class);
        assertNotNull("CES found", ces);

    }
    
    public void testPrepareDocument() throws Exception {
        
        MockLookup.setInstances(XMLDataLoader.getLoader(XMLDataLoader.class), new XMLCESTest.Redirector());

        FileObject document = FileUtil.toFileObject(
                FileUtil.normalizeFile(getDataDir())).getFileObject("dummyXMLDocument.xml");

        assertNotNull(document);

        DataObject object = DataObject.find(document);
        assertTrue(object instanceof XMLDataObject);

        XMLDataObject dataObject = (XMLDataObject) object;

        EditorCookie ces = (EditorCookie)dataObject.getLookup().lookup(EditorCookie.class);
        assertNotNull("CES found", ces);
        Task t = ces.prepareDocument();
        t.waitFinished();
        
        String mime = TextESAccessor.getMimeType((TextEditorSupport)ces);
        assertEquals("text/plain+xml", mime);
    }
    
    public static final class Redirector extends CloneableEditorSupportRedirector {
    
        @Override
        protected CloneableEditorSupport redirect(Lookup ces) {
            return ces.lookup(CloneableEditorSupport.class);
        }
    }
}
