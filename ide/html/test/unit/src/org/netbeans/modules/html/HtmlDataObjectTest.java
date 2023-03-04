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
package org.netbeans.modules.html;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
public class HtmlDataObjectTest extends CslTestBase {

    private static final Logger LOGGER = Logger.getLogger(HtmlDataObjectTest.class.getName());

    @SuppressWarnings("deprecation")
    private static void init() {
        FileUtil.setMIMEType("html", "text/html");
    }

    static {
        init();
    }

    public HtmlDataObjectTest(String testName) {
        super(testName);
    }

    public void testConstructorHasToRunWithoutChildrenLockBeingNeeded() throws Exception {
        MockServices.setServices(HtmlLoader.class);

        class Block implements Runnable {

            @Override
            public void run() {
                if (!Children.MUTEX.isReadAccess()) {
                    Children.MUTEX.readAccess(this);
                    return;
                }
                synchronized (this) {
                    try {
                        notifyAll();

                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        Block b = new Block();

        synchronized (b) {
            RequestProcessor.getDefault().post(b);
            b.wait();
        }

        try {

            FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "my.html");
            DataObject obj = DataObject.find(fo);
            assertEquals("Successfully created html object", obj.getClass(), HtmlDataObject.class);
            assertNotNull("File encoding query is in the object's lookup", obj.getLookup().lookup(FileEncodingQueryImplementation.class));
        } finally {
            synchronized (b) {
                b.notifyAll();
            }
        }
    }

    public void testModifySave() throws IOException, BadLocationException {
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "test1.html");
        assertNotNull(fo);
        final DataObject obj = DataObject.find(fo);

        assertNotNull(obj);
        assertFalse(obj.isModified());
        assertNull(obj.getLookup().lookup(SaveCookie.class));
        final StyledDocument doc = obj.getLookup().lookup(EditorCookie.class).openDocument();
        assertTrue(doc instanceof BaseDocument);

        //listen on DO's lookup for the savecookie
        final Lookup.Result<SaveCookie> saveCookieResult = obj.getLookup().lookupResult(SaveCookie.class);
        saveCookieResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                //change - save cookie should appear upon the modification
                Collection<? extends SaveCookie> allInstances = saveCookieResult.allInstances();
                assertNotNull(allInstances);
                assertEquals(1, allInstances.size());

                //remove the listener
                saveCookieResult.removeLookupListener(this);

                assertTrue(obj.isModified());
                SaveCookie sc = allInstances.iterator().next();
                try {
                    sc.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assertFalse(obj.isModified());

                assertNull(obj.getLookup().lookup(SaveCookie.class));

            }

        });

        ((BaseDocument) doc).runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    //the document modification synchronously triggers the DataObject's lookup change  - SaveCookie added
                    doc.insertString(0, "hello", null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

    }

    public void testUnmodifyViaSetModified() throws IOException, BadLocationException {
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "test2.html");
        assertNotNull(fo);
        final DataObject obj = DataObject.find(fo);

        assertNotNull(obj);
        assertFalse(obj.isModified());
        assertNull(obj.getLookup().lookup(SaveCookie.class));

        final StyledDocument doc = obj.getLookup().lookup(EditorCookie.class).openDocument();
        assertTrue(doc instanceof BaseDocument);

        ((BaseDocument) doc).runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    doc.insertString(0, "hello", null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        ((BaseDocument) doc).runAtomic(new Runnable() {
            @Override
            public void run() {
                assertTrue(obj.isModified());
            }
        });
        assertNotNull(obj.getLookup().lookup(SaveCookie.class));

        //some QE unit tests needs to silently discard the changed made to the editor document
        obj.setModified(false);

        assertFalse(obj.isModified());
        assertNull(obj.getLookup().lookup(SaveCookie.class));
    }

    public void testSetModifiedNestedChange() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("modify.html");
        final DataObject dob = DataObject.find(f);
        assertEquals("The right object", HtmlDataObject.class, dob.getClass());
        dob.getLookup().lookup(EditorCookie.class).openDocument().insertString(0,
                "modified", null);
        assertTrue("Should be modified.", dob.isModified());
        dob.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String s = evt.getPropertyName();
                if (DataObject.PROP_MODIFIED.equals(s) && !dob.isModified()) {
                    dob.setModified(true);
                }
            }
        });
        dob.setModified(false);
        assertTrue("Should be still modified.", dob.isModified());
        assertNotNull("Still should have save cookie.",
                dob.getLookup().lookup(SaveCookie.class));
    }

    public void testFindEncoding() {
        assertEquals("UTF-8",
                HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"));

        assertEquals("UTF-8",
                HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='text/html; charset=UTF-8'/>"));

        assertEquals("UTF-8",
                HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='charset=UTF-8; text/html'/>"));

        assertEquals("UTF-8",
                HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='charset=UTF-8'/>"));

        assertEquals("UTF-8",
                HtmlDataObject.findEncoding(
                "<meta charset=\"UTF-8\"/>"));

        assertEquals(null,
                HtmlDataObject.findEncoding(
                "<meta blabla"));

        assertEquals(null,
                HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content=\"text/html\"/>"));

        assertEquals(null,
                HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"/>"));

    }
    
    public void testFindEncodingInXHTML() {
        //<?xml version="1.0" encoding="windows-1252"?>
        assertEquals("windows-1252",
                HtmlDataObject.findEncoding(
                "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n"
                        + "<html>\n"
                        + "<head></head>\n"
                        + "<body></body>\n"
                        + "</html>"));
        
        assertEquals(null,
                HtmlDataObject.findEncoding(
                "<?xml version=\"1.0\" encooooding=\"UTF-8\"?>\n"
                        + "<html>\n"
                        + "<head></head>\n"
                        + "<body></body>\n"
                        + "</html>"));        
    }
    
    //https://netbeans.org/bugzilla/show_bug.cgi?id=243643
     public void testIssue243643() {
        //test whether we get the value of the charset attribute just from the meta tag but not from the others.
        assertNull(HtmlDataObject.findEncoding(
                "<script type=\"text/javascript\" src=\"//yandex.st/share/share.js\" charset=\"utf-1234\"></script>"));
        
        assertEquals("UTF-8", HtmlDataObject.findEncoding(
                "<meta charset=\"UTF-8\"/>\n" + 
                "<script type=\"text/javascript\" src=\"//yandex.st/share/share.js\" charset=\"utf-1234\"></script>"));
        
        assertEquals("UTF-16", HtmlDataObject.findEncoding(
                "<meta http-equiv=\"Content-Type\" content='charset=UTF-16'/>\n" + 
                "<script type=\"text/javascript\" src=\"//yandex.st/share/share.js\" charset=\"utf-8\"></script>"));
        
     }
}
