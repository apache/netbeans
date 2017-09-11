/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
