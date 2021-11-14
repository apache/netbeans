/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.Reader;
import java.io.Writer;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class SourceFileObjectTest extends NbTestCase {
    private static final RequestProcessor RP = new RequestProcessor(SourceFileObjectTest.class.getName(), 1, false, false);
    
    public SourceFileObjectTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeadlock() throws Exception {
        clearWorkDir();
        
        FileObject work = FileUtil.toFileObject(getWorkDir());
        FileObject data = FileUtil.createData(work, "test.java");
        Document doc = DataObject.find(data).getLookup().lookup(EditorCookie.class).openDocument();
        SourceFileObject sfo = new SourceFileObject(
            new AbstractSourceFileObject.Handle(data, work),
            new FilterImplementation(doc),
            null,
            true,
            false);
    }
    
    private static final class FilterImplementation implements JavaFileFilterImplementation {

        private Document doc;

        public FilterImplementation(Document doc) {
            this.doc = doc;
        }
        
        public Reader filterReader(Reader r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            try {
                RequestProcessor.Task t = RP.post(new Runnable() {
                    public void run() {
                        try {
                            doc.insertString(0, "1", null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

                assertTrue("Deadlock detected.", t.waitFinished(10000));
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
            
            return charSequence;
        }

        public Writer filterWriter(Writer w) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
