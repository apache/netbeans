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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.editor.indent.api;

import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.modules.editor.indent.api.Indent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.openide.util.Exceptions;

/**
 *
 * @author Miloslav Metelka
 */
public class IndentTest extends NbTestCase {

    private Logger LOG;

    private static final String MIME_TYPE = "text/x-test";

    public IndentTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
    }

    public void testFindIndentTaskFactory() throws BadLocationException {
        TestIndentTask.TestFactory factory = new TestIndentTask.TestFactory();
        
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse(MIME_TYPE), factory);
        
        Document doc = new PlainDocument();
        doc.putProperty("mimeType", MIME_TYPE);
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            //doc.atomicLock();
            try {
                indent.reindent(0);
            } finally {
                //doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }
        // Check that the factory was used
        assertTrue(TestIndentTask.TestFactory.lastCreatedTask.indentPerformed);
    }

    public void testIndentMultiLock() throws BadLocationException {
        TestIndentTask.TestFactory factory = new TestIndentTask.TestFactory();

        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse(MIME_TYPE), factory);

        Document doc = new PlainDocument();
        doc.putProperty("mimeType", MIME_TYPE);
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            //doc.atomicLock();
            // re-lock
            indent.lock();
            try {
                indent.reindent(0);
            } finally {
                // release inner lock
                indent.unlock();
                //doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }

        // Repeat again if anything was not forgotten e.g. clear a var(s) etc.
        indent.lock();
        try {
            //doc.atomicLock();
            // re-lock
            indent.lock();
            try {
                indent.reindent(0);
            } finally {
                // release inner lock
                indent.unlock();
                //doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }

        // Check that the factory was used
        assertTrue(TestIndentTask.TestFactory.lastCreatedTask.indentPerformed);
    }

    public void testIndentWait() throws BadLocationException {
        TestIndentTask.TestFactory factory = new TestIndentTask.TestFactory();

        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse(MIME_TYPE), factory);

        Document doc = new PlainDocument();
        doc.putProperty("mimeType", MIME_TYPE);
        Indent indent = Indent.get(doc);
        IndentPerformer indentPerformer = new IndentPerformer(doc);
        String indent2ThreadName = "indent2";
        Thread indent2Thread = new Thread(indentPerformer, indent2ThreadName);
        int joinTimeout = 20;
//        String indent1ThreadName = Thread.currentThread().getName();
//        Log.controlFlow(
//            LOG,
//            null,
//            "THREAD: " + indent1ThreadName + " MSG: indent1 locked" +
//            "THREAD: " + indent2ThreadName + " MSG: indent2 locked" +
//            "THREAD: " + indent1ThreadName + " MSG: indent1 finished",
//            300
//        );

        indent.lock();
        try {
            //doc.atomicLock();
            try {
                LOG.info("indent1 locked");
                indent2Thread.start();
                try {
                    indent2Thread.join(joinTimeout);
                } catch (InterruptedException ex) {
                }
                assertFalse(indentPerformer.lockAcquired);
                assertFalse(indentPerformer.indentFinished);

                indent.reindent(0);
            } finally {
                //doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }
        try {
            indent2Thread.join(joinTimeout);
        } catch (InterruptedException ex) {
        }
        assertTrue(indentPerformer.lockAcquired);
        assertTrue(indentPerformer.indentFinished);
        LOG.info("indent1 finished");
    }

    private static final class TestIndentTask implements IndentTask {
        
        private Context context;
        
        TestExtraLocking lastCreatedLocking;
        
        boolean indentPerformed;

        TestIndentTask(Context context) {
            this.context = context;
        }

        public void reindent() throws BadLocationException {
            assertTrue(lastCreatedLocking.locked);
            context.document().insertString(0, " ", null);
            indentPerformed = true;
        }
        
        public ExtraLock indentLock() {
            return (lastCreatedLocking = new TestExtraLocking());
        }
        
        static final class TestFactory implements IndentTask.Factory {
            
            static TestIndentTask lastCreatedTask;

            public IndentTask createTask(Context context) {
                return (lastCreatedTask = new TestIndentTask(context));
            }
            
        }

    }
    
    private static final class TestExtraLocking implements ExtraLock {
        
        Boolean locked;
        
        public Boolean locked() {
            return locked;
        }

        public void lock() {
            if (locked != null)
                assertFalse(locked);
            locked = true;
        }

        public void unlock() {
            assertTrue(locked);
            locked = false;
        }
        
    }

    private static final class IndentPerformer implements Runnable {

        public IndentPerformer(Document doc) {
            this.doc = doc;
        }

        private final Document doc;

        boolean lockAcquired;

        boolean indentFinished;

        public void run() {
            Indent indent = Indent.get(doc);
            indent.lock();
            try {
                //doc.atomicLock();
                try {
                    lockAcquired = true;
                    indent.reindent(0);
                } catch (BadLocationException ex) {
                    fail();
                } finally {
                    //doc.atomicUnlock();
                }
            } finally {
                indent.unlock();
            }
            indentFinished = true;
        }


    }

}
