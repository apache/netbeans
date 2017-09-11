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

package org.netbeans.lib.editor.util.swing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

public class DocumentUtilitiesTest extends NbTestCase {

    public DocumentUtilitiesTest(String testName) {
        super(testName);
    }

    public void testDebugOffset() throws Exception {
        PlainDocument doc = new PlainDocument(); // tabSize is 8
        //                   0123 45 678 90123456 789
        doc.insertString(0, "abc\na\tbc\nabcdefg\thij", null);
        assertEquals("0[1:1]", DocumentUtilities.debugOffset(doc, 0));
        assertEquals("5[2:2]", DocumentUtilities.debugOffset(doc, 5));
        assertEquals("6[2:9]", DocumentUtilities.debugOffset(doc, 6));
        assertEquals("7[2:10]", DocumentUtilities.debugOffset(doc, 7));
        assertEquals("16[3:8]", DocumentUtilities.debugOffset(doc, 16));
        assertEquals("17[3:9]", DocumentUtilities.debugOffset(doc, 17));
        assertEquals("19[3:11]", DocumentUtilities.debugOffset(doc, 19));
    }

    public void testIsReadLocked() throws Exception {
        PlainDocument doc = new PlainDocument();
        assertFalse(DocumentUtilities.isReadLocked(doc));
        doc.readLock();
        try {
            assertTrue(DocumentUtilities.isReadLocked(doc));
        } finally {
            doc.readUnlock();
        }
    }

    public void testGetText() throws Exception {
        PlainDocument doc = new PlainDocument();
        CharSequence text = DocumentUtilities.getText(doc);
        assertEquals(1, text.length());
        assertEquals('\n', text.charAt(0));

        text = DocumentUtilities.getText(doc);
        doc.insertString(0, "a\nb", null);
        for (int i = 0; i < doc.getLength() + 1; i++) {
            assertEquals(doc.getText(i, 1).charAt(0), text.charAt(i));
        }
    }
    
    public void testIsWriteLocked() throws Exception {
        PlainDocument doc = new PlainDocument();
        assertFalse(DocumentUtilities.isWriteLocked(doc));
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                assertTrue(DocumentUtilities.isWriteLocked(evt.getDocument()));
            }
            public void removeUpdate(DocumentEvent evt) {
            }
            public void changedUpdate(DocumentEvent evt) {
            }
        });
        doc.insertString(0, "test", null);
    }

    public void testInsertedTextIsPresent() throws Exception {
        final PlainDocument doc = new PlainDocument();
        final CountDownLatch insertDone = new CountDownLatch(1);
        final CountDownLatch removeDone = new CountDownLatch(1);

        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                insertDone.countDown();
                try {
                    removeDone.await(1, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                try {
                    String insertedText = evt.getDocument().getText(evt.getOffset(), evt.getLength());
                } catch (BadLocationException ex) {
                    throw new IllegalStateException(
                            "Inserted text not present in document !!! docLen=" + doc.getLength(), ex);
                }
            }
            public void removeUpdate(DocumentEvent evt) {
            }
            public void changedUpdate(DocumentEvent evt) {
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    insertDone.await();
                    doc.remove(0, doc.getLength());
                    removeDone.countDown();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }).start();

        doc.insertString(0, "Hello", null);
        
    }

}
