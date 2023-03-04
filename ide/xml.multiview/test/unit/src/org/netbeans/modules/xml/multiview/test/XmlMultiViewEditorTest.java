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

package org.netbeans.modules.xml.multiview.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.AssertionFailedErrorException;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;

import org.netbeans.modules.xml.multiview.test.util.Helper;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

/**
 *
 * @author Milan Kuchtiak
 */
public class XmlMultiViewEditorTest extends NbTestCase {
    private DataLoader loader;

    public XmlMultiViewEditorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        DataLoaderPool pool = DataLoaderPool.getDefault();
        assertNotNull (pool);
        loader = DataLoader.getLoader(BookDataLoader.class);
    }


    /** Tet if sample.book was correctly recognized by BookDataLoader and
     * if sample.book was open in editor (XML view) 
     */
    public void testBookDataObject() throws IOException {
        initDataObject();
    }

    public void testChangeModel() throws IOException {
        BookDataObject bookDO = initDataObject();
        try {
            Book book = bookDO.getBook();
            book.setAttributeValue("chapter", 0, "length", "110");
            bookDO.modelUpdatedFromUI();
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to change book model",ex);
        }
        // test if data object was modified
        SaveCookie cookie = Helper.getSaveCookie(bookDO);
        assertNotNull("Data Object Not Modified", cookie);
        cookie.save();

        // test to golden file
        File original = Helper.getBookFile(getDataDir(), getWorkDir());
        assertTrue("File doesn't contain the text : <chapter length=\"110\">",
                    Helper.isTextInFile("<chapter length=\"110\">",original));
    }

    public void testChangeModelInDesignView() throws IOException, InterruptedException {
        final BookDataObject bookDO = initDataObject();
        try {
            bookDO.showElement(bookDO.getBook().getChapter()[1]);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Chapter section", ex);
        }
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    final JTextField titleTF = Helper.getChapterTitleTF(bookDO, bookDO.getBook().getChapter()[1]);
                    titleTF.requestFocus();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                javax.swing.text.Document doc = titleTF.getDocument();
                                doc.remove(0, doc.getLength());
                                doc.insertString(0, "The garden full of beans", null);
                            } catch (Exception ex) {
                                throw new AssertionFailedErrorException("Failed to set the title for Chapter: ", ex);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                } catch (Exception ex) {
                    throw new AssertionFailedErrorException("Failed to set the title for Chapter: ", ex);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
        latch.await(30, TimeUnit.SECONDS);

        // open XML View
        ((EditCookie) bookDO.getCookie(EditCookie.class)).edit();
        // handle consequent calls of SwingUtilities.invokeLater();
        Helper.waitForDispatchThread();

        // test if data object was modified
        SaveCookie cookie = Helper.getSaveCookie(bookDO);
        assertNotNull("Data Object Not Modified", cookie);
        cookie.save();

        // test to golden file
        File original = Helper.getBookFile(getDataDir(), getWorkDir());
        assertTrue("File doesn't contain the text : <title lang=\"en\">The garden full of beans</title>",
                Helper.isTextInFile("<title lang=\"en\">The garden full of beans</title>", original));
    }

    public void testExternalChange() throws IOException {
        BookDataObject bookDO = initDataObject();
        String golden = "ChangedChapterTitle.pass";
        FileObject fo = bookDO.getPrimaryFile();
        InputStream is = new FileInputStream(getGoldenFile(golden));
        try {
            org.openide.filesystems.FileLock lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            try {

                int b;
                while ((b = is.read()) != -1) {
                    char ch = (char) b;
                    if (ch == '2') {
                        os.write(b);
                    }
                    os.write(b);
                }
            }
            finally {
                os.close();
                is.close();
                lock.releaseLock();
            }
        } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
            throw new AssertionFailedErrorException("Lock problem : ", ex);
        }
        
        Helper.sleep(2000); // wait for external change update
        
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) bookDO.getCookie(EditorCookie.class);
        Document doc = editor.getDocument();
        try {
            assertTrue("XML document doesn't contain the external changes: ",
                    doc.getText(0, doc.getLength()).indexOf("<chapter length=\"122\">") > 0);
        } catch (BadLocationException ex) {
            throw new AssertionFailedErrorException(ex);
        }
    }

    private void doSetPreferredLoader (FileObject fo, DataLoader loader) throws IOException {
        DataLoaderPool.setPreferredLoader (fo, loader);
    }

    private BookDataObject initDataObject() throws IOException {
        BookDataObject ret = null;
        File f = Helper.getBookFile(getDataDir(), getWorkDir());
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);

        doSetPreferredLoader(fo, loader);
        DataObject dObj = DataObject.find(fo);
        assertNotNull("Book DataObject not found", dObj);
        assertEquals(BookDataObject.class, dObj.getClass());

        ret = (BookDataObject) dObj;
        ((EditCookie) ret.getCookie(EditCookie.class)).edit();

        // wait to open the document
        Helper.waitForDispatchThread();

        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) ret.getCookie(EditorCookie.class);
        Document doc = Helper.getDocument(editor);
        assertTrue("The document is empty :", doc == null || doc.getLength() > 0);
        return ret;
    }

    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        TestRunner.run(new NbTestSuite(XmlMultiViewEditorTest.class));
//    }
    

    public void testSetModifiedNestedChange() throws Exception {
        File f = Helper.getBookFile(getDataDir(), getWorkDir());
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);

        doSetPreferredLoader(fo, loader);
        final DataObject dob = DataObject.find(fo);

        assertTrue("The right object", dob instanceof XmlMultiViewDataObject);
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

    
}
