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
package org.netbeans.modules.html;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
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
