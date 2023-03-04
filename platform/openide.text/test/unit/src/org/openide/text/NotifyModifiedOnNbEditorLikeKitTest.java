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

package org.openide.text;

import org.netbeans.junit.RandomlyFails;
import org.openide.util.RequestProcessor;

/** Testing different features of NotifyModifieTest with NbEditorKit
 *
 * @author Jaroslav Tulach
 */
public class NotifyModifiedOnNbEditorLikeKitTest extends NotifyModifiedTest {
    private NbLikeEditorKit k;

    public NotifyModifiedOnNbEditorLikeKitTest (String s) {
        super (s);
    }
    
    //
    // overwrite editor kit
    //
    
    @Override
    protected javax.swing.text.EditorKit createEditorKit () {
        NbLikeEditorKit tmp = new NbLikeEditorKit ();
        return tmp;
    }
    
    @Override
    protected void doesVetoedInsertFireBadLocationException (javax.swing.text.BadLocationException e) {
        if (e == null) {
            fail("Vetoed insert has to generate BadLocationException");
        }
    }
    
    private static RequestProcessor testRP = new RequestProcessor("Test");
    
    private boolean outerNotifyModify = true;

    @Override
    protected void checkThatDocumentLockIsNotHeld () {
        // Since 6.43 (UndoRedoManager addition) the CES installs DocumentFilter (that prevents modification)
        // to any AbstractDocument-based document including javax.swing.text.PlainDocument but the filter is checked
        // under document lock. However if document has "supportsModificationListener" property turned on
        // then the outer access will be serviced by the vetoable listener without being document locked.
        if (!Boolean.TRUE.equals(support.getDocument().getProperty("supportsModificationListener")) || !outerNotifyModify) {
            return;
        }
        outerNotifyModify = false; // inner accesses are by the filter and thus document-locked
        
        class X implements Runnable {
            private boolean second;
            private boolean ok;

            public void run () {
                if (second) {
                    ok = true;
                    return;
                } else {
                    second = true;
                    javax.swing.text.Document doc = support.getDocument ();
                    assertNotNull (doc);
                    // we have to pass thru read access
                    doc.render (this);

                    // Document modifications in Env.markModified() disabled
                    // due to deadlock after fixing of #218626.
                    // Since markModified() is called from doc.insertString()/remove()
                    // which already have an offset passed it would not be wise
                    // to modify the document at this moment anyway.
                    if (false && ok) {
                        try {
                            // we have to be allowed to do modifications as well
                            doc.insertString (-1, "A", null);
                            ok = false;
                        } catch (javax.swing.text.BadLocationException ex) {
                        }

                        try {
                            doc.remove (-1, 1);
                            ok = false;
                        } catch (javax.swing.text.BadLocationException ex) {
                        }
                    }
                        
                    return;
                }
            }
        }
        //Avoid random deadlock with ARQ thread
        if (!"Active Reference Queue Daemon".equals(Thread.currentThread().getName())) {
            X x = new X ();
            testRP.post (x).waitFinished ();
            assertTrue ("No lock is held on document when running notifyModified", x.ok);
        }
    }
}
